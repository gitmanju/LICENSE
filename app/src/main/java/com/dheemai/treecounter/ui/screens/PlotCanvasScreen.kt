package com.dheemai.treecounter.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.dheemai.treecounter.data.FarmPlot
import com.dheemai.treecounter.data.Tree
import com.dheemai.treecounter.viewmodel.TreeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlotCanvasScreen(
    farmPlot: FarmPlot,
    viewModel: TreeViewModel,
    onBack: () -> Unit
) {
    val trees by viewModel.getTreesForPlot(farmPlot.id).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    // Normalized positions (0..1) keyed by tree id
    val positions = remember { mutableStateMapOf<Long, Offset>() }
    var initialised by remember { mutableStateOf(false) }

    var canvasW by remember { mutableStateOf(0f) }
    var canvasH by remember { mutableStateOf(0f) }

    val green = Color(0xFF2E7D32)

    // Pre-populate all trees on first load
    LaunchedEffect(trees, canvasW, canvasH) {
        if (initialised || trees.isEmpty() || canvasW == 0f) return@LaunchedEffect
        initialised = true

        // Separate: trees with saved canvas positions and trees needing GPS-derived placement
        val withCanvas = trees.filter { it.canvasX >= 0f }
        val withoutCanvas = trees.filter { it.canvasX < 0f }

        // Place trees that already have saved positions
        withCanvas.forEach { tree ->
            positions[tree.id] = Offset(tree.canvasX, tree.canvasY)
        }

        // For trees without canvas positions, derive from GPS
        if (withoutCanvas.isNotEmpty()) {
            val minLat = withoutCanvas.minOf { it.latitude }
            val maxLat = withoutCanvas.maxOf { it.latitude }
            val minLon = withoutCanvas.minOf { it.longitude }
            val maxLon = withoutCanvas.maxOf { it.longitude }
            val latSpan = if (maxLat - minLat < 1e-7) 1e-7 else maxLat - minLat
            val lonSpan = if (maxLon - minLon < 1e-7) 1e-7 else maxLon - minLon

            // Track used positions to spread GPS duplicates
            val usedSlots = mutableMapOf<Pair<Int, Int>, Int>()

            withoutCanvas.forEach { tree ->
                var nx = ((tree.longitude - minLon) / lonSpan * 0.8f + 0.1f).toFloat()
                var ny = (1f - (tree.latitude - minLat) / latSpan * 0.8f - 0.1f).toFloat()

                // Spread duplicates into a small grid
                val key = Pair((nx * 20).toInt(), (ny * 20).toInt())
                val slot = usedSlots.getOrDefault(key, 0)
                usedSlots[key] = slot + 1
                if (slot > 0) {
                    val col = slot % 3
                    val row = slot / 3
                    nx = (nx + col * 0.06f - 0.03f).coerceIn(0.05f, 0.95f)
                    ny = (ny + row * 0.06f).coerceIn(0.05f, 0.95f)
                }

                positions[tree.id] = Offset(
                    nx.coerceIn(0.05f, 0.95f),
                    ny.coerceIn(0.05f, 0.95f)
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plot Layout — ${farmPlot.name}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    TextButton(onClick = {
                        scope.launch {
                            trees.forEach { tree ->
                                val pos = positions[tree.id]
                                viewModel.updateTree(
                                    tree.copy(
                                        canvasX = pos?.x ?: -1f,
                                        canvasY = pos?.y ?: -1f
                                    )
                                )
                            }
                            onBack()
                        }
                    }) {
                        Text("Save", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Canvas ────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(2.dp, green, MaterialTheme.shapes.medium)
                    .onSizeChanged { size ->
                        canvasW = size.width.toFloat()
                        canvasH = size.height.toFloat()
                    }
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            val touch = down.position
                            val w = canvasW
                            val h = canvasH
                            if (w == 0f || h == 0f) return@awaitEachGesture

                            // Find a tree within 36px of touch
                            val hit = positions.entries.firstOrNull { (_, norm) ->
                                val dx = norm.x * w - touch.x
                                val dy = norm.y * h - touch.y
                                dx * dx + dy * dy < 36f * 36f
                            }
                            if (hit != null) {
                                down.consume()
                                var sx = hit.value.x * w
                                var sy = hit.value.y * h
                                drag(down.id) { change ->
                                    val delta = change.position - change.previousPosition
                                    sx += delta.x
                                    sy += delta.y
                                    positions[hit.key] = Offset(
                                        (sx / w).coerceIn(0.02f, 0.98f),
                                        (sy / h).coerceIn(0.02f, 0.98f)
                                    )
                                    change.consume()
                                }
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Grid
                    val gridColor = Color(0xFFEEEEEE)
                    for (i in 1..4) {
                        drawLine(gridColor, Offset(size.width * i / 5f, 0f), Offset(size.width * i / 5f, size.height), strokeWidth = 1f)
                        drawLine(gridColor, Offset(0f, size.height * i / 5f), Offset(size.width, size.height * i / 5f), strokeWidth = 1f)
                    }

                    // Tree dots
                    trees.forEachIndexed { index, tree ->
                        val norm = positions[tree.id] ?: return@forEachIndexed
                        val cx = norm.x * size.width
                        val cy = norm.y * size.height
                        val notation = "T${index + 1}"

                        drawCircle(Color.White, 15f, Offset(cx, cy))
                        drawCircle(green, 13f, Offset(cx, cy))

                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawText(
                                notation,
                                cx,
                                cy + 5f,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.WHITE
                                    textSize = 20f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isFakeBoldText = true
                                }
                            )
                        }
                    }
                }
            }

            // ── Hint ──────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    when {
                        trees.isEmpty() -> "No trees in this plot yet."
                        else -> "Drag any dot to reposition. Tap Save when done."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
