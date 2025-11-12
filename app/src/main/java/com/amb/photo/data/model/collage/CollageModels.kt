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
    // Legacy fields (optional, for backward compatibility)
    @SerializedName("x") val x: Float? = null,          // 0..1 (left)
    @SerializedName("y") val y: Float? = null,          // 0..1 (top)
    @SerializedName("width") val width: Float? = null,  // 0..1 (relative to parent width)
    @SerializedName("height") val height: Float? = null,// 0..1
    // Points: relative coordinates in bound (0..1), format: [x0,y0,x1,y1,x2,y2,x3,y3]
    @SerializedName("points") val points: List<Float>,
    // Clear area points: relative coordinates in bound (0..1), để khoét lỗ
    @SerializedName("clearAreaPoints") val clearAreaPoints: List<Float>? = null
)

class FreePolygonShape(
    private val points: List<Float>
) : Shape {
    init {
        require(points.size >= 6 && points.size % 2 == 0) { 
            "points must have at least 6 floats (3 points) and be even (x0,y0,x1,y1,...)" 
        }
    }

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        if (points.isEmpty()) {
            return Outline.Generic(path)
        }
        
        val x0 = points[0] * size.width
        val y0 = points[1] * size.height
        path.moveTo(x0, y0)
        
        for (i in 2 until points.size step 2) {
            val x = points[i] * size.width
            val y = points[i + 1] * size.height
            path.lineTo(x, y)
        }
        path.close()
        return Outline.Generic(path)
    }
}


