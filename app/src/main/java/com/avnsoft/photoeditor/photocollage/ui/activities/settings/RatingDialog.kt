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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.ui.theme.BackgroundWhite
import com.basesource.base.components.CustomButton
import com.basesource.base.utils.clickableWithAlphaEffect

@Composable
fun RatingDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onRatingSubmitted: (Int) -> Unit
) {
    if (!isVisible) return

    var selectedRating by remember { mutableIntStateOf(5) }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.ic_rate_app_arrow))
    val composition2 by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.ic_rate_app_blink))
    val composition3 by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.ic_rate_app_star))

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(391.dp)) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(297.dp)
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
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Close button
                    Image(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "",
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.End)
                            .clickableWithAlphaEffect {
                                onDismiss()
                            }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Title
                    Text(
                        text = stringResource(id = R.string.do_you_like_gps_checker),
                        style = AppStyle.title2().semibold().grayScale09(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Subtitle
                    Text(
                        text = stringResource(id = R.string.rate_your_experience),
                        style = AppStyle.body1().regular().gray400(),
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.align(Alignment.End),
                        ) {
                        LottieAnimation(
                            modifier = Modifier.size(62.dp,40.dp).offset(
                                y = 10.dp,
                                x = 10.dp
                            ),
                            composition = composition,
                            iterations = LottieConstants.IterateForever
                        )
                        LottieAnimation(
                            modifier = Modifier.size(48.dp,40.dp).offset(
                                y = 20.dp
                            ),
                            composition = composition2,
                            iterations = LottieConstants.IterateForever
                        )
                    }
                    // Star rating
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(5) { index ->
                            val starIndex = index + 1
                            if(starIndex > selectedRating){
                                LottieAnimation(
                                    modifier = Modifier.size(44.dp).clickableWithAlphaEffect {
                                        selectedRating = starIndex
                                    },
                                    composition = composition3,
                                    iterations = LottieConstants.IterateForever
                                )
                            }else{
                                Image(
                                    painter = painterResource(
                                        R.drawable.ic_star_rating
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
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit button
                    CustomButton(
                        text = if (selectedRating <= 3) stringResource(R.string.setting_send_feedback) else stringResource(R.string.rate_now),
                        onClick = {
                            onRatingSubmitted(selectedRating)
                            onDismiss()
                        }
                    )
                }
            }
            Image(
                modifier = Modifier
                    .size(240.dp, 146.dp)
                    .align(Alignment.TopCenter),
                painter = when (selectedRating) {
                    5 -> painterResource(R.drawable.ic_emotion_rating_5)
                    4 -> painterResource(R.drawable.ic_emotion_rating_4)
                    3 -> painterResource(R.drawable.ic_emotion_rating_3)
                    2 -> painterResource(R.drawable.ic_emotion_rating_2)
                    1 -> painterResource(R.drawable.ic_emotion_rating_1)
                    else -> painterResource(R.drawable.ic_emotion_rating_5)
                },
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
