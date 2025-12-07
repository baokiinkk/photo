package com.avnsoft.photoeditor.photocollage.ui.activities.editor.filter

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class FilterViewModel(
    private val context: Context
) : BaseViewModel() {

    val uiState = MutableStateFlow(FilterUIState())


    fun getConfigFilter(screenInput: ToolInput?) {
        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = screenInput?.getBitmap(context)
            uiState.update {
                it.copy(
                    originBitmap = bitmap,
                    isLoading = true
                )
            }
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

    override fun showLoading() {
        uiState.update {
            it.copy(
                isLoading = true
            )
        }
    }

    override fun hideLoading() {
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