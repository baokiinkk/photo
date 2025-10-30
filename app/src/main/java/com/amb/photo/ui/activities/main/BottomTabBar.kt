package com.amb.photo.ui.activities.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.basesource.base.components.CustomCardView
import com.basesource.base.utils.clickableWithAlphaEffect

@Composable
fun BottomTabBar(
    tabs: List<TabItem>,
    onTabSelected: (TabType) -> Unit,
    modifier: Modifier = Modifier,
    selected: TabType
) {
    CustomCardView(
        elevation = 10.dp,
        shape = RoundedCornerShape(topEnd = 16.dp, topStart = 16.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                TabItem(
                    selected = selected,
                    tab = tab,
                    onClick = { onTabSelected(tab.type) }
                )
            }
        }
    }
}

@Composable
private fun TabItem(
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
        AppStyle.button().bold().purple500()
    } else {
        AppStyle.button().medium().regular()
    }

    Column(
        modifier = Modifier
            .clickableWithAlphaEffect { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Image(
            painter = painterResource(iconResource),
            contentDescription = tab.title,
            modifier = Modifier.size(29.dp),
            contentScale = ContentScale.FillBounds
        )

        Text(
            text = tab.title,
            style = styleText,
        )
    }
}

@Preview(showBackground = true, name = "BottomTabBar - Trending Selected")
@Composable
fun BottomTabBarTrendingPreview() {
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
