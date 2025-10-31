package com.amb.photo.ui.activities.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import com.amb.photo.ui.activities.discover.DiscoverUI
import com.amb.photo.ui.theme.BackgroundWhite

@Composable
fun MainScreenUI(
    modifier: Modifier,
    selectedTab: TabType,
    viewModel: MainViewModel,
) {
    val isPreview = LocalInspectionMode.current
    Box(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize())
        {

            Box(
                modifier = Modifier.weight(1f),
            ) {
                when (selectedTab) {
                    TabType.DISCOVER -> {
                        if (!isPreview) {
                            DiscoverUI(viewModel)
                        } else {
                            DiscoverUI(viewModel)
                        }
                    }

                    TabType.CUSTOMIZE -> {
                        if (!isPreview) {

                        } else {

                        }
                    }
                }
            }
            BottomTabBar(
                modifier = Modifier
                    .background(BackgroundWhite)
                    .height(75.dp),
                tabs = viewModel.getTabs(),
                selected = selectedTab,
                onTabSelected = {
                    viewModel.navigateToTab(it)
                },
            )
        }
    }
}