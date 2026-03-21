package com.weaper.presentation.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weaper.domain.model.SyncStatus
import com.weaper.domain.usecase.SyncFilesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncFilesUseCase: SyncFilesUseCase
) : ViewModel() {

    private val _syncStatus = MutableStateFlow<SyncStatus?>(null)
    val syncStatus: StateFlow<SyncStatus?> = _syncStatus.asStateFlow()

    fun startSync() {
        viewModelScope.launch {
            syncFilesUseCase()
                .collect { status ->
                    _syncStatus.value = status
                }
        }
    }
}
