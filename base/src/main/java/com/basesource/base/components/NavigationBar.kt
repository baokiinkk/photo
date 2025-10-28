package com.basesource.base.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
    onClickBack: (() -> Unit)? = {},
    height: Dp = 44.dp,
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .height(height)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when {
                onClickBack != null -> {
                    IconButton(
                        onClick = { onClickBack() },
                        modifier = Modifier
                            .size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back_24),
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
                    text = it,
                    textAlign = textAlign,
                    maxLines = 1,
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