package com.basesource.base.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    scale: Float = 0.75f
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier = modifier
            .scale(scale)
            .graphicsLayer(scaleX = 1.1f),
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = Color(0xFFA59BFF),
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = Color(0xFFB0B2B5),
            checkedBorderColor = Color.Transparent,
            uncheckedBorderColor = Color.Transparent,
            disabledUncheckedTrackColor = Color(0xFFB0B2B5),
            disabledUncheckedThumbColor = Color.White,
            disabledUncheckedBorderColor = Color(0xFFB0B2B5),
        ),
        thumbContent = {
            Box(
                modifier = Modifier
                    .background(
                        color = Color.White,
                        shape = CircleShape
                    )
            )
        }
    )
}

@Preview(showBackground = true, name = "CustomSwitch - Default")
@Composable
fun CustomSwitchPreview() {
    var checked1 by remember { mutableStateOf(true) }
    var checked2 by remember { mutableStateOf(false) }
    var checked3 by remember { mutableStateOf(true) }
    
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Switch ON")
            CustomSwitch(
                checked = checked1,
                onCheckedChange = { checked1 = it }
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Switch OFF")
            CustomSwitch(
                checked = checked2,
                onCheckedChange = { checked2 = it }
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Disabled")
            CustomSwitch(
                checked = checked3,
                onCheckedChange = { checked3 = it },
                enabled = false
            )
        }
    }
}

@Preview(showBackground = true, name = "CustomSwitch - Custom")
@Composable
fun CustomSwitchCustomPreview() {
    var checked1 by remember { mutableStateOf(true) }
    var checked2 by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Small Switch")
            CustomSwitch(
                checked = checked1,
                onCheckedChange = { checked1 = it },
                scale = 0.5f
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Large Switch")
            CustomSwitch(
                checked = checked2,
                onCheckedChange = { checked2 = it },
                scale = 1.0f
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Different Scale")
            CustomSwitch(
                checked = checked1,
                onCheckedChange = { checked1 = it },
                scale = 1.2f
            )
        }
    }
}
