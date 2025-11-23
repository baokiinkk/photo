package com.avnsoft.photoeditor.photocollage.ui.activities.editor.background

import android.graphics.Bitmap
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.BackgroundSelection
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class BackgroundViewModel : BaseViewModel() {

    val uiState = MutableStateFlow(BackgroundUIState())

    fun getConfigBackground(
        bitmap: Bitmap?,
        isBackgroundTransparent: Boolean
    ) {
        uiState.update {
            it.copy(
                originBitmap = bitmap,
                backgroundSelection = if (isBackgroundTransparent) {
                    BackgroundSelection.BackgroundTransparent(R.drawable.bg_transparent)
                } else {
                    null
                }
            )
        }
    }

    fun updateBackground(selection: BackgroundSelection) {
        uiState.update {
            it.copy(
                backgroundSelection = selection,
            )
        }
    }
}

data class BackgroundUIState(
    val originBitmap: Bitmap? = null,
    val backgroundSelection: BackgroundSelection? = null,
)