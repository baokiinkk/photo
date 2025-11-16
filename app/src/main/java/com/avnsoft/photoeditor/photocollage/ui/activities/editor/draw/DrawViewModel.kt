package com.avnsoft.photoeditor.photocollage.ui.activities.editor.draw

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.draw.lib.DrawAsset
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.draw.lib.DrawBitmapModel
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class DrawViewModel : BaseViewModel() {

    private val tabs = listOf(
        DrawTabData(
            stringResId = R.string.solid,
            icon = R.drawable.ic_draw_solid,
            tab = DrawTabData.TAB.Solid
        ),//ic_draw_neon
        DrawTabData(
            stringResId = R.string.pattern,
            icon = R.drawable.ic_pattern,
            tab = DrawTabData.TAB.Pattern
        ),
        DrawTabData(
            stringResId = R.string.pattern,
            icon = R.drawable.ic_draw_neon,
            tab = DrawTabData.TAB.Neon
        )
    )
    val uiState = MutableStateFlow(
        DrawUIState(
            currentTab = tabs[0].tab,
            tabs = tabs,
            patterns = DrawAsset.lstDrawBitmapModel()
        )
    )

    fun getConfig(bitmap: Bitmap?) {
        uiState.update {
            it.copy(
                originBitmap = bitmap,
                isLoading = true,
                tabs = tabs
            )
        }
    }

    fun onTabSelected(tab: DrawTabData) {
        val drawInput = when (tab.tab) {
            DrawTabData.TAB.Solid -> {
                uiState.value.drawColor
            }

            DrawTabData.TAB.Pattern -> {
                uiState.value.drawPattern
            }

            DrawTabData.TAB.Neon -> {
                uiState.value.drawNeon
            }
        }
        uiState.update {
            it.copy(
                currentTab = tab.tab,
                drawInput = drawInput
            )
        }
    }

    fun onSizeChange(size: Float) {
        val currentTab = uiState.value.currentTab
        when (currentTab) {
            DrawTabData.TAB.Solid -> {
                uiState.update {
                    val drawColor = it.drawColor.copy(
                        size = size
                    )
                    it.copy(
                        drawColor = drawColor,
                        drawInput = drawColor
                    )
                }
            }

            DrawTabData.TAB.Pattern -> {
                uiState.update {
                    val drawPattern = it.drawPattern.copy(
                        size = size
                    )
                    it.copy(
                        drawPattern = drawPattern,
                        drawInput = drawPattern
                    )
                }
            }

            DrawTabData.TAB.Neon -> {
                uiState.update {
                    val drawNeon = it.drawNeon.copy(
                        size = size
                    )
                    it.copy(
                        drawNeon = drawNeon,
                        drawInput = drawNeon
                    )
                }
            }
        }
    }

    fun onSelectedColor(color: Color) {
        val currentTab = uiState.value.currentTab
        when (currentTab) {
            DrawTabData.TAB.Solid -> {
                uiState.update {
                    val drawColor = it.drawColor.copy(
                        color = color
                    )
                    it.copy(
                        drawColor = drawColor,
                        drawInput = drawColor
                    )
                }
            }

            DrawTabData.TAB.Neon -> {
                uiState.update {
                    val drawNeon = it.drawNeon.copy(
                        color = color
                    )
                    it.copy(
                        drawNeon = drawNeon,
                        drawInput = drawNeon
                    )
                }
            }

            else -> {

            }
        }
    }

    fun onPatternSelected(item: DrawBitmapModel) {
        uiState.update {
            val drawPattern = it.drawPattern.copy(
                drawBitmapModel = item
            )
            it.copy(
                patternSelected = item.mainIcon,
                drawPattern = drawPattern,
                drawInput = drawPattern
            )
        }
    }

    fun undo() {
        uiState.update {
            it.copy(
                drawInput = Undo
            )
        }
    }

    fun redo() {
        uiState.update {
            it.copy(
                drawInput = Redo
            )
        }
    }
}

data class DrawUIState(
    val originBitmap: Bitmap? = null,
    val isLoading: Boolean = false,
    val currentTab: DrawTabData.TAB,
    val tabs: List<DrawTabData> = emptyList(),
    val drawColor: DrawColor = DrawColor(),
    val drawPattern: DrawPattern = DrawPattern(),
    val drawNeon: DrawNeon = DrawNeon(),
    val drawInput: DrawInput = drawColor,
    val patterns: List<DrawBitmapModel> = emptyList(),
    val patternSelected: Int = DrawAsset.lstDrawBitmapModel().first().mainIcon
)

data class DrawTabData(
    val stringResId: Int,
    val icon: Int,
    val tab: TAB
) {
    enum class TAB {
        Solid,
        Pattern,
        Neon
    }
}