package net.gsm.user.base.sharedui.utils

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.colorResource
import com.basesource.base.R

@Composable
fun shimmerBrush(): Brush {
    val baseColor = colorResource(id = R.color.Neutral_Background_Element_c_bg_element_normal)
    val colors = listOf(baseColor.copy(alpha = 0.7f), baseColor.copy(alpha = 0.1f), baseColor.copy(alpha = 0.7f))
    val transition = rememberInfiniteTransition(label = "")
    val translateAnimation = transition.animateFloat(
        initialValue = 0F,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "",
    )
    return Brush.linearGradient(
        colors = colors,
        start = Offset.Zero,
        end = Offset(translateAnimation.value, translateAnimation.value)
    )
}
