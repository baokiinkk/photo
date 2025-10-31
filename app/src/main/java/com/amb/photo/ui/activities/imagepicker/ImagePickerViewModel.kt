package com.amb.photo.ui.activities.imagepicker

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Model folder/bucket của gallery
data class GalleryBucket(
    val id: String,
    val name: String,
    val thumbnail: Uri?,
    val count: Int
)

// Model ảnh
data class GalleryImage(
    val uri: Uri,
    val bucketId: String,
    val displayName: String?,
    val dateAdded: Long
)

class ImagePickerViewModel(app: Application) : AndroidViewModel(app) {
    // Danh sách bucket/folder
    private val _buckets = MutableStateFlow<List<GalleryBucket>>(emptyList())
    val buckets: StateFlow<List<GalleryBucket>> = _buckets

    // Bucket đang chọn
    private val _currentBucket = MutableStateFlow<GalleryBucket?>(null)
    val currentBucket: StateFlow<GalleryBucket?> = _currentBucket

    // Danh sách ảnh trong bucket
    private val _images = MutableStateFlow<List<GalleryImage>>(emptyList())
    val images: StateFlow<List<GalleryImage>> = _images

    // Danh sách URI ảnh được chọn
    private val _selected = MutableStateFlow<List<Uri>>(emptyList())
    val selected: StateFlow<List<Uri>> = _selected

    val MAX_SELECT = 10

    fun toggleSelect(img: GalleryImage) {
        val curr = _selected.value.toMutableList()
        if(curr.contains(img.uri)) {
            curr.remove(img.uri)
        } else if (curr.size < MAX_SELECT) {
            curr.add(img.uri)
        }
        _selected.value = curr
    }

    fun setCurrentBucket(bucket: GalleryBucket) {
        _currentBucket.value = bucket
        loadImages(bucketId = bucket.id)
    }

    fun loadInitial() {
        viewModelScope.launch(Dispatchers.IO) {
            val loadedBuckets = queryBuckets()
            _buckets.value = loadedBuckets
            // Chọn Recent (id "ALL") hoặc bucket đầu tiên
            val recent = GalleryBucket(id = "ALL", name = "Recent", thumbnail = loadedBuckets.firstOrNull()?.thumbnail, count = loadedBuckets.sumOf { it.count })
            _currentBucket.value = recent
            loadImages(bucketId = null) // Recent = tất cả ảnh
        }
    }

    private fun loadImages(bucketId: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            _images.value = queryImages(bucketId)
        }
    }

    private fun queryBuckets(): List<GalleryBucket> {
        val resolver = getApplication<Application>().contentResolver
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )
        val sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC"
        val map = LinkedHashMap<String, MutableList<Long>>()
        resolver.query(uri, projection, null, null, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val bucketIdCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val bucketNameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val mediaId = cursor.getLong(idCol)
                val bId = cursor.getString(bucketIdCol) ?: "unknown"
                map.getOrPut(bId) { mutableListOf() }.add(mediaId)
            }
        }
        val result = mutableListOf<GalleryBucket>()
        map.forEach { (bId, ids) ->
            val firstId = ids.firstOrNull()
            val thumb = firstId?.let { id -> ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id) }
            // name: query name for this bucket
            var name: String = "Unknown"
            val nameProjection = arrayOf(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            getApplication<Application>().contentResolver.query(uri, nameProjection, MediaStore.Images.Media.BUCKET_ID + "=?", arrayOf(bId), null)?.use { c ->
                if(c.moveToFirst()) {
                    val idx = c.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                    name = c.getString(idx) ?: "Unknown"
                }
            }
            result.add(GalleryBucket(id = bId, name = name, thumbnail = thumb, count = ids.size))
        }
        return result.sortedByDescending { it.count }
    }

    private fun queryImages(bucketId: String?): List<GalleryImage> {
        val resolver = getApplication<Application>().contentResolver
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED
        )
        val selection = if(bucketId == null || bucketId == "ALL") null else MediaStore.Images.Media.BUCKET_ID + "=?"
        val selectionArgs = if(bucketId == null || bucketId == "ALL") null else arrayOf(bucketId)
        val sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC"
        val list = mutableListOf<GalleryImage>()
        resolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val bucketIdCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val bId = cursor.getString(bucketIdCol) ?: ""
                val name = cursor.getString(nameCol)
                val date = cursor.getLong(dateCol)
                val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                list.add(GalleryImage(uri = contentUri, bucketId = bId, displayName = name, dateAdded = date))
            }
        }
        return list
    }
}
