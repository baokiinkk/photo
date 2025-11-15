package com.avnsoft.photoeditor.photocollage.ui.activities.editor.draw

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import com.avnsoft.photoeditor.photocollage.R
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
            drawInput = DrawInput.DrawColor(
                color = Color.White,
                mode = 1,
                size = 20f
            )
        ),
        DrawTabData(
            stringResId = R.string.pattern,
            icon = R.drawable.ic_pattern,
            drawInput = DrawInput.DrawPattern(
                color = Color.White,
                mode = 1,
                size = 20f
            )
        )
    )
    val uiState = MutableStateFlow(
        DrawUIState(
            currentTab = tabs[0],
            tabs = tabs
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
        uiState.update {
            it.copy(
                currentTab = tab
            )
        }
    }

    fun onSizeColorChange(size: Float) {
        val drawInput = uiState.value.currentTab.drawInput
        when (drawInput) {
            is DrawInput.DrawColor -> {
                uiState.update {
                    it.copy(
                        currentTab = it.currentTab.copy(
                            drawInput = drawInput.copy(size = size)
                        )
                    )
                }
            }

            is DrawInput.DrawPattern -> {

            }
        }
    }

    fun onSelectedColor(color: Color) {
        val drawInput = uiState.value.currentTab.drawInput
        when (drawInput) {
            is DrawInput.DrawColor -> {
                uiState.update {
                    it.copy(
                        currentTab = it.currentTab.copy(
                            drawInput = drawInput.copy(color = color)
                        )
                    )
                }
            }

            is DrawInput.DrawPattern -> {

            }
        }
    }

}

data class DrawUIState(
    val originBitmap: Bitmap? = null,
    val isLoading: Boolean = false,
    val currentTab: DrawTabData,
    val tabs: List<DrawTabData> = emptyList(),
)

data class DrawTabData(
    val stringResId: Int,
    val icon: Int,
    val drawInput: DrawInput
) {
    enum class TAB {
        Solid,
        Pattern,
    }
}