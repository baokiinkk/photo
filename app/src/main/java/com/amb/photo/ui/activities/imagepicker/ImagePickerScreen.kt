package com.amb.photo.ui.activities.imagepicker

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amb.photo.ui.activities.imagepicker.components.BucketSheet
import com.amb.photo.ui.activities.imagepicker.components.GalleryGrid
import com.amb.photo.ui.activities.imagepicker.components.PickerHeaderBar
import com.amb.photo.ui.activities.imagepicker.components.SelectedBar

@Composable
fun ImagePickerScreen(
    viewModel: ImagePickerViewModel = viewModel(),
    onDone: (List<Uri>) -> Unit,
    onCancel: () -> Unit,
) {
    val buckets by viewModel.buckets.collectAsState()
    val currentBucket by viewModel.currentBucket.collectAsState()
    val images by viewModel.images.collectAsState()
    val selected by viewModel.selected.collectAsState()

    var showSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadInitial() }

    Box(Modifier
        .fillMaxSize()
        .background(Color.White)) {
        Column(Modifier.fillMaxSize()) {
            PickerHeaderBar(
                folderName = currentBucket?.name ?: "Gallery",
                canNext = selected.isNotEmpty(),
                onBack = onCancel,
                onNext = { onDone(selected) },
                onFolderClick = { showSheet = true }
            )
            GalleryGrid(
                images = images,
                selected = selected,
                modifier = Modifier.weight(1f),
                onImageClick = { viewModel.toggleSelect(it) }
            )
            SelectedBar(
                selected = selected,
                onRemove = { uri -> viewModel.toggleSelect(GalleryImage(uri, currentBucket?.id ?: "", null, 0)) },
                onClearAll = { selected.toList().forEach { viewModel.toggleSelect(GalleryImage(it, currentBucket?.id ?: "", null, 0)) } }
            )
        }
        if (showSheet) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0x66000000)),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(Color.White)) {
                    BucketSheet(
                        buckets = buckets,
                        currentBucketId = currentBucket?.id,
                        onSelect = {
                            viewModel.setCurrentBucket(it)
                            showSheet = false
                        }
                    )
                }
            }
        }
    }
}
