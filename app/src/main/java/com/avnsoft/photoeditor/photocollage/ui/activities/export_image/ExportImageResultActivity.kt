package com.avnsoft.photoeditor.photocollage.ui.activities.export_image

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.EditorActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.EditorInput
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.ui.theme.Gray100
import com.avnsoft.photoeditor.photocollage.ui.theme.Gray500
import com.avnsoft.photoeditor.photocollage.ui.theme.Gray900
import com.avnsoft.photoeditor.photocollage.ui.theme.PurpleLight
import com.avnsoft.photoeditor.photocollage.utils.FileUtil
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.ui.base.IScreenData
import com.basesource.base.ui.image.LoadImage
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.clickableWithAlphaEffect
import com.basesource.base.utils.launchActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

data class ExportImageData(
    val pathUriMark: String,
    val pathBitmapOriginal: String,
) : IScreenData

class ExportImageResultActivity : BaseActivity() {

    val screenInput: ExportImageData by lazy {
        intent.getInput()
    }

    private val viewmodel: ExportImageResultViewmodel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Scaffold(
                containerColor = Color.White
            ) { innerPadding ->

                ExportResultScreen(
                    onBackClick = { finish() },
                    onHomeClick = { finish() },
                    onNewEditClick = {
                        navigateToEdit()
                    },
                    onShareClick = {
                        shareImage()
                    },
                    onRemoveWatermarkClick = {
                        viewmodel.removeWatermarkClick()
                    },
                    onRemoveObjectClick = {

                    },
                    onAiEnhanceClick = {

                    },
                    onRemoveBgClick = {

                    },
                    viewmodel = viewmodel
                )
            }
        }
    }

    fun navigateToRemoveObject() {

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
                uri = screenInput.pathUriMark.toUri()
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
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        HeaderExportImageResult(
            onBackClick = onBackClick,
            onHomeClick = onHomeClick
        )
        // Image Preview (Collage)
        ImageCollagePreview(
            imageUrl = uiState.imageUrl
        )

        Spacer(modifier = Modifier.height(24.dp))

        // New Edit and Share Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton(
                text = "+ New Edit",
                onClick = onNewEditClick,
                backgroundColor = PurpleLight,
                textColor = Color(0xFF354CC4),
                modifier = Modifier.weight(1f)
            )

            ActionButton(
                text = "Share",
                onClick = onShareClick,
                backgroundColor = PurpleLight,
                textColor = Color(0xFF354CC4),
                icon = Icons.Default.Share,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Remove Watermark Button
        GradientButton(
            text = "✨ Remove Watermark",
            onClick = onRemoveWatermarkClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

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
            .padding(16.dp)
            .background(Color.White),
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
fun SavedStatusBadge() {
    Row(
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(1.dp, Gray100, RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(Color(0xFF4CAF50), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Success",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Saved to Device",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Gray900
        )
    }
}

@Composable
fun ImageCollagePreview(
    imageUrl: String?
) {
    Box(
        modifier = Modifier
            .size(240.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Gray100)
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        LoadImage(
            model = imageUrl,
            modifier = Modifier
                .fillMaxSize()
        )
    }
}

@Composable
fun CollageImagePlaceholder(
    modifier: Modifier = Modifier,
    color: Color
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(color)
    )
}

@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    textColor: Color,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
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
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF6425F3), Color(0xFF9C27B0))
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
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
            text = "AI Tools",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Gray900
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AIToolCard(
                title = "Remove Object",
                iconRes = R.drawable.ic_remove_object,
                onClick = onRemoveObjectClick,
                modifier = Modifier.weight(1f)
            )

            AIToolCard(
                title = "AI Enhance",
                iconRes = R.drawable.ic_ai_enhance,
                onClick = onAiEnhanceClick,
                modifier = Modifier.weight(1f)
            )

            AIToolCard(
                title = "Remove BG",
                iconRes = R.drawable.ic_background,
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
            .clip(RoundedCornerShape(16.dp))
            .background(Gray100)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Gray900,
            textAlign = TextAlign.Center
        )
    }
}