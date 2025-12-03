package com.avnsoft.photoeditor.photocollage.ui.activities.editor.frame

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.FrameSelection
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class FrameViewModel(
    private val context: Context
) : BaseViewModel() {

    val uiState = MutableStateFlow(FrameUIState())


    fun getConfig(screenInput: ToolInput?) {
        viewModelScope.launch(Dispatchers.IO) {
            uiState.update {
                it.copy(originBitmap = screenInput?.getBitmap(context))
            }
        }
    }

    fun updateFrame(selection: FrameSelection) {
        uiState.update { it.copy(frameSelection = selection) }
    }

}

data class FrameUIState(
    val originBitmap: Bitmap? = null,
    val frameSelection: FrameSelection? = null
)