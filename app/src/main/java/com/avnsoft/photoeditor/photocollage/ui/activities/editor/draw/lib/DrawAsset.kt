package com.avnsoft.photoeditor.photocollage.ui.activities.editor.draw.lib

import com.avnsoft.photoeditor.photocollage.BaseApplication
import com.avnsoft.photoeditor.photocollage.R

object DrawAsset {

    fun lstColorForBrush(): MutableList<String?> {
        val arrayList: MutableList<String?> = ArrayList<String?>()
        arrayList.add("#f44336")
        arrayList.add("#E91E63")
        arrayList.add("#EC407A")
        arrayList.add("#9C27B0")
        arrayList.add("#673AB7")
        arrayList.add("#3F51B5")
        arrayList.add("#2196F3")
        arrayList.add("#03A9F4")
        arrayList.add("#00BFA5")
        arrayList.add("#00BCD4")
        arrayList.add("#009688")
        arrayList.add("#4CAF50")
        arrayList.add("#8BC34A")
        arrayList.add("#CDDC39")
        arrayList.add("#FFEB3B")
        arrayList.add("#FFC107")
        arrayList.add("#FF9800")
        arrayList.add("#FF5722")
        arrayList.add("#795548")
        arrayList.add("#9E9E9E")
        arrayList.add("#607D8B")
        arrayList.add("#FFFFFF")
        arrayList.add("#000000")
        return arrayList
    }

    var drawBitmapModels: MutableList<DrawBitmapModel> = ArrayList<DrawBitmapModel>()

    fun lstDrawBitmapModel(): MutableList<DrawBitmapModel> {
        if (drawBitmapModels.isNotEmpty()) {
            return drawBitmapModels
        }
        val arrayList: ArrayList<Int> = ArrayList<Int>()
        arrayList.add(Integer.valueOf(R.drawable.ic_magic_br_b4))
        arrayList.add(Integer.valueOf(R.drawable.ic_magic_br_b5))
        arrayList.add(Integer.valueOf(R.drawable.ic_magic_br_b6))
        arrayList.add(Integer.valueOf(R.drawable.ic_magic_br_b7))
        arrayList.add(Integer.valueOf(R.drawable.ic_magic_br_b8))
        arrayList.add(Integer.valueOf(R.drawable.ic_magic_br_b9))
        arrayList.add(Integer.valueOf(R.drawable.ic_magic_br_b10))
        arrayList.add(Integer.valueOf(R.drawable.ic_magic_br_b11))
        arrayList.add(Integer.valueOf(R.drawable.ic_magic_br_b12))
        arrayList.add(Integer.valueOf(R.drawable.ic_magic_br_b13))
        arrayList.add(Integer.valueOf(R.drawable.ic_magic_br_b14))
        arrayList.add(Integer.valueOf(R.drawable.ic_magic_br_b15))
        drawBitmapModels.add(
            DrawBitmapModel(
                R.drawable.ic_magic_br_butterfly,
                arrayList,
                true,
                BaseApplication.getInstanceApp()
            )
        )
        val arrayList2: ArrayList<Int> = ArrayList<Int>()
        arrayList2.add(Integer.valueOf(R.drawable.ic_magic_br_magic21))
        arrayList2.add(Integer.valueOf(R.drawable.ic_magic_br_magic22))
        arrayList2.add(Integer.valueOf(R.drawable.ic_magic_br_magic23))
        arrayList2.add(Integer.valueOf(R.drawable.ic_magic_br_magic24))
        drawBitmapModels.add(
            DrawBitmapModel(
                R.drawable.ic_magic_br_heart_1,
                arrayList2,
                true,
                BaseApplication.getInstanceApp()
            )
        )
        val arrayList3: ArrayList<Int> = ArrayList<Int>()
        arrayList3.add(Integer.valueOf(R.drawable.ic_magic_br_f1))
        arrayList3.add(Integer.valueOf(R.drawable.ic_magic_br_f1))
        drawBitmapModels.add(
            DrawBitmapModel(
                R.drawable.ic_magic_br_f1_icon,
                arrayList3,
                true,
                BaseApplication.getInstanceApp()
            )
        )
        val arrayList4: ArrayList<Int> = java.util.ArrayList<Int>()
        arrayList4.add(Integer.valueOf(R.drawable.bg_magic_br_s1))
        arrayList4.add(Integer.valueOf(R.drawable.bg_magic_br_s1))
        drawBitmapModels.add(
            DrawBitmapModel(
                R.drawable.bg_magic_br_s1_icon,
                arrayList4,
                true,
                BaseApplication.getInstanceApp()
            )
        )
        val arrayList5: java.util.ArrayList<Int> = java.util.ArrayList<Int>()
        arrayList5.add(Integer.valueOf(R.drawable.ic_magic_br_b1))
        arrayList5.add(Integer.valueOf(R.drawable.ic_magic_br_b2))
        arrayList5.add(Integer.valueOf(R.drawable.ic_magic_br_b3))
        drawBitmapModels.add(
            DrawBitmapModel(
                R.drawable.ic_magic_br_b1_icon,
                arrayList5,
                true,
                BaseApplication.getInstanceApp()
            )
        )
        val arrayList6: java.util.ArrayList<Int> = java.util.ArrayList<Int>()
        arrayList6.add(Integer.valueOf(R.drawable.ic_magic_br_f3))
        arrayList6.add(Integer.valueOf(R.drawable.ic_magic_br_f7))
        arrayList6.add(Integer.valueOf(R.drawable.ic_magic_br_f5))
        arrayList6.add(Integer.valueOf(R.drawable.ic_magic_br_f6))
        drawBitmapModels.add(
            DrawBitmapModel(
                R.drawable.ic_magic_br_f2_icon,
                arrayList6,
                true,
                BaseApplication.getInstanceApp()
            )
        )
        val arrayList7: java.util.ArrayList<Int> = java.util.ArrayList<Int>()
        arrayList7.add(Integer.valueOf(R.drawable.ic_magic_br_m1))
        arrayList7.add(Integer.valueOf(R.drawable.ic_magic_br_m2))
        arrayList7.add(Integer.valueOf(R.drawable.ic_magic_br_m3))
        arrayList7.add(Integer.valueOf(R.drawable.ic_magic_br_m4))
        drawBitmapModels.add(
            DrawBitmapModel(
                R.drawable.ic_magic_br_bb2_icon,
                arrayList7,
                true,
                BaseApplication.getInstanceApp()
            )
        )
        val arrayList8: java.util.ArrayList<Int> = java.util.ArrayList<Int>()
        arrayList8.add(Integer.valueOf(R.drawable.ic_magic_br_ss1))
        arrayList8.add(Integer.valueOf(R.drawable.ic_magic_br_ss2))
        arrayList8.add(Integer.valueOf(R.drawable.ic_magic_br_ss3))
        arrayList8.add(Integer.valueOf(R.drawable.ic_magic_br_ss5))
        drawBitmapModels.add(
            DrawBitmapModel(
                R.drawable.ic_magic_br_f3_icon,
                arrayList8,
                true,
                BaseApplication.getInstanceApp()
            )
        )
        val arrayList9: java.util.ArrayList<Int> = java.util.ArrayList<Int>()
        arrayList9.add(Integer.valueOf(R.drawable.bg_magic_br_s17))
        arrayList9.add(Integer.valueOf(R.drawable.bg_magic_br_s17))
        drawBitmapModels.add(
            DrawBitmapModel(
                R.drawable.ic_magic_br_smile_icon1,
                arrayList9,
                true,
                BaseApplication.getInstanceApp()
            )
        )
        val arrayList10: java.util.ArrayList<Int> = java.util.ArrayList<Int>()
        arrayList10.add(Integer.valueOf(R.drawable.bg_magic_br_s21))
        arrayList10.add(Integer.valueOf(R.drawable.bg_magic_br_s21))
        drawBitmapModels.add(
            DrawBitmapModel(
                R.drawable.ic_magic_br_smile_icon2,
                arrayList10,
                true,
                BaseApplication.getInstanceApp()
            )
        )
        return drawBitmapModels
    }
}