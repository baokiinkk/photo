package com.avnsoft.photoeditor.photocollage.ui.activities.store.editor

import android.graphics.Bitmap
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.CollageTool
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.ToolItem
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class EditorStoreViewModel : BaseViewModel() {

    val items = listOf(
        ToolItem(
            tool = CollageTool.TEMPLATE,
            label = R.string.template,
            icon = R.drawable.ic_tstore_editor_emplate,
        ),
        ToolItem(
            CollageTool.TEXT,
            R.string.text_tool,
            R.drawable.ic_text_tool
        ),
        ToolItem(CollageTool.FILTER, R.string.filter, R.drawable.ic_filter),
        ToolItem(CollageTool.STICKER, R.string.sticker_tool, R.drawable.ic_sticker_tool),
        ToolItem(CollageTool.REPLACE, R.string.replace, R.drawable.ic_photo_tool)
    )

    val uiState = MutableStateFlow(EditorStoreUIState(items))

    fun initData(bitmap: Bitmap?) {
        uiState.update {
            it.copy(
                bitmap = bitmap
            )
        }
    }

    fun onToolClick(tool: CollageTool) {
        uiState.update {
            it.copy(
                tool = tool,
                isShowTextSticker = tool == CollageTool.TEXT,
                isShowSticker = tool == CollageTool.STICKER,
                isShowFilter = tool == CollageTool.FILTER
            )
        }
    }

    fun hideTextSticker() {
        uiState.update {
            it.copy(
                isShowTextSticker = false
            )
        }
    }

    fun hideSticker() {
        uiState.update {
            it.copy(
                isShowSticker = false
            )
        }
    }

    fun hideFilter() {
        uiState.update {
            it.copy(
                isShowFilter = false,
                tool = CollageTool.STICKER
            )
        }
    }
}

data class EditorStoreUIState(
    val items: List<ToolItem>,
    val tool: CollageTool = CollageTool.NONE,
    val isShowTextSticker: Boolean = false,
    val isShowSticker: Boolean = false,
    val bitmap: Bitmap? = null,
    val isShowFilter: Boolean = false
)