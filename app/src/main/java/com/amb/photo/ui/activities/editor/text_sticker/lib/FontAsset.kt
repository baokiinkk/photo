package com.amb.photo.ui.activities.editor.text_sticker.lib

import android.content.Context
import android.graphics.Typeface
import android.widget.TextView


object FontAsset {
    fun setFontByName(context: Context, textView: TextView, str: String?) {
        val assets = context.assets
        textView.setTypeface(Typeface.createFromAsset(assets, str)) //"fonts/" + str
    }

    val fontDefault: String
        get() = "fonts/3Frijole-Regular.ttf"

    val listFonts: List<FontItem>
        get() {
            val paths = listOf(
                "fonts/3Frijole-Regular.ttf",
                "fonts/4PermanentMarker.ttf",
                "fonts/5Codystar-Regular.ttf",
                "fonts/6VT323-Regular.ttf",
                "fonts/7FasterOne-Regular.ttf",
                "fonts/8Akronim-Regular.ttf",
                "fonts/9Smokum-Regular.ttf",
                "fonts/11AbeatFont.ttf",
                "fonts/AbrilFatface-Regular.ttf",
                "fonts/Acidic.TTF",
                "fonts/Airpine.ttf",
                "fonts/AlaskanNights.ttf",
                "fonts/AlphaWood.ttf",
                "fonts/Anderson.ttf",
                "fonts/Angelina.TTF",
                "fonts/Anton.ttf",
                "fonts/Atreyu.otf",
                "fonts/AuntJudy.ttf",
                "fonts/Autograf.ttf",
                "fonts/Barron.ttf",
                "fonts/BatmanFont.ttf",
                "fonts/BlackJack.ttf",
                "fonts/BlackOpsOne-Regular.ttf",
                "fonts/BladeTwo.ttf",
                "fonts/BlazedFont.ttf",
                "fonts/Brannboll.ttf",
                "fonts/CantataOne-Regular.ttf",
                "fonts/ChangaOne-Regular.ttf",
                "fonts/CheekyRabbit.ttf",
                "fonts/Christmas.ttf",
                "fonts/ComingSoon.ttf",
                "fonts/Electrolize-Regular.ttf",
                "fonts/Escuela.ttf",
                "fonts/FaracoHand.ttf",
                "fonts/FreebooterScript.ttf",
                "fonts/FugazOne-Regular.ttf",
                "fonts/Galindo-Regular.ttf",
                "fonts/GeostarFill-Regular.ttf",
                "fonts/Halloween.ttf",
                "fonts/HanaleiFill-Regular.ttf",
                "fonts/Infinity.ttf",
                "fonts/Inkburrow.ttf",
                "fonts/JoshHandwriting.ttf",
                "fonts/Journal.TTF",
                "fonts/Limelight-Regular.ttf",
                "fonts/LoveSong.ttf",
                "fonts/LuckiestGuy.ttf",
                "fonts/MarcelleScript.ttf",
                "fonts/Marmelad-Regular.ttf",
                "fonts/Metropolis1920.otf",
                "fonts/Mexcelle.ttf",
                "fonts/MiltonianTattoo-Regular.ttf",
                "fonts/PaquetCadeaux.ttf",
                "fonts/Parisish.ttf",
                "fonts/PlayfairDisplaySC-Black.ttf",
                "fonts/Regency.ttf",
                "fonts/Roboto-Light.ttf",
                "fonts/Roboto-Regular.ttf",
                "fonts/SansitaOne.ttf",
                "fonts/SerreriaSobria.otf",
                "fonts/Shojumaru-Regular.ttf",
                "fonts/SoulMission.ttf",
                "fonts/StarStud.ttf",
                "fonts/StraightBaller.ttf",
                "fonts/TheMockingBird.ttf",
                "fonts/Tommaso.ttf",
                "fonts/Vidaloka-Regular.ttf",
                "fonts/Wakandaforever-Regular.ttf",
                "fonts/WaltoGraph.ttf",
                "fonts/1Monofett.ttf",
                "fonts/2Monoton-Regular.ttf"
            )

            return paths.map { path ->
                val name = path
                    .substringAfterLast("/")  // lấy phần sau dấu "/"
                    .substringBeforeLast(".") // bỏ phần mở rộng (.ttf/.otf)
                    .substringBeforeLast("-")
                FontItem(
                    fontName = name,
                    fontPath = path
                )
            }
        }
}

data class FontItem(
    val fontName: String,
    val fontPath: String
)