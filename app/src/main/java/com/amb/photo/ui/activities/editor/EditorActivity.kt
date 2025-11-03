package com.amb.photo.ui.activities.editor

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.amb.photo.ui.activities.editor.crop.CropActivity
import com.amb.photo.ui.activities.editor.crop.CropInput
import com.amb.photo.ui.activities.editor.crop.PickImageFromGallery
import com.amb.photo.ui.activities.imagepicker.ImagePickerActivity
import com.basesource.base.components.CustomButton
import com.basesource.base.ui.base.BaseActivity

class EditorActivity : BaseActivity() {

    var pathBitmap: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Scaffold(
                containerColor = Color(0xFFF2F4F8)
            ) { inner ->

                Column(
                    modifier = Modifier.padding(inner),
                    verticalArrangement = Arrangement.Center
                ) {
                    CustomButton("crop") {
                        navigateTo(
                            startClazz = CropActivity::class.java,
                            input = CropInput(pathBitmap = pathBitmap)
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                    CustomButton("ImagePickerActivity") {
                        navigateTo(ImagePickerActivity::class.java)
                    }

                    PickImageFromGallery { uri ->
                        pathBitmap = uri.toString()
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

