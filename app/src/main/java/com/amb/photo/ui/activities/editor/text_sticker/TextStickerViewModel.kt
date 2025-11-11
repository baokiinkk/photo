package com.amb.photo.ui.activities.editor.text_sticker

import android.graphics.Bitmap
import com.amb.photo.ui.activities.editor.text_sticker.lib.FontAsset
import com.amb.photo.ui.activities.editor.text_sticker.lib.FontItem
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class TextStickerViewModel : BaseViewModel() {

    val uiState = MutableStateFlow(TextStickerUIState())

    fun getConfigTextSticker(bitmap: Bitmap?) {
        uiState.update {
            it.copy(
                originBitmap = bitmap,
                items = FontAsset.listFonts
            )
        }
    }

}

data class TextStickerUIState(
    val originBitmap: Bitmap? = null,
    val items: List<FontItem> = emptyList()
)