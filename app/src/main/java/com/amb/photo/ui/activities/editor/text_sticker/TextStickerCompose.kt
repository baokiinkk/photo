package com.amb.photo.ui.activities.editor.text_sticker

import android.content.Context
import android.graphics.Typeface
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amb.photo.ui.activities.editor.text_sticker.lib.FontItem
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
    itemFont: FontItem
) {
    val context = LocalContext.current
    val customFont = rememberFontFromAssets(context, itemFont.fontPath)
    Column {
        Text(
            text = "Collage",
            fontFamily = customFont,
            fontSize = 18.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = itemFont.fontName,
            style = AppStyle.caption2().semibold().gray500()
        )
    }

}