package com.basesource.base.ui.animation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.basesource.base.utils.clickableWithAlphaEffect


@Composable
fun LoadAnimation(
    modifier: Modifier = Modifier,
    isShowDialog: Boolean = false,
    json: Int,
    onClick: (() -> Unit)? = null,
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(json)
    )
    if (isShowDialog){
        LottieAnimation(
            modifier = modifier
                .clickableWithAlphaEffect(onClick = onClick),
            composition = composition,
            iterations = LottieConstants.IterateForever
        )
    }
}