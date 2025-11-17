package com.avnsoft.photoeditor.photocollage.ui.activities.store

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
class StoreViewModel(
    private val stickerRepo: StickerRepoImpl
) : BaseViewModel() {

    val uiState = MutableStateFlow(StoreUIState())

    init {
        getPreviewStickers()
    }

    fun getPreviewStickers() {
        viewModelScope.launch(Dispatchers.IO) {
            val response = stickerRepo.getPreviewStickers()
            response.collect { item ->
                uiState.update {
                    it.copy(
                        stickers = item
                    )
                }
            }
        }
    }
}

data class StoreUIState(
    val stickers: List<StickerModel> = emptyList(),
    val selectedTabSticker: StickerModel? = null,
)