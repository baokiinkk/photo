package com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_background

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.avnsoft.photoeditor.photocollage.data.model.remove_background.RemoveBackgroundResponse
import com.avnsoft.photoeditor.photocollage.data.repository.RemoveBackgroundRepoImpl
import com.avnsoft.photoeditor.photocollage.data.repository.UPLOAD_TYPE_STATUS
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object.downloadAndSaveToFile
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import kotlin.math.max
import kotlin.math.roundToInt

@KoinViewModel
class RemoveBackgroundViewModel(
    private val context: Context,
    private val removeBackgroundRepo: RemoveBackgroundRepoImpl
) : BaseViewModel() {
    companion object {
        private const val MAX_UPLOAD_DIMENSION = 1504
    }

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
                val jpegFile = File(pathBitmap)
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

    private suspend fun uploadFileToS3(data: RemoveBackgroundResponse, file: File) {
        val fileForUpload = ensureUploadConstraints(file)
        try {
            removeBackgroundRepo.uploadFileToS3(
                uploadUrl = data.links.first(),
                file = fileForUpload
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
        } finally {
            if (fileForUpload != file && fileForUpload.exists()) {
                fileForUpload.delete()
            }
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
        val pathFile = saveFileAndReturnPathFile(response.result.url)
        hideLoading()
        _removeBgState.send(pathFile)
        uiState.update {
            it.copy(
                imageUrl = pathFile
            )
        }
    }

    private fun ensureUploadConstraints(sourceFile: File): File {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(sourceFile.absolutePath, bounds)

        val maxDimension = max(bounds.outWidth, bounds.outHeight)
        if (maxDimension <= MAX_UPLOAD_DIMENSION || bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return sourceFile
        }

        val scale = MAX_UPLOAD_DIMENSION.toFloat() / maxDimension
        val targetWidth = max(1, (bounds.outWidth * scale).roundToInt())
        val targetHeight = max(1, (bounds.outHeight * scale).roundToInt())

        val bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath) ?: return sourceFile
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)

        val tempFile = File(context.cacheDir, "upload_resized_${System.currentTimeMillis()}.jpg")
        FileOutputStream(tempFile).use {
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
        }

        if (resizedBitmap != bitmap) {
            resizedBitmap.recycle()
        }
        bitmap.recycle()

        return tempFile
    }

    suspend fun saveFileAndReturnPathFile(url: String): String {
        val folderTemp = context.cacheDir.absolutePath + "/ImageRemoveObjTemp"
        val folder = File(folderTemp)
        folder.deleteRecursively()
        if (!folder.exists()) {
            folder.mkdirs()
        }
        val pathSave = folderTemp + "/${System.currentTimeMillis()}.jpeg"
        url.downloadAndSaveToFile(pathSave)
        return pathSave
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
}


data class RemoveBackgroundUIState(
    val imageUrl: String = "",
    val isShowLoading: Boolean = true
)