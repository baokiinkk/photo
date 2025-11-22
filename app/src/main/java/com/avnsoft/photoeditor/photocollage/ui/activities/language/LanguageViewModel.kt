package com.avnsoft.photoeditor.photocollage.ui.activities.language

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basesource.base.utils.LanguageManager
import com.basesource.base.utils.LanguageType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LanguageViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LanguageUiState())
    val uiState: StateFlow<LanguageUiState> = _uiState.asStateFlow()

    fun initializeLanguage(context: Context) {
        val currentLanguage = LanguageManager.getCurrentLanguage(context)
        _uiState.value = _uiState.value.copy(currentLanguage = currentLanguage)
    }

    fun selectLanguage(language: LanguageType) {
        _uiState.value = _uiState.value.copy(currentLanguage = language)
    }

    fun changeLanguage(context: Context, language: LanguageType) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Apply language change
                LanguageManager.setLanguage(context, language)

                _uiState.value = _uiState.value.copy(
                    currentLanguage = language,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                // Handle error if needed
            }
        }
    }
}
