package com.dheemai.treecounter.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Park
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dheemai.treecounter.data.FarmPlot
import com.dheemai.treecounter.viewmodel.TreeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TreeViewModel,
    userName: String,
    onFarmPlotClick: (FarmPlot) -> Unit
) {
    val farmPlots by viewModel.farmPlots.collectAsState()
    val trees by viewModel.trees.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddFarmPlotDialog(
            onConfirm = { name -> viewModel.addFarmPlot(FarmPlot(name = name)); showAddDialog = false },
            onDismiss = { showAddDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (userName.isNotBlank()) "Hello, $userName!" else "TreeSpecNMap") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Farm Plot", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        if (farmPlots.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Park,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("No farm plots yet", style = MaterialTheme.typography.titleMedium)
                    Text("Tap + to add your first farm plot", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "${farmPlots.size} farm plot${if (farmPlots.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                items(farmPlots, key = { it.id }) { farmPlot ->
                    val treeCount = trees.count { it.plotId == farmPlot.id }
                    FarmPlotCard(
                        farmPlot = farmPlot,
                        treeCount = treeCount,
                        onClick = { onFarmPlotClick(farmPlot) },
                        onDelete = { viewModel.deleteFarmPlot(farmPlot) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FarmPlotCard(farmPlot: FarmPlot, treeCount: Int, onClick: () -> Unit, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete farm plot?") },
            text = { Text("\"${farmPlot.name}\" and all $treeCount tree${if (treeCount != 1) "s" else ""} in it will be removed.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
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
                Text(farmPlot.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "$treeCount tree${if (treeCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete farm plot", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun AddFarmPlotDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Farm Plot") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Farm Plot Name / Number") },
                placeholder = { Text("e.g. Farm Plot A, Block 3, Survey 12") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name.trim()) }, enabled = name.isNotBlank()) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
