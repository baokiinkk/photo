package com.avnsoft.photoeditor.photocollage.ui.activities.freestyle

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.AddTextProperties
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.FreeStyleSticker
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.Photo
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class FreeStyleViewModel(
    private val context: Context
) : BaseViewModel() {

    val freeStyleSticker = MutableLiveData<MutableList<FreeStyleSticker>>()

    val uiState = MutableStateFlow(FreeStyleUIState())

    fun initData(uriList: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = uriList.mapIndexed { index, uri ->
                val drawable = decodeUriToDrawable(context, uri, 400, 400)
                FreeStyleSticker(index, Photo(uri, 0), drawable)
            }.toMutableList()
            freeStyleSticker.postValue(data)
        }
    }

    fun showStickerTool() {
        uiState.update {
            it.copy(
                isShowStickerTool = true
            )
        }
    }

    fun cancelSticker() {
        uiState.update {
            it.copy(
                isShowStickerTool = false
            )
        }
    }

    fun applySticker() {
        uiState.update {
            it.copy(
                isShowStickerTool = false
            )
        }
    }

    fun showTextSticker() {
        uiState.update {
            it.copy(
                isShowTextStickerTool = true
            )
        }
    }

    fun cancelTextSticker() {
        uiState.update {
            it.copy(
                isShowTextStickerTool = false,
                isVisibleTextField = false
            )
        }
    }

    fun applyTextSticker() {
        uiState.update {
            it.copy(
                isShowTextStickerTool = false,
                isVisibleTextField = false
            )
        }
    }

    fun showEditTextSticker(editTextProperties: AddTextProperties?) {
        uiState.update {
            it.copy(
                isVisibleTextField = true,
                editTextProperties = editTextProperties ?: AddTextProperties.defaultProperties
            )
        }
    }

    fun hideEditTextSticker() {
        uiState.update {
            it.copy(
                isVisibleTextField = false,
            )
        }
    }
}

data class FreeStyleUIState(
    val isShowStickerTool: Boolean = false,
    val isShowTextStickerTool: Boolean = false,
    val isVisibleTextField: Boolean = false,
    val editTextProperties: AddTextProperties = AddTextProperties.defaultProperties
)

fun decodeUriToDrawable(context: Context, uri: Uri?, w: Int, h: Int): BitmapDrawable? {
    try {
        var mBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri)
        if (mBitmap == null) mBitmap =
            BitmapFactory.decodeResource(context.getResources(), R.drawable.thumbs)
        val bm: Bitmap = getBitmapResize(mBitmap, w, h)
        return BitmapDrawable(context.getResources(), bm)
    } catch (ex: Exception) {
        ex.printStackTrace()
        System.gc()
    } catch (ex: OutOfMemoryError) {
        ex.printStackTrace()
        System.gc()
    }
    return null
}

fun getBitmapResize(bitmap: Bitmap, w: Int, h: Int): Bitmap {
    var maxWidth = 1080
    var maxHeight = 1920

    if (w != -1 && h != -1) {
        maxWidth = w
        maxHeight = h
    }

    val width = bitmap.getWidth()
    val height = bitmap.getHeight()
    if (width >= height) {
        val i3 = (height * maxWidth) / width
        if (i3 > maxHeight) {
            maxWidth = (maxWidth * maxHeight) / i3
        } else {
            maxHeight = i3
        }
    } else {
        val i4 = (width * maxHeight) / height
        if (i4 > maxWidth) {
            maxHeight = (maxHeight * maxWidth) / i4
        } else {
            maxWidth = i4
        }
    }
    return Bitmap.createScaledBitmap(bitmap, maxWidth, maxHeight, true)
}