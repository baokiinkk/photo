package com.avnsoft.photoeditor.photocollage.data.model.collage

import androidx.compose.ui.geometry.Offset
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
    // Optional: nếu có path hoặc clearPath thì có thể không có points
    @SerializedName("points") val points: List<Float>? = null,
    // Clear area points: relative coordinates in bound (0..1), để khoét lỗ
    @SerializedName("clearAreaPoints") val clearAreaPoints: List<Float>? = null,
    // Clear path type: "CIRCLE", "RECT", "POLYGON", "HEXAGON" hoặc null
    @SerializedName("clearPathType") val clearPathType: String? = null,
    // Clear path ratio bound: [left, top, right, bottom] - vị trí của clearPath trong bound (có thể < 0 hoặc > 1)
    @SerializedName("clearPathRatioBound") val clearPathRatioBound: List<Float>? = null,
    // Clear path căn giữa theo chiều ngang
    @SerializedName("clearPathInCenterHorizontal") val clearPathInCenterHorizontal: Boolean? = null,
    // Clear path căn giữa theo chiều dọc
    @SerializedName("clearPathInCenterVertical") val clearPathInCenterVertical: Boolean? = null,
    // Path type: "CIRCLE", "RECT", "POLYGON", "HEXAGON" hoặc null (dùng cho cell chính, không phải clearPath)
    @SerializedName("pathType") val pathType: String? = null,
    // Path ratio bound: [left, top, right, bottom] - vị trí của path trong bound (có thể < 0 hoặc > 1)
    @SerializedName("pathRatioBound") val pathRatioBound: List<Float>? = null,
    // Path căn giữa theo chiều ngang
    @SerializedName("pathInCenterHorizontal") val pathInCenterHorizontal: Boolean? = null,
    // Path căn giữa theo chiều dọc
    @SerializedName("pathInCenterVertical") val pathInCenterVertical: Boolean? = null,
    // Path align parent right (cho template 3-2)
    @SerializedName("pathAlignParentRight") val pathAlignParentRight: Boolean? = null,
    // Fit bound: boolean để fit image vào bound
    @SerializedName("fitBound") val fitBound: Boolean? = null,
    // Shrink method: Integer constant matching Java Photo class
    // 0 = SHRINK_METHOD_DEFAULT, 1 = SHRINK_METHOD_3_3, 2 = SHRINK_METHOD_USING_MAP,
    // 3 = SHRINK_METHOD_3_6, 4 = SHRINK_METHOD_3_8, 5 = SHRINK_METHOD_COMMON
    // Hoặc có thể dùng string: "DEFAULT", "3_3", "USING_MAP", "3_6", "3_8", "COMMON"
    @SerializedName("shrinkMethod") val shrinkMethod: Any? = null, // Int hoặc String
    // Shrink map: map từ point index đến shrink direction/curve [x, y]
    // Format: {"0": [2, 2], "1": [2, 1], ...} - key là index của point trong points array
    // Giá trị [x, y] có thể là direction vector hoặc control point cho đường cong (curve)
    // Để tạo hiệu ứng tròn/curved, các giá trị này được interpolate để tạo path mượt
    @SerializedName("shrinkMap") val shrinkMap: Map<String, List<Float>>? = null,
    // Corner method: Integer constant matching Java Photo class
    // 0 = CORNER_METHOD_DEFAULT, 1 = CORNER_METHOD_3_6, 2 = CORNER_METHOD_3_13
    // Hoặc có thể dùng string: "DEFAULT", "3_6", "3_13"
    @SerializedName("cornerMethod") val cornerMethod: Any? = null // Int hoặc String
)

class FreePolygonShape(
    private val points: List<Float>,
    private val shrinkMap: Map<String, List<Float>>? = null,
    private val shrinkSpacingPx: Float = 0f
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
        
        val pointCount = points.size / 2
        val hasShrinkMap = shrinkMap != null && shrinkMap.isNotEmpty()
        val spacing = shrinkSpacingPx

        if (hasShrinkMap && pointCount > 0) {
            val shrunkPoints = MutableList(pointCount) { index ->
                val baseX = points[index * 2] * size.width
                val baseY = points[index * 2 + 1] * size.height
                val dir = shrinkMap!![index.toString()]
                val deltaX = if (dir != null && dir.size >= 2) dir[0] * spacing else 0f
                val deltaY = if (dir != null && dir.size >= 2) dir[1] * spacing else 0f
                Offset(baseX + deltaX, baseY + deltaY)
            }

            path.moveTo(shrunkPoints[0].x, shrunkPoints[0].y)
            for (i in 1 until shrunkPoints.size) {
                path.lineTo(shrunkPoints[i].x, shrunkPoints[i].y)
            }
        } else {
        val x0 = points[0] * size.width
        val y0 = points[1] * size.height
        path.moveTo(x0, y0)
            
            for (i in 2 until points.size step 2) {
            val x = points[i] * size.width
            val y = points[i + 1] * size.height
            path.lineTo(x, y)
        }
        }
        
        path.close()
        return Outline.Generic(path)
    }
}


