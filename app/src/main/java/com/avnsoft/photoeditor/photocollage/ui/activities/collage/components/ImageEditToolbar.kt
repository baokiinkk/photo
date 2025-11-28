package com.avnsoft.photoeditor.photocollage.ui.activities.collage.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.basesource.base.utils.clickableWithAlphaEffect

enum class ImageEditAction {
    REPLACE, SWAP, CROP, ROTATE, FLIP_HORIZONTAL, FLIP_VERTICAL, DELETE
}

data class ImageEditToolItem(
    val action: ImageEditAction,
    @StringRes val label: Int,
    @DrawableRes val icon: Int
)

val imageEditTools = listOf(
    ImageEditToolItem(ImageEditAction.REPLACE, R.string.replace, R.drawable.ic_add_image),
    ImageEditToolItem(ImageEditAction.SWAP, R.string.swap, R.drawable.ic_refresh),
    ImageEditToolItem(ImageEditAction.CROP, R.string.crop, R.drawable.ic_crop),
    ImageEditToolItem(ImageEditAction.ROTATE, R.string.rotate, R.drawable.ic_rotate_left),
    ImageEditToolItem(ImageEditAction.FLIP_HORIZONTAL, R.string.horizontal, R.drawable.ic_flip_horizontal),
    ImageEditToolItem(ImageEditAction.FLIP_VERTICAL, R.string.vertical, R.drawable.ic_flip_vertical),
    ImageEditToolItem(ImageEditAction.DELETE, R.string.saved_to_device, R.drawable.ic_delete_image)
)

@Composable
fun ImageEditToolbar(
    modifier: Modifier = Modifier,
    onActionClick: (ImageEditAction) -> Unit = {},
    onClose: () -> Unit = {},
    disabledActions: Set<ImageEditAction> = emptySet()
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 12.dp)
    ) {
        imageEditTools.forEach { item ->
            ImageEditToolItem(
                item = item,
                onClick = { onActionClick(item.action) },
                enabled = !disabledActions.contains(item.action)
            )
        }
    }
}

@Composable
private fun ImageEditToolItem(
    item: ImageEditToolItem,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(65.dp)
            .then(
                if (enabled) {
                    Modifier.clickableWithAlphaEffect {
                        onClick.invoke()
                    }
                } else {
                    Modifier
                }
            )
    ) {
        Image(
            painterResource(item.icon),
            contentDescription = "",
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(
                if (enabled) AppColor.Gray900 else AppColor.Gray400
            )
        )
        Text(
            modifier = Modifier.padding(top = 2.dp),
            text = stringResource(item.label),
            style = AppStyle.caption2().medium().run {
                if (enabled) gray900() else gray400()
            }
        )
    }
}

