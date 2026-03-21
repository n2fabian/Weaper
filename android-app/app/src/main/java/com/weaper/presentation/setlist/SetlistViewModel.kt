package com.weaper.presentation.setlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weaper.domain.model.SetlistItem
import com.weaper.domain.repository.SetlistRepository
import com.weaper.domain.usecase.PlaySetlistItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SetlistUiState(
    val items: List<SetlistItem> = emptyList(),
    val activeItemId: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class SetlistViewModel @Inject constructor(
    private val setlistRepository: SetlistRepository,
    private val playSetlistItemUseCase: PlaySetlistItemUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetlistUiState(isLoading = true))
    val uiState: StateFlow<SetlistUiState> = _uiState.asStateFlow()

    init {
        loadSetlist()
    }

    private fun loadSetlist() {
        viewModelScope.launch {
            setlistRepository.getSetlist()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
                .collect { items ->
                    _uiState.value = _uiState.value.copy(
                        items = items,
                        isLoading = false
                    )
                }
        }
    }

    fun playItem(item: SetlistItem) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(activeItemId = item.id)
            playSetlistItemUseCase(item)
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to send OSC: ${e.message}"
                    )
                }
        }
    }

    fun addItem(item: SetlistItem) {
        viewModelScope.launch {
            setlistRepository.saveSetlistItem(item)
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            setlistRepository.deleteSetlistItem(id)
            if (_uiState.value.activeItemId == id) {
                _uiState.value = _uiState.value.copy(activeItemId = null)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
