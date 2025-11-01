package com.amb.photo.ui.activities.imagepicker

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.amb.photo.ui.theme.MainTheme
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.requestPermission

class ImagePickerActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (hasPermission()) {
            showPicker()
        } else {
            val perm = if (Build.VERSION.SDK_INT >= 33) android.Manifest.permission.READ_MEDIA_IMAGES else android.Manifest.permission.READ_EXTERNAL_STORAGE
            requestPermission(perm) {
                if (it) {
                    showPicker()
                } else {
                    finish()
                }
            }
        }
    }

    private fun hasPermission(): Boolean {
        val perm = if (Build.VERSION.SDK_INT >= 33) android.Manifest.permission.READ_MEDIA_IMAGES else android.Manifest.permission.READ_EXTERNAL_STORAGE
        return ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
    }

    private fun showPicker() {
        setContent {
            MainTheme {
                ImagePickerScreen(
                    onDone = { uris ->
                        val data = Intent().apply {
                            putParcelableArrayListExtra(RESULT_LIST, ArrayList(uris))
                        }
                        setResult(RESULT_OK, data)
                        finish()
                    },
                    onCancel = { finish() }
                )
            }
        }
    }

    companion object {
        const val RESULT_LIST = "selected_image_uris"
    }
}
