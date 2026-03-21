package com.weaper.presentation.soundboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weaper.domain.model.SoundboardItem
import com.weaper.domain.repository.SoundboardRepository
import com.weaper.domain.usecase.TriggerSoundUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SoundboardUiState(
    val items: List<SoundboardItem> = emptyList(),
    val activeItemId: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class SoundboardViewModel @Inject constructor(
    private val soundboardRepository: SoundboardRepository,
    private val triggerSoundUseCase: TriggerSoundUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SoundboardUiState(isLoading = true))
    val uiState: StateFlow<SoundboardUiState> = _uiState.asStateFlow()

    init {
        loadSoundboard()
    }

    private fun loadSoundboard() {
        viewModelScope.launch {
            soundboardRepository.getSoundboard()
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

    fun triggerSound(item: SoundboardItem) {
        if (!item.isAvailable) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(activeItemId = item.id)
            triggerSoundUseCase(item)
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "OSC error: ${e.message}"
                    )
                }
            // Clear active state after brief feedback
            kotlinx.coroutines.delay(200)
            _uiState.value = _uiState.value.copy(activeItemId = null)
        }
    }

    fun addItem(item: SoundboardItem) {
        viewModelScope.launch {
            soundboardRepository.saveSoundboardItem(item)
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            soundboardRepository.deleteSoundboardItem(id)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
