package com.avnsoft.photoeditor.photocollage.ui.activities.export_image

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.EditorActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.EditorInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.ai_enhance.AIEnhanceActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.copyImageToAppStorage
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_background.RemoveBackgroundActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object.RemoveObjectActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.main.MainActivity
import com.avnsoft.photoeditor.photocollage.ui.dialog.NetworkDialog
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.network.NetworkUtils.isNetworkAvailable
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.ui.base.IScreenData
import com.basesource.base.ui.image.LoadImage
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.clickableWithAlphaEffect
import com.basesource.base.utils.figmaShadow
import com.basesource.base.utils.launchActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

data class ExportImageData(
    val pathUriMark: String?,
    val pathBitmapOriginal: String,
    val quality: Quality
) : IScreenData

class ExportImageResultActivity : BaseActivity() {

    val screenInput: ExportImageData by lazy {
        intent.getInput()
    }

    private val viewmodel: ExportImageResultViewmodel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel.initData(screenInput)
        setContent {
            Scaffold(
                containerColor = Color.White
            ) { innerPadding ->
                val networkUIState by viewmodel.networkUIState.collectAsStateWithLifecycle()

                ExportResultScreen(
                    onBackClick = { finish() },
                    onHomeClick = {
                        MainActivity.newScreen(this)
                    },
                    onNewEditClick = {
                        navigateToEdit()
                    },
                    onShareClick = {
                        shareImage()
                    },
                    onRemoveWatermarkClick = {
                        viewmodel.removeWatermarkClick(screenInput.quality)
                    },
                    onRemoveObjectClick = {
                        navigateToRemoveObject()
                    },
                    onAiEnhanceClick = {
                        navigateToAIEnhance()
                    },
                    onRemoveBgClick = {
                        navigateToRemoveBackground()
                    },
                    viewmodel = viewmodel
                )

                if (networkUIState.showNetworkDialog) {
                    NetworkDialog(
                        onClose = {
                            viewmodel.hideNetworkDialog()
                        }
                    )
                }
            }
        }
    }

    fun navigateToRemoveObject() {
        if (isNetworkAvailable(this)) {
            lifecycleScope.launch {
                if (viewmodel.isMark()) {
                    val uri = screenInput.pathUriMark?.toUri() ?: return@launch
//                    val pathBitmap = copyImageToAppStorage(this@ExportImageResultActivity, uri)
                    launchActivity(
                        toActivity = RemoveObjectActivity::class.java,
                        input = EditorInput(pathBitmap = uri.toString()),
                    )
                } else {
                    val file = File(screenInput.pathBitmapOriginal)
                    val uri = file.toUri()
                    launchActivity(
                        toActivity = RemoveObjectActivity::class.java,
                        input = ToolInput(
                            pathBitmap = uri.toString()
                        ),
                    )
                }
            }
        } else {
            viewmodel.showNetworkDialog()
        }
    }

    fun navigateToAIEnhance() {
        if (isNetworkAvailable(this)) {
            lifecycleScope.launch {
                if (viewmodel.isMark()) {
                    val uri = screenInput.pathUriMark?.toUri()
//                    val pathBitmap = copyImageToAppStorage(this@ExportImageResultActivity, uri)
                    launchActivity(
                        toActivity = AIEnhanceActivity::class.java,
                        input = EditorInput(pathBitmap = uri.toString()),
                    )
                } else {
                    val file = File(screenInput.pathBitmapOriginal)
                    val uri = file.toUri()
                    launchActivity(
                        toActivity = AIEnhanceActivity::class.java,
                        input = ToolInput(pathBitmap = uri.toString()),
                    )
                }
            }
        } else {
            viewmodel.showNetworkDialog()
        }
    }

    fun navigateToRemoveBackground() {
        if (isNetworkAvailable(this)) {
            lifecycleScope.launch {
                if (viewmodel.isMark()) {
                    val uri = screenInput.pathUriMark?.toUri()
//                    val pathBitmap = copyImageToAppStorage(this@ExportImageResultActivity, uri)
                    launchActivity(
                        toActivity = RemoveBackgroundActivity::class.java,
                        input = EditorInput(pathBitmap = uri.toString()),
                    )
                } else {
                    val file = File(screenInput.pathBitmapOriginal)
                    val uri = file.toUri()
                    launchActivity(
                        toActivity = RemoveBackgroundActivity::class.java,
                        input = ToolInput(pathBitmap = uri.toString()),
                    )
                }
            }
        } else {
            viewmodel.showNetworkDialog()
        }
    }


    fun navigateToEdit() {
        if (viewmodel.isMark()) {
            launchActivity(
                toActivity = EditorActivity::class.java,
                input = EditorInput(pathBitmap = screenInput.pathUriMark),
            )
        } else {
            val file = File(screenInput.pathBitmapOriginal)
            val uri = file.toUri()
            launchActivity(
                toActivity = EditorActivity::class.java,
                input = EditorInput(pathBitmap = uri.toString()),
            )
        }
    }

    fun shareImage() {
        if (viewmodel.isMark()) {
            shareFile(
                uri = screenInput.pathUriMark?.toUri()
            )
        } else {
            val file = File(screenInput.pathBitmapOriginal)
            val uri = file.toUri()
            shareFile(
                uri = uri
            )
        }
    }

}

fun BaseActivity.shareFile(uri: Uri?) {
    if (uri == null) return
    val share = Intent(Intent.ACTION_SEND)
    share.type = "image/*"
    share.putExtra(Intent.EXTRA_STREAM, uri)
    val intent = Intent.createChooser(
        share,
        null
    )
    startActivity(intent)
}

@Composable
fun ExportResultScreen(
    modifier: Modifier = Modifier,
    viewmodel: ExportImageResultViewmodel,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onNewEditClick: () -> Unit,
    onShareClick: () -> Unit,
    onRemoveWatermarkClick: () -> Unit,
    onRemoveObjectClick: () -> Unit,
    onAiEnhanceClick: () -> Unit,
    onRemoveBgClick: () -> Unit,
) {
    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .background(AppColor.backgroundAppColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        HeaderExportImageResult(
            onBackClick = onBackClick,
            onHomeClick = onHomeClick
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Image Preview (Collage)
        ImageCollagePreview(
            imageUrl = uiState.imageUrl
        )

        Spacer(modifier = Modifier.height(20.dp))

        // New Edit and Share Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton(
                iconRes = R.drawable.ic_add_24_24,
                text = stringResource(R.string.new_edit),
                onClick = onNewEditClick,
                modifier = Modifier.weight(1f)
            )

            ActionButton(
                text = stringResource(R.string.share),
                iconRes = R.drawable.ic_share,
                onClick = onShareClick,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Remove Watermark Button
        GradientButton(
            text = stringResource(R.string.remove_watermark),
            onClick = onRemoveWatermarkClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // AI Tools Section
        AIToolsSection(
            onRemoveObjectClick = onRemoveObjectClick,
            onAiEnhanceClick = onAiEnhanceClick,
            onRemoveBgClick = onRemoveBgClick
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun HeaderExportImageResult(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ImageWidget(
            resId = R.drawable.ic_arrow_left,
            modifier = Modifier.clickableWithAlphaEffect(onClick = onBackClick)
        )

        Spacer(modifier = Modifier.weight(1f))
        ImageWidget(resId = R.drawable.ic_check)

        Text(
            text = stringResource(R.string.saved_to_device),
            modifier = Modifier
                .padding(start = 8.dp),
            style = AppStyle.title2().bold().Color_101828()
        )
        Spacer(modifier = Modifier.weight(1f))

        ImageWidget(
            resId = R.drawable.ic_home,
            modifier = Modifier.clickableWithAlphaEffect(onClick = onHomeClick)
        )
    }
}

@Composable
fun ImageCollagePreview(
    imageUrl: String?
) {
    Box(
        modifier = Modifier
            .size(220.dp)
            .figmaShadow(
                color = Color(0x0000000D),
                alpha = 0.15f,
                cornerRadius = 0.dp,
                x = 0.dp,
                y = 0.98.dp,
                blur = 9.76.dp
            )
    ) {
        LoadImage(
            modifier = Modifier.fillMaxSize(),
            model = imageUrl,
            contentScale = ContentScale.Crop
        )
    }
//    Card(
//        modifier = Modifier
//            .size(220.dp),
//        shape = RectangleShape,
//        elevation = CardDefaults.cardElevation(
//            defaultElevation = 4.dp
//        )
//    ) {
//        LoadImage(
//            modifier = Modifier.fillMaxSize(),
//            model = imageUrl,
//            contentScale = ContentScale.Crop
//        )
//    }
}

@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    iconRes: Int,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFEEECFE)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ImageWidget(
                resId = iconRes
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                modifier = Modifier
                    .basicMarquee(),
                text = text,
                style = AppStyle.buttonMedium().semibold().primary500(),
                maxLines = 1
            )
        }
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .fillMaxWidth()
//            .figmaShadow(
//                color = Color(0xFF6425F3),
//                alpha = 0.4f,
//                cornerRadius = 12.dp,
//                effects = Effects.DROP_SHADOW
//            )
            .clickableWithAlphaEffect(onClick = onClick)
    ) {
        ImageWidget(
            resId = R.drawable.button_remove_watermark,
            modifier = Modifier
                .fillMaxWidth()
                .height(69.11.dp),
        )
        Row(
            modifier = modifier
                .height(48.dp)
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            ImageWidget(resId = R.drawable.ic_store_star)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = AppStyle.buttonLarge().semibold().white(),
            )
        }
    }
}

@Composable
fun AIToolsSection(
    onRemoveObjectClick: () -> Unit,
    onAiEnhanceClick: () -> Unit,
    onRemoveBgClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.ai_tools),
            style = AppStyle.title1().bold().Color_101828()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AIToolCard(
                title = stringResource(R.string.remove_object),
                iconRes = R.drawable.ic_remove_object,
                onClick = onRemoveObjectClick,
                modifier = Modifier
                    .weight(1f)
            )

            AIToolCard(
                title = stringResource(R.string.ai_enhance),
                iconRes = R.drawable.ic_enhance_yellow,
                onClick = onAiEnhanceClick,
                modifier = Modifier.weight(1f)
            )

            AIToolCard(
                title = stringResource(R.string.remove_bg),
                iconRes = R.drawable.ic_remove_bg_green,
                onClick = onRemoveBgClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun AIToolCard(
    title: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .figmaShadow(
                color = Color(0x0000000D),
                alpha = 0.05f,
                cornerRadius = 12.dp,
                x = 0.dp,
                y = 0.dp,
                blur = 20.dp
            )
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(16.dp)
            .clickableWithAlphaEffect(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = title,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            modifier = Modifier
                .basicMarquee(),
            text = title,
            style = AppStyle.caption1().semibold().Color_1D2939(),
            maxLines = 1,
        )
    }
}