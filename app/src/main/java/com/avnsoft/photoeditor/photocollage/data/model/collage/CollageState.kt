package com.avnsoft.photoeditor.photocollage.data.model.collage

import android.graphics.Bitmap
import android.net.Uri
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.BackgroundSelection
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.FrameSelection
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.preview.ImageTransformState
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.TextStickerUIState
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.Sticker
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.FreeStyleStickerView

/**
 * State object chứa toàn bộ state của collage editor
 * Sử dụng cho undo/redo functionality
 */
data class CollageState(
    // Layout & Margin
    val templateId: CollageTemplate? = null,
    val topMargin: Float = 0f,        // 0-1
    val columnMargin: Float = 0f,     // 0-1 (map to gap)
    val cornerRadius: Float = 0f,     // 0-1

    // Image URIs - danh sách ảnh trong collage (URIs ban đầu)
    val imageUris: List<Uri> = emptyList(),
    
    // Image Bitmaps - lưu bitmap trực tiếp sau khi transform
    val imageBitmaps: Map<Int, Bitmap> = emptyMap(),  // Map<imageIndex, Bitmap>

    // Ratio tool
    val ratio: Pair<Int, Int>? = null,        // e.g., "1:1", "4:3", "16:9"

    // Background tool
    val backgroundSelection: BackgroundSelection? = null,  // Current background selection (SOLID, PATTERN, GRADIENT)

    // Frame tool
    val frameSelection: FrameSelection? = null,
    val stickerView: FreeStyleStickerView? = null,

    // Text tool
    val texts: List<TextState> = emptyList(),

    // Sticker tool - lưu danh sách Sticker objects để restore khi undo/redo
    val stickerList: List<Sticker> = emptyList(),
    val stickers: List<StickerState> = emptyList(),
    val stickerBitmapPath: String? = null,  // Path của bitmap sau khi apply sticker

    // Image transform tool (zoom & pan)
    val imageTransforms: Map<Int, ImageTransformState> = emptyMap(),  // Map<imageIndex, ImageTransformState>

    // Other tools (mở rộng trong tương lai)
    val filter: String? = null,
    val blur: Float = 0f,
    val brightness: Float = 1f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val textState: TextStickerUIState? = null,
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

