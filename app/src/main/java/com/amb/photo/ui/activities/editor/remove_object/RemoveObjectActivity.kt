package com.amb.photo.ui.activities.editor.remove_object

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amb.photo.ui.activities.editor.crop.ToolInput
import com.amb.photo.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class RemoveObjectActivity : BaseActivity() {

    private val viewmodel: RemoveObjectViewModel by viewModel()

    private val screenInput: ToolInput? by lazy {
        intent.getInput()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel.setOriginalBitmap(
            bitmap = screenInput?.getBitmap(this),
            newPathBitmap = cacheDir.absolutePath + "/BitmapOriginal_For_remove_obj.jpeg"
        )

        setContent {
            Scaffold(
                containerColor = Color(0xFFF2F4F8)
            ) { inner ->

                val uiState by viewmodel.uiState.collectAsStateWithLifecycle()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF2F4F8))
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        uiState.bitmap?.let {
                            RemoveObjectComposeView(
                                modifier = Modifier.fillMaxSize(),
                                bitmapOrigin = uiState.bitmap,
                                onDrawView = {
//                                viewmodel.onDrawView()
                                },
                                onFinishDrawView = {
//                                viewmodel.onFinishDrawView(it)
                                },
                                eventViewClickObj = {
                                    Log.d("aaa","aaaaa")
                                }
                            )
                        }
                    }

                }
            }
        }
    }

}