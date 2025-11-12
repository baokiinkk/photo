package com.amb.photo.ui.activities.editor.text_sticker

import android.content.Context
import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amb.photo.ui.activities.editor.text_sticker.lib.FontItem
import com.amb.photo.ui.theme.AppColor
import com.amb.photo.ui.theme.AppStyle

@Composable
fun rememberFontFromAssets(context: Context, fontPath: String): FontFamily {
    return remember(fontPath) {
        val typeface = Typeface.createFromAsset(context.assets, fontPath)
        FontFamily(typeface)
    }
}

@Composable
fun CustomFontText(
    modifier: Modifier,
    isSelected: Boolean = false,
    itemFont: FontItem
) {
    val context = LocalContext.current
    val customFont = rememberFontFromAssets(context, itemFont.fontPath)
    Box(
        modifier = modifier
            .background(AppColor.Gray100, RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) AppColor.Primary500 else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Collage",
                fontFamily = customFont,
                fontSize = 18.sp,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = itemFont.fontName,
                style = AppStyle.caption2().semibold().gray500(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }

}