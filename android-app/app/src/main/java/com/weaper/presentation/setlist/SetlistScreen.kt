package com.weaper.presentation.setlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.weaper.domain.model.SetlistItem
import com.weaper.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetlistScreen(
    viewModel: SetlistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Header
        TopAppBar(
            title = {
                Text(
                    text = "Setlist",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            actions = {
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = WeaperBlue)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = DarkSurface
            )
        )

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = WeaperBlue)
            }
        } else if (uiState.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No setlist items", color = TextSecondary, fontSize = 18.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Tap + to add songs", color = TextDisabled, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.items, key = { it.id }) { item ->
                    SetlistItemCard(
                        item = item,
                        isActive = uiState.activeItemId == item.id,
                        onPlay = { viewModel.playItem(item) },
                        onDelete = { viewModel.deleteItem(item.id) }
                    )
                }
            }
        }
    }

    // Error snackbar
    uiState.errorMessage?.let { msg ->
        LaunchedEffect(msg) {
            viewModel.clearError()
        }
    }

    if (showAddDialog) {
        AddSetlistItemDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { item ->
                viewModel.addItem(item)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun SetlistItemCard(
    item: SetlistItem,
    isActive: Boolean,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    val borderColor = if (isActive) WeaperBlue else Color.Transparent
    val bgColor = if (isActive) DarkSurfaceVariant else DarkSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Order index badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isActive) WeaperBlue else DarkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${item.orderIndex + 1}",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
                if (item.artist.isNotBlank()) {
                    Text(
                        text = item.artist,
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
                if (item.bpm != null) {
                    Text(
                        text = "${item.bpm} BPM",
                        color = TextDisabled,
                        fontSize = 12.sp
                    )
                }
            }

            // Play button
            IconButton(
                onClick = onPlay,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isActive) WeaperBlue else WeaperBlue.copy(alpha = 0.3f))
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = TextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = WeaperRed.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun AddSetlistItemDialog(
    onDismiss: () -> Unit,
    onAdd: (SetlistItem) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var markerId by remember { mutableStateOf("1") }
    var bpm by remember { mutableStateOf("") }
    var autoPlay by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Add Setlist Item", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Song Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artist") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = markerId,
                    onValueChange = { markerId = it },
                    label = { Text("REAPER Marker ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bpm,
                    onValueChange = { bpm = it },
                    label = { Text("BPM (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = autoPlay, onCheckedChange = { autoPlay = it })
                    Text("Auto-play on select", color = TextPrimary)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(
                            SetlistItem(
                                title = title,
                                artist = artist,
                                markerId = markerId.toIntOrNull() ?: 1,
                                bpm = bpm.toIntOrNull(),
                                autoPlay = autoPlay
                            )
                        )
                    }
                }
            ) {
                Text("Add", color = WeaperBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
