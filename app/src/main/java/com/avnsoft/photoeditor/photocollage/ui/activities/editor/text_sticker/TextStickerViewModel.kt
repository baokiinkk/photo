package com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker

import android.graphics.Bitmap
import androidx.compose.ui.unit.IntSize
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.AddTextProperties
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.FontAsset
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.FontItem
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.TextSticker
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class TextStickerViewModel : BaseViewModel() {

    val uiState = MutableStateFlow(TextStickerUIState())

    var textMeasured: Boolean = false

    fun getConfigTextSticker(bitmap: Bitmap? = null) {
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
    ) {
        val addTextProperties = AddTextProperties.defaultProperties
        addTextProperties.fontName = item.fontPath
        addTextProperties.fontIndex = index
        addTextProperties.text = "Click to Edit"
        addTextProperties.textWidth = originTextFieldSize.width
        addTextProperties.textHeight = originTextFieldSize.height
        uiState.update {
            it.copy(
                addTextProperties = addTextProperties,
                editTextProperties = addTextProperties,
                textIndex = index
            )
        }
    }

    var originTextFieldSize: IntSize = IntSize.Zero

    fun addFirstTextSticker(
        textFieldSize: IntSize
    ) {
        originTextFieldSize = textFieldSize
        addTextSticker(
            index = 0,
            item = FontAsset.listFonts.first(),
        )
    }

    fun editTextSticker(textSticker: TextSticker) {
        uiState.update {
            it.copy(
                editTextProperties = textSticker.getAddTextProperties()
            )
        }
    }

    fun showLoading() {
        uiState.update {
            it.copy(
                isLoading = true
            )
        }
    }

    fun hideLoading() {
        uiState.update {
            it.copy(
                isLoading = false
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
    val textIndex: Int = 0,
    val isLoading: Boolean = false,
)