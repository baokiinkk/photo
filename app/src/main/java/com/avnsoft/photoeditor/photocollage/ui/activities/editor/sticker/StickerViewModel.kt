package com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker

import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.EmojiTab
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.StickerAsset
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.StickerAsset.lstEmoj
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

    fun addStickerFromAsset(path: String) {
        uiState.update {
            it.copy(
                pathSticker = StickerData.StickerFromAsset(path)
            )
        }
    }

    fun addStickerFromGallery(path: String) {
        uiState.update {
            it.copy(
                pathSticker = StickerData.StickerFromGallery(path)
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

data class StickerUIState(
    val originBitmap: Bitmap? = null,
    val currentTab: EmojiTab = EmojiTab(
        "Emoji",
        R.drawable.ic_cate_sticker_emoij,
        items = lstEmoj()
    ),
    val emojiTabs: List<EmojiTab> = emptyList(),
    val pathSticker: StickerData = StickerData.StickerFromAsset(""),
    val isLoading: Boolean = false,
)

