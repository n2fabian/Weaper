package com.weaper.presentation.soundboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.weaper.domain.model.SoundboardItem
import com.weaper.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundboardScreen(
    viewModel: SoundboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Soundboard",
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
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
        )

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = WeaperBlue)
            }
        } else if (uiState.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No soundboard items", color = TextSecondary, fontSize = 18.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Tap + to add samples", color = TextDisabled, fontSize = 14.sp)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.items, key = { it.id }) { item ->
                    SoundboardButton(
                        item = item,
                        isActive = uiState.activeItemId == item.id,
                        onClick = { viewModel.triggerSound(item) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddSoundboardItemDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { item ->
                viewModel.addItem(item)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun SoundboardButton(
    item: SoundboardItem,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val buttonColor = when {
        !item.isAvailable -> ButtonMissing
        isActive -> ButtonPressed
        else -> Color(item.color)
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(buttonColor)
            .then(
                if (item.isAvailable) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .border(
                width = if (isActive) 2.dp else 0.dp,
                color = ButtonPressed,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = item.label,
                color = if (item.isAvailable) TextPrimary else TextDisabled,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            if (!item.isAvailable) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "MISSING",
                    color = TextDisabled,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AddSoundboardItemDialog(
    onDismiss: () -> Unit,
    onAdd: (SoundboardItem) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var fileName by remember { mutableStateOf("") }
    var trackId by remember { mutableStateOf("1") }
    var oscPath by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Add Sample Button", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Button Label") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("File Name (e.g. kick.wav)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = trackId,
                    onValueChange = { trackId = it },
                    label = { Text("REAPER Track ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = oscPath,
                    onValueChange = { oscPath = it },
                    label = { Text("Custom OSC Path (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (label.isNotBlank()) {
                        onAdd(
                            SoundboardItem(
                                label = label,
                                fileName = fileName,
                                trackId = trackId.toIntOrNull() ?: 1,
                                oscPath = oscPath
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
