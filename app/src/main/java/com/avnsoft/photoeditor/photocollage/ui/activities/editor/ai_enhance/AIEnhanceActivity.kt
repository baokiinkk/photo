package com.avnsoft.photoeditor.photocollage.ui.activities.editor.ai_enhance

import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_background.LoadingAnimation
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_background.returnToData
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.ui.image.LoadImage
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import kotlin.getValue

class AIEnhanceActivity : BaseActivity() {

    private val screenInput: ToolInput? by lazy {
        intent.getInput()
    }
    private val viewmodel: AIEnhanceViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel.initData(screenInput?.pathBitmap)
        setContent {
            Scaffold(
                containerColor = Color(0xFFF2F4F8)
            ) { inner ->
                val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
                Box {
                    LoadImage(
                        modifier = Modifier.fillMaxSize(),
                        model = uiState.imageUrl
                    )
                    LoadingAnimation(
                        isShowLoading = uiState.isShowLoading,
                        content = stringResource(R.string.content_magic_ai_is_enhancing),
                        isCancel = true,
                        onCancel = {
                            finish()
                        }
                    )
                    Button(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        onClick = {
                            val file = File(uiState.imageUrl)
                            val uriString = Uri.fromFile(file).toString()
                            returnToData(
                                type = screenInput?.type ?: ToolInput.TYPE.NEW,
                                pathUri = uriString,
                                pathFile = uiState.imageUrl
                            )
                        }
                    ) {
                        Text("Go Editor")
                    }
                }
            }
        }
    }
}