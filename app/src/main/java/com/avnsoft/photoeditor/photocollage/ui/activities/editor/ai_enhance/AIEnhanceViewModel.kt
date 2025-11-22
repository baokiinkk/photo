package com.avnsoft.photoeditor.photocollage.ui.activities.editor.ai_enhance

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.avnsoft.photoeditor.photocollage.data.repository.AIEnhanceRepoImpl
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object.downloadAndSaveToFile
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
        uiState.update {
            it.copy(imageUrl = pathBitmap)
        }
        requestAIEnhance(pathBitmap)
    }

    fun requestAIEnhance(pathBitmap: String) {
        showLoading()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jpegFile = File(pathBitmap)
                val response = aiEnhanceRepoImpl.requestAIEnhance(jpegFile)
                getProgressRemoveBg(response.id)
            } catch (ex: Exception) {
                ex.printStackTrace()
                hideLoading()
            }
        }
    }

    private suspend fun getProgressRemoveBg(id: String) {
        var continueRequest = true
        while (continueRequest) {
            try {
                val response = aiEnhanceRepoImpl.getProgressRemoveBg(id)
                val pathFile = saveFileAndReturnPathFile(response.result.url)
                hideLoading()
                uiState.update {
                    it.copy(
                        imageUrl = pathFile
                    )
                }
                continueRequest = false
            } catch (ex: Exception) {

            }
            if (continueRequest) {
                delay(4000)
            }
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

data class AIEnhanceUIState(
    val imageUrl: String = "",
    val isShowLoading: Boolean = true
)