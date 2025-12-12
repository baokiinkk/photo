package com.basesource.base.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.gsm.user.base.sharedui.utils.shimmerBrush

@Composable
fun Shimmer(
    width: Dp,
    height: Dp,
    radius: Dp = 8.dp,
    modifier: Modifier = Modifier,
) {
    Spacer(
        modifier = modifier
            .then(
                Modifier
                    .width(width)
                    .height(height)
                    .background(shimmerBrush(), shape = RoundedCornerShape(radius))
            )
    )
}

@Composable
fun Shimmer(
    radius: Dp = 8.dp,
    modifier: Modifier = Modifier,
) {
    Spacer(
        modifier = modifier
            .background(shimmerBrush(), shape = RoundedCornerShape(radius))
    )
}
