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
            it.copy(
                originBitmap = bitmap,
                isLoading = true
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val filters = FilterUtils.initDataFilter(bitmap)
            uiState.update {
                it.copy(
                    filters = filters,
                    isLoading = false
                )
            }
        }
    }

    fun onItemClick(item: FilterBean) {
        uiState.update {
            it.copy(
                filterId = item.name.orEmpty(),
                currentConfig = item.config
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

data class FilterUIState(
    val filters: List<FilterBean> = emptyList(),
    val filterId: String = "Original",
    val originBitmap: Bitmap? = null,
    val isLoading: Boolean = false,
    val currentConfig: String = ""
)