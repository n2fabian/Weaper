package com.weaper.presentation.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.weaper.domain.model.Playlist
import com.weaper.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistListScreen(
    onNavigateToCreate: () -> Unit,
    onNavigateToPlayer: (String) -> Unit,
    viewModel: PlaylistListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Playlists",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            actions = {
                IconButton(onClick = onNavigateToCreate) {
                    Icon(Icons.Default.Add, contentDescription = "Add playlist", tint = WeaperBlue)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
        )

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = WeaperBlue)
            }
        } else if (uiState.playlists.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No playlists yet", color = TextSecondary, fontSize = 18.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Tap + to create one", color = TextDisabled, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.playlists, key = { it.id }) { playlist ->
                    PlaylistCard(
                        playlist = playlist,
                        onPlay = { onNavigateToPlayer(playlist.id) },
                        onDelete = { viewModel.deletePlaylist(playlist.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistCard(
    playlist: Playlist,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(WeaperBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = WeaperBlue,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${playlist.trackIds.size} song${if (playlist.trackIds.size != 1) "s" else ""}",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            IconButton(
                onClick = onPlay,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(WeaperBlue)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

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
