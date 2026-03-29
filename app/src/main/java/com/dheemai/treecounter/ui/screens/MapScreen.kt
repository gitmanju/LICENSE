package com.dheemai.treecounter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.dheemai.treecounter.viewmodel.TreeViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(viewModel: TreeViewModel, plotId: Long, onBack: () -> Unit) {
    val context = LocalContext.current
    val trees by viewModel.getTreesForPlot(plotId).collectAsState(initial = emptyList())

    Configuration.getInstance().userAgentValue = context.packageName

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tree Map") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(padding),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)
                }
            },
            update = { mapView ->
                mapView.overlays.clear()
                val points = trees.map { tree ->
                    val marker = Marker(mapView).apply {
                        position = GeoPoint(tree.latitude, tree.longitude)
                        title = tree.species
                        snippet = tree.notes.takeIf { it.isNotBlank() }
                    }
                    marker
                }
                mapView.overlays.addAll(points)

                if (trees.isNotEmpty()) {
                    val first = trees.first()
                    mapView.controller.setCenter(GeoPoint(first.latitude, first.longitude))
                }
                mapView.invalidate()
            }
        )
    }
}
