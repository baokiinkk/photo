package com.basesource.base.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.basesource.base.R
import kotlinx.coroutines.delay

@Composable
fun SuccessToast(
    modifier:Modifier = Modifier,
    isVisible: Boolean,
    message: String = "Congratulations success!",
    onDismiss: () -> Unit,
    duration: Long = 3000L,
    style: TextStyle = LocalTextStyle.current
) {
    AnimatedVisibility(
        visible = isVisible
    ) {
        Row(
            modifier = modifier
                .wrapContentSize()
                .background(
                    color = Color(0x99000000),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Success Icon
            Image(
                painterResource(R.drawable.ic_check),
                contentDescription = "Success",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(16.dp)
            )

            // Message Text
            Text(
                text = message,
                style = style,
            )
        }
    }

    // Auto dismiss after duration
    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(duration)
            onDismiss()
        }
    }
}
