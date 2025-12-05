package com.avnsoft.photoeditor.photocollage.ui.activities.store.tab.template.detail

import android.net.Uri
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.avnsoft.photoeditor.photocollage.BaseApplication
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateContentModel
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateModel
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.FreeStyleSticker
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.Photo
import com.avnsoft.photoeditor.photocollage.utils.FileUtil.toFile
import com.avnsoft.photoeditor.photocollage.utils.FileUtil.urlToDrawable
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
        viewModelScope.launch(Dispatchers.IO) {
            val icons = template?.layer?.mapIndexed { index, model ->
                model.urlThumb =
                    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQPW_IipOBJnoB3E02a1yO5ylPoy2Jw-SoQ7w&s"
                model.toFreeStyleSticker(index)
            }
            uiState.update {
                it.copy(
                    icons = icons
                )
            }
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
    val icons: List<FreeStyleSticker>? = null
)

suspend fun TemplateContentModel.toFreeStyleSticker(
    index: Int,
): FreeStyleSticker {
    val model = this
    val drawable = model.urlThumb?.urlToDrawable(BaseApplication.getInstanceApp())
    val file = drawable?.toBitmap()?.toFile(BaseApplication.getInstanceApp())
    val uri = file?.toUri()
    return FreeStyleSticker(
        id = index,
        photo = Photo(uri, 0),
        drawable = drawable,
        x = model.x ?: 0f,
        y = model.y ?: 00f,
        widthRatio = model.width ?: 0f,
        heightRatio = model.height ?: 0f,
        rotate = model.rotate?.toFloat() ?: 0f
    )
}

