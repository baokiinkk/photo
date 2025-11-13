package com.amb.photo.ui.activities.editor.background

import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class BackgroundViewModel : BaseViewModel() {

    val uiState = MutableStateFlow(BackgroundUIState())

    fun getConfigBackground(bitmap: Bitmap?) {
        uiState.update {
            it.copy(
                originBitmap = bitmap,
            )
        }
    }

    fun updateBackgroundColor(color: String?) {
        uiState.update {
            it.copy(
                backgroundColor = color,
            )
        }
    }
}

data class BackgroundUIState(
    val originBitmap: Bitmap? = null,
    val backgroundColor: String? = null,  // hex color
)