package com.example.anthar_jalawatch.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.anthar_jalawatch.R
import com.example.anthar_jalawatch.ui.components.VisualDepthIndicator
import com.example.anthar_jalawatch.util.LocationHelper
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogBorewellScreen(
    onNavigateBack: () -> Unit,
    viewModel: BorewellViewModel
) {
    var depthInput by remember { mutableStateOf("") }
    var yieldInput by remember { mutableStateOf("") }
    var yearInput by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var locationLabel by remember { mutableStateOf("Fetching location…") }
    var locationReady by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationHelper = remember { LocationHelper(context) }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    fun fetchLocation() {
        scope.launch {
            locationLabel = "Fetching location…"
            locationReady = false
            val result = locationHelper.getCurrentLocation()
            result.fold(
                onSuccess = { loc ->
                    latitude = loc.latitude
                    longitude = loc.longitude
                    locationLabel = "${String.format("%.3f", loc.latitude)}, ${String.format("%.3f", loc.longitude)}"
                    locationReady = true
                },
                onFailure = {
                    locationLabel = "Location unavailable — grant permission & retry"
                    locationReady = false
                }
            )
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) fetchLocation() else {
            val msg = context.getString(R.string.location_permission_required)
            locationLabel = msg
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        if (locationHelper.hasLocationPermission()) {
            fetchLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            viewModel.clearSaveSuccess()
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val depthValue = depthInput.toDoubleOrNull() ?: 0.0
    val depthPercentage = if (depthValue > 0) (depthValue / 500.0).toFloat().coerceIn(0f, 1f) else 0f

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Log Borewell") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.privacy_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = locationLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Location (~100 m precision)") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    TextButton(onClick = {
                        if (locationHelper.hasLocationPermission()) fetchLocation()
                        else permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }) { Text("Retry") }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = depthInput,
                onValueChange = { depthInput = it },
                label = { Text("Water Depth (feet)") },
                placeholder = { Text("e.g., 150") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = yieldInput,
                onValueChange = { yieldInput = it },
                label = { Text("Yield (inches/hour)") },
                placeholder = { Text("e.g., 3.5") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = yearInput,
                onValueChange = { yearInput = it.filter { c -> c.isDigit() }.take(4) },
                label = { Text("Year of digging") },
                placeholder = { Text("e.g., 2010") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Visual Depth",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            VisualDepthIndicator(depthPercentage = depthPercentage)

            Spacer(modifier = Modifier.height(32.dp))

            val year = yearInput.toIntOrNull()
            val depth = depthInput.toDoubleOrNull()
            val yield = yieldInput.toDoubleOrNull()
            val formValid = depth != null && yield != null && year != null &&
                depth > 0 && yield >= 0 &&
                year in 1950..currentYear &&
                locationReady && latitude != null && longitude != null

            Button(
                onClick = {
                    viewModel.saveReading(
                        depth = depth!!,
                        yield = yield!!,
                        yearOfDigging = year!!,
                        latitude = latitude!!,
                        longitude = longitude!!
                    )
                },
                enabled = formValid && !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save Reading", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
