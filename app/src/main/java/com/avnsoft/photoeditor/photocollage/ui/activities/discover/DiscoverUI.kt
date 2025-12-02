package com.avnsoft.photoeditor.photocollage.ui.activities.discover

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.main.FeatureType
import com.avnsoft.photoeditor.photocollage.ui.activities.main.MainViewModel
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.ui.theme.BackgroundLight
import com.basesource.base.utils.clickableWithAlphaEffect

@Composable
fun DiscoverUI(viewModel: MainViewModel? = null) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(BackgroundLight)
    ) {
        DiscoverBanner(viewModel)
        Spacer(Modifier.height(20.dp))
        DiscoverFunctionCards(
            onCollageClick = {
                viewModel?.navigateScreen(FeatureType.COLLAGE)
            },
            onFreeStyleClick = {
                viewModel?.navigateScreen(FeatureType.FREE_STYLE)
            }
        )
        Spacer(Modifier.height(12.dp))
        DiscoverShortcuts(
            onRemoveObject = {
                viewModel?.navigateScreen(FeatureType.REMOVE_OBJECT)
            },
            onAIEnhance = {
                viewModel?.navigateScreen(FeatureType.AI_ENHANCE)
            },
            onRemoveBG = {
                viewModel?.navigateScreen(FeatureType.REMOVE_BACKGROUND)
            }
        )
        val templates by viewModel?.templates?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
        DiscoverTemplates(
            templates = templates,
            onSeeAll = {
                viewModel?.navigateScreen(FeatureType.STORE)
            },
            onTemplateClick = { template ->
                viewModel?.navigateScreen(FeatureType.TEMPLATE, data = template)
            }
        )
        DiscoverQuickEdits {
            viewModel?.navigateScreen(FeatureType.EDIT_PHOTO, it.tool)
        }
    }
}

@Composable
fun DiscoverHeader(
    viewModel: MainViewModel?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Image(
            modifier = Modifier
                .size(24.dp)
                .clickableWithAlphaEffect {
                    viewModel?.navigateScreen(FeatureType.SETTING)
                },
            painter = painterResource(R.drawable.ic_menu),
            contentDescription = ""
        )
        Text(
            text = stringResource(R.string.collage_maker),
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
            modifier = Modifier
                .size(24.dp)
                .clickableWithAlphaEffect {
                    viewModel?.navigateScreen(FeatureType.STORE)
                },
            painter = painterResource(R.drawable.ic_market),
            contentDescription = ""
        )
    }
}

@Composable
fun DiscoverBanner(viewModel: MainViewModel?) {
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
        DiscoverHeader(viewModel)
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
                    text = stringResource(R.string.collage),
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
                .clickableWithAlphaEffect { onFreeStyleClick() },
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
                    text = stringResource(R.string.free_style),
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