package com.dheemai.treecounter.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.dheemai.treecounter.data.Tree
import com.dheemai.treecounter.viewmodel.TreeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeDetailScreen(tree: Tree, viewModel: TreeViewModel, onBack: () -> Unit) {
    val fmt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    var isEditing by remember { mutableStateOf(false) }
    var species by remember(tree) { mutableStateOf(tree.species) }
    var notes by remember(tree) { mutableStateOf(tree.notes) }
    val additionalNames = remember(tree) {
        mutableStateListOf<String>().also { list ->
            tree.additionalNames.split(",").filter { it.isNotBlank() }.forEach { list.add(it) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Tree" else tree.species) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isEditing) {
                            // Cancel — reset fields
                            species = tree.species
                            notes = tree.notes
                            additionalNames.clear()
                            tree.additionalNames.split(",").filter { it.isNotBlank() }.forEach { additionalNames.add(it) }
                            isEditing = false
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(
                            if (isEditing) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isEditing) "Cancel" else "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    if (isEditing) {
                        IconButton(onClick = {
                            viewModel.updateTree(
                                tree.copy(
                                    species = species.trim(),
                                    notes = notes.trim(),
                                    additionalNames = additionalNames.filter { it.isNotBlank() }.joinToString(",")
                                )
                            )
                            isEditing = false
                        }) {
                            Icon(Icons.Default.Save, contentDescription = "Save", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    } else {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            if (tree.photoPath != null) {
                Image(
                    painter = rememberAsyncImagePainter(tree.photoPath),
                    contentDescription = "Tree photo",
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Park,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                if (isEditing) {
                    // ── Edit mode ──────────────────────────────────────────────
                    OutlinedTextField(
                        value = species,
                        onValueChange = { species = it },
                        label = { Text("Species *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("Additional Names", style = MaterialTheme.typography.labelLarge)
                    additionalNames.forEachIndexed { index, name ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { additionalNames[index] = it },
                                label = { Text("Name ${index + 1}") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            IconButton(onClick = { additionalNames.removeAt(index) }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = { additionalNames.add("") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Another Name")
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )

                    Text(
                        "Location and photo can only be updated by adding a new tree.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                } else {
                    // ── View mode ──────────────────────────────────────────────
                    Text(tree.species, style = MaterialTheme.typography.headlineSmall)
                    Text(fmt.format(Date(tree.timestamp)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)

                    HorizontalDivider()

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Location", style = MaterialTheme.typography.labelMedium)
                            Text("Lat: %.7f".format(tree.latitude))
                            Text("Lon: %.7f".format(tree.longitude))
                        }
                    }

                    if (tree.additionalNames.isNotBlank()) {
                        HorizontalDivider()
                        Text("Also Known As", style = MaterialTheme.typography.labelMedium)
                        tree.additionalNames.split(",").filter { it.isNotBlank() }.forEach { name ->
                            Text("• $name", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    if (tree.notes.isNotBlank()) {
                        HorizontalDivider()
                        Text("Notes", style = MaterialTheme.typography.labelMedium)
                        Text(tree.notes, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
