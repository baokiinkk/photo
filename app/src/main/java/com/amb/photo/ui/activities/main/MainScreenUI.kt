package com.amb.photo.ui.activities.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import com.amb.photo.ui.activities.discover.DiscoverUI

@Composable
fun MainScreenUI(
    modifier: Modifier,
    selectedTab: TabType,
    viewModel: MainViewModel,
) {
    val isPreview = LocalInspectionMode.current
    Box(modifier = Modifier.fillMaxSize())
    {
        Box(
            modifier = Modifier.padding(bottom = 64.dp)
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
            modifier = Modifier.align(Alignment.BottomCenter),
            tabs = viewModel.getTabs(),
            selected = selectedTab,
            onTabSelected = {
                viewModel.navigateToTab(it)
            },
        )
    }
}