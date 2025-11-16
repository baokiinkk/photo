package com.avnsoft.photoeditor.photocollage.data.model.collage

import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.BackgroundSelection
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.FrameSelection

/**
 * State object chứa toàn bộ state của collage editor
 * Sử dụng cho undo/redo functionality
 */
data class CollageState(
    // Layout & Margin
    val templateId: String? = null,
    val topMargin: Float = 0f,        // 0-1
    val columnMargin: Float = 0f,     // 0-1 (map to gap)
    val cornerRadius: Float = 0f,     // 0-1

    // Ratio tool
    val ratio: String? = null,        // e.g., "1:1", "4:3", "16:9"

    // Background tool
    val backgroundSelection: BackgroundSelection? = null,  // Current background selection (SOLID, PATTERN, GRADIENT)

    // Frame tool
    val frameSelection: FrameSelection? = null,

    // Text tool
    val texts: List<TextState> = emptyList(),

    // Sticker tool
    val stickers: List<StickerState> = emptyList(),
    val stickerBitmapPath: String? = null,  // Path của bitmap sau khi apply sticker

    // Other tools (mở rộng trong tương lai)
    val filter: String? = null,
    val blur: Float = 0f,
    val brightness: Float = 1f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
)

data class TextState(
    val id: String,
    val text: String,
    val x: Float,           // 0-1
    val y: Float,           // 0-1
    val fontSize: Float,
    val color: String,      // hex color
    val fontFamily: String? = null,
    val alignment: String = "center", // left, center, right
)

data class StickerState(
    val id: String,
    val stickerId: String,  // resource ID or URI
    val x: Float,           // 0-1
    val y: Float,           // 0-1
    val scale: Float = 1f,
    val rotation: Float = 0f,
)

