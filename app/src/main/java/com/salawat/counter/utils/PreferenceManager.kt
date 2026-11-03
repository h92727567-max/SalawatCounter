package com.salawat.counter.utils

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

class PreferenceManager(private val context: Context) {

    private object Keys {
        val CONFIDENCE = floatPreferencesKey("confidence_threshold")
        val DEBOUNCE = longPreferencesKey("debounce_ms")
        val VIBRATION = booleanPreferencesKey("vibration_enabled")
        val SOUND = booleanPreferencesKey("sound_enabled")
        val DEFAULT_TARGET = intPreferencesKey("default_target")
    }

    val confidenceThreshold: Flow<Float> = context.dataStore.data.map {
        it[Keys.CONFIDENCE] ?: 0.65f
    }

    val debounceMs: Flow<Long> = context.dataStore.data.map {
        it[Keys.DEBOUNCE] ?: 2000L
    }

    val vibrationEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.VIBRATION] ?: true
    }

    val soundEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.SOUND] ?: false
    }

    val defaultTarget: Flow<Int> = context.dataStore.data.map {
        it[Keys.DEFAULT_TARGET] ?: 100
    }

    suspend fun setConfidence(value: Float) {
        context.dataStore.edit { it[Keys.CONFIDENCE] = value }
    }

    suspend fun setDebounce(value: Long) {
        context.dataStore.edit { it[Keys.DEBOUNCE] = value }
    }

    suspend fun setVibration(enabled: Boolean) {
        context.dataStore.edit { it[Keys.VIBRATION] = enabled }
    }

    suspend fun setSound(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND] = enabled }
    }

    suspend fun setDefaultTarget(value: Int) {
        context.dataStore.edit { it[Keys.DEFAULT_TARGET] = value }
    }
}
