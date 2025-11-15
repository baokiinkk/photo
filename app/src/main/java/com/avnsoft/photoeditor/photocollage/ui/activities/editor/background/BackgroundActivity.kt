package com.avnsoft.photoeditor.photocollage.ui.activities.editor.background

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.BackgroundSheet
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.BackgroundSelection
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.BackgroundLayer
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.toJson
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.roundToInt

class BackgroundActivity : BaseActivity() {

    private val viewmodel: BackgroundViewModel by viewModel()

    private val screenInput: ToolInput? by lazy {
        intent.getInput()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel.getConfigBackground(screenInput?.getBitmap(this))
        enableEdgeToEdge()

        setContent {
            Scaffold(
                containerColor = Color(0xFFF2F4F8)
            ) { inner ->
                val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
                var boxBounds by remember { mutableStateOf<Rect?>(null) }
                val localView = LocalView.current


                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = inner.calculateTopPadding(),
                            bottom = inner.calculateBottomPadding()
                        )
                ) {
                    // Background layer
                    BackgroundLayer(
                        backgroundSelection = uiState.backgroundSelection,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))
                        uiState.originBitmap?.let {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .aspectRatio(it.width / it.height.toFloat())
                                        .align(Alignment.Center)
                                        .graphicsLayer()
                                        .clipToBounds()
                                        .onGloballyPositioned { coords ->
                                            val position = coords.positionInRoot()
                                            val size = coords.size
                                            boxBounds = Rect(
                                                position.x.roundToInt(),
                                                position.y.roundToInt(),
                                                (position.x + size.width).roundToInt(),
                                                (position.y + size.height).roundToInt()
                                            )
                                        }
                                ) {
                                    Image(
                                        bitmap = it.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(36.dp))
                        BackgroundSheet(
                            selectedBackgroundSelection = uiState.backgroundSelection,
                            onBackgroundSelect = { _, selection ->
                                viewmodel.updateBackground(selection)
                            },
                            onClose = {
                                finish()
                            },
                            onConfirm = {
                                val output = BackgroundResult(
                                    backgroundSelection = uiState.backgroundSelection
                                )
                                val intent = Intent()
                                intent.putExtra("pathBitmap", output.toJson())
                                setResult(RESULT_OK, intent)
                                finish()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        )
                    }
                }
            }
        }
    }
}

data class BackgroundResult(
    val backgroundSelection: BackgroundSelection? = null,  // Current background selection
    val bgIcon: Int? = null
)