package com.amb.photo.ui.activities.collage.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.amb.photo.ui.activities.collage.CollageTemplates
import com.amb.photo.ui.activities.collage.CollageViewModel
import com.amb.photo.ui.theme.Background2
import com.amb.photo.ui.theme.BackgroundWhite

@Composable
fun CollageScreen(uris: List<Uri>, vm: CollageViewModel, onBack: () -> Unit) {
    var gap by remember { mutableStateOf(1.dp) }
    var corner by remember { mutableStateOf(1.dp) }
    var showGridsSheet by remember { mutableStateOf(false) }

    val options by vm.templates.collectAsState()
    val selected by vm.selected.collectAsState()

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
            CollageHeader(
                onBack = onBack,
                onUndo = { /* TODO */ },
                onRedo = { /* TODO */ },
                onSave = { /* TODO */ },
                canUndo = false,
                canRedo = false
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .padding(top = 80.dp, bottom = 175.dp)
                    .background(BackgroundWhite)
                    .padding(8.dp)

            ) {

                val templateToUse = selected ?: options.firstOrNull() ?: CollageTemplates.defaultFor(uris.size.coerceAtLeast(1))
                CollagePreview(
                    images = uris,
                    template = templateToUse,
                    gap = gap,
                    corner = corner,
                )
            }
            // Bottom tools
            CollageBottomTools(
                onToolClick = { tool ->
                    when (tool) {
                        CollageTool.GRIDS -> {
                            showGridsSheet = true
                        }

                        else -> {
                            showGridsSheet = false
                        }
                    }
                }
            )
        }

        // Grids Sheet (hiện khi click Grids tool)
        if (showGridsSheet) {
            GridsSheet(
                    templates = options,
                    selectedTemplate = selected,
                    onTemplateSelect = { template ->
                        vm.select(template)
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
            CollageHeader(
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
            CollageBottomTools(
                onToolClick = {}
            )
        }
    }
}