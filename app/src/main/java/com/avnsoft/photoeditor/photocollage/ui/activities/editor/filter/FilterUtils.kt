package com.avnsoft.photoeditor.photocollage.ui.activities.editor.filter

import android.graphics.Bitmap
import org.wysaid.common.SharedContext
import org.wysaid.nativePort.CGEImageHandler

class FilterBean(var config: String, var name: String?, val bitmap: Bitmap? = null)

object FilterUtils {

    val EFFECT_CONFIGS: Array<FilterBean> =
        arrayOf<FilterBean>(
            FilterBean("", "Original"),
            FilterBean(
                "@adjust lut filters/bright01.webp",
                "Fresh 01"
            ),
            FilterBean(
                "@adjust lut filters/bright02.webp",
                "Fresh 02"
            ),
            FilterBean(
                "@adjust lut filters/bright03.webp",
                "Fresh 03"
            ),
            FilterBean(
                "@adjust lut filters/bright05.webp",
                "Fresh 04"
            ),
            FilterBean(
                "@adjust lut filters/euro01.webp",
                "Euro 01"
            ),
            FilterBean(
                "@adjust lut filters/euro02.webp",
                "Euro 02"
            ),
            FilterBean(
                "@adjust lut filters/euro05.webp",
                "Euro 03"
            ),
            FilterBean(
                "@adjust lut filters/euro04.webp",
                "Euro 04"
            ),
            FilterBean(
                "@adjust lut filters/euro06.webp",
                "Euro 05"
            ),
            FilterBean(
                "@adjust lut filters/euro07.webp",
                "Euro 06"
            ),
            FilterBean(
                "@adjust lut filters/film01.webp",
                "Film 01"
            ),
            FilterBean(
                "@adjust lut filters/film02.webp",
                "Film 02"
            ),
            FilterBean(
                "@adjust lut filters/film03.webp",
                "Film 03"
            ),
            FilterBean(
                "@adjust lut filters/film04.webp",
                "Film 04"
            ),
            FilterBean(
                "@adjust lut filters/film05.webp",
                "Film 05"
            ),
            FilterBean(
                "@adjust lut filters/lomo1.webp",
                "Lomo 01"
            ),
            FilterBean(
                "@adjust lut filters/lomo2.webp",
                "Lomo 02"
            ),
            FilterBean(
                "@adjust lut filters/lomo3.webp",
                "Lomo 03"
            ),
            FilterBean(
                "@adjust lut filters/lomo4.webp",
                "Lomo 04"
            ),
            FilterBean(
                "@adjust lut filters/lomo5.webp",
                "Lomo 05"
            ),
            FilterBean(
                "@adjust lut filters/movie01.webp",
                "Movie 01"
            ),
            FilterBean(
                "@adjust lut filters/movie02.webp",
                "Movie 02"
            ),
            FilterBean(
                "@adjust lut filters/movie03.webp",
                "Movie 03"
            ),
            FilterBean(
                "@adjust lut filters/movie04.webp",
                "Movie 04"
            ),
            FilterBean(
                "@adjust lut filters/movie05.webp",
                "Movie 05"
            )
        )

    fun initDataFilter(bitmap: Bitmap?): MutableList<FilterBean> {
        val arrayList = ArrayList<FilterBean>()
        val create = SharedContext.create()
        create.makeCurrent()
        val cGEImageHandler = CGEImageHandler()
        cGEImageHandler.initWithBitmap(bitmap)

        for (config in EFFECT_CONFIGS) {
            cGEImageHandler.setFilterWithConfig(config.config)
            cGEImageHandler.processFilters()
            val bmt = cGEImageHandler.resultBitmap
            if (bmt != null && bmt.getWidth() > 0 && bmt.getHeight() > 0) {
                arrayList.add(FilterBean(config.config, config.name, bmt))
            }
        }
        create.release()
        return arrayList
    }
}