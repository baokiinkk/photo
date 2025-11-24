package com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_background

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.avnsoft.photoeditor.photocollage.data.repository.RemoveBackgroundRepoImpl
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
                val jpegFile = File(pathBitmap)
                val response = removeBackgroundRepo.requestRemoveBg(jpegFile)
                uploadFileToS3(
                    url = response.links.first(),
                    file = jpegFile
                )
            } catch (ex: Exception) {
                hideLoading()
            }
        }
    }

    private suspend fun uploadFileToS3(url: String, file: File) {
        try {
            val response = removeBackgroundRepo.uploadFileToS3(uploadUrl = url, file = file)
            val pathFile = saveFileAndReturnPathFile(response)
            hideLoading()
            _removeBgState.send(pathFile)
            uiState.update {
                it.copy(
                    imageUrl = pathFile
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            hideLoading()
        }
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