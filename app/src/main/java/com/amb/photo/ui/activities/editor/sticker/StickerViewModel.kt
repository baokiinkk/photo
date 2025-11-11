package com.amb.photo.ui.activities.editor.sticker

import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import com.amb.photo.R
import com.amb.photo.ui.activities.editor.sticker.lib.EmojiTab
import com.amb.photo.ui.activities.editor.sticker.lib.StickerAsset
import com.amb.photo.ui.activities.editor.sticker.lib.StickerAsset.lstCatFaces
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
        viewModelScope.launch(Dispatchers.IO) {
            val emojiTabs = StickerAsset.initStickerPager()
            uiState.update {
                it.copy(
                    emojiTabs = emojiTabs
                )
            }
        }
    }

    fun selectedTab(tab: EmojiTab) {
        uiState.update {
            it.copy(
                currentTab = tab
            )
        }
    }

    fun selectedSticker(path: String) {
        uiState.update {
            it.copy(
                pathSticker = path
            )
        }
    }

}

data class StickerUIState(
    val originBitmap: Bitmap? = null,
    val currentTab: EmojiTab = EmojiTab(
        "CatFace",
        R.drawable.ic_cate_sticker_cat,
        items = lstCatFaces()
    ),
    val emojiTabs: List<EmojiTab> = emptyList(),
    val pathSticker: String= ""
)

