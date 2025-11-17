package com.avnsoft.photoeditor.photocollage.ui.activities.store.tab.sticker.detail

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
class StoreStickerDetailViewModel(
    private val stickerRepo: StickerRepoImpl
) : BaseViewModel() {

    val uiState = MutableStateFlow(StoreStickerDetailUIState())

    fun initData(item: StickerModel?) {
        uiState.update {
            it.copy(
                item = item
            )
        }
    }

    fun updateIsUsedById(eventId: Long) {
        viewModelScope.launch(Dispatchers.IO){
            val response = stickerRepo.updateIsUsedById(
                eventId,
                true
            )
            if (response) {
                uiState.update {
                    it.copy(
                        item = it.item?.copy(
                            isUsed = true
                        )
                    )
                }
            }
        }
    }
}

data class StoreStickerDetailUIState(
    val item: StickerModel? = null
)