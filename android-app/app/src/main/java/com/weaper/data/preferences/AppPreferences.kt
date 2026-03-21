package com.weaper.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "weaper_prefs")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_OSC_HOST = stringPreferencesKey("osc_host")
        val KEY_OSC_PORT = intPreferencesKey("osc_port")
        val KEY_SYNC_SERVER_URL = stringPreferencesKey("sync_server_url")
        val KEY_SYNC_DIRECTORY = stringPreferencesKey("sync_directory")

        const val DEFAULT_OSC_PORT = 8000
        const val DEFAULT_SYNC_SERVER_URL = "http://192.168.0.100:3000"
    }

    private val dataStore = context.dataStore

    // Synchronous getters for use in non-suspend contexts (e.g., OscClient)
    val oscHost: String
        get() = runBlocking { dataStore.data.first()[KEY_OSC_HOST] ?: "" }

    val oscPort: Int
        get() = runBlocking { dataStore.data.first()[KEY_OSC_PORT] ?: DEFAULT_OSC_PORT }

    val syncServerUrl: String
        get() = runBlocking { dataStore.data.first()[KEY_SYNC_SERVER_URL] ?: DEFAULT_SYNC_SERVER_URL }

    // Flow-based getters for reactive UI
    val oscHostFlow: Flow<String> = dataStore.data.map { it[KEY_OSC_HOST] ?: "" }
    val oscPortFlow: Flow<Int> = dataStore.data.map { it[KEY_OSC_PORT] ?: DEFAULT_OSC_PORT }
    val syncServerUrlFlow: Flow<String> = dataStore.data.map { it[KEY_SYNC_SERVER_URL] ?: DEFAULT_SYNC_SERVER_URL }

    suspend fun setOscHost(host: String) {
        dataStore.edit { it[KEY_OSC_HOST] = host }
    }

    suspend fun setOscPort(port: Int) {
        dataStore.edit { it[KEY_OSC_PORT] = port }
    }

    suspend fun setSyncServerUrl(url: String) {
        dataStore.edit { it[KEY_SYNC_SERVER_URL] = url }
    }
}
