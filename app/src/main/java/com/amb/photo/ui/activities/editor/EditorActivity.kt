package com.amb.photo.ui.activities.editor

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.amb.photo.ui.activities.editor.crop.CropActivity
import com.amb.photo.ui.activities.editor.crop.CropInput
import com.amb.photo.utils.getInput
import com.basesource.base.components.CustomButton
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.ui.base.IScreenData
import com.basesource.base.ui.image.LoadImage
import com.basesource.base.utils.launchActivity

data class EditorInput(
    val pathBitmap: String? = null
) : IScreenData

class EditorActivity : BaseActivity() {

    var pathBitmap: String? = null

    private val screenInput: EditorInput? by lazy {
        intent.getInput()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var pathBitmapResult by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(Unit) {
                pathBitmapResult = screenInput?.pathBitmap
            }
            Scaffold(
                containerColor = Color(0xFFF2F4F8)
            ) { inner ->
                Column(
                    modifier = Modifier.padding(inner),
                    verticalArrangement = Arrangement.Center
                ) {
                    CustomButton("crop") {
                        launchActivity(
                            toActivity = CropActivity::class.java,
                            input = CropInput(pathBitmap = pathBitmapResult),
                            callback = { result ->
                                if (result.resultCode == Activity.RESULT_OK) {
                                    val pathBitmap = result.data?.getStringExtra("pathBitmap")
                                    pathBitmapResult = pathBitmap
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    pathBitmapResult?.let {
                        val uri = it.toUri()
                        LoadImage(model = uri)
                    }
                }
            }
        }
    }
}

fun Uri.toBitmap(context: Context): Bitmap? {
    val bitmap = if (Build.VERSION.SDK_INT < 28) {
        MediaStore.Images.Media.getBitmap(
            context.contentResolver,
            this
        )
    } else {
        val source = ImageDecoder.createSource(
            context.contentResolver,
            this
        )
        ImageDecoder.decodeBitmap(source)
    }
    return bitmap
}

