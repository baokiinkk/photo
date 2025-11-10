package com.amb.photo.ui.activities.editor.filter

import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class FilterViewModel : BaseViewModel() {

    val uiState = MutableStateFlow(FilterUIState())

    fun getConfigFilter(bitmap: Bitmap?) {
        uiState.update {
            it.copy(originBitmap = bitmap)
        }
        viewModelScope.launch(Dispatchers.IO) {
            val filters = FilterUtils.initDataFilter(bitmap)
            uiState.update {
                it.copy(filters = filters)
            }
        }
    }

    fun onItemClick(item: FilterBean) {
        uiState.update {
            it.copy(filterId = item.name.orEmpty())
        }
    }

}

data class FilterUIState(
    val filters: List<FilterBean> = emptyList(),
    val filterId: String = "Fresh 01",
    val originBitmap: Bitmap? = null,
)