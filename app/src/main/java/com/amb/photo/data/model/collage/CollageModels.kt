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
    @SerializedName("points") val points: List<Float>? = null
)

class FreePolygonShape(
    private val points: List<Float>
) : Shape {
    init {
        require(points.size == 8) { "points must have 8 floats (x0,y0,...,x3,y3)" }
    }

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        val x0 = points[0] * size.width
        val y0 = points[1] * size.height
        path.moveTo(x0, y0)
        for (i in 2 until 8 step 2) {
            val x = points[i] * size.width
            val y = points[i + 1] * size.height
            path.lineTo(x, y)
        }
        path.close()
        return Outline.Generic(path)
    }
}


