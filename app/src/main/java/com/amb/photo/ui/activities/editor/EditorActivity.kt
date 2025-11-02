package com.amb.photo.ui.activities.editor

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.basesource.base.components.CustomButton
import com.basesource.base.ui.base.BaseActivity

class EditorActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Scaffold(
                containerColor = Color(0xFFF2F4F8)
            ) { inner ->

                Column(
                    modifier = Modifier.padding(inner)
                ) {
                    CustomButton("crop") {
                        startScreen(CropActivityActivity::class.java)
                    }
                }
            }
        }
    }
}


