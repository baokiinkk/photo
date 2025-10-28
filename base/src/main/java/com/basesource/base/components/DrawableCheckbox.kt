package com.basesource.base.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.basesource.base.R
import com.basesource.base.utils.clickableWithAlphaEffect

@Composable
fun DrawableCheckbox(
    modifier: Modifier = Modifier,
    iconChecked: Int = R.drawable.checkbox_active,
    iconUnchecked: Int = R.drawable.checkbox_inactive,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val iconRes = if (checked) iconChecked else iconUnchecked
    Image(
        painter = painterResource(iconRes),
        contentDescription = null,
        modifier = modifier
            .size(24.dp)
            .clickableWithAlphaEffect(
                onClick = {
                    onCheckedChange.invoke(!checked)
                }
            )
    )
}