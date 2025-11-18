package com.avnsoft.photoeditor.photocollage.ui.activities.store

import androidx.lifecycle.viewModelScope
import com.avnsoft.photoeditor.photocollage.data.model.pattern.PatternModel
import com.avnsoft.photoeditor.photocollage.data.model.sticker.StickerModel
import com.avnsoft.photoeditor.photocollage.data.repository.PatternRepository
import com.avnsoft.photoeditor.photocollage.data.repository.StickerRepoImpl
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class StoreViewModel(
    private val stickerRepo: StickerRepoImpl,
    private val patternRepo: PatternRepository
) : BaseViewModel() {

    val uiState = MutableStateFlow(StoreUIState())

    init {
        initAppData()
    }

    fun initAppData() {
        viewModelScope.launch(Dispatchers.IO) {
            async {
                getPreviewStickers()
            }

            async {
                getPreviewPatterns()
            }
        }
    }

    suspend fun getPreviewStickers() {
        try {
            val response = stickerRepo.getPreviewStickers()
            response.collect { item ->
                uiState.update {
                    it.copy(
                        stickers = item
                    )
                }
            }
        } catch (ex: Exception) {

        }
    }

    suspend fun getPreviewPatterns() {
        try {
            val response = patternRepo.getPreviewPatterns()
            response.collect { item ->
                uiState.update {
                    it.copy(
                        patterns = item
                    )
                }
            }
        } catch (ex: Exception) {

        }
    }

    fun updateIsUsedStickerById(eventId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            stickerRepo.updateIsUsedById(
                eventId,
                true
            )
        }
    }

    fun updateIsUsedPatternById(eventId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            patternRepo.updateIsUsedPatternById(
                eventId,
                true
            )
        }
    }
}

data class StoreUIState(
    val stickers: List<StickerModel> = emptyList(),
    val selectedTabSticker: StickerModel? = null,

    val patterns: List<PatternModel> = emptyList(),
    val selectedTabPattern: PatternModel? = null,
)

