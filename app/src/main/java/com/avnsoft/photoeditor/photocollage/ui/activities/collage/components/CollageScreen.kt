package com.avnsoft.photoeditor.photocollage.ui.activities.collage.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.CollageTemplates
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.CollageViewModel
import com.avnsoft.photoeditor.photocollage.ui.theme.Background2
import com.avnsoft.photoeditor.photocollage.ui.theme.BackgroundWhite

@Composable
fun CollageScreen(uris: List<Uri>, vm: CollageViewModel, onBack: () -> Unit) {
    // Observe state from ViewModel
    val templates by vm.templates.collectAsState()
    val selected by vm.selected.collectAsState()
    val collageState by vm.collageState.collectAsState()
    val canUndo by vm.canUndo.collectAsState()
    val canRedo by vm.canRedo.collectAsState()

    var showGridsSheet by remember { mutableStateOf(false) }
    var showRatioSheet by remember { mutableStateOf(false) }
    var showBackgroundSheet by remember { mutableStateOf(false) }

    // Extract values from state
    val topMargin = collageState.topMargin
    val columnMargin = collageState.columnMargin
    val cornerRadius = collageState.cornerRadius
    val ratio = collageState.ratio

    LaunchedEffect(Unit) {
        vm.load(uris.size.coerceAtLeast(1))
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Background2)
    ) {
        Column(Modifier.fillMaxSize()) {
            // Header
            FeaturePhotoHeader(
                onBack = onBack,
                onUndo = { vm.undo() },
                onRedo = { vm.redo() },
                onSave = { /* TODO */ },
                canUndo = canUndo && !showGridsSheet && !showRatioSheet,
                canRedo = canRedo && !showGridsSheet && !showRatioSheet
            )
            // Calculate aspect ratio from ratio string (e.g., "1:1" -> 1.0, "4:5" -> 0.8)
            val aspectRatioValue = remember(ratio) {
                when (ratio) {
                    "Original" -> null // No aspect ratio constraint for Original
                    "1:1" -> 1f
                    "4:5" -> 4f / 5f
                    "5:4" -> 5f / 4f
                    "3:4" -> 3f / 4f
                    else -> null
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .padding(top = 80.dp, bottom = 175.dp)
                    .then(
                        if (aspectRatioValue != null) {
                            Modifier.aspectRatio(aspectRatioValue)
                        } else {
                            Modifier
                        }
                    )
                    .background(BackgroundWhite)

            ) {
                val templateToUse = selected ?: templates.firstOrNull() ?: CollageTemplates.defaultFor(uris.size.coerceAtLeast(1))
                // Map slider values to Dp
                val gapValue = (1 + columnMargin * 19).dp // columnMargin: 0-1 -> gap: 1-20dp
                val cornerValue = (1 + cornerRadius * 19).dp // cornerRadius: 0-1 -> corner: 1-20dp
                
                CollagePreview(
                    images = uris,
                    template = templateToUse,
                    gap = gapValue,
                    corner = cornerValue,
                    backgroundColor = collageState.backgroundColor
                )
            }
            // Bottom tools
            FeatureBottomTools(
                tools = toolsCollage,
                onToolClick = { tool ->
                    when (tool) {
                        CollageTool.GRIDS -> {
                            showGridsSheet = true
                            showRatioSheet = false
                            showBackgroundSheet = false
                        }
                        CollageTool.RATIO -> {
                            showRatioSheet = true
                            showGridsSheet = false
                            showBackgroundSheet = false
                        }
                        CollageTool.BACKGROUND -> {
                            showBackgroundSheet = true
                            showGridsSheet = false
                            showRatioSheet = false
                        }
                        else -> {
                            showGridsSheet = false
                            showRatioSheet = false
                            showBackgroundSheet = false
                        }
                    }
                }
            )
        }

        // Grids Sheet (hiện khi click Grids tool)
        if (showGridsSheet) {
            GridsSheet(
                templates = templates,
                selectedTemplate = selected,
                onTemplateSelect = { template ->
                    vm.selectTemplate(template)
                },
                onClose = { showGridsSheet = false },
                onConfirm = { tab ->
                    vm.confirmGridsChanges(tab)
                    showGridsSheet = false
                },
                topMargin = topMargin,
                onTopMarginChange = { vm.updateTopMargin(it) },
                columnMargin = columnMargin,
                onColumnMarginChange = { vm.updateColumnMargin(it) },
                cornerRadius = cornerRadius,
                onCornerRadiusChange = { vm.updateCornerRadius(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.BottomCenter)
            )
        }

        // Ratio Sheet (hiện khi click Ratio tool)
        if (showRatioSheet) {
            RatioSheet(
                selectedRatio = ratio,
                onRatioSelect = { aspect ->
                    vm.updateRatio(aspect.label)
                },
                onClose = {
                    vm.cancelRatioChanges()
                    showRatioSheet = false
                },
                onConfirm = {
                    vm.confirmRatioChanges()
                    showRatioSheet = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.BottomCenter)
            )
        }

        if (showBackgroundSheet) {
            BackgroundSheet(
                selectedColor = collageState.backgroundColor,
                onColorSelect = { color ->
                    vm.updateBackgroundColor(color)
                },
                onClose = {
                    vm.cancelBackgroundChanges()
                    showBackgroundSheet = false
                },
                onConfirm = {
                    vm.confirmBackgroundChanges()
                    showBackgroundSheet = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun CollageScreenPreview() {
    // Mock ViewModel state cho preview
    val mockUris = listOf(Uri.EMPTY, Uri.EMPTY, Uri.EMPTY)

    // Preview đơn giản không dùng ViewModel
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF2C2C2E))
    ) {
        Column(Modifier.fillMaxSize()) {
            // Header
            FeaturePhotoHeader(
                onBack = {},
                onUndo = {},
                onRedo = {},
                onSave = {},
                canUndo = false,
                canRedo = false
            )

            // Preview area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                val templateToUse = CollageTemplates.LEFT_BIG_RIGHT_2
                CollagePreview(
                    images = mockUris,
                    template = templateToUse,
                    gap = 6.dp,
                    corner = 12.dp,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Bottom tools
            FeatureBottomTools(
                tools = toolsCollage,
                onToolClick = {}
            )
        }
    }
}