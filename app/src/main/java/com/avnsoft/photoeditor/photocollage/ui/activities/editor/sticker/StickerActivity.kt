package com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.view.drawToBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.FooterEditor
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.saveImage
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.EmojiTab
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.ui.theme.LoadingScreen
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.ui.image.LoadImage
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.clickableWithAlphaEffect
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.roundToInt

class StickerActivity : BaseActivity() {

    private val viewmodel: StickerViewModel by viewModel()

    private val screenInput: ToolInput? by lazy {
        intent.getInput()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel.getConfigSticker(screenInput?.getBitmap(this))
        enableEdgeToEdge()
        setContent {
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                uri?.let {
                    viewmodel.addStickerFromGallery(it.toString())
                }
            }
            Scaffold(
                containerColor = Color(0xFFF2F4F8)
            ) { inner ->
                val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
                var boxBounds by remember { mutableStateOf<Rect?>(null) }
                val context = LocalContext.current
                val localView = LocalView.current
                val coroutineScope = rememberCoroutineScope()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = inner.calculateTopPadding(),
                            bottom = inner.calculateBottomPadding()
                        )
                        .background(Color(0xFFF2F4F8))
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    uiState.originBitmap?.let {
                        Box(
                            Modifier
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
                                StickerViewCompose(
                                    modifier = Modifier.fillMaxSize(),
                                    input = uiState.pathSticker
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(46.dp))
                    StickerToolPanel(
                        modifier = Modifier.fillMaxWidth(),
                        uiState = uiState,
                        onTabSelected = {
                            viewmodel.selectedTab(it)
                        },
                        onStickerSelected = {
                            viewmodel.addStickerFromAsset(it)
                        },
                        onCancel = {
                            finish()
                        },
                        onApply = {
                            viewmodel.showLoading()
                            captureView(
                                localView,
                                callback = { bitmap ->
                                    bitmap ?: return@captureView
                                    val bounds = boxBounds ?: return@captureView
                                    val captured = Bitmap.createBitmap(
                                        bitmap,
                                        bounds.left,
                                        bounds.top,
                                        bounds.width(),
                                        bounds.height()
                                    )
                                    saveImage(
                                        context = this@StickerActivity,
                                        bitmap = captured,
                                        onImageSaved = { pathBitmap ->
                                            viewmodel.hideLoading()
                                            val intent = Intent()
                                            intent.putExtra("pathBitmap", "$pathBitmap")
                                            setResult(RESULT_OK, intent)
                                            finish()
                                        }
                                    )
                                }
                            )
                        },
                        onAddStickerFromGallery = {
                            launcher.launch("image/*")
                        }
                    )
                }
                if (uiState.isLoading) {
                    LoadingScreen()
                }
            }
        }
    }
}

fun captureView(view: View, callback: (Bitmap?) -> Unit) {
    if (view.width == 0 || view.height == 0) {
        callback(null)
        return
    }

    val bmp = createBitmap(view.width, view.height)
    val window = view.context.findActivity()?.window ?: return callback(null)

    val location = IntArray(2)
    view.getLocationInWindow(location)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        PixelCopy.request(
            window,
            Rect(location[0], location[1], location[0] + view.width, location[1] + view.height),
            bmp,
            { result ->
                if (result == PixelCopy.SUCCESS) callback(bmp)
                else callback(null)
            },
            Handler(Looper.getMainLooper())
        )
    } else {
        val bitmap = view.drawToBitmap()
        callback(bitmap)
    }
}

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Composable
fun StickerToolPanel(
    modifier: Modifier = Modifier,
    uiState: StickerUIState,
    onTabSelected: (EmojiTab) -> Unit,
    onCancel: () -> Unit,
    onApply: () -> Unit,
    onStickerSelected: (String) -> Unit,
    onAddStickerFromGallery: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Row(
                Modifier
                    .horizontalScroll(scrollState)
                    .padding(top = 8.dp, end = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ImageWidget(
                    resId = R.drawable.ic_add_image,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 4.dp)
                        .clickableWithAlphaEffect(onClick = onAddStickerFromGallery)
                )
                uiState.emojiTabs.forEach { tab ->
                    CategoryButton(
                        selected = uiState.currentTab == tab,
                        item = tab,
                        onClick = {
                            onTabSelected.invoke(tab)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(uiState.currentTab.items) { emoji ->
                    EmojiItem(
                        url = emoji,
                        onStickerSelected = onStickerSelected
                    )
                }
            }
        }
        FooterEditor(
            modifier = Modifier
                .fillMaxWidth(),
            title = stringResource(R.string.sticker),
            onCancel = onCancel,
            onApply = onApply
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun CategoryButton(
    selected: Boolean,
    item: EmojiTab,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                if (selected) AppColor.Gray100 else Color.White,
                RoundedCornerShape(16.dp)
            )
            .clickableWithAlphaEffect { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(item.tabIcon),
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun EmojiItem(url: String, onStickerSelected: (String) -> Unit) {
    LoadImage(
        model = "file:///android_asset/$url",
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(46.dp)
            .clickableWithAlphaEffect {
                onStickerSelected.invoke(url)
            }
    )
}
