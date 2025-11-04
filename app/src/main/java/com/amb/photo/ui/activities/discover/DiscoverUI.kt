package com.amb.photo.ui.activities.discover

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amb.photo.R
import com.amb.photo.ui.activities.imagepicker.ImagePickerActivity
import com.amb.photo.ui.activities.imagepicker.ImageRequest
import com.amb.photo.ui.activities.imagepicker.TypeSelect
import com.amb.photo.ui.activities.main.MainViewModel
import com.amb.photo.ui.theme.AppStyle
import com.amb.photo.ui.theme.BackgroundLight
import com.basesource.base.utils.clickableWithAlphaEffect

@Composable
fun DiscoverUI(viewModel: MainViewModel? = null) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(BackgroundLight)
    ) {
        DiscoverBanner()
        Spacer(Modifier.height(20.dp))
        DiscoverFunctionCards(
            onCollageClick = {
                viewModel?.launchActivity(ImagePickerActivity::class.java, ImageRequest(type = TypeSelect.MULTI))
            }
        )
        Spacer(Modifier.height(12.dp))
        DiscoverShortcuts()
        DiscoverTemplates()
        DiscoverQuickEdits(){}
    }
}

@Composable
fun DiscoverHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Image(
            modifier = Modifier.size(24.dp),
            painter = painterResource(R.drawable.ic_menu),
            contentDescription = ""
        )
        Text(
            text = "Collage Maker",
            style = AppStyle.h5().bold().white(),
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        )
        Image(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .size(56.dp, 28.dp),
            painter = painterResource(R.drawable.btn_pro),
            contentDescription = ""
        )
        Image(
            modifier = Modifier.size(24.dp),
            painter = painterResource(R.drawable.ic_market),
            contentDescription = ""
        )
    }
}

@Composable
fun DiscoverBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)

    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = R.drawable.bg_home),
            contentDescription = null,
            contentScale = ContentScale.FillBounds
        )
        DiscoverHeader()
    }
}

@Composable
fun DiscoverFunctionCards(
    onCollageClick: () -> Unit = {},
    onFreeStyleClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(92.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickableWithAlphaEffect { onCollageClick() },
        ) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(R.drawable.bg_collage_view),
                contentScale = ContentScale.Crop,
                contentDescription = ""
            )
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                Image(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 4.dp)
                        .size(32.dp)
                        .align(Alignment.End),
                    painter = painterResource(R.drawable.ic_collage_discover),
                    contentScale = ContentScale.Crop,
                    contentDescription = ""
                )
                Text(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    text = "Collage",
                    style = AppStyle.title2().semibold().white()
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(92.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickableWithAlphaEffect { onCollageClick() },
        ) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(R.drawable.bg_freestyle_view),
                contentScale = ContentScale.Crop,
                contentDescription = ""
            )
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                Image(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 4.dp)
                        .size(32.dp)
                        .align(Alignment.End),
                    painter = painterResource(R.drawable.ic_freestyle_discover),
                    contentScale = ContentScale.Crop,
                    contentDescription = ""
                )
                Text(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    text = "Free Style",
                    style = AppStyle.title2().semibold().white()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DiscoverHeaderPreview() {
    Surface { DiscoverUI() }
}