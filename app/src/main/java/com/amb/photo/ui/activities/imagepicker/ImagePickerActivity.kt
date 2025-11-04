package com.amb.photo.ui.activities.imagepicker

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import com.amb.photo.ui.theme.MainTheme
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.ui.base.IScreenData
import com.basesource.base.utils.fromJson
import com.basesource.base.utils.requestPermission
import com.basesource.base.utils.toJson

class ImagePickerActivity : BaseActivity() {
    val args: ImageRequest? by lazy { intent.getStringExtra("arg")?.fromJson() }

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
                    dataRequest = args,
                    onDone = { uris ->
                        val data = Intent().apply {
                            if (uris.size > 1) {
                                putExtra(RESULT_URI, uris.map { it.path }.toJson())
                            } else {
                                putExtra(RESULT_URI, uris.firstOrNull()?.path?.toJson())
                            }
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
        const val RESULT_URI = "RESULT_URI"
    }
}

data class ImageRequest(
    val type: TypeSelect = TypeSelect.MULTI
) : IScreenData

enum class TypeSelect {
    MULTI,
    SINGLE
}
