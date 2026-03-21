package com.weaper.presentation.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weaper.domain.model.Playlist
import com.weaper.domain.model.ReaperTrack
import com.weaper.domain.repository.PlaylistRepository
import com.weaper.domain.repository.ReaperTrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistCreationUiState(
    val availableTracks: List<ReaperTrack> = emptyList(),
    val selectedTrackIds: List<String> = emptyList(),
    val orderedSelectedTracks: List<ReaperTrack> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class PlaylistCreationViewModel @Inject constructor(
    private val trackRepository: ReaperTrackRepository,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistCreationUiState())
    val uiState: StateFlow<PlaylistCreationUiState> = _uiState.asStateFlow()

    init {
        loadTracks()
    }

    private fun loadTracks() {
        viewModelScope.launch {
            trackRepository.getTracks()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
                .collect { tracks ->
                    _uiState.value = _uiState.value.copy(
                        availableTracks = tracks,
                        isLoading = false
                    )
                }
        }
    }

    fun toggleTrackSelection(track: ReaperTrack) {
        val current = _uiState.value
        val selectedIds = current.selectedTrackIds.toMutableList()
        val orderedTracks = current.orderedSelectedTracks.toMutableList()
        if (selectedIds.contains(track.id)) {
            selectedIds.remove(track.id)
            orderedTracks.removeAll { it.id == track.id }
        } else {
            selectedIds.add(track.id)
            orderedTracks.add(track)
        }
        _uiState.value = current.copy(
            selectedTrackIds = selectedIds,
            orderedSelectedTracks = orderedTracks
        )
    }

    fun reorderTracks(fromIndex: Int, toIndex: Int) {
        val ordered = _uiState.value.orderedSelectedTracks.toMutableList()
        if (fromIndex < 0 || toIndex < 0 || fromIndex >= ordered.size || toIndex >= ordered.size) return
        val item = ordered.removeAt(fromIndex)
        ordered.add(toIndex, item)
        _uiState.value = _uiState.value.copy(
            orderedSelectedTracks = ordered,
            selectedTrackIds = ordered.map { it.id }
        )
    }

    fun createPlaylist(name: String) {
        val current = _uiState.value
        if (current.orderedSelectedTracks.isEmpty()) return
        viewModelScope.launch {
            _uiState.value = current.copy(isSaving = true)
            try {
                val playlist = Playlist(
                    name = name,
                    trackIds = current.orderedSelectedTracks.map { it.id }
                )
                playlistRepository.savePlaylist(playlist)
                _uiState.value = _uiState.value.copy(isSaving = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun addTrack(track: ReaperTrack) {
        viewModelScope.launch {
            try {
                trackRepository.saveTrack(track)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
