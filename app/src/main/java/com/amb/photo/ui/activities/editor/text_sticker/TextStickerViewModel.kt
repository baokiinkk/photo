package com.amb.photo.ui.activities.editor.text_sticker

import android.graphics.Bitmap
import androidx.compose.ui.unit.IntSize
import com.amb.photo.ui.activities.editor.text_sticker.lib.AddTextProperties
import com.amb.photo.ui.activities.editor.text_sticker.lib.FontAsset
import com.amb.photo.ui.activities.editor.text_sticker.lib.FontItem
import com.amb.photo.ui.activities.editor.text_sticker.lib.TextSticker
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class TextStickerViewModel : BaseViewModel() {

    val uiState = MutableStateFlow(TextStickerUIState())

    var textMeasured: Boolean = false

    fun getConfigTextSticker(bitmap: Bitmap?) {
        uiState.update {
            it.copy(
                originBitmap = bitmap,
                items = FontAsset.listFonts
            )
        }
    }

    fun addTextSticker(
        index: Int,
        item: FontItem,
        textFieldSize: IntSize
    ) {
        val addTextProperties = AddTextProperties.defaultProperties
        addTextProperties.fontName = item.fontPath
        addTextProperties.fontIndex = index
        addTextProperties.text = "Click to Edit"
        addTextProperties.textWidth = textFieldSize.width
        addTextProperties.textHeight = textFieldSize.height
        uiState.update {
            it.copy(
                addTextProperties = addTextProperties,
                editTextProperties = addTextProperties,
                textIndex = index
            )
        }
    }

    fun addFirstTextSticker(
        textFieldSize: IntSize
    ) {
        addTextSticker(
            index = 0,
            item = FontAsset.listFonts.first(),
            textFieldSize = textFieldSize
        )
    }

    fun editTextSticker(textSticker: TextSticker) {
        uiState.update {
            it.copy(
                editTextProperties = textSticker.getAddTextProperties()
            )
        }
    }

    fun showEditText() {
        uiState.update {
            it.copy(
                isShowEditText = true
            )
        }
    }

    fun hideEditText() {
        uiState.update {
            it.copy(
                isShowEditText = false
            )
        }
    }
}

data class TextStickerUIState(
    val originBitmap: Bitmap? = null,
    val items: List<FontItem> = emptyList(),
    val addTextProperties: AddTextProperties? = null,
    val editTextProperties: AddTextProperties? = null,
    val isShowEditText: Boolean = false,
    val textIndex: Int = 0
)