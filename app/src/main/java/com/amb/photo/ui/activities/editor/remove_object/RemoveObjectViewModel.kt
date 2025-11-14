package com.amb.photo.ui.activities.editor.remove_object

import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import com.amb.photo.R
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.io.FileOutputStream

@KoinViewModel
class RemoveObjectViewModel : BaseViewModel() {

    val uiState = MutableStateFlow(RemoveObjectUIState())
    val composeUIState = MutableStateFlow(RemoveObjectComposeUIState())

    val tabs = listOf(
        RemoveObjectTab(
            tab = RemoveObjectTab.TAB.AUTO,
            stringResId = R.string.auto,
            icon = R.drawable.ic_remove_object_ai
        ),
        RemoveObjectTab(
            tab = RemoveObjectTab.TAB.BRUSH,
            stringResId = R.string.brush,
            icon = R.drawable.ic_remove_object_brush
        ),
        RemoveObjectTab(
            tab = RemoveObjectTab.TAB.LASSO,
            stringResId = R.string.lasso,
            icon = R.drawable.ic_remove_object_lasso
        )
    )

    init {
        initData()
    }

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

    fun initData() {
        composeUIState.update {
            it.copy(
                tabs = tabs
            )
        }
    }

    fun updateBlurBrush(blur: Float) {
        composeUIState.update {
            it.copy(blurBrush = blur)
        }
    }

    fun updateTabIndex(tab: RemoveObjectTab.TAB) {
        composeUIState.update {
            it.copy(tab = tab)
        }
    }
}

data class RemoveObjectUIState(
    val bitmap: Bitmap? = null,
)

data class RemoveObjectComposeUIState(
    val blurBrush: Float = 50f,
    val tabs: List<RemoveObjectTab> = emptyList(),
    val tab: RemoveObjectTab.TAB = RemoveObjectTab.TAB.AUTO
)

data class RemoveObjectTab(
    val tab: TAB,
    val stringResId: Int,
    val icon: Int
) {
    enum class TAB {
        AUTO,
        BRUSH,
        LASSO
    }
}