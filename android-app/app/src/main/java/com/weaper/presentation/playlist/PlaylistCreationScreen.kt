package com.weaper.presentation.playlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.weaper.domain.model.ReaperTrack
import com.weaper.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistCreationScreen(
    onNavigateToPlayer: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: PlaylistCreationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showNameDialog by remember { mutableStateOf(false) }
    var showAddTrackDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    uiState.errorMessage?.let { msg ->
        LaunchedEffect(msg) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DarkBackground)
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "New Playlist",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                actions = {
                    // Green Create button – visible only when ≥1 song selected
                    AnimatedVisibility(
                        visible = uiState.selectedTrackIds.isNotEmpty(),
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut()
                    ) {
                        Button(
                            onClick = { showNameDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = WeaperGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Create", fontWeight = FontWeight.Bold)
                        }
                    }
                    IconButton(onClick = { showAddTrackDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add song", tint = WeaperBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = WeaperBlue)
                }
            } else {
                // Selected & reorderable songs section
                if (uiState.orderedSelectedTracks.isNotEmpty()) {
                    Text(
                        text = "Selected songs  •  long-press & drag to reorder",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
                    )
                    ReorderableSelectedList(
                        tracks = uiState.orderedSelectedTracks,
                        onReorder = { from, to -> viewModel.reorderTracks(from, to) },
                        onRemove = { track -> viewModel.toggleTrackSelection(track) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )
                    HorizontalDivider(
                        color = DarkSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Available tracks section header
                Text(
                    text = "Available songs from REAPER",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
                )

                if (uiState.availableTracks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "No tracks found",
                                color = TextSecondary,
                                fontSize = 16.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Tap + to add songs from REAPER",
                                color = TextDisabled,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(uiState.availableTracks) { _, track ->
                            val isSelected = uiState.selectedTrackIds.contains(track.id)
                            AvailableTrackRow(
                                track = track,
                                isSelected = isSelected,
                                onToggle = { viewModel.toggleTrackSelection(track) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showNameDialog) {
        PlaylistNameDialog(
            onDismiss = { showNameDialog = false },
            onCreate = { name ->
                viewModel.createPlaylist(name)
                showNameDialog = false
                onNavigateToPlayer()
            }
        )
    }

    if (showAddTrackDialog) {
        AddTrackDialog(
            onDismiss = { showAddTrackDialog = false },
            onAdd = { track ->
                viewModel.addTrack(track)
                showAddTrackDialog = false
            }
        )
    }
}

// --------------------------------------------------------------------------
// Reorderable selected list (drag handle approach)
// --------------------------------------------------------------------------

@Composable
private fun ReorderableSelectedList(
    tracks: List<ReaperTrack>,
    onReorder: (Int, Int) -> Unit,
    onRemove: (ReaperTrack) -> Unit,
    modifier: Modifier = Modifier
) {
    // Local list for smooth visual feedback during drag; syncs from parent when idle
    var localItems by remember { mutableStateOf(tracks) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(tracks) {
        if (!isDragging) localItems = tracks
    }

    var draggedId by remember { mutableStateOf<String?>(null) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    val itemHeightDp = 68.dp
    // Capture LocalDensity here (in Composable scope) for use inside pointerInput
    val localDensity = androidx.compose.ui.platform.LocalDensity.current
    val itemHeightPx = with(localDensity) { itemHeightDp.toPx() }

    Column(modifier = modifier) {
        localItems.forEachIndexed { index, track ->
            val isDragged = draggedId == track.id

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 3.dp)
                    .graphicsLayer {
                        if (isDragged) {
                            translationY = dragOffsetY
                            shadowElevation = 8f
                            alpha = 0.9f
                        }
                    }
            ) {
                SelectedTrackRow(
                    track = track,
                    index = index,
                    onRemove = { onRemove(track) },
                    dragHandle = Modifier.pointerInput(track.id) {
                        // track.id is stable → pointerInput isn't restarted on reorder
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                isDragging = true
                                draggedId = track.id
                                dragOffsetY = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffsetY += dragAmount.y
                                val currentIdx = localItems.indexOfFirst { it.id == track.id }
                                if (currentIdx == -1) return@detectDragGesturesAfterLongPress
                                val targetIdx = (currentIdx + (dragOffsetY / itemHeightPx).toInt())
                                    .coerceIn(0, localItems.size - 1)
                                if (targetIdx != currentIdx) {
                                    val newList = localItems.toMutableList()
                                    val item = newList.removeAt(currentIdx)
                                    newList.add(targetIdx, item)
                                    localItems = newList
                                    // Remove the visual offset for the cells we crossed
                                    dragOffsetY -= (targetIdx - currentIdx) * itemHeightPx
                                }
                            },
                            onDragEnd = {
                                val finalIdx = localItems.indexOfFirst { it.id == track.id }
                                val originalIdx = tracks.indexOfFirst { it.id == track.id }
                                if (finalIdx != -1 && originalIdx != -1 && finalIdx != originalIdx) {
                                    onReorder(originalIdx, finalIdx)
                                }
                                isDragging = false
                                draggedId = null
                                dragOffsetY = 0f
                            },
                            onDragCancel = {
                                localItems = tracks
                                isDragging = false
                                draggedId = null
                                dragOffsetY = 0f
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SelectedTrackRow(
    track: ReaperTrack,
    index: Int,
    onRemove: () -> Unit,
    dragHandle: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, WeaperGreen.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag handle
            Icon(
                Icons.Default.DragHandle,
                contentDescription = "Drag to reorder",
                tint = TextDisabled,
                modifier = dragHandle
                    .size(24.dp)
            )

            Spacer(Modifier.width(10.dp))

            Text(
                text = "${index + 1}",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(22.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
                if (track.artist.isNotBlank()) {
                    Text(track.artist, color = TextSecondary, fontSize = 12.sp)
                }
            }

            // Green selection dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(WeaperGreen)
            )

            Spacer(Modifier.width(10.dp))

            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Text("✕", color = TextDisabled, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun AvailableTrackRow(
    track: ReaperTrack,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val bgColor = if (isSelected) DarkSurfaceVariant else DarkSurface

    Card(
        onClick = onToggle,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) Modifier.border(1.dp, WeaperGreen.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                else Modifier
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
                if (track.artist.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(track.artist, color = TextSecondary, fontSize = 12.sp)
                }
                if (track.bpm != null) {
                    Text("${track.bpm} BPM", color = TextDisabled, fontSize = 11.sp)
                }
            }

            if (track.duration.isNotBlank()) {
                Text(
                    text = track.duration,
                    color = TextDisabled,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }

            // Green dot when selected
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) WeaperGreen else DarkSurfaceVariant
                    )
            )
        }
    }
}

@Composable
private fun PlaylistNameDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Name your playlist", color = TextPrimary) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Playlist name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onCreate(name.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = WeaperGreen),
                enabled = name.isNotBlank()
            ) {
                Text("Create", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun AddTrackDialog(
    onDismiss: () -> Unit,
    onAdd: (ReaperTrack) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var markerId by remember { mutableStateOf("1") }
    var bpm by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Add REAPER Song", color = TextPrimary) },
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
                    label = { Text("Artist (optional)") },
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
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration e.g. 3:45 (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(
                            ReaperTrack(
                                title = title.trim(),
                                artist = artist.trim(),
                                markerId = markerId.toIntOrNull() ?: 1,
                                bpm = bpm.toIntOrNull(),
                                duration = duration.trim()
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
