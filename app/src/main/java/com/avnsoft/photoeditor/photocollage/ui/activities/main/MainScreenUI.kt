package com.avnsoft.photoeditor.photocollage.ui.activities.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import com.avnsoft.photoeditor.photocollage.ui.activities.discover.DiscoverUI
import com.avnsoft.photoeditor.photocollage.ui.activities.mycreate.MyCreateUI

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
                    DiscoverUI(viewModel)

                }
                TabType.MY_CREATE -> {
                    MyCreateUI(mainViewModel = viewModel)

                }
            }
        }
        BottomTabBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            tabs = viewModel.getTabs(),
            selected = selectedTab,
            onEditPhoto = {
                viewModel.navigateScreen(FeatureType.EDIT_PHOTO)
            },
            onTabSelected = {
                viewModel.navigateToTab(it)
            },
        )
    }
}