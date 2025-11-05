package com.amb.photo.ui.activities.collage

import com.amb.photo.data.model.collage.CellSpec
import com.amb.photo.data.model.collage.CollageTemplate

object CollageTemplates {
    // 1 ảnh: full
    val ONE_FULL = CollageTemplate(
        id = "1-full",
        cells = listOf(
            CellSpec(0f, 0f, 1f, 1f)
        )
    )
    // 1 ảnh: viền trắng giả (dùng để minh họa thêm)
    val ONE_FULL_MARGIN = CollageTemplate(
        id = "1-full-margin",
        cells = listOf(
            CellSpec(0.03f, 0.03f, 0.94f, 0.94f)
        )
    )

    // 2 ảnh: chia đôi dọc (đơn giản, không chéo)
    val TWO_VERTICAL = CollageTemplate(
        id = "2-vertical",
        cells = listOf(
            CellSpec(0f, 0f, 0.5f, 1f),
            CellSpec(0.5f, 0f, 0.5f, 1f)
        )
    )
    // 2 ảnh: chia đôi ngang
    val TWO_HORIZONTAL = CollageTemplate(
        id = "2-horizontal",
        cells = listOf(
            CellSpec(0f, 0f, 1f, 0.5f),
            CellSpec(0f, 0.5f, 1f, 0.5f)
        )
    )
    // 2 ảnh: chia chéo
    val TWO_DIAGONAL = CollageTemplate(
        id = "2-diagonal",
        cells = listOf(
            CellSpec(0f, 0f, 1f, 1f, "diag_tlbr"),
            CellSpec(0f, 0f, 1f, 1f, "diag_bltr")
        )
    )

    // 3 ảnh: trái lớn, phải 2 ô (theo ví dụ)
    val LEFT_BIG_RIGHT_2 = CollageTemplate(
        id = "left-big-right-2",
        cells = listOf(
            CellSpec(0f, 0f, 0.63f, 1f),
            CellSpec(0.66f, 0f, 0.34f, 0.48f),
            CellSpec(0.66f, 0.52f, 0.34f, 0.48f)
        )
    )
    // 3 ảnh: trên 2 ô, dưới 1 ô
    val TOP_2_BOTTOM_1 = CollageTemplate(
        id = "top2-bottom1",
        cells = listOf(
            CellSpec(0f, 0f, 0.5f, 0.52f),
            CellSpec(0.5f, 0f, 0.5f, 0.52f),
            CellSpec(0f, 0.54f, 1f, 0.46f)
        )
    )
    // 3 ảnh: phải lớn, trái 2 ô
    val RIGHT_BIG_LEFT_2 = CollageTemplate(
        id = "right-big-left-2",
        cells = listOf(
            CellSpec(0.37f, 0f, 0.63f, 1f),
            CellSpec(0f, 0f, 0.34f, 0.48f),
            CellSpec(0f, 0.52f, 0.34f, 0.48f)
        )
    )

    fun defaultFor(count: Int): CollageTemplate = when (count) {
        1 -> ONE_FULL
        2 -> TWO_VERTICAL
        else -> LEFT_BIG_RIGHT_2
    }

    fun listForCount(count: Int): List<CollageTemplate> = when (count) {
        1 -> listOf(ONE_FULL, ONE_FULL_MARGIN)
        2 -> listOf(TWO_VERTICAL, TWO_HORIZONTAL, TWO_DIAGONAL)
        else -> listOf(LEFT_BIG_RIGHT_2, TOP_2_BOTTOM_1, RIGHT_BIG_LEFT_2)
    }
}


