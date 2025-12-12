package com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_background

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.utils.FileUtil
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.ui.animation.LoadAnimation
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.ui.image.LoadImageUrl
import com.basesource.base.utils.clickableWithAlphaEffect
import com.basesource.base.utils.launchActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class RemoveBackgroundActivity : BaseActivity() {

    private val screenInput: ToolInput? by lazy {
        intent.getInput()
    }

    private val viewmodel: RemoveBackgroundViewModel by viewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel.initData(screenInput?.pathBitmap)
        observerData()
        setContent {
            Scaffold(
                containerColor = Color(0xFFF2F4F8)
            ) { inner ->
                val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
                Box {
                    LoadImageUrl(
                        modifier = Modifier.fillMaxSize(),
                        model = screenInput?.pathBitmap,
                        size = FileUtil.MAX_SIZE_FILE
                    )
//                    LoadImage(
//                        modifier = Modifier.fillMaxSize(),
//                        model = uiState.imageUrl,
//                    )
                    LoadingAnimation(
                        isShowLoading = uiState.isShowLoading,
                        content = stringResource(R.string.content_removing_object),
                        isCancel = true,
                        onCancel = {
                            finish()
                        }
                    )
//                    Button(
//                        modifier = Modifier.align(Alignment.BottomCenter),
//                        onClick = {
//                            val file = File(uiState.imageUrl)
//                            val uriString = Uri.fromFile(file).toString()
//                            returnToData(
//                                type = screenInput?.type ?: ToolInput.TYPE.NEW,
//                                pathUri = uriString,
//                                pathFile = uiState.imageUrl
//                            )
//                        }
//                    ) {
//                        Text("Go Editor")
//                    }
                }
            }
        }
    }

    private fun observerData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewmodel.removeBgState.collect { imageUrl ->
                    launchActivity(
                        toActivity = RemoveBackgroundResultActivity::class.java,
                        input = ToolInput(
                            pathBitmap = imageUrl,
                            isBackgroundTransparent = true,
                            type = screenInput?.type ?: ToolInput.TYPE.NEW
                        )
                    )
                    finish()

//                    val file = File(imageUrl)
//                    val uriString = Uri.fromFile(file).toString()
//                    when (screenInput?.type) {
//                        ToolInput.TYPE.NEW -> {
//
//                        }
//
//                        ToolInput.TYPE.BACK_AND_RETURN -> {
//
//                        }
//
//                        else -> {
//
//                        }
//                    }
                }
            }
        }
    }
}

@Composable
fun LoadingAnimation(
    isShowLoading: Boolean,
    content: String,
    isCancel: Boolean = false,
    onCancel: (() -> Unit)? = null
) {
    if (isShowLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickableWithAlphaEffect(enabled = false) {

                }
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LoadAnimation(
                    modifier = Modifier
                        .size(120.dp),
                    isShowDialog = true,
                    json = R.raw.anim_ai_loading
                )
//                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = content,
                    style = AppStyle.title2().semibold().white()
                )

                if (isCancel) {
                    Box(
                        modifier = Modifier
                            .padding(top = 20.dp)
                            .background(Color.Transparent)
                            .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                            .clickableWithAlphaEffect(onClick = onCancel)
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            text = stringResource(R.string.cancel),
                            style = AppStyle.buttonMedium().semibold().white(),
                        )
                    }
                }
            }
        }
    }
}
