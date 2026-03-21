package com.weaper.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weaper.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val oscHost: String = "",
    val oscPort: String = "8000",
    val syncServerUrl: String = ""
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: AppPreferences
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferences.oscHostFlow,
        preferences.oscPortFlow,
        preferences.syncServerUrlFlow
    ) { host, port, syncUrl ->
        SettingsUiState(
            oscHost = host,
            oscPort = port.toString(),
            syncServerUrl = syncUrl
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun saveOscHost(host: String) {
        viewModelScope.launch { preferences.setOscHost(host) }
    }

    fun saveOscPort(port: String) {
        val portInt = port.toIntOrNull() ?: AppPreferences.DEFAULT_OSC_PORT
        viewModelScope.launch { preferences.setOscPort(portInt) }
    }

    fun saveSyncServerUrl(url: String) {
        viewModelScope.launch { preferences.setSyncServerUrl(url) }
    }
}
