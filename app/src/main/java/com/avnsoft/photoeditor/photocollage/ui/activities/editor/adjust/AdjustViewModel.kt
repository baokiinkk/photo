package com.avnsoft.photoeditor.photocollage.ui.activities.editor.adjust

import android.graphics.Bitmap
import androidx.compose.ui.unit.IntRect
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.CollageTool
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.ToolItem
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class AdjustViewModel : BaseViewModel() {

    val items = listOf(
        ToolItem(
            CollageTool.BRIGHTNESS, R.string.brightness,
            R.drawable.ic_brightness,
            index = 0
        ),
        ToolItem(CollageTool.CONTRAST, R.string.contrast, R.drawable.ic_contrast, index = 1),
        ToolItem(CollageTool.SATURATION, R.string.saturation, R.drawable.ic_saturation, index = 2),
        ToolItem(CollageTool.WARMTH, R.string.warmth, R.drawable.ic_warmth, index = 20),
//        ToolItem(CollageTool.FADE, R.string.fade, R.drawable.ic_fade, index = 7),
        ToolItem(CollageTool.HIGHLIGHT, R.string.highlight, R.drawable.ic_highlight, index = 21),
        ToolItem(CollageTool.SHADOW, R.string.shadow, R.drawable.ic_shadow, index = 9),
        ToolItem(CollageTool.HUE, R.string.hue, R.drawable.ic_hue, index = 4),
        ToolItem(CollageTool.VIGNETTE, R.string.vignette, R.drawable.ic_vignette, index = 6),
        ToolItem(CollageTool.SHARPEN, R.string.sharpen, R.drawable.ic_sharpen, index = 3),
//        ToolItem(CollageTool.GRAIN, R.string.grain, R.drawable.ic_grain, index = 10),
    )

    val uiState = MutableStateFlow(AdjustUIState(items = items))

    var captureRect: IntRect? = null

    fun initBitmap(bitmap: Bitmap?) {
        uiState.update {
            it.copy(
                bitmap = bitmap,
                originBitmap = bitmap
            )
        }
    }

    fun itemSelected(index: Int, tool: CollageTool) {
        uiState.update {
            it.copy(
                tool = tool,
                index = index
            )
        }
    }

    fun updateBrightness(brightness: Float) {
        uiState.update {
            it.copy(
                brightness = brightness
            )
        }
    }

    fun updateContrast(contrast: Float) {
        uiState.update {
            it.copy(
                contrast = contrast
            )
        }
    }

    fun updateSaturation(saturation: Float) {
        uiState.update {
            it.copy(
                saturation = saturation
            )
        }
    }

    fun updateWarmth(warmth: Float) {
        uiState.update {
            it.copy(
                warmth = warmth
            )
        }
    }

    fun updateFade(fade: Float) {
        uiState.update {
            it.copy(
                fade = fade
            )

        }
    }

    fun updateHue(hue: Float) {
        uiState.update {
            it.copy(
                hue = hue
            )
        }
    }

    fun updateShadow(shadow: Float) {
        uiState.update {
            it.copy(
                shadow = shadow
            )
        }
    }

    fun updateHighlight(highlight: Float) {
        uiState.update {
            it.copy(
                highlight = highlight
            )
        }
    }

    fun updateVignette(vignette: Float) {
        uiState.update {
            it.copy(
                vignette = vignette
            )
        }
    }

    fun updateSharpen(sharpen: Float) {
        uiState.update {
            it.copy(
                sharpen = sharpen
            )
        }
    }

    fun updateGrain(grain: Float) {
        uiState.update {
            it.copy(
                grain = grain
            )
        }
    }

    fun reset() {
        uiState.update {
            it.copy(
                brightness = 50f,
                contrast = 50f,
                saturation = 50f,
                warmth = 50f,
                fade = 50f,
                hue = 50f,
                shadow = 50f,
                highlight = 50f,
                vignette = 50f,
                sharpen = 50f,
                grain = 50f,
            )
        }
    }

}

data class AdjustUIState(
    val bitmap: Bitmap? = null,
    val originBitmap: Bitmap? = null,
    val items: List<ToolItem> = emptyList(),
    val tool: CollageTool = CollageTool.BRIGHTNESS,
    val index: Int = 0,
    val brightness: Float = 50f,
    val contrast: Float = 50f,
    val saturation: Float = 50f,
    val warmth: Float = 50f,
    val fade: Float = 50f,
    val hue: Float = 50f,
    val shadow: Float = 50f,
    val highlight: Float = 50f,
    val vignette: Float = 50f,
    val sharpen: Float = 50f,
    val grain: Float = 50f,
)