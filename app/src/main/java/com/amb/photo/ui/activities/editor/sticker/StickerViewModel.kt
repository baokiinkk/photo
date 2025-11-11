package com.amb.photo.ui.activities.editor.sticker

import android.graphics.Bitmap
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class StickerViewModel : BaseViewModel() {

    val uiState = MutableStateFlow(StickerUIState())


    fun getConfigSticker(bitmap: Bitmap?) {
        uiState.update {
            it.copy(
                originBitmap = bitmap,
            )
        }
    }


}

data class StickerUIState(
    val originBitmap: Bitmap? = null,
    val isTabSelected: Boolean = false,
    val emojiTabs: List<EmojiTab> = emptyList()
)

data class EmojiTab(
    val tabName: String,
    val tabIcon: String,
    val items: List<Emoji>,
)

data class Emoji(
    val id: Int,
    val name: String = "",
    val icon: String
)