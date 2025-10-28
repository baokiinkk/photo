package com.basesource.base.utils

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.core.app.ActivityOptionsCompat
import com.basesource.base.ui.base.ActivityResultManager
import com.basesource.base.ui.base.BaseActivity

/**
 * Extension functions để sử dụng ActivityResultManager một cách gọn gàng hơn
 */

/**
 * Extension function cho BaseActivity để launch activity với callback inline
 */
fun BaseActivity.launchActivity(
    intent: Intent,
    options: ActivityOptionsCompat? = null,
    callback: (ActivityResult) -> Unit
) {
    activityResultManager.launchActivity(intent, options, callback)
}

/**
 * Extension function cho BaseActivity để request permissions
 */
fun BaseActivity.requestPermissions(
    permissions: Array<String>,
    callback: (Map<String, Boolean>) -> Unit
) {
    activityResultManager.requestPermissions(permissions, callback)
}

/**
 * Extension function cho BaseActivity để request single permission
 */
fun BaseActivity.requestPermission(
    permission: String,
    callback: (Boolean) -> Unit
) {
    activityResultManager.requestPermission(permission, callback)
}

/**
 * Extension function cho BaseActivity để pick image
 */
fun BaseActivity.pickImage(
    mimeType: String = "image/*",
    callback: (Uri?) -> Unit
) {
    activityResultManager.pickImage(mimeType, callback)
}

/**
 * Extension function cho BaseActivity để take picture
 */
fun BaseActivity.takePicture(
    outputUri: Uri,
    callback: (Boolean) -> Unit
) {
    activityResultManager.takePicture(outputUri, callback)
}

/**
 * Extension function cho ComponentActivity để tạo ActivityResultManager nhanh
 * (Dành cho trường hợp không kế thừa từ BaseActivity)
 */
fun ComponentActivity.createActivityResultManager(): ActivityResultManager {
    return ActivityResultManager(this)
}
