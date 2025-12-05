package com.avnsoft.photoeditor.photocollage.ui.activities.editor.ai_enhance

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.avnsoft.photoeditor.photocollage.data.model.remove_background.AIDetectResponse
import com.avnsoft.photoeditor.photocollage.data.repository.AIEnhanceRepoImpl
import com.avnsoft.photoeditor.photocollage.data.repository.UPLOAD_TYPE_STATUS
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_background.ensureUploadConstraints
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object.downloadAndSaveToFile
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object.saveFileAndReturnPathFile
import com.avnsoft.photoeditor.photocollage.utils.FileUtil
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.io.File

@KoinViewModel
class AIEnhanceViewModel(
    private val context: Context,
    private val aiEnhanceRepoImpl: AIEnhanceRepoImpl
) : BaseViewModel() {

    val uiState = MutableStateFlow(AIEnhanceUIState())

    fun initData(pathBitmap: String?) {
        if (pathBitmap == null) return
        requestAIEnhance(pathBitmap)
    }

    fun requestAIEnhance(pathBitmap: String) {
        showLoading()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jpegFile = File(pathBitmap)
                val response = aiEnhanceRepoImpl.requestAIEnhance(jpegFile)
                uploadFileToS3(
                    data = response,
                    file = jpegFile
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
                hideLoading()
            }
        }
    }

    private suspend fun uploadFileToS3(data: AIDetectResponse, file: File) {
        val fileForUpload = ensureUploadConstraints(file)
        try {
            aiEnhanceRepoImpl.uploadFileToS3(
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
        val response = aiEnhanceRepoImpl.getImageStatus(
            id = id,
            status = status
        )
        val pathImage = saveFileAndReturnPathFile(
            context = context,
            url = response.result.origin
        )
        uiState.update {
            it.copy(
                imageUrl = pathImage,
            )
        }
    }

    fun hideImageOriginalAfterLoaded() {
        uiState.update {
            it.copy(
                showOriginal = false
            )
        }
        hideLoading()
    }

    fun updateIsOriginal(isOriginal: Boolean) {
        uiState.update {
            it.copy(
                showOriginal = isOriginal
            )
        }
    }

    fun onItemClick(item: AIEnhanceResult) {
        uiState.update {
            it.copy(
                imageUrl = item.imageUrl,
            )
        }
    }

    override fun showLoading() {
        uiState.update {
            it.copy(
                isShowLoading = true
            )
        }
    }

    override fun hideLoading() {
        uiState.update {
            it.copy(
                isShowLoading = false
            )
        }
    }

    fun showDiscardDialog() {
        uiState.update {
            it.copy(
                showDiscardDialog = true
            )
        }
    }

    fun hideDiscardDialog() {
        uiState.update {
            it.copy(
                showDiscardDialog = false
            )
        }
    }
}

data class AIEnhanceUIState(
    val imageUrl: String = "",
    val isShowLoading: Boolean = true,
    val showOriginal: Boolean = true,
    val showDiscardDialog: Boolean = false
)

data class AIEnhanceResult(
    val name: String,
    val imageUrl: String
)