package com.dheemai.treecounter.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.dheemai.treecounter.data.PdfExportService
import com.dheemai.treecounter.data.FarmPlot
import com.dheemai.treecounter.data.Tree
import com.dheemai.treecounter.viewmodel.TreeViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlotDetailScreen(
    farmPlot: FarmPlot,
    userName: String,
    viewModel: TreeViewModel,
    onBack: () -> Unit,
    onAddTree: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenCanvas: () -> Unit,
    onTreeClick: (Tree) -> Unit
) {
    val context = LocalContext.current
    Configuration.getInstance().userAgentValue = context.packageName
    val trees by viewModel.getTreesForPlot(farmPlot.id).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(farmPlot.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    if (trees.isNotEmpty()) {
                        IconButton(onClick = onOpenCanvas) {
                            Icon(Icons.Default.Edit, contentDescription = "Canvas Layout", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        IconButton(onClick = onOpenMap) {
                            Icon(Icons.Default.Map, contentDescription = "Map", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        IconButton(onClick = { PdfExportService.exportAndShare(context, trees, userName, farmPlot.name) }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTree,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Tree", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clickable { if (trees.isNotEmpty()) onOpenMap() },
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    if (trees.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Park,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("No trees yet", style = MaterialTheme.typography.bodyMedium)
                                Text("Tap + to log your first tree", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    } else {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx ->
                                MapView(ctx).apply {
                                    setTileSource(TileSourceFactory.MAPNIK)
                                    setMultiTouchControls(false)
                                    isClickable = false
                                    controller.setZoom(14.0)
                                }
                            },
                            update = { mapView ->
                                mapView.overlays.clear()
                                trees.forEach { tree ->
                                    val marker = Marker(mapView).apply {
                                        position = GeoPoint(tree.latitude, tree.longitude)
                                        title = tree.species
                                        snippet = tree.notes.takeIf { it.isNotBlank() }
                                    }
                                    mapView.overlays.add(marker)
                                }
                                mapView.controller.setCenter(GeoPoint(trees.first().latitude, trees.first().longitude))
                                mapView.invalidate()
                            }
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${trees.size} tree${if (trees.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (trees.isNotEmpty()) {
                        TextButton(onClick = onOpenMap) { Text("View full map") }
                    }
                }
            }

            if (trees.isNotEmpty()) {
                items(trees, key = { it.id }) { tree ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        TreeCard(
                            tree = tree,
                            onDelete = { viewModel.deleteTree(tree) },
                            onClick = { onTreeClick(tree) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun TreeCard(tree: Tree, onDelete: () -> Unit, onClick: () -> Unit) {
    val fmt = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete tree?") },
            text = { Text("Remove ${tree.species} from this plot?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Park,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(tree.species, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "%.7f, %.7f".format(tree.latitude, tree.longitude),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(fmt.format(Date(tree.timestamp)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                if (tree.notes.isNotBlank()) {
                    Text(tree.notes, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
