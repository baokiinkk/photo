package com.avnsoft.photoeditor.photocollage.ui.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.ui.theme.BackgroundWhite
import com.avnsoft.photoeditor.photocollage.ui.theme.Primary500
import com.basesource.base.utils.clickableWithAlphaEffect

@Composable
fun DeleteImageDialog(
    isVisible: Boolean,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: (() -> Unit)? = null,
) {
    if (!isVisible) return

    Dialog(
        onDismissRequest = {
            onDismiss?.invoke()
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = BackgroundWhite,
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Background image
                Image(
                    painter = painterResource(R.drawable.bg_popup_delete),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, end = 12.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.close),
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.TopEnd)
                            .clickableWithAlphaEffect {
                                onCancel()
                            }
                    )
                }

            }
            Text(
                text = stringResource(R.string.delete_image_title),
                style = AppStyle.title1().bold().gray900(),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Message
            Text(
                text = stringResource(R.string.delete_image_message),
                style = AppStyle.body1().medium().gray500(),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cancel button (gray)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF2F4F7))
                        .clickableWithAlphaEffect {
                            onCancel()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = AppStyle.buttonMedium().bold().gray800()
                    )
                }

                // Delete button (purple/red)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Primary500)
                        .clickableWithAlphaEffect(onClick = onDelete),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.delete),
                        style = AppStyle.buttonMedium().bold().white()
                    )
                }
            }
        }
    }
}

