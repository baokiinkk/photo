package com.basesource.base.utils

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt

fun Modifier.clickableWithAlphaEffect(
    pressedAlpha: Float = 1f, // Default alpha value
    enabled: Boolean = true,
    debounceTime: Long = 300L,
    onClick: (() -> Unit)?,
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    var lastClickTime by remember { mutableLongStateOf(0L) }

    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> isPressed = true
                is PressInteraction.Release -> isPressed = false
                is PressInteraction.Cancel -> isPressed = false
            }
        }
    }

    val alpha by animateFloatAsState(if (isPressed) pressedAlpha else 1f, label = "")
    if (onClick != null) {
        this
            .alpha(alpha)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastClickTime >= debounceTime) {
                        lastClickTime = currentTime
                        onClick()
                    }
                }
            )
    } else {
        this.alpha(alpha)
    }
}

fun Modifier.clickWithAlphaEffect(
    pressedAlpha: Float = 0.3f, // Default alpha value
    enabled: Boolean = true,
    debounceTime: Long = 300L,
    onClick: (() -> Unit)?,
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    var lastClickTime by remember { mutableLongStateOf(0L) }

    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> isPressed = true
                is PressInteraction.Release -> isPressed = false
                is PressInteraction.Cancel -> isPressed = false
            }
        }
    }

    val alpha by animateFloatAsState(if (isPressed) pressedAlpha else 1f, label = "")
    if (onClick != null) {
        this
            .alpha(alpha)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastClickTime >= debounceTime) {
                        lastClickTime = currentTime
                        onClick()
                    }
                }
            )
    } else {
        this.alpha(alpha)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.setTagAndId(tag: String): Modifier {
    return this
        .semantics { this.testTagsAsResourceId = true }
        .testTag(tag)
}

@Composable
fun pixelToDpConverter(pixelValue: Int): Dp {
    val density = LocalDensity.current
    return with(density) {
        pixelValue.toDp()
    }
}


enum class Effects(val offsetX: Dp, val offsetY: Dp, val blur: Dp) {
    DROP_SHADOW(2.dp, 2.dp, 16.dp)
}

fun Modifier.figmaShadow(
    color: Color,
    alpha: Float = 0.4f,
    cornerRadius: Dp = 0.dp,
    x: Dp = 2.dp,
    y: Dp = 2.dp,
    blur: Dp = 16.dp,
): Modifier = this.then(
    Modifier.drawBehind {

        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            this.color = android.graphics.Color.TRANSPARENT
            setShadowLayer(
                blur.toPx(),
                x.toPx(),
                y.toPx(),
                color.copy(alpha = alpha).toArgb()
            )
        }

        val rect = android.graphics.RectF(
            0f,
            0f,
            size.width,
            size.height
        )

        drawContext.canvas.nativeCanvas.drawRoundRect(
            rect,
            cornerRadius.toPx(),
            cornerRadius.toPx(),
            paint
        )
    }
)

fun Modifier.backgroundLinearGradient(
    colors: List<Color>
): Modifier {
    return this.then(
        Modifier
            .background(
                brush = Brush.linearGradient(
                    colors = colors
                )
            )
    )
}

@Composable
fun rememberCaptureController() = rememberGraphicsLayer()
fun Modifier.capturable(graphicsLayer: GraphicsLayer): Modifier {
    return this.then(
        Modifier
            .drawWithContent {
                // call record to capture the content in the graphics layer
                graphicsLayer.record {
                    // draw the contents of the composable into the graphics layer
                    this@drawWithContent.drawContent()
                }
                // draw the graphics layer on the visible canvas
                drawLayer(graphicsLayer)
            }
    )
}