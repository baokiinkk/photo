package com.avnsoft.photoeditor.photocollage.ui.activities.store.tab.background.detail

import com.avnsoft.photoeditor.photocollage.data.model.pattern.PatternModel
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class StoreBackgroundDetailViewModel : BaseViewModel() {

    val uiState = MutableStateFlow(StoreBackgroundDetailUIState())

    fun initData(item: PatternModel?) {
        uiState.update {
            it.copy(
                item = item
            )
        }
    }

}

data class StoreBackgroundDetailUIState(
    val item: PatternModel? = null
)