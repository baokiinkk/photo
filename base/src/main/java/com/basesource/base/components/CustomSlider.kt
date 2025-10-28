package com.basesource.base.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CustomSlider(
    modifier: Modifier = Modifier,
    title: String? = null,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f,
    valueSuffix: String = "dp",
    enabled: Boolean = true,
    isShowPrefix: Boolean = false,
    isShowSuffix: Boolean = true,
    valueSuffixStyle: TextStyle = LocalTextStyle.current,
    titleStyle: TextStyle = LocalTextStyle.current,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (title?.isNotBlank() == true) {
            Text(
                text = title,
                style = titleStyle
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isShowPrefix) {
                Text(
                    modifier = Modifier.padding(end = 8.dp),
                    text = "${valueRange.start}$valueSuffix",
                    style = valueSuffixStyle
                )
            }

            Slider(
                modifier = Modifier.weight(1f),
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF8F82FF),
                    activeTrackColor = Color(0xFF8F82FF),
                    inactiveTrackColor = Color(0xFFF4F3FF)
                )
            )
            if (isShowSuffix) {
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = "${value.toInt()}$valueSuffix",
                    style = valueSuffixStyle
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CustomSliderPreview() {
    var value by remember { mutableFloatStateOf(40f) }
    CustomSlider(
        value = value,
        onValueChange = {

        }
    )
}
