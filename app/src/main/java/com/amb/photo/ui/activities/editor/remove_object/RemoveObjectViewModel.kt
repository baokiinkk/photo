package com.amb.photo.ui.activities.editor.remove_object

import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel
import java.io.FileOutputStream

@KoinViewModel
class RemoveObjectViewModel : BaseViewModel() {

    val uiState = MutableStateFlow(RemoveObjectUIState())

    val listPathImgRemoved = ArrayList<String>()

    fun setOriginalBitmap(
        bitmap: Bitmap?,
        newPathBitmap: String,
    ) {
        viewModelScope.launch {
            if (bitmap != null) {
                FileOutputStream(newPathBitmap, false).use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                }
                listPathImgRemoved.add(newPathBitmap)
            }
            uiState.update {
                it.copy(
                    bitmap = bitmap
                )
            }
        }
    }
}

data class RemoveObjectUIState(
    val bitmap: Bitmap? = null
)