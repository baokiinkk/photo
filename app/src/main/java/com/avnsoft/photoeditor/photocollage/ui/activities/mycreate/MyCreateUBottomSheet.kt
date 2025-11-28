package com.avnsoft.photoeditor.photocollage.ui.activities.mycreate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.basesource.base.ui.image.LoadImage
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.clickableWithAlphaEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCreateUIBottomSheet(
    isVisible: Boolean,
    pathBitmap: String,
    onDismissRequest: () -> Unit,
    onClose: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
) {
    if (!isVisible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = null // We have our own handle in the design
    ) {
        MyCreateUIContentBottomSheet(
            onEdit = onEdit,
            onDelete = onDelete,
            onShare = onShare,
            onClose = onClose,
            pathBitmap = pathBitmap
        )
    }
}

@Composable
fun MyCreateUIContentBottomSheet(
    pathBitmap: String,
    onClose: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
) {
    val scrollState = rememberScrollState()
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
                .size(240.dp),
            shape = RoundedCornerShape(20.dp),
        ) {
            LoadImage(
                modifier = Modifier.fillMaxSize(),
                model = pathBitmap,
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quality Selection
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            MyCreateUIItem(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.edit),
                onClick = onEdit,
                resId = R.drawable.ic_edit_my_create
            )
            Spacer(modifier = Modifier.width(12.dp))
            MyCreateUIItem(
                title = stringResource(R.string.share),
                onClick = onShare,
                modifier = Modifier.weight(1f),
                resId = R.drawable.ic_share
            )
            Spacer(modifier = Modifier.width(12.dp))
            MyCreateUIItem(
                title = stringResource(R.string.delete),
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                resId = R.drawable.ic_delete_my_create
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun MyCreateUIItem(
    modifier: Modifier,
    resId: Int,
    title: String,
    onClick: () -> Unit,
    tintColor: Color = AppColor.Gray900
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = Color(0xFFF2F4F7),
                    shape = CircleShape
                )
                .clickableWithAlphaEffect(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            ImageWidget(
                resId = resId,
                modifier = Modifier
                    .size(24.dp),
                tintColor = tintColor
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = AppStyle.title3().medium().gray900(),
        )
    }
}