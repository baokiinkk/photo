package com.avnsoft.photoeditor.photocollage.ui.activities.collage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.ui.theme.BackgroundWhite
import com.avnsoft.photoeditor.photocollage.ui.theme.Gray300
import com.avnsoft.photoeditor.photocollage.ui.theme.Gray900
import com.avnsoft.photoeditor.photocollage.ui.theme.Primary500
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.clickableWithAlphaEffect

@Composable
fun FeaturePhotoHeader(
    onBack: (() -> Unit)? = null,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSave: () -> Unit,
    canUndo: Boolean = false,
    canRedo: Boolean = false,
    canSave: Boolean = false,
    type: TEXT_TYPE = TEXT_TYPE.ROUND,
    textRight: String = stringResource(R.string.save)
) {
    val context = LocalContext.current
    Spacer(modifier = Modifier.fillMaxWidth().height(24.dp).background(BackgroundWhite))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp).align(Alignment.Center),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ImageWidget(
                modifier = Modifier
                    .clickableWithAlphaEffect {
                        if (onBack != null) onBack.invoke() else {
                            (context as? BaseActivity)?.onBackPressedDispatcher?.onBackPressed()
                        }
                    },
                resId = R.drawable.ic_arrow_left
            )
            when (type) {
                TEXT_TYPE.TEXT -> {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .clickableWithAlphaEffect {
                                if (canSave) onSave.invoke()
                            },
                        text = textRight,
                        textAlign = TextAlign.Center,
                        style = if (canSave) {
                            AppStyle.buttonMedium().semibold().primary500()
                        } else {
                            AppStyle.buttonMedium().semibold().gray300()
                        }
                    )
                }

                TEXT_TYPE.ROUND -> {
                    Text(
                        modifier = Modifier
                            .background(Primary500, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .clickableWithAlphaEffect {
                                onSave.invoke()
                            },
                        text = textRight,
                        textAlign = TextAlign.Center,
                        style = AppStyle.button().semibold().white()
                    )
                }
            }

        }

        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            IconButton(onClick = {}, enabled = canUndo) {
                Icon(
                    painter = painterResource(R.drawable.ic_undo),
                    contentDescription = "Undo",
                    tint = if (canUndo) Gray900 else Gray300,
                    modifier = Modifier.clickableWithAlphaEffect(onClick = onUndo)
                )
            }
            IconButton(onClick = onRedo, enabled = canRedo) {
                Icon(
                    painter = painterResource(R.drawable.ic_redo),
                    contentDescription = "Redo",
                    tint = if (canRedo) Gray900 else Gray300,
                    modifier = Modifier.clickableWithAlphaEffect(onClick = onRedo)
                )
            }
        }
    }
}

enum class TEXT_TYPE {
    TEXT,
    ROUND
}

@Preview(showBackground = true)
@Composable
private fun CollageHeaderPreview() {
    FeaturePhotoHeader(
        onBack = {},
        onUndo = {},
        onRedo = {},
        onSave = {},
        canUndo = true,
        canRedo = false
    )
}

