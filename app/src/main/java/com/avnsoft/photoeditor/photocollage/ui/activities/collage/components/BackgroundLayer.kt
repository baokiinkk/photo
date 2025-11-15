package com.avnsoft.photoeditor.photocollage.ui.activities.collage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.toColorInt
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.avnsoft.photoeditor.photocollage.ui.theme.BackgroundWhite
import com.avnsoft.photoeditor.photocollage.utils.getPatternImageUri
import com.avnsoft.photoeditor.photocollage.utils.loadPatternAssetPainter

/**
 * Composable để render background layer dựa trên BackgroundSelection
 * - SOLID: Box với background color
 * - PATTERN: AsyncImage load pattern image
 * - GRADIENT: TODO
 */
@Composable
fun BackgroundLayer(
    backgroundSelection: BackgroundSelection?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Get background color (default white if null or not Solid)
    val bgColor = remember(backgroundSelection) {
        when (val selection = backgroundSelection) {
            is BackgroundSelection.Solid -> {
                try {
                    Color(selection.color.toColorInt())
                } catch (e: Exception) {
                    BackgroundWhite
                }
            }
            else -> BackgroundWhite
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        when (val selection = backgroundSelection) {
            is BackgroundSelection.Pattern -> {
                val imageUri = remember(selection.urlRoot, selection.item.urlThumb) {
                    getPatternImageUri(selection.urlRoot, selection.item.urlThumb)
                }
                val fallbackPainter = remember(selection.item.name) {
                    loadPatternAssetPainter(context, selection.item.name)
                }
                
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUri)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    error = fallbackPainter,
                    placeholder = fallbackPainter,
                    modifier = Modifier.fillMaxSize()
                )
            }
            is BackgroundSelection.Gradient -> {
                // TODO: Implement gradient background
            }
            else -> {
                // Solid color or null - already handled by bgColor background
            }
        }
    }
}

