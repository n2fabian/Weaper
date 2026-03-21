package com.weaper.presentation.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weaper.data.osc.OscClient
import com.weaper.domain.model.OscCommand
import com.weaper.domain.model.Playlist
import com.weaper.domain.model.ReaperTrack
import com.weaper.domain.repository.PlaylistRepository
import com.weaper.domain.repository.ReaperTrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val playlist: Playlist? = null,
    val tracks: List<ReaperTrack> = emptyList(),
    val currentIndex: Int = 0,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val currentTrack: ReaperTrack? get() = tracks.getOrNull(currentIndex)
    val hasPrevious: Boolean get() = currentIndex > 0
    val hasNext: Boolean get() = currentIndex < tracks.size - 1
}

@HiltViewModel
class PlaylistPlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playlistRepository: PlaylistRepository,
    private val trackRepository: ReaperTrackRepository,
    private val oscClient: OscClient
) : ViewModel() {

    private val playlistId: String = checkNotNull(savedStateHandle["playlistId"]) {
        "PlaylistPlayerViewModel requires a non-null 'playlistId' navigation argument"
    }

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        loadPlaylist()
    }

    private fun loadPlaylist() {
        viewModelScope.launch {
            try {
                val playlists = playlistRepository.getPlaylists().first()
                val playlist = playlists.find { it.id == playlistId }
                if (playlist == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Playlist not found"
                    )
                    return@launch
                }
                val allTracks = trackRepository.getTracks().first()
                val orderedTracks = playlist.trackIds.mapNotNull { id ->
                    allTracks.find { it.id == id }
                }
                _uiState.value = _uiState.value.copy(
                    playlist = playlist,
                    tracks = orderedTracks,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun play() {
        val track = _uiState.value.currentTrack ?: return
        viewModelScope.launch {
            try {
                oscClient.send(OscCommand.GoToMarker(track.markerId))
                oscClient.send(OscCommand.Play)
                _uiState.value = _uiState.value.copy(isPlaying = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun pause() {
        viewModelScope.launch {
            try {
                // REAPER OSC: /action/40046 = Pause
                oscClient.send(OscCommand.CustomPath("/action/40046"))
                _uiState.value = _uiState.value.copy(isPlaying = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun next() {
        val state = _uiState.value
        if (!state.hasNext) return
        val newIndex = state.currentIndex + 1
        _uiState.value = state.copy(currentIndex = newIndex, isPlaying = false)
        play()
    }

    fun previous() {
        val state = _uiState.value
        if (!state.hasPrevious) return
        val newIndex = state.currentIndex - 1
        _uiState.value = state.copy(currentIndex = newIndex, isPlaying = false)
        play()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
