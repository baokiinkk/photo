package com.basesource.base.utils

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.Dp
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
