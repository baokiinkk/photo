package com.amb.photo.data.model.collage

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.google.gson.annotations.SerializedName

data class CollageTemplate(
    @SerializedName("id") val id: String,
    @SerializedName("cells") val cells: List<CellSpec>
)

data class CellSpec(
    @SerializedName("x") val x: Float,          // 0..1 (left)
    @SerializedName("y") val y: Float,          // 0..1 (top)
    @SerializedName("width") val width: Float,  // 0..1 (relative to parent width)
    @SerializedName("height") val height: Float,// 0..1
    @SerializedName("shape") val shape: String? = null // rect | diag_tlbr | diag_bltr
)

/**
 * Compose Shape cho 2 loại split chéo. Dùng để clip nội dung ảnh theo hình.
 */
class DiagonalShape(private val tlbr: Boolean) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        if (tlbr) {
            // tam giác phía trên-trái
            path.moveTo(0f, 0f)
            path.lineTo(size.width, 0f)
            path.lineTo(0f, size.height)
        } else {
            // tam giác phía dưới-trái
            path.moveTo(0f, size.height)
            path.lineTo(size.width, size.height)
            path.lineTo(size.width, 0f)
        }
        path.close()
        return Outline.Generic(path)
    }
}


