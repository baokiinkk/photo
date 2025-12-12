package com.avnsoft.photoeditor.photocollage.ui.activities.settings

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.ui.theme.BackgroundWhite
import com.basesource.base.components.CustomButton
import com.basesource.base.utils.clickableWithAlphaEffect
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeApi::class)

@Composable
fun RatingDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onRatingSubmitted: (Int) -> Unit
) {
    if (!isVisible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedRating by remember { mutableIntStateOf(5) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = null
    ){
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(436.dp)) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        color = BackgroundWhite,
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            )
            {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Close button
                    Image(
                        painter = painterResource(R.drawable.ic_close_black),
                        contentDescription = "",
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.End)
                            .clickableWithAlphaEffect {
                                onDismiss()
                            }
                    )

                    Spacer(modifier = Modifier.height(62.dp))

                    // Title
                    Row {
                        Text(
                            text = stringResource(id = R.string.do_you_like),
                            style = AppStyle.title1().bold().gray900(),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            modifier = Modifier.padding(start = 4.dp),
                            text = stringResource(id = R.string.app_name) + "?",
                            style = AppStyle.title1().bold().primary800(),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Subtitle
                    Text(
                        text = stringResource(id = R.string.rate_your),
                        style = AppStyle.title3().medium().gray500(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    // Star rating
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(5) { index ->
                            val starIndex = index + 1
                            Image(
                                painter = painterResource(
                                    if (starIndex <= selectedRating) R.drawable.ic_star_rating
                                    else R.drawable.ic_star_rating_disable
                                ),
                                contentDescription = "Star $starIndex",
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickableWithAlphaEffect {
                                        selectedRating = starIndex
                                    }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Submit button
                    CustomButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.rate_now),
                        textStyle = AppStyle.buttonLarge().bold().white(),
                        onClick = {
                            onRatingSubmitted(selectedRating)
                            onDismiss()
                        }
                    )
                }
            }
            Image(
                modifier = Modifier
                    .size(164.dp)
                    .align(Alignment.TopCenter),
                painter = painterResource(R.drawable.ic_emotion_rating),
                contentScale = ContentScale.Crop,
                contentDescription = "Rating emotion",
            )
        }
    }
}

fun openPlayStore(context: Context, packageName: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri())
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to web browser if Play Store app is not available
        val intent = Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$packageName".toUri())
        context.startActivity(intent)
    }
}

@Preview(showBackground = true, name = "Rating Dialog - 5 Stars")
@Composable
private fun RatingDialogPreview5Stars() {
    RatingDialog(
        isVisible = true,
        onDismiss = { },
        onRatingSubmitted = { }
    )
}
