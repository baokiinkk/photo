package com.avnsoft.photoeditor.photocollage.ui.activities.store.tab.background.detail

import androidx.lifecycle.viewModelScope
import com.avnsoft.photoeditor.photocollage.data.model.pattern.PatternModel
import com.avnsoft.photoeditor.photocollage.data.repository.PatternRepository
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class StoreBackgroundDetailViewModel(
    private val patternRepository: PatternRepository
) : BaseViewModel() {

    val uiState = MutableStateFlow(StoreBackgroundDetailUIState())

    fun initData(item: PatternModel?) {
        uiState.update {
            it.copy(
                item = item
            )
        }
    }

    fun updateIsUsedById(eventId: Long) {
        viewModelScope.launch(Dispatchers.IO){
            val response = patternRepository.updateIsUsedPatternById(
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

data class StoreBackgroundDetailUIState(
    val item: PatternModel? = null
)