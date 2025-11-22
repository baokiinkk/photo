package com.avnsoft.photoeditor.photocollage.utils

import android.content.Context
import com.avnsoft.photoeditor.photocollage.data.model.collage.CollageTemplate
import java.io.IOException

/**
 * Data class để lưu thông tin frame grid item
 */
data class FrameGridItem(
    val framePath: String, // Path trong assets, ví dụ: "frame/2_image/02_0.png"
    val templateId: String, // Template ID tương ứng, ví dụ: "2-0"
    val template: CollageTemplate? = null // Template object nếu có
)

/**
 * Utility để load frame files từ assets và map với templates
 */
object FrameGridLoader {
    /**
     * Load danh sách frame files từ assets dựa trên số lượng ảnh
     * và map với templates
     */
    fun loadFrameGridItems(
        context: Context,
        imageCount: Int,
        templates: List<CollageTemplate>
    ): List<FrameGridItem> {
        val frameFolder = "${imageCount}_image"
        val framePathPrefix = "frame/$frameFolder"
        
        val frameItems = mutableListOf<FrameGridItem>()
        
        try {
            // List tất cả files trong folder frame/{imageCount}_image
            val files = context.assets.list(framePathPrefix)
            files?.let { fileList ->
                // Sort files để đảm bảo thứ tự
                val sortedFiles = fileList.sorted()
                
                sortedFiles.forEach { fileName ->
                    if (fileName.endsWith(".png")) {
                        // Extract index từ filename: "02_0.png" -> "0"
                        val index = extractIndexFromFileName(fileName, imageCount)
                        if (index != null) {
                            val templateId = "${imageCount}-${index}"
                            val framePath = "$framePathPrefix/$fileName"
                            
                            // Tìm template tương ứng
                            val template = templates.find { it.id == templateId }
                            
                            // Chỉ thêm frame nếu có template tương ứng
                            if (template != null) {
                                frameItems.add(
                                    FrameGridItem(
                                        framePath = framePath,
                                        templateId = templateId,
                                        template = template
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        
        return frameItems
    }
    
    /**
     * Extract index từ filename
     * Ví dụ: "02_0.png" -> 0, "02_1.png" -> 1, "03_10.png" -> 10
     */
    private fun extractIndexFromFileName(fileName: String, imageCount: Int): Int? {
        return try {
            // Remove extension
            val nameWithoutExt = fileName.substringBeforeLast(".")
            // Split by "_" và lấy phần cuối
            val parts = nameWithoutExt.split("_")
            if (parts.size >= 2) {
                parts.last().toInt()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

