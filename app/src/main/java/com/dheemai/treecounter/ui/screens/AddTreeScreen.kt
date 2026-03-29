package com.dheemai.treecounter.ui.screens

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.dheemai.treecounter.data.InaturalistService
import com.dheemai.treecounter.data.FarmPlot
import com.dheemai.treecounter.data.Tree
import com.dheemai.treecounter.viewmodel.TreeViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.*
import java.io.File
import java.util.concurrent.Executors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AddTreeScreen(viewModel: TreeViewModel, farmPlot: FarmPlot, onBack: () -> Unit) {
    val context = LocalContext.current
    var species by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var location by remember { mutableStateOf<Location?>(null) }
    var photoPath by remember { mutableStateOf<String?>(null) }
    var showCamera by remember { mutableStateOf(false) }
    var locationLoading by remember { mutableStateOf(false) }
    var identifyLoading by remember { mutableStateOf(false) }
    var identifyMessage by remember { mutableStateOf<String?>(null) }
    val additionalNames = remember { mutableStateListOf<String>() }
    val scope = rememberCoroutineScope()

    val permissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA
        )
    )

    LaunchedEffect(Unit) {
        if (!permissions.allPermissionsGranted) {
            permissions.launchMultiplePermissionRequest()
        }
    }

    // Auto-capture location once permissions are granted
    LaunchedEffect(permissions.allPermissionsGranted) {
        if (permissions.allPermissionsGranted && location == null && !locationLoading) {
            locationLoading = true
            getLocation(context) { loc ->
                location = loc
                locationLoading = false
            }
        }
    }

    if (showCamera) {
        CameraScreen(
            onPhotoTaken = { path ->
                photoPath = path
                showCamera = false
                locationLoading = true
                getLocation(context) { loc ->
                    location = loc
                    locationLoading = false
                }
            },
            onDismiss = { showCamera = false }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log a Tree") },
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
        },
        floatingActionButton = {
            val canSave = species.isNotBlank() && location != null
            ExtendedFloatingActionButton(
                onClick = {
                    if (canSave) {
                        viewModel.addTree(
                            Tree(
                                species = species.trim(),
                                notes = notes.trim(),
                                latitude = location!!.latitude,
                                longitude = location!!.longitude,
                                photoPath = photoPath,
                                additionalNames = additionalNames.filter { it.isNotBlank() }.joinToString(","),
                                plotId = farmPlot.id
                            )
                        )
                        onBack()
                    }
                },
                containerColor = if (canSave) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                icon = { Icon(Icons.Default.Save, contentDescription = null, tint = Color.White) },
                text = { Text("Save Tree", color = Color.White) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photo section
            Card(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (photoPath != null) {
                        Image(
                            painter = rememberAsyncImagePainter(photoPath),
                            contentDescription = "Tree photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Text("No photo", color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
            Button(
                onClick = { showCamera = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (photoPath != null) "Retake Photo" else "Take Photo")
            }

            if (photoPath != null) {
                OutlinedButton(
                    onClick = {
                        identifyMessage = null
                        identifyLoading = true
                        scope.launch {
                            val result = InaturalistService.identifyTree(photoPath!!)
                            result.fold(
                                onSuccess = { id ->
                                    species = id.commonName
                                    val pct = "%.0f".format(id.score)
                                    identifyMessage = "Identified: ${id.commonName} (${id.scientificName}) — ${pct}% confidence"
                                },
                                onFailure = { e ->
                                    identifyMessage = "Could not identify: ${e.message}"
                                }
                            )
                            identifyLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !identifyLoading
                ) {
                    if (identifyLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (identifyLoading) "Identifying…" else "Identify Tree with iNaturalist")
                }

                identifyMessage?.let { msg ->
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (msg.startsWith("Could not")) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Location section
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (location != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.width(8.dp))
                    if (location != null) {
                        Column {
                            Text(
                                "%.7f, %.7f".format(location!!.latitude, location!!.longitude),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Accuracy: ±%.1fm".format(location!!.accuracy),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (location!!.accuracy <= 5f) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline
                            )
                        }
                    } else {
                        Text("Capturing location…", color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
            Button(
                onClick = {
                    locationLoading = true
                    getLocation(context) { loc ->
                        location = loc
                        locationLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !locationLoading
            ) {
                if (locationLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (location != null) "Refresh Location" else "Retry Location")
            }

            // Species
            OutlinedTextField(
                value = species,
                onValueChange = { species = it },
                label = { Text("Species *") },
                placeholder = { Text("e.g. Banyan, Neem, Mango") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Additional names
            Text("Additional Names", style = MaterialTheme.typography.labelLarge)
            additionalNames.forEachIndexed { index, name ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { additionalNames[index] = it },
                        label = { Text("Name ${index + 1}") },
                        placeholder = { Text("e.g. Local or common name") },
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

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                placeholder = { Text("Condition, size, observations…") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun CameraScreen(onPhotoTaken: (String) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also { it.setSurfaceProvider(surfaceProvider) }
                        val capture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()
                        imageCapture = capture
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, capture)
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Row(
            modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
            ) { Text("Cancel", color = Color.White) }

            FloatingActionButton(
                onClick = {
                    val file = File(context.getExternalFilesDir("Pictures"), "tree_${System.currentTimeMillis()}.jpg")
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                    imageCapture?.takePicture(outputOptions, executor,
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                onPhotoTaken(file.absolutePath)
                            }
                            override fun onError(exc: ImageCaptureException) {}
                        }
                    )
                },
                containerColor = Color.White
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Capture", tint = Color.Black)
            }
        }
    }
}

@Suppress("MissingPermission")
private fun getLocation(context: Context, onResult: (Location?) -> Unit) {
    val client = LocationServices.getFusedLocationProviderClient(context)

    // Collect up to 5 readings over ~8 seconds, pick the best accuracy
    val readings = mutableListOf<Location>()
    val MAX_READINGS = 5
    val MAX_WAIT_MS = 8000L

    val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1500L)
        .setMinUpdateIntervalMillis(1000L)
        .setMaxUpdates(MAX_READINGS)
        .build()

    var finished = false

    val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.locations.forEach { readings.add(it) }
            if (readings.size >= MAX_READINGS) {
                finish()
            }
        }

        fun finish() {
            if (finished) return
            finished = true
            client.removeLocationUpdates(this)
            onResult(readings.minByOrNull { it.accuracy })
        }
    }

    try {
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())

        // Fallback: stop after MAX_WAIT_MS even if we haven't got MAX_READINGS
        android.os.Handler(Looper.getMainLooper()).postDelayed({
            callback.finish()
        }, MAX_WAIT_MS)

    } catch (e: Exception) {
        onResult(null)
    }
}
