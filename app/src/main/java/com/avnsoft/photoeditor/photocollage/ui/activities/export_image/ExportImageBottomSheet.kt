package com.avnsoft.photoeditor.photocollage.ui.activities.export_image

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.ui.theme.Gray100
import com.basesource.base.ui.image.LoadImage
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.clickableWithAlphaEffect

enum class Quality(val value: Int) {
    LOW(60), MEDIUM(90), HIGH(100), NONE(0)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeApi::class)
@Composable
fun ExportImageBottomSheet(
    pathBitmap: String,
    onDismissRequest: () -> Unit,
    onDownload: (Quality) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = null // We have our own handle in the design
    ) {
        ExportImageScreen(
            onDownload = onDownload,
            onClose = onDismissRequest,
            pathBitmap = pathBitmap
        )
    }
}

@Composable
fun ExportImageScreen(
    pathBitmap: String,
    onDownload: (Quality) -> Unit,
    onClose: () -> Unit,
) {
    var selectedQuality by remember { mutableStateOf(Quality.NONE) }

    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onDownload(selectedQuality)
        } else {
            Toast.makeText(context, "Denied!", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with Close Button
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(4.dp)
                    .background(Color(0xFFD0D5DD), CircleShape)
                    .align(Alignment.TopCenter)
            )

            ImageWidget(
                resId = R.drawable.ic_close_black,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clickableWithAlphaEffect(onClick = onClose)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Image Preview (Placeholder)
        Card(
            modifier = Modifier
                .size(220.dp),
            shape = RectangleShape,
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            LoadImage(
                modifier = Modifier.fillMaxSize(),
                model = pathBitmap,
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = stringResource(R.string.photo_quality),
            style = AppStyle.title1().bold().Color_101828()
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Subtitle
        Text(
            text = stringResource(R.string.pick_your_preferred_download_resolution),
            style = AppStyle.title3().medium().gray500(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Quality Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QualityOption(
                text = stringResource(R.string.low),
                isSelected = selectedQuality == Quality.LOW,
                onClick = { selectedQuality = Quality.LOW },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            QualityOption(
                text = stringResource(R.string.medium),
                isSelected = selectedQuality == Quality.MEDIUM,
                onClick = { selectedQuality = Quality.MEDIUM },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            QualityOption(
                text = stringResource(R.string.high),
                isSelected = selectedQuality == Quality.HIGH,
                onClick = { selectedQuality = Quality.HIGH },
                isPro = true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Download Button

        Button(
            enabled = selectedQuality != Quality.NONE,
            onClick = {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    onDownload(selectedQuality)
                } else {
                    launcher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedQuality != Quality.NONE) {
                    Color(0xFF6425F3)
                } else {
                    Color(0xFFF2F4F7)
                }
            ),
            contentPadding = PaddingValues(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.download),
                    style = if (selectedQuality != Quality.NONE) {
                        AppStyle.buttonLarge().semibold().white()
                    } else {
                        AppStyle.buttonLarge().semibold().gray300()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun QualityOption(
    modifier: Modifier = Modifier,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isPro: Boolean = false,
) {
    val borderColor = if (isSelected) AppColor.Primary500 else Color.Transparent
    val backgroundColor = if (isSelected) Color(0xFFE6E2FD) else Gray100
    val style =
        if (isSelected)
            AppStyle.title3().semibold().primary500()
        else
            AppStyle.title3().semibold().gray800()

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickableWithAlphaEffect { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = style
        )

        if (isPro) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp)
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                ImageWidget(
                    resId = R.drawable.button_pro,
                    modifier = Modifier
                        .width(30.dp)
                        .height(16.dp)
                )
            }
        }
    }
}
