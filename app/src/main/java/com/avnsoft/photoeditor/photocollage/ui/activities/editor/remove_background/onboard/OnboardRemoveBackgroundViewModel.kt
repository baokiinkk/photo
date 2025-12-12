package com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_background.onboard

import android.content.Context
import android.graphics.Bitmap
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.utils.FileUtil.toScaledBitmapForUpload
import com.avnsoft.photoeditor.photocollage.utils.FileUtil.uriToBitmap
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class OnboardRemoveBackgroundViewModel(private val context: Context) : BaseViewModel() {

    val uiState = MutableStateFlow(OnboardRemoveBackgroundUIState())

    fun initData(
        screenInput: ToolInput?,
//        width: Int,
//        height: Int
    ) {

        showLoading()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bitmap = screenInput?.pathBitmap?.toUri()?.toScaledBitmapForUpload(context)
//                val original = (screenInput?.pathBitmap.uriToBitmap(context)) ?: return@launch
//                val bitmap = Bitmap.createScaledBitmap(original, width, height, true)
                uiState.update {
                    it.copy(
                        bitmap = bitmap
                    )
                }
                hideLoading()
            } catch (ex: Exception) {
                ex.printStackTrace()
                hideLoading()
            } finally {
                hideLoading()
            }
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

data class OnboardRemoveBackgroundUIState(
    val isLoading: Boolean = false,
    val bitmap: Bitmap? = null
)