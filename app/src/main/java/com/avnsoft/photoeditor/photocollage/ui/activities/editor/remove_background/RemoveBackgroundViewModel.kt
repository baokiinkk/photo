package com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_background

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.viewModelScope
import com.avnsoft.photoeditor.photocollage.BaseApplication
import com.avnsoft.photoeditor.photocollage.data.model.remove_background.AIDetectResponse
import com.avnsoft.photoeditor.photocollage.data.repository.RemoveBackgroundRepoImpl
import com.avnsoft.photoeditor.photocollage.data.repository.UPLOAD_TYPE_STATUS
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object.saveFileAndReturnPathFile
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

@KoinViewModel
class RemoveBackgroundViewModel(
    private val context: Context,
    private val removeBackgroundRepo: RemoveBackgroundRepoImpl
) : BaseViewModel() {


    val uiState = MutableStateFlow(RemoveBackgroundUIState())

    val _removeBgState = Channel<String>()
    val removeBgState = _removeBgState.receiveAsFlow()

    fun initData(pathBitmap: String?) {
        if (pathBitmap == null) return
        uiState.update {
            it.copy(imageUrl = pathBitmap)
        }
        requestRemoveBg(pathBitmap)
    }

    fun requestRemoveBg(pathBitmap: String) {
        showLoading()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jpegFile = ensureUploadConstraints(File(pathBitmap))
                val response = removeBackgroundRepo.requestRemoveBg(jpegFile)
                uploadFileToS3(
                    data = response,
                    file = jpegFile
                )
            } catch (ex: Exception) {
                hideLoading()
            }
        }
    }

    private suspend fun uploadFileToS3(data: AIDetectResponse, file: File) {
        try {
            removeBackgroundRepo.uploadFileToS3(
                uploadUrl = data.links.first(),
                file = file
            )
            getImageStatus(
                id = data.id,
                status = UPLOAD_TYPE_STATUS.SUCCESS
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            getImageStatus(
                id = data.id,
                status = UPLOAD_TYPE_STATUS.FAILED
            )
            hideLoading()
        }
    }


    private suspend fun getImageStatus(
        id: String,
        status: UPLOAD_TYPE_STATUS
    ) {
        val response = removeBackgroundRepo.getImageStatus(
            id = id,
            status = status
        )
        val pathFile = saveFileAndReturnPathFile(context, response.result.url)
        hideLoading()
        _removeBgState.send(pathFile)
        uiState.update {
            it.copy(
                imageUrl = pathFile
            )
        }
    }

    fun showLoading() {
        uiState.update {
            it.copy(
                isShowLoading = true
            )
        }
    }

    fun hideLoading() {
        uiState.update {
            it.copy(
                isShowLoading = false
            )
        }
    }

    fun showDiscardDialog() {
        uiState.update {
            it.copy(showDiscardDialog = true)
        }
    }

    fun hideDiscardDialog() {
        uiState.update {
            it.copy(showDiscardDialog = false)
        }
    }
}

fun ensureUploadConstraints(sourceFile: File, maxUploadSize: Int = 1504): File {

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(sourceFile.absolutePath, bounds)

    val maxDimension = max(bounds.outWidth, bounds.outHeight)
    if (maxDimension <= maxUploadSize || bounds.outWidth <= 0 || bounds.outHeight <= 0) {
        return sourceFile
    }

    val scale = maxUploadSize.toFloat() / maxDimension
    val targetWidth = max(1, (bounds.outWidth * scale).roundToInt())
    val targetHeight = max(1, (bounds.outHeight * scale).roundToInt())

    val bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath) ?: return sourceFile
    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)

    val tempFile = File(
        BaseApplication.getInstanceApp().cacheDir,
        "upload_resized_${System.currentTimeMillis()}.jpg"
    )
    FileOutputStream(tempFile).use {
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
    }

    if (resizedBitmap != bitmap) {
        resizedBitmap.recycle()
    }
    bitmap.recycle()

    return tempFile
}

data class RemoveBackgroundUIState(
    val imageUrl: String = "",
    val isShowLoading: Boolean = true,
    val showDiscardDialog: Boolean = false
)