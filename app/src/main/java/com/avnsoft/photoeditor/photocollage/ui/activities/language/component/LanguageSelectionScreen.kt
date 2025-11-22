package com.avnsoft.photoeditor.photocollage.ui.activities.language.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.language.LanguageListener
import com.avnsoft.photoeditor.photocollage.ui.activities.language.LanguageUiState
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor.Companion.Purple50
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor.Companion.Purple500
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.ui.theme.BackgroundLight
import com.basesource.base.utils.LanguageType
import com.basesource.base.utils.clickableWithAlphaEffect

@Composable
fun LanguageSelectionScreen(
    modifier: Modifier = Modifier,
    uiState: LanguageUiState,
    listener: LanguageListener
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.padding(horizontal = 16.dp).statusBarsPadding()) {
                Text(
                    text = stringResource(R.string.choose_language),
                    style = AppStyle.title1().semibold().grayScale09(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    textAlign = TextAlign.Center
                )
                Image(
                    painterResource(R.drawable.ic_check_active),
                    contentDescription = "",
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterEnd)
                        .clickableWithAlphaEffect {
                            listener.onLanguageChanged(uiState.currentLanguage)
                        }
                )
            }
            Spacer(Modifier.height(8.dp))

            // Language List
            LanguageList(
                currentLanguage = uiState.currentLanguage,
                onLanguageSelected = { listener.onLanguageSelected(it) }
            )
        }
    }
}

@Composable
private fun LanguageList(
    currentLanguage: LanguageType,
    onLanguageSelected: (LanguageType) -> Unit
) {
    Column {
        LanguageType.entries.forEach { language ->
            LanguageItem(
                language = language,
                isSelected = language == currentLanguage,
                onClick = { onLanguageSelected(language) }
            )
        }
    }
}

@Composable
private fun LanguageItem(
    language: LanguageType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Purple50 else Color.White
    val borderColor = if (isSelected) Purple500 else null
    val style = if (isSelected) AppStyle.buttonLarge().semibold().grayScale09() else AppStyle.title2().regular().grayScale09()
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.cursor))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
    )
    {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp)
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(12.dp)
                )
                .then(
                    borderColor?.let {
                        Modifier.border(
                            2.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                    } ?: run {
                        Modifier.padding(2.dp)
                    }
                )
                .clickableWithAlphaEffect { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        )
        {
            Image(
                painterResource(language.flag),
                contentDescription = "",
                modifier = Modifier.size(32.dp).padding(end = 10.dp)
            )
            Text(
                modifier = Modifier.weight(1f),
                text = language.displayName,
                style = style,
            )
            RadioButton(
                modifier = Modifier.size(24.dp),
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Purple500,
                    unselectedColor = Color.Gray
                )
            )
        }
        if (isSelected)
            LottieAnimation(
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.BottomEnd),
                composition = composition,
                iterations = LottieConstants.IterateForever
            )
    }
}

@Preview(showBackground = true, name = "Language Selection Screen")
@Composable
private fun LanguageSelectionScreenPreview() {
    val uiState = LanguageUiState(
        currentLanguage = LanguageType.ENGLISH
    )

    val previewListener = object : LanguageListener {
        override fun onBackClicked() {}
        override fun onLanguageSelected(language: LanguageType) {}
        override fun onLanguageChanged(language: LanguageType) {}
    }

    MaterialTheme {
        LanguageSelectionScreen(
            uiState = uiState,
            listener = previewListener
        )
    }
}