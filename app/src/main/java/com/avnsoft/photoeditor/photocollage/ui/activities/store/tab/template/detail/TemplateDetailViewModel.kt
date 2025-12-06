package com.avnsoft.photoeditor.photocollage.ui.activities.store.tab.template.detail

import android.net.Uri
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateModel
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class TemplateDetailViewModel : BaseViewModel() {

    val uiState = MutableStateFlow(TemplateDetailUIState())

    fun initData(template: TemplateModel?) {
        uiState.update {
            it.copy(
                template = template,
            )
        }
    }

    fun selectImage(index: Int, uri: Uri) {
        uiState.update { state ->
            val newSelectedImages = state.selectedImages.toMutableMap()
            newSelectedImages[index] = uri
            state.copy(selectedImages = newSelectedImages)
        }
    }

    fun unselectImage(index: Int) {
        uiState.update { state ->
            val newSelectedImages = state.selectedImages.toMutableMap()
            newSelectedImages.remove(index)
            state.copy(selectedImages = newSelectedImages)
        }
    }
}

data class TemplateDetailUIState(
    val template: TemplateModel? = null,
    val selectedImages: Map<Int, Uri> = emptyMap(),
)

