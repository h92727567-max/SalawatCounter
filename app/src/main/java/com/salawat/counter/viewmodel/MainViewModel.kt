package com.salawat.counter.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.salawat.counter.SalawatApp
import com.salawat.counter.data.entity.DailyRecord
import com.salawat.counter.utils.PreferenceManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as SalawatApp).repository
    private val preferenceManager = PreferenceManager(application)

    val todayRecord: StateFlow<DailyRecord?> = repository.getTodayRecord()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalCount: StateFlow<Int> = repository.getTotalCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _isListening = MutableStateFlow(false)
    val isListening = _isListening.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused = _isPaused.asStateFlow()

    fun incrementManual() {
        viewModelScope.launch {
            repository.incrementToday()
        }
    }

    fun resetCounter() {
        viewModelScope.launch {
            repository.resetToday()
        }
    }

    fun updateTarget(target: Int) {
        viewModelScope.launch {
            repository.updateTarget(target)
        }
    }

    fun setListening(active: Boolean) {
        _isListening.value = active
    }

    fun togglePause() {
        _isPaused.value = !_isPaused.value
    }
}
