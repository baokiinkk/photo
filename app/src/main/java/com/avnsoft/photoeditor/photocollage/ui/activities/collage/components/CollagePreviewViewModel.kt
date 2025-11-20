package com.avnsoft.photoeditor.photocollage.ui.activities.collage.components

import android.net.Uri
import com.avnsoft.photoeditor.photocollage.data.model.collage.CellSpec
import com.avnsoft.photoeditor.photocollage.data.model.collage.CollageTemplate

/**
 * Data class để lưu thông tin đã xử lý cho mỗi cell
 */
data class ProcessedCellData(
    val left: Float,           // Pixel
    val top: Float,            // Pixel
    val width: Float,          // Pixel
    val height: Float,         // Pixel
    val normalizedPoints: List<Float>?,  // Points đã normalize về 0..1 (optional nếu có path)
    val clearAreaPoints: List<Float>? = null,  // Clear area points đã normalize về 0..1
    val shrinkMap: Map<String, List<Float>>? = null,  // Shrink map để tạo đường cong
    val pathType: String? = null,  // Path type: "CIRCLE", "RECT", "POLYGON", "HEXAGON"
    val pathRatioBound: List<Float>? = null,  // Path ratio bound
    val pathInCenterHorizontal: Boolean? = null,
    val pathInCenterVertical: Boolean? = null,
    val clearPathType: String? = null,  // Clear path type
    val clearPathRatioBound: List<Float>? = null,  // Clear path ratio bound
    val clearPathInCenterHorizontal: Boolean? = null,
    val clearPathInCenterVertical: Boolean? = null,
    val fitBound: Boolean? = null,  // Fit bound
    val pathAlignParentRight: Boolean? = null,  // Path align parent right
    val cornerMethod: String? = null, // Corner method identifier
    val imageUri: Uri
)

/**
 * Data class để lưu thông tin preview đã xử lý
 */
data class CollagePreviewData(
    val cells: List<ProcessedCellData>,
    val backgroundColor: androidx.compose.ui.graphics.Color
)

/**
 * Helper object để xử lý và lưu data cho CollagePreview
 * (Không dùng ViewModel vì đây là logic xử lý thuần túy, không cần lifecycle)
 */
object CollagePreviewDataProcessor {
    
    /**
     * Xử lý template và trả về data đã được tính toán
     */
    fun processTemplate(
        template: CollageTemplate,
        images: List<Uri>,
        canvasWidth: Float,
        canvasHeight: Float
    ): List<ProcessedCellData> {
        return template.cells.mapIndexed { index, cell ->
            processCell(
                cell = cell,
                imageUri = images.getOrNull(index % images.size) ?: Uri.EMPTY,
                canvasWidth = canvasWidth,
                canvasHeight = canvasHeight
            )
        }
    }
    
    /**
     * Xử lý một cell: tính bounding box và normalize points
     */
    private fun processCell(
        cell: CellSpec,
        imageUri: Uri,
        canvasWidth: Float,
        canvasHeight: Float
    ): ProcessedCellData {
        val cornerMethod = parseCornerMethod(cell.cornerMethod)
        // Points là optional nếu có path hoặc clearPath
        if (cell.points != null) {
            require(cell.points.size >= 6 && cell.points.size % 2 == 0) {
                "Cell must have at least 6 points (3 vertices) and be even (x0,y0,x1,y1,...)"
            }
        }
        
        // Logic mới: Ưu tiên dùng bound (x, y, width, height) nếu có
        // - Nếu có bound: points là tương đối trong bound (0..1), giống code Java gốc
        // - Nếu không có bound: tính từ points (absolute coordinates 0..1) - backward compatibility
        val result = if (
            cell.x != null && cell.y != null &&
            cell.width != null && cell.height != null
        ) {
            // Có bound: x, y là left, top (0..1); width, height là kích thước (0..1)
            // Points là tương đối trong bound (0..1), không cần normalize
            
            // Tính left và top margin (giống code cũ)
            val leftPx = cell.x!! * canvasWidth
            val topPx = cell.y!! * canvasHeight
            
            // Tính width và height theo logic cũ:
            // - Nếu bound.right == 1 (tức x + width == 1): dùng containerWidth - leftMargin
            // - Nếu không: dùng containerWidth * width
            // Tương tự cho height
            val rightBound = cell.x!! + cell.width!!
            val bottomBound = cell.y!! + cell.height!!
            val epsilon = 0.0001f // Để xử lý floating point precision
            
            val widthPx = if (kotlin.math.abs(rightBound - 1f) < epsilon) {
                // bound.right == 1: dùng containerWidth - leftMargin (giống code cũ)
                canvasWidth - leftPx
            } else {
                // bound.right != 1: dùng containerWidth * width
                cell.width!! * canvasWidth
            }
            
            val heightPx = if (kotlin.math.abs(bottomBound - 1f) < epsilon) {
                // bound.bottom == 1: dùng containerHeight - topMargin (giống code cũ)
                canvasHeight - topPx
            } else {
                // bound.bottom != 1: dùng containerHeight * height
                cell.height!! * canvasHeight
            }
            
            // Points đã là tương đối trong bound (0..1), chỉ cần đảm bảo trong range
            // Nếu không có points (có path hoặc clearPath), dùng empty list
            val points = cell.points?.map { it.coerceIn(0f, 1f) } ?: emptyList()
            
            // Clear area points cũng là relative trong bound (0..1)
            val clearAreaPoints = cell.clearAreaPoints?.map { it.coerceIn(0f, 1f) }
            
            // Shrink map giữ nguyên
            val shrinkMap = cell.shrinkMap
            
            ProcessedCellData(
                leftPx, topPx, widthPx, heightPx, 
                points, clearAreaPoints, shrinkMap,
                cell.pathType, cell.pathRatioBound, 
                cell.pathInCenterHorizontal, cell.pathInCenterVertical,
                cell.clearPathType, cell.clearPathRatioBound,
                cell.clearPathInCenterHorizontal, cell.clearPathInCenterVertical,
                cell.fitBound, cell.pathAlignParentRight,
                cornerMethod,
                imageUri
            )
        } else {
            // Không có bound: tính từ points (absolute coordinates 0..1)
            // Fallback cho các template cũ không có bound
            // Nếu không có points, không thể tính bound - trả về default
            if (cell.points == null || cell.points.isEmpty()) {
                return ProcessedCellData(
                    0f, 0f, 0f, 0f, 
                    null, null, null,
                    cell.pathType, cell.pathRatioBound,
                    cell.pathInCenterHorizontal, cell.pathInCenterVertical,
                    cell.clearPathType, cell.clearPathRatioBound,
                    cell.clearPathInCenterHorizontal, cell.clearPathInCenterVertical,
                    cell.fitBound, cell.pathAlignParentRight,
                    cornerMethod,
                    imageUri
                )
            }
            
            val xCoords = cell.points.filterIndexed { i, _ -> i % 2 == 0 }
            val yCoords = cell.points.filterIndexed { i, _ -> i % 2 == 1 }
            
            val minXRel = xCoords.minOrNull() ?: 0f
            val maxXRel = xCoords.maxOrNull() ?: 0f
            val minYRel = yCoords.minOrNull() ?: 0f
            val maxYRel = yCoords.maxOrNull() ?: 0f
            
            val leftPx = minXRel * canvasWidth
            val topPx = minYRel * canvasHeight
            val widthPx = (maxXRel - minXRel) * canvasWidth
            val heightPx = (maxYRel - minYRel) * canvasHeight
            
            // Normalize points to 0..1 relative to bounding box
            val widthRel = maxXRel - minXRel
            val heightRel = maxYRel - minYRel
            
            val points = cell.points.mapIndexed { i, v ->
                if (i % 2 == 0) {
                    if (widthRel > 0.0001f) {
                        ((v - minXRel) / widthRel).coerceIn(0f, 1f)
                    } else {
                        0f
                    }
                } else {
                    if (heightRel > 0.0001f) {
                        ((v - minYRel) / heightRel).coerceIn(0f, 1f)
                    } else {
                        0f
                    }
                }
            }
            
            ProcessedCellData(
                leftPx, topPx, widthPx, heightPx, 
                points, null, null,
                cell.pathType, cell.pathRatioBound,
                cell.pathInCenterHorizontal, cell.pathInCenterVertical,
                cell.clearPathType, cell.clearPathRatioBound,
                cell.clearPathInCenterHorizontal, cell.clearPathInCenterVertical,
                cell.fitBound, cell.pathAlignParentRight,
                cornerMethod,
                imageUri
            )
        }
        
        return result
    }
}

private fun parseCornerMethod(value: Any?): String? {
    return when (value) {
        is String -> value
        is Number -> when (value.toInt()) {
            1 -> "3_6"
            2 -> "3_13"
            else -> null
        }
        else -> null
    }
}

