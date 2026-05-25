package com.example.voltrelay.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voltrelay.ui.theme.VoltRelinkTheme
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelayManagementScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val customDeviceName by viewModel.customDeviceName.collectAsStateWithLifecycle()
    val serverChanSendKey by viewModel.serverChanSendKey.collectAsStateWithLifecycle()
    
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is SettingsViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    RelayManagementContent(
        customDeviceName = customDeviceName ?: "",
        serverChanSendKey = serverChanSendKey ?: "",
        snackbarHostState = snackbarHostState,
        onUpdateDeviceName = { viewModel.updateCustomDeviceName(it) },
        onUpdateSendKey = { viewModel.updateServerChanSendKey(it) },
        onSendTest = { viewModel.sendTestNotification() },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelayManagementContent(
    customDeviceName: String,
    serverChanSendKey: String,
    snackbarHostState: SnackbarHostState,
    onUpdateDeviceName: (String) -> Unit,
    onUpdateSendKey: (String) -> Unit,
    onSendTest: () -> Unit,
    onBack: () -> Unit
) {
    var deviceNameInput by remember(customDeviceName) { mutableStateOf(customDeviceName) }
    var sendKeyInput by remember(serverChanSendKey) { mutableStateOf(serverChanSendKey) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("ServerChan Relay") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Custom Device Name
            SectionHeader("Device Identity")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Display Name", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "This name will appear in your push notifications.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = deviceNameInput,
                        onValueChange = { deviceNameInput = it },
                        label = { Text("Device Name") },
                        placeholder = { Text("e.g. My Pixel 7") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Rounded.Badge, null) },
                        singleLine = true
                    )
                    Button(
                        onClick = { onUpdateDeviceName(deviceNameInput) },
                        modifier = Modifier.align(Alignment.End),
                        enabled = deviceNameInput != customDeviceName
                    ) { Text("Save Name") }
                }
            }

            // ServerChan Configuration
            SectionHeader("ServerChan Config")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Push Key", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = sendKeyInput,
                        onValueChange = { sendKeyInput = it },
                        label = { Text("SCTKey (SendKey)") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Rounded.Key, null) },
                        singleLine = true
                    )
                    Button(
                        onClick = { onUpdateSendKey(sendKeyInput) },
                        modifier = Modifier.align(Alignment.End),
                        enabled = sendKeyInput != serverChanSendKey && sendKeyInput.isNotBlank()
                    ) { Text("Save Key") }
                }
            }

            HorizontalDivider()

            ServerChanTestingSection(onSendTest)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RelayManagementPreview() {
    VoltRelinkTheme {
        RelayManagementContent(
            customDeviceName = "My Primary Phone",
            serverChanSendKey = "SCT123456789",
            snackbarHostState = SnackbarHostState(),
            onUpdateDeviceName = {},
            onUpdateSendKey = {},
            onSendTest = {},
            onBack = {}
        )
    }
}
