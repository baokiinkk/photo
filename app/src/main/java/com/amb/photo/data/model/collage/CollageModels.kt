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
    private val shrinkMap: Map<String, List<Float>>? = null
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
        
        // Nếu có shrinkMap, sử dụng đường cong (quadratic bezier)
        if (shrinkMap != null && shrinkMap.isNotEmpty()) {
            // Tính toán các điểm với shrink direction để tạo đường cong
            val actualPoints = mutableListOf<androidx.compose.ui.geometry.Offset>()
            
            for (i in 0 until pointCount) {
                val x = points[i * 2] * size.width
                val y = points[i * 2 + 1] * size.height
                actualPoints.add(androidx.compose.ui.geometry.Offset(x, y))
            }
            
            // Tạo path với đường cong
            path.moveTo(actualPoints[0].x, actualPoints[0].y)
            
            for (i in 0 until pointCount) {
                val currentIdx = i
                val nextIdx = (i + 1) % pointCount
                val currentPoint = actualPoints[currentIdx]
                val nextPoint = actualPoints[nextIdx]
                
                // Lấy shrink direction từ shrinkMap
                val shrinkDir = shrinkMap[currentIdx.toString()]?.let { 
                    if (it.size >= 2) androidx.compose.ui.geometry.Offset(it[0], it[1]) else null
                }
                
                if (shrinkDir != null && (shrinkDir.x != 0f || shrinkDir.y != 0f)) {
                    // Tính control point dựa trên shrink direction
                    // shrinkDir là vector direction, scale nó để tạo control point
                    val shrinkAmount = kotlin.math.min(size.width, size.height) * 0.1f
                    val controlX = currentPoint.x + shrinkDir.x * shrinkAmount
                    val controlY = currentPoint.y + shrinkDir.y * shrinkAmount
                    
                    // Sử dụng quadratic bezier để tạo đường cong
                    path.quadraticTo(
                        x1 = controlX,
                        y1 = controlY,
                        x2 = nextPoint.x,
                        y2 = nextPoint.y
                    )
                } else {
                    // Không có shrinkMap hoặc direction = 0, dùng đường thẳng
                    path.lineTo(nextPoint.x, nextPoint.y)
                }
            }
        } else {
            // Không có shrinkMap, dùng đường thẳng như cũ
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


