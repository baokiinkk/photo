package com.amb.photo.ui.activities.main

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import com.amb.photo.R
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.component.KoinComponent

@KoinViewModel
class MainViewModel() : BaseViewModel(), KoinComponent {
    private val _selectedTab = MutableStateFlow(TabType.DISCOVER)
    val selectedTab: StateFlow<TabType> = _selectedTab.asStateFlow()
    private val _events = MutableSharedFlow<MainScreenEvent>()
    val events: SharedFlow<MainScreenEvent> = _events.asSharedFlow()

    fun <T> launchActivity(cls: Class<T>, bundle: Bundle? = null) {
        viewModelScope.launch {
            _events.emit(MainScreenEvent.LaunchActivity(cls, bundle))
        }
    }

    fun navigateToTab(tabType: TabType) {
        viewModelScope.launch {
            _events.emit(MainScreenEvent.NavigateToTab(tabType))
        }
    }

    fun setSelectedTab(tabType: TabType) {
        _selectedTab.value = tabType
    }

    @Composable
    fun getTabs(): List<com.amb.photo.ui.activities.main.TabItem> {
        return listOf(
            TabItem(
                type = TabType.DISCOVER,
                title = stringResource(id = R.string.tab_trending),
                iconEnabled = R.drawable.ic_home_tab_trending_enable,
                iconDisabled = R.drawable.ic_home_tab_trending_disable,
            ),
            TabItem(
                type = TabType.CUSTOMIZE,
                title = stringResource(id = R.string.tab_customize),
                iconEnabled = R.drawable.ic_home_tab_custumize_enable,
                iconDisabled = R.drawable.ic_home_tab_custumize_disable,
            ),
        )
    }
}

sealed class MainScreenEvent {
    data class LaunchActivity(val cls: Class<*>, val bundle: Bundle?) : MainScreenEvent()

    data class NavigateToTab(val tabType: TabType) : MainScreenEvent()
}

enum class TabType {
    DISCOVER, CUSTOMIZE
}

data class TabItem(
    val type: TabType,
    val title: String,
    val iconEnabled: Int,
    val iconDisabled: Int,
)