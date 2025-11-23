package com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker

import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import com.avnsoft.photoeditor.photocollage.data.model.sticker.StickerModel
import com.avnsoft.photoeditor.photocollage.data.repository.StickerRepoImpl
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class StickerViewModel(
    private val stickerRepo: StickerRepoImpl
) : BaseViewModel() {

    val uiState = MutableStateFlow(StickerUIState())

    var isAddSticker = false

    fun getConfigSticker(bitmap: Bitmap?=null) {
        if (bitmap!=null){
            uiState.update {
                it.copy(
                    originBitmap = bitmap,
                )
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            val response = stickerRepo.getStickers()
            when (response) {
                is com.basesource.base.result.Result.Success -> {
                    uiState.update {
                        it.copy(
                            stickers = response.data,
                            currentTab = response.data.first()
                        )
                    }
                }

                is com.basesource.base.result.Result.Error -> {
                    uiState.update {
                        it.copy(
                            error = response.exception.message
                        )
                    }
                }

                else -> {

                }
            }
        }
    }

    fun selectedTab(tab: StickerModel) {
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
    val currentTab: StickerModel? = null,
    val stickers: List<StickerModel> = emptyList(),
    val pathSticker: StickerData = StickerData.StickerFromAsset(""),
    val isLoading: Boolean = false,
    val error: String? = null
)

