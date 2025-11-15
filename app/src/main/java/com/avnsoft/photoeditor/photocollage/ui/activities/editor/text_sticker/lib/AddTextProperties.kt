package com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib

import android.graphics.Color
import android.graphics.Shader
import androidx.core.view.InputDeviceCompat


class AddTextProperties {
    var backgroundAlpha: Int = 0
    var backgroundBorder: Int = 0
    var backgroundColor: Int = 0
    var backgroundColorIndex: Int = 0
    var fontIndex: Int = 0
    var fontName: String? = null
    var isFullScreen: Boolean = false
    var isShowBackground: Boolean = false
    var paddingHeight: Int = 0
    var paddingWidth: Int = 0
    var text: String? = null
    var textAlign: Int = 0
    var textAlpha: Int = 0
    var textColor: Int = 0
    var textColorIndex: Int = 0
    var textHeight: Int = 0
    var textShader: Shader? = null
    var textShaderIndex: Int = 0
    var textShadow: TextShadow? = null
    var textShadowIndex: Int = 0
    var textSize: Int = 0
    var textWidth: Int = 0

    class TextShadow internal constructor(
        var radius: Int,
        val dx: Int,
        val dy: Int,
        val colorShadow: Int
    )


    companion object {
        val lstTextShadow: MutableList<TextShadow?>
            get() {
                val arrayList = ArrayList<TextShadow?>()
                arrayList.add(TextShadow(0, 0, 0, -16711681))
                arrayList.add(TextShadow(8, 4, 4, Color.parseColor("#FF1493")))
                arrayList.add(TextShadow(8, 4, 4, -65281))
                arrayList.add(TextShadow(8, 4, 4, Color.parseColor("#24ffff")))
                arrayList.add(TextShadow(8, 4, 4, InputDeviceCompat.SOURCE_ANY))
                arrayList.add(TextShadow(8, 4, 4, -1))
                arrayList.add(TextShadow(8, 4, 4, -7829368))
                arrayList.add(TextShadow(8, -4, -4, Color.parseColor("#FF1493")))
                arrayList.add(TextShadow(8, -4, -4, -65281))
                arrayList.add(TextShadow(8, -4, -4, Color.parseColor("#24ffff")))
                arrayList.add(TextShadow(8, -4, -4, InputDeviceCompat.SOURCE_ANY))
                arrayList.add(TextShadow(8, -4, -4, -1))
                arrayList.add(TextShadow(8, -4, -4, Color.parseColor("#696969")))
                arrayList.add(TextShadow(8, -4, 4, Color.parseColor("#FF1493")))
                arrayList.add(TextShadow(8, -4, 4, -65281))
                arrayList.add(TextShadow(8, -4, 4, Color.parseColor("#24ffff")))
                arrayList.add(TextShadow(8, -4, 4, InputDeviceCompat.SOURCE_ANY))
                arrayList.add(TextShadow(8, -4, 4, -1))
                arrayList.add(TextShadow(8, -4, 4, Color.parseColor("#696969")))
                arrayList.add(TextShadow(8, 4, -4, Color.parseColor("#FF1493")))
                arrayList.add(TextShadow(8, 4, -4, -65281))
                arrayList.add(TextShadow(8, 4, -4, Color.parseColor("#24ffff")))
                arrayList.add(TextShadow(8, 4, -4, InputDeviceCompat.SOURCE_ANY))
                arrayList.add(TextShadow(8, 4, -4, -1))
                arrayList.add(TextShadow(8, 4, -4, Color.parseColor("#696969")))
                return arrayList
            }

        val defaultProperties: AddTextProperties
            get() {
                val addTextProperties = AddTextProperties()
                addTextProperties.textSize = 18
                addTextProperties.textAlign = 4
                addTextProperties.fontName = getFontDefault() //"fonts/36.ttf"
                addTextProperties.textColor = -1
                addTextProperties.textAlpha = 255
                addTextProperties.backgroundAlpha = 255
                addTextProperties.paddingWidth = 16
                addTextProperties.textShaderIndex = 7
                addTextProperties.backgroundColorIndex = 21
                addTextProperties.textColorIndex = 16
                addTextProperties.fontIndex = 0
                addTextProperties.isShowBackground = false
                addTextProperties.backgroundBorder = 8
                return addTextProperties
            }
    }
}

fun getFontDefault(): String {
    return "fonts/3Frijole-Regular.ttf"
}