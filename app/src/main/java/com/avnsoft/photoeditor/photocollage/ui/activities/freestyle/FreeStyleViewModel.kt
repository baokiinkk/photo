package com.avnsoft.photoeditor.photocollage.ui.activities.freestyle

import android.content.Context
import android.net.Uri
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.avnsoft.photoeditor.photocollage.BaseApplication
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.BackgroundSelection
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.FrameSelection
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.Sticker
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.AddTextProperties
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.FreeStyleSticker
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.Photo
import com.avnsoft.photoeditor.photocollage.utils.FileUtil
import com.avnsoft.photoeditor.photocollage.utils.FileUtil.toFile
import com.avnsoft.photoeditor.photocollage.utils.FileUtil.urlToDrawable
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class FreeStyleViewModel(
    private val context: Context
) : BaseViewModel() {

    val freeStyleSticker = MutableLiveData<MutableList<FreeStyleSticker>>()

    val uiState = MutableStateFlow(FreeStyleUIState())


    private val _removeSticker = Channel<Sticker>()
    val removeSticker = _removeSticker.receiveAsFlow()

    var isEditTextSticker: Boolean = false

    fun initData(uriList: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = uriList.mapIndexed { index, uri ->
                val drawable = FileUtil.decodeUriToDrawable(context, uri, 400, 400)
                FreeStyleSticker(index, Photo(uri, 0), drawable)
            }.toMutableList()
            freeStyleSticker.postValue(data)
        }
    }

    fun showStickerTool() {
        uiState.update {
            it.copy(
                isShowStickerTool = true
            )
        }
    }

    fun cancelSticker() {
        uiState.update {
            it.copy(
                isShowStickerTool = false
            )
        }
    }

    fun applySticker(sticker: Sticker?) {
        uiState.update {
            it.copy(
                isShowStickerTool = false
            )
        }
    }

    fun showTextSticker() {
        uiState.update {
            it.copy(
                isShowTextStickerTool = true
            )
        }
    }

    fun cancelTextSticker() {
        uiState.update {
            it.copy(
                isShowTextStickerTool = false,
                isVisibleTextField = false
            )
        }
        isEditTextSticker = false
    }

    fun applyTextSticker() {
        uiState.update {
            it.copy(
                isShowTextStickerTool = false,
                isVisibleTextField = false
            )
        }
        isEditTextSticker = false
    }


    fun showEditTextSticker(editTextProperties: AddTextProperties?) {
//        isEditTextSticker = true
        uiState.update {
            it.copy(
//                isVisibleTextField = true,
//                editTextProperties = editTextProperties ?: AddTextProperties.defaultProperties,
                isShowTextStickerTool = true
            )
        }
    }

    fun hideEditTextSticker() {
        uiState.update {
            it.copy(
                isVisibleTextField = false,
            )
        }
    }

    fun showBackgroundTool() {
        uiState.update {
            it.copy(
                isShowBackgroundTool = true
            )
        }
    }

    fun cancelBackgroundTool() {
        uiState.update {
            it.copy(
                isShowBackgroundTool = false
            )
        }
    }

    fun applyBackgroundTool() {
        uiState.update {
            it.copy(
                isShowBackgroundTool = false
            )
        }
    }

    fun updateBackground(selection: BackgroundSelection) {
        uiState.update {
            it.copy(
                backgroundSelection = selection
            )
        }
    }

    fun addMorePhoto(result: List<String>?) {
        viewModelScope.launch(Dispatchers.IO) {
            val uris = result?.map { it.toUri() } ?: emptyList()
            val data = uris.mapIndexed { index, uri ->
//                val url = "https://cdn-icons-png.freepik.com/512/3135/3135715.png"
//                url.toFreeStyleSticker(index)
            val drawable = FileUtil.decodeUriToDrawable(context, uri, 400, 400)
            FreeStyleSticker(index, Photo(uri, 0), drawable)
            }.toMutableList()
            freeStyleSticker.postValue(data)
        }
    }

    fun showRatioTool() {
        uiState.update {
            it.copy(
                isShowRatioTool = true
            )
        }
    }

    fun cancelRatioTool() {
        uiState.update {
            it.copy(
                isShowRatioTool = false
            )
        }
    }

    fun applyRatioTool() {
        uiState.update {
            it.copy(
                isShowRatioTool = false
            )
        }
    }

    fun updateRatio(ratio: Pair<Int, Int>?) {
        uiState.update {
            it.copy(
                ratio = ratio
            )
        }
    }

    fun showFrameTool() {
        uiState.update {
            it.copy(
                isShowFrameTool = true
            )
        }
    }

    fun cancelFrameTool() {
        uiState.update {
            it.copy(
                isShowFrameTool = false
            )
        }
    }

    fun applyFrameTool() {
        uiState.update {
            it.copy(
                isShowFrameTool = false
            )
        }
    }

    fun updateFrame(selection: FrameSelection) {
        uiState.update {
            it.copy(
                frameSelection = selection
            )
        }
    }

    fun showDiscardDialog() {
        uiState.update {
            it.copy(
                showDiscardDialog = true
            )
        }
    }

    fun hideDiscardDialog() {
        uiState.update {
            it.copy(
                showDiscardDialog = false
            )
        }
    }

    fun clearAllTool() {
        uiState.update {
            it.copy(
                isShowStickerTool = false,
                isShowTextStickerTool = false,
                isShowBackgroundTool = false,
                isShowRatioTool = false,
                isShowFrameTool = false
            )
        }
    }
}

data class FreeStyleUIState(
    val isShowStickerTool: Boolean = false,
    val isShowTextStickerTool: Boolean = false,
    val isVisibleTextField: Boolean = false,
    val editTextProperties: AddTextProperties = AddTextProperties.getAddTextProperties(),
    val isShowBackgroundTool: Boolean = false,
    val backgroundSelection: BackgroundSelection? = null,
    val isShowRatioTool: Boolean = false,
    val ratio: Pair<Int, Int>? = null,
    val isShowFrameTool: Boolean = false,
    val frameSelection: FrameSelection? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val showDiscardDialog: Boolean = false
)

sealed class StackFreeStyle(
    open val sticker: Sticker?,
    open val background: BackgroundSelection?
) {

    data class StackOriginal(
        override val sticker: Sticker,
        override val background: BackgroundSelection?
    ) : StackFreeStyle(sticker, background)

    data class Background(
        override val sticker: Sticker,
        override val background: BackgroundSelection?
    ) : StackFreeStyle(sticker, background)

    data class StackFrame(
        override val sticker: Sticker,
        override val background: BackgroundSelection?
    ) : StackFreeStyle(sticker, background)

    data class StackSticker(
        override val sticker: Sticker,
        override val background: BackgroundSelection?
    ) : StackFreeStyle(sticker, background)

    data class StackAddPhoto(
        override val sticker: Sticker,
        override val background: BackgroundSelection?
    ) : StackFreeStyle(sticker, background)

    data object NONE : StackFreeStyle(null, null)
}