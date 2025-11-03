package com.amb.photo.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object AppStyle {

    fun titleBar(): BaseStyle {
        val style = TextStyle(
            fontSize = 20.sp,
            lineHeight = 28.sp,
            fontFamily = fontFamily,
        )
        return BaseStyle(style)
    }

    fun title1(): BaseStyle {
        val style = TextStyle(
            fontSize = 18.sp,
            lineHeight = 24.sp,
            fontFamily = fontFamily,
        )
        return BaseStyle(style)
    }

    fun title2(): BaseStyle {
        val style = TextStyle(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontFamily = fontFamily,
        )
        return BaseStyle(style)
    }

    fun body1(): BaseStyle {
        val style = TextStyle(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontFamily = fontFamily,
        )
        return BaseStyle(style)
    }

    fun button(): BaseStyle {
        val style = TextStyle(
            fontSize = 14.sp,
            lineHeight = 24.sp,
            fontFamily = fontFamily,
        )
        return BaseStyle(style)
    }

    fun buttonMedium(): BaseStyle {
        val style = TextStyle(
            fontSize = 14.sp,
            lineHeight = 24.sp,
            fontFamily = fontFamily,
        )
        return BaseStyle(style)
    }

    fun button16(): BaseStyle {
        val style = TextStyle(
            fontSize = 16.sp,
            lineHeight = 20.sp,
            fontFamily = fontFamily,
        )
        return BaseStyle(style)
    }

    fun buttonLarge(): BaseStyle {
        val style = TextStyle(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontFamily = fontFamily,
        )
        return BaseStyle(style)
    }

    fun body2(): BaseStyle {
        val style = TextStyle(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontFamily = fontFamily,
        )
        return BaseStyle(style)
    }

    fun caption1(): BaseStyle {
        val style = TextStyle(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontFamily = fontFamily,
        )
        return BaseStyle(style)
    }

    fun h1(): BaseStyle {
        val style = TextStyle(
            fontSize = 34.sp,
            lineHeight = 48.sp,
            fontFamily = fontFamily,

            )
        return BaseStyle(style)
    }
    fun titleL(): BaseStyle {
        val style = TextStyle(
            fontSize = 38.sp,
            lineHeight = 48.sp,
            fontFamily = fontFamily,

            )
        return BaseStyle(style)
    }

    fun h2(): BaseStyle {
        val style = TextStyle(
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontFamily = fontFamily,

            )
        return BaseStyle(style)
    }
    fun h5(): BaseStyle {
        val style = TextStyle(
            fontSize = 20.sp,
            lineHeight = 28.sp,
            fontFamily = fontFamily,

            )
        return BaseStyle(style)
    }

    fun font28(): BaseStyle {
        val style = TextStyle(
            fontSize = 28.sp,
            fontFamily = fontFamily,
            lineHeight = 36.sp // Example lineHeight, adjust as necessary
        )
        return BaseStyle(style)
    }

    fun font32(): BaseStyle {
        val style = TextStyle(
            fontSize = 32.sp,
            fontFamily = fontFamily,
            lineHeight = 40.sp // Example lineHeight, adjust as necessary
        )
        return BaseStyle(style)
    }
}

class BaseStyle(private val textStyle: TextStyle) {

    fun bold(): AppColor {
        val newStyle = textStyle.copy(
            fontWeight = FontWeight.Bold
        )
        return AppColor(newStyle)
    }

    fun semibold(): AppColor {
        val newStyle = textStyle.copy(
            fontWeight = FontWeight.SemiBold
        )
        return AppColor(newStyle)
    }

    fun regular(): AppColor {
        val newStyle = textStyle.copy(
            fontWeight = FontWeight.Normal
        )
        return AppColor(newStyle)
    }

    fun medium(): AppColor {
        val newStyle = textStyle.copy(
            fontWeight = FontWeight.Medium
        )
        return AppColor(newStyle)
    }
}

class AppColor(private val textStyle: TextStyle) {

    companion object {
        val Green = Color(0xFF1CA33E)

        val Primary = Color(0xFF354CC4)

        val Gray100 = Color(0xFFE4E4E7)
        val Gray400 = Color(0xFF98A2B3)
        val Gray500 = Color(0xFF7C7E83)
        val Gray700 = Color(0xFF4A4B4F)

        val White = Color(0xFFFFFFFF)

        val Gray300 = Color(0xFFB0B2B5)

        val Gray50 = Color(0xFFEBEDF9)

        val background = Color(0xFFF3F7FA)

        val Purple50 = Color(0xFFF4F3FF)
        val Purple300 = Color(0xFFB4ABFF)
        val Purple500 = Color(0xFF6425F3)
        val Purple400 = Color(0xFFA59BFF)
        val Purple700 = Color(0xFF665CB5)

        val ItemSelected = Color(0xFF8F82FF)
        val ItemUnSelected = Color(0xFFF4F3FF)
    }

    fun gray950(): TextStyle {
        return textStyle.copy(
            color = Color(0xFF0D0D0D)
        )
    }

    fun grayScale09(): TextStyle {
        return textStyle.copy(
            color = Color(0xFF1B1A1F)
        )
    }

    fun grayScale08(): TextStyle {
        return textStyle.copy(
            color = Color(0xFF302F37)
        )
    }

    fun grayScale07(): TextStyle {
        return textStyle.copy(
            color = Color(0xFF484653)
        )
    }
    fun grayScale04(): TextStyle {
        return textStyle.copy(
            color = Color(0xFF9490A2)
        )
    }

    fun grayScale05(): TextStyle {
        return textStyle.copy(
            color = Color(0xFF79748B)
        )
    }

    fun gray800(): TextStyle {
        return textStyle.copy(
            color = Color(0xFF1D2939)
        )
    }
    fun gray900(): TextStyle {
        return textStyle.copy(
            color = Color(0xFF101828)
        )
    }

    fun gray700(): TextStyle {
        return textStyle.copy(
            color = Color(0xFF2B2B2C)
        )
    }

    fun gray600(): TextStyle {
        return textStyle.copy(
            color = Color(0xFF4B5563)
        )
    }

    fun gray500(): TextStyle {
        return textStyle.copy(
            color = Color(0xFF7C7E83)
        )
    }

    fun gray400(): TextStyle {
        return textStyle.copy(
            color = Color(0xFF98A2B3)
        )
    }

    fun gray300(): TextStyle {
        return textStyle.copy(
            color = Color(0xFFD1D5DB)
        )
    }

    fun gray200(): TextStyle {
        return textStyle.copy(
            color = Color(0xFFE5E7EB)
        )
    }

    fun gray100(): TextStyle {
        return textStyle.copy(
            color = Color(0xFFF3F4F6)
        )
    }

    fun gray50(): TextStyle {
        return textStyle.copy(
            color = Color(0xFFF9FAFB)
        )
    }

    fun gray25(): TextStyle {
        return textStyle.copy(
            color = Color(0xFFFCFCFD)
        )
    }

    fun neutral01(): TextStyle {
        return textStyle.copy(
            color = Color(0xFF1A1B1D)
        )
    }

    fun neutral012(): TextStyle {
        return textStyle.copy(
            color = Color(0xFF161719)
        )
    }

    fun neutral02(): TextStyle {
        return textStyle.copy(
            color = Color(0xFF374151)
        )
    }

    fun neutral03(): TextStyle {
        return textStyle.copy(
            color = Color(0xFF505050)
        )
    }

    fun neutral04(): TextStyle {
        return textStyle.copy(
            color = Color(0xFF9CA3AF)
        )
    }

    fun neutral05(): TextStyle {
        return textStyle.copy(
            color = Color(0xFFD1D5DB)
        )
    }

    fun primary(): TextStyle {
        return textStyle.copy(
            color = Color(0xFF354CC4)
        )
    }

    fun primary500(): TextStyle {
        return textStyle.copy(
            color = Color(0xFF6425F3)
        )
    }

    fun main(): TextStyle {
        return textStyle.copy(
            color = Color(0xFF3972FE)
        )
    }


    fun primaryLight(): TextStyle {
        return textStyle.copy(
            color = Color(0xFF768FE6)
        )
    }

    fun success(): TextStyle {
        return textStyle.copy(
            color = Color(0xFF1CA33E)
        )
    }

    fun error(): TextStyle {
        return textStyle.copy(
            color = Color(0xFFEF4444)
        )
    }

    fun warning(): TextStyle {
        return textStyle.copy(
            color = Color(0xFFF59E0B)
        )
    }

    fun white(): TextStyle {
        return textStyle.copy(
            color = Color.White
        )
    }

    fun purple500(): TextStyle {
        return textStyle.copy(
            color = Purple500
        )
    }

    fun purple400(): TextStyle {
        return textStyle.copy(
            color = Purple400
        )
    }

    fun purple300(): TextStyle {
        return textStyle.copy(
            color = Purple300
        )
    }

    fun purple700(): TextStyle {
        return textStyle.copy(
            color = Purple700
        )
    }

    fun regular(): TextStyle {
        return textStyle.copy(
            color = Color(0xFF918DB3)
        )
    }

    fun black(): TextStyle {
        return textStyle.copy(
            color = Color.Black
        )
    }

    fun black50(): TextStyle {
        return textStyle.copy(
            color = Color(0xFFFBFBFB)
        )
    }

    fun Neutral01(): TextStyle {
        return textStyle.copy(
            color = Color(0xFF1A1B1D)
        )
    }

    fun Neutral03(): TextStyle {
        return textStyle.copy(
            color = Color(0xFF505050)
        )
    }


}
