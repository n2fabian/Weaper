package com.weaper.presentation.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.weaper.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistPlayerScreen(
    onNavigateBack: () -> Unit,
    viewModel: PlaylistPlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    uiState.errorMessage?.let { msg ->
        LaunchedEffect(msg) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.playlist?.name ?: "Player",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Back", color = TextSecondary, fontSize = 15.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = WeaperBlue)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(DarkBackground),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.weight(1f))

                // Album art placeholder
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    WeaperBlue.copy(alpha = 0.4f),
                                    DarkSurface
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.currentTrack?.title?.take(2)?.uppercase() ?: "♪",
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary.copy(alpha = 0.9f)
                    )
                }

                Spacer(Modifier.height(36.dp))

                // Song title & artist
                Text(
                    text = uiState.currentTrack?.title ?: "No track",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )

                Spacer(Modifier.height(8.dp))

                if (uiState.currentTrack?.artist?.isNotBlank() == true) {
                    Text(
                        text = uiState.currentTrack!!.artist,
                        color = TextSecondary,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                }

                // Track position indicator
                Text(
                    text = "${uiState.currentIndex + 1} / ${uiState.tracks.size}",
                    color = TextDisabled,
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(8.dp))

                // BPM badge
                uiState.currentTrack?.bpm?.let { bpm ->
                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "$bpm BPM",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                // Transport controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous
                    IconButton(
                        onClick = { viewModel.previous() },
                        enabled = uiState.hasPrevious,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(DarkSurface)
                    ) {
                        Icon(
                            Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = if (uiState.hasPrevious) TextPrimary else TextDisabled,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Play / Pause
                    IconButton(
                        onClick = {
                            if (uiState.isPlaying) viewModel.pause() else viewModel.play()
                        },
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(WeaperBlue)
                    ) {
                        Icon(
                            imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                            tint = TextPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // Next
                    IconButton(
                        onClick = { viewModel.next() },
                        enabled = uiState.hasNext,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(DarkSurface)
                    ) {
                        Icon(
                            Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = if (uiState.hasNext) TextPrimary else TextDisabled,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Track list preview
                if (uiState.tracks.size > 1) {
                    HorizontalDivider(color = DarkSurfaceVariant)
                    TrackQueuePreview(
                        tracks = uiState.tracks,
                        currentIndex = uiState.currentIndex
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun TrackQueuePreview(
    tracks: List<com.weaper.domain.model.ReaperTrack>,
    currentIndex: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Up next",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        val previewRange = ((currentIndex + 1)..(currentIndex + 3)).filter { it < tracks.size }
        previewRange.forEach { idx ->
            val track = tracks[idx]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${idx + 1}",
                    color = TextDisabled,
                    fontSize = 12.sp,
                    modifier = Modifier.width(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(track.title, color = TextSecondary, fontSize = 14.sp)
                    if (track.artist.isNotBlank()) {
                        Text(track.artist, color = TextDisabled, fontSize = 11.sp)
                    }
                }
                if (track.duration.isNotBlank()) {
                    Text(track.duration, color = TextDisabled, fontSize = 12.sp)
                }
            }
        }
    }
}
