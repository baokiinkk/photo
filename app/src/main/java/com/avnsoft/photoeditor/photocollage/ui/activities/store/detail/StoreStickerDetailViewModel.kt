package com.avnsoft.photoeditor.photocollage.ui.activities.store.detail

import com.avnsoft.photoeditor.photocollage.data.model.sticker.StickerModel
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class StoreStickerDetailViewModel : BaseViewModel() {

    val uiState = MutableStateFlow(StoreStickerDetailUIState())

    fun initData(item: StickerModel?) {
        uiState.value = uiState.value.copy(
            item = item
        )
    }
}

data class StoreStickerDetailUIState(
    val item: StickerModel? = null
)