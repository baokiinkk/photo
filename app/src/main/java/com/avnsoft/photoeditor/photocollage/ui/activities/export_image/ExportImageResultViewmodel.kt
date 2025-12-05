package com.avnsoft.photoeditor.photocollage.ui.activities.export_image

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.avnsoft.photoeditor.photocollage.data.repository.GetImageInfoRepoImpl
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.toBitmap
import com.avnsoft.photoeditor.photocollage.utils.FileUtil
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ExportImageResultViewmodel(
    private val context: Context,
    private val getImageInfoRepoImpl: GetImageInfoRepoImpl
) : BaseViewModel() {

    val uiState = MutableStateFlow(ExportImageResultUIState())
    var isSaved: Boolean = false
    var imageId: Int = -1
    fun initData(
        exportImageData: ExportImageData
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            imageId = getImageInfoRepoImpl.insertImage(exportImageData.pathUriMark.toString())
            uiState.update {
                it.copy(
                    imageUrl = exportImageData.pathUriMark,
                    pathBitmapOriginal = exportImageData.pathBitmapOriginal
                )
            }
        }
    }

    fun removeWatermarkClick(quality: Quality) {
        if (isSaved) return
        viewModelScope.launch(Dispatchers.IO) {
            uiState.update {
                it.copy(
                    imageUrl = uiState.value.pathBitmapOriginal,
                    isMark = false
                )
            }
            val uri = FileUtil.saveImageToStorageWithQuality(
                context = context,
                quality = quality,
                bitmap = uiState.value.pathBitmapOriginal.toBitmap(context) ?: return@launch
            )
            uri?.let {
                getImageInfoRepoImpl.updateImageById(id = imageId, it.toString())
            }
            isSaved = true
        }
    }

    fun isMark(): Boolean {
        return uiState.value.isMark
    }
}



data class ExportImageResultUIState(
    val imageUrl: String? = null,
    val pathBitmapOriginal: String? = null,
    val isMark: Boolean = true,
)