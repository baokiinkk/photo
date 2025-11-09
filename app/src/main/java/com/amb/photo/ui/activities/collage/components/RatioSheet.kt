package com.amb.photo.ui.activities.collage.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amb.photo.R
import com.amb.photo.ui.activities.editor.crop.CropAspect
import com.amb.photo.ui.theme.AppStyle
import com.amb.photo.ui.theme.AppColor
import com.basesource.base.utils.clickableWithAlphaEffect

// Danh sách các ratio options cho collage (chỉ cần một số ratio cơ bản)
val collageRatioOptions = listOf(
    CropAspect.ORIGINAL,
    CropAspect.RATIO_1_1,
    CropAspect.RATIO_4_5,
    CropAspect.RATIO_5_4,
    CropAspect.RATIO_3_4
)

@Composable
fun RatioSheet(
    selectedRatio: String? = null,
    onRatioSelect: (CropAspect) -> Unit,
    onClose: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(vertical = 16.dp)
    ) {
        // Ratio Options - Horizontal scrollable list
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
        ) {
            items(collageRatioOptions) { aspect ->
                RatioOptionItem(
                    aspect = aspect,
                    isSelected = selectedRatio == aspect.label,
                    onClick = { onRatioSelect(aspect) }
                )
            }
        }

        // Bottom Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.Black
                )
            }

            Text(
                text = stringResource(R.string.ratio_tool),
                style = AppStyle.title2().medium().gray900()
            )

            IconButton(onClick = onConfirm) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Confirm",
                    tint = Color(0xFF9747FF)
                )
            }
        }
    }
}

@Composable
private fun RatioOptionItem(
    aspect: CropAspect,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickableWithAlphaEffect(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = if (isSelected) Color(0xFF9747FF) else Color(0xFFF3F4F6),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(aspect.iconAspect.resId),
                contentDescription = aspect.label,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(32.dp),
                colorFilter = if (isSelected) {
                    ColorFilter.tint(Color.White)
                } else {
                    ColorFilter.tint(Color(0xFF6B7280))
                }
            )
        }
        
        Text(
            text = aspect.label,
            modifier = Modifier.padding(top = 8.dp),
            style = if (isSelected) {
                AppStyle.caption2().medium().primary500()
            } else {
                AppStyle.caption2().medium().gray600()
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RatioSheetPreview() {
    RatioSheet(
        selectedRatio = "1:1",
        onRatioSelect = {},
        onClose = {},
        onConfirm = {}
    )
}

