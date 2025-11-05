package com.amb.photo.ui.activities.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amb.photo.R
import com.amb.photo.ui.theme.AppStyle
import com.basesource.base.utils.clickableWithAlphaEffect

@Composable
fun BottomTabBar(
    tabs: List<TabItem>,
    onTabSelected: (TabType) -> Unit,
    onEditPhoto: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    selected: TabType
) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Image(
            painter = painterResource(R.drawable.bg_bottom_bar),
            contentDescription = "",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.Transparent),
        ) {
            TabItem(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                selected = selected,
                tab = tabs.first(),
                onClick = { onTabSelected(tabs.first().type) }
            )
            Image(
                painter = painterResource(R.drawable.ic_fab),
                contentDescription = "",
                modifier = Modifier
                    .padding(bottom = 28.dp)
                    .size(64.dp)
                    .clickableWithAlphaEffect {
                        onEditPhoto?.invoke()
                    },
                contentScale = ContentScale.Crop
            )
            TabItem(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                selected = selected,
                tab = tabs.last(),
                onClick = { onTabSelected(tabs.last().type) })
        }
    }
}


@Composable
private fun TabItem(
    modifier: Modifier = Modifier,
    selected: TabType,
    tab: TabItem,
    onClick: () -> Unit
) {
    val iconResource = if (tab.type == selected) {
        tab.iconEnabled
    } else {
        tab.iconDisabled
    }

    val styleText = if (tab.type == selected) {
        AppStyle.caption1().medium().purple500()
    } else {
        AppStyle.caption1().medium().gray400()
    }
    Box(
        modifier = modifier.clickableWithAlphaEffect { onClick() }
    ) {
        if (tab.type == selected) {
            Image(
                painter = painterResource(R.drawable.bg_selected_home),
                contentDescription = tab.title,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.Center),
                contentScale = ContentScale.FillBounds
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Image(
                painter = painterResource(iconResource),
                contentDescription = tab.title,
                modifier = Modifier.size(24.dp),
                contentScale = ContentScale.FillBounds
            )

            Text(
                text = tab.title,
                style = styleText,
            )
        }
    }

}

@Preview(showBackground = true, name = "BottomTabBar - Trending Selected")
@Composable
fun BottomTabBarTrendingPreview() {
    val mockTabs = listOf(
        TabItem(
            type = TabType.DISCOVER,
            title = stringResource(id = R.string.tab_trending),
            iconEnabled = R.drawable.ic_home_selected,
            iconDisabled = R.drawable.ic_home_unselect,
        ),
        TabItem(
            type = TabType.CUSTOMIZE,
            title = stringResource(id = R.string.tab_customize),
            iconEnabled = R.drawable.ic_creative_selected,
            iconDisabled = R.drawable.ic_create_unselect,
        ),
    )

    BottomTabBar(
        tabs = mockTabs,
        selected = TabType.DISCOVER,
        onTabSelected = { /* Preview không cần action */ }
    )
}

@Preview(showBackground = true, name = "BottomTabBar - Gesture Selected")
@Composable
fun BottomTabBarGesturePreview() {
    val mockTabs = listOf(
        TabItem(
            type = TabType.DISCOVER,
            title = stringResource(id = R.string.trending_tab),
            iconEnabled = R.drawable.ic_home_tab_trending_enable,
            iconDisabled = R.drawable.ic_home_tab_trending_disable,
        ),
        TabItem(
            type = TabType.CUSTOMIZE,
            title = stringResource(id = R.string.customize_tab),
            iconEnabled = R.drawable.ic_home_tab_custumize_enable,
            iconDisabled = R.drawable.ic_home_tab_custumize_disable,
        )
    )

    BottomTabBar(
        tabs = mockTabs,
        selected = TabType.DISCOVER,
        onTabSelected = { /* Preview không cần action */ }
    )
}

@Preview(showBackground = true, name = "BottomTabBar - Customize Selected")
@Composable
fun BottomTabBarCustomizePreview() {
    val mockTabs = listOf(
        TabItem(
            type = TabType.DISCOVER,
            title = stringResource(id = R.string.trending_tab),
            iconEnabled = R.drawable.ic_home_tab_trending_enable,
            iconDisabled = R.drawable.ic_home_tab_trending_disable,
        ),
        TabItem(
            type = TabType.CUSTOMIZE,
            title = stringResource(id = R.string.customize_tab),
            iconEnabled = R.drawable.ic_home_tab_custumize_enable,
            iconDisabled = R.drawable.ic_home_tab_custumize_disable,
        )
    )

    BottomTabBar(
        tabs = mockTabs,
        selected = TabType.CUSTOMIZE,
        onTabSelected = { /* Preview không cần action */ }
    )
}

@Preview(showBackground = true, name = "BottomTabBar - Dark Background")
@Composable
fun BottomTabBarDarkBackgroundPreview() {
    val mockTabs = listOf(
        TabItem(
            type = TabType.DISCOVER,
            title = stringResource(id = R.string.trending_tab),
            iconEnabled = R.drawable.ic_home_tab_trending_enable,
            iconDisabled = R.drawable.ic_home_tab_trending_disable,
        ),
        TabItem(
            type = TabType.CUSTOMIZE,
            title = stringResource(id = R.string.customize_tab),
            iconEnabled = R.drawable.ic_home_tab_custumize_enable,
            iconDisabled = R.drawable.ic_home_tab_custumize_disable,
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
    ) {
        BottomTabBar(
            tabs = mockTabs,
            selected = TabType.DISCOVER,
            onTabSelected = { /* Preview không cần action */ }
        )
    }
}
