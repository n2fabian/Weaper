package com.weaper.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.weaper.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var oscHost by remember(uiState.oscHost) { mutableStateOf(uiState.oscHost) }
    var oscPort by remember(uiState.oscPort) { mutableStateOf(uiState.oscPort) }
    var syncUrl by remember(uiState.syncServerUrl) { mutableStateOf(uiState.syncServerUrl) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        TopAppBar(
            title = {
                Text("Settings", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // OSC Settings
            SettingsSectionCard(title = "OSC / REAPER Connection") {
                OutlinedTextField(
                    value = oscHost,
                    onValueChange = { oscHost = it },
                    label = { Text("REAPER IP Address") },
                    placeholder = { Text("192.168.1.100") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = oscPort,
                    onValueChange = { oscPort = it },
                    label = { Text("OSC Port") },
                    placeholder = { Text("8000") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        viewModel.saveOscHost(oscHost)
                        viewModel.saveOscPort(oscPort)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WeaperBlue),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save OSC Settings")
                }
            }

            // Sync Server Settings
            SettingsSectionCard(title = "Local Sync Server") {
                OutlinedTextField(
                    value = syncUrl,
                    onValueChange = { syncUrl = it },
                    label = { Text("Sync Server URL") },
                    placeholder = { Text("http://192.168.1.100:3000") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.saveSyncServerUrl(syncUrl) },
                    colors = ButtonDefaults.buttonColors(containerColor = WeaperBlue),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save Sync Settings")
                }
            }

            // Info card
            SettingsSectionCard(title = "About") {
                Text("Weaper v1.0", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Android controller for REAPER DAW.\nCommunicates via OSC over local WiFi.\nMetadata synced via Firebase.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = WeaperBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}
