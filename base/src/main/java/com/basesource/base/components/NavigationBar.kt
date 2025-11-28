package com.basesource.base.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.basesource.base.R

@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    textAlign: TextAlign = TextAlign.Left,
    titleStyle: TextStyle = LocalTextStyle.current,
    height: Dp = 44.dp,
    onClickBack: (() -> Unit)? = {},
    ) {
    Column(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .height(height)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when {
                onClickBack != null -> {
                    IconButton(
                        onClick = { onClickBack() },
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.CenterStart)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_left),
                            modifier = Modifier.size(24.dp),
                            contentDescription = ""
                        )
                    }
                }

                textAlign == TextAlign.Left -> {
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }
            title?.let {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = it,
                    textAlign = textAlign,
                    maxLines = 1,
                    style = titleStyle
                )
            }
        }
    }
}

@Preview
@Composable
fun NavigationBarPreview() {
    NavigationBar(title = "Tên màn hình hiện tại", onClickBack = {})
}