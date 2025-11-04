package com.basesource.base.ui.base

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat

/**
 * Manager để quản lý các ActivityResultLauncher một cách gọn gàng
 * Cho phép sử dụng callback ngay tại chỗ cần launch activity
 */
class ActivityResultManager(activity: ComponentActivity) {
    
    private val activityLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        activityCallback?.invoke(result)
    }
    
    private val permissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionCallback?.invoke(permissions)
    }
    
    private val singlePermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        singlePermissionCallback?.invoke(isGranted)
    }
    
    private val pickImageLauncher = activity.registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        pickImageCallback?.invoke(uri)
    }
    
    private val takePictureLauncher = activity.registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        takePictureCallback?.invoke(success)
    }
    
    // Callbacks
    private var activityCallback: ((ActivityResult) -> Unit)? = null
    private var permissionCallback: ((Map<String, Boolean>) -> Unit)? = null
    private var singlePermissionCallback: ((Boolean) -> Unit)? = null
    private var pickImageCallback: ((android.net.Uri?) -> Unit)? = null
    private var takePictureCallback: ((Boolean) -> Unit)? = null
    
    /**
     * Launch activity với callback cho kết quả
     */
    fun launchActivity(
        intent: Intent,
        options: ActivityOptionsCompat? = null,
        callback: ((ActivityResult) -> Unit)? = null
    ) {
        activityCallback = callback
        activityLauncher.launch(intent, options)
    }
    
    /**
     * Request multiple permissions với callback
     */
    fun requestPermissions(
        permissions: Array<String>,
        callback: (Map<String, Boolean>) -> Unit
    ) {
        permissionCallback = callback
        permissionLauncher.launch(permissions)
    }
    
    /**
     * Request single permission với callback
     */
    fun requestPermission(
        permission: String,
        callback: (Boolean) -> Unit
    ) {
        singlePermissionCallback = callback
        singlePermissionLauncher.launch(permission)
    }
    
    /**
     * Pick image từ gallery với callback
     */
    fun pickImage(
        mimeType: String = "image/*",
        callback: (android.net.Uri?) -> Unit
    ) {
        pickImageCallback = callback
        pickImageLauncher.launch(mimeType)
    }
    
    /**
     * Take picture với camera với callback
     */
    fun takePicture(
        outputUri: android.net.Uri,
        callback: (Boolean) -> Unit
    ) {
        takePictureCallback = callback
        takePictureLauncher.launch(outputUri)
    }
    
    /**
     * Clear tất cả callbacks để tránh memory leak
     */
    fun clearCallbacks() {
        activityCallback = null
        permissionCallback = null
        singlePermissionCallback = null
        pickImageCallback = null
        takePictureCallback = null
    }
}
