package com.weaper.presentation.sync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.weaper.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(
    viewModel: SyncViewModel = hiltViewModel()
) {
    val syncStatus by viewModel.syncStatus.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        TopAppBar(
            title = {
                Text("File Sync", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Delta Sync", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Compares local audio files with the MacBook server using MD5 hashes. Only uploads changed or missing files.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            // Sync status
            syncStatus?.let { status ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (status.error != null) WeaperRed.copy(alpha = 0.15f)
                        else DarkSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (status.inProgress) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = WeaperBlue,
                                    strokeWidth = 2.dp
                                )
                            } else if (status.error != null) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = WeaperRed, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WeaperGreen, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = when {
                                    status.error != null -> "Error: ${status.error}"
                                    status.inProgress -> "Syncing... ${status.syncedFiles}/${status.totalFiles}"
                                    else -> "Synced ${status.syncedFiles}/${status.totalFiles} files"
                                },
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                        }

                        if (status.inProgress) {
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { if (status.totalFiles > 0) status.syncedFiles.toFloat() / status.totalFiles else 0f },
                                modifier = Modifier.fillMaxWidth(),
                                color = WeaperBlue
                            )
                        }

                        if (status.missingFiles.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text("Pending:", color = TextSecondary, fontSize = 12.sp)
                            status.missingFiles.take(5).forEach { file ->
                                Text("  • ${file.name}", color = TextDisabled, fontSize = 12.sp)
                            }
                            if (status.missingFiles.size > 5) {
                                Text("  ... +${status.missingFiles.size - 5} more", color = TextDisabled, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Sync button
            Button(
                onClick = { viewModel.startSync() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WeaperBlue),
                shape = RoundedCornerShape(12.dp),
                enabled = syncStatus?.inProgress != true
            ) {
                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Start Delta Sync", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
