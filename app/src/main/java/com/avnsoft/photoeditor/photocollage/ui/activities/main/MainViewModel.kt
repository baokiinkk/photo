package com.avnsoft.photoeditor.photocollage.ui.activities.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.data.repository.PatternRepository
import com.avnsoft.photoeditor.photocollage.data.repository.StickerRepoImpl
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
class MainViewModel(
    private val stickerRepo: StickerRepoImpl,
    private val patternRepo: PatternRepository
) : BaseViewModel(), KoinComponent {
    private val _selectedTab = MutableStateFlow(TabType.DISCOVER)
    val selectedTab: StateFlow<TabType> = _selectedTab.asStateFlow()
    private val _events = MutableSharedFlow<MainScreenEvent>()
    val events: SharedFlow<MainScreenEvent> = _events.asSharedFlow()

    init {
        initAppData()
    }

    fun navigateFeature(type: FeatureType) {
        viewModelScope.launch {
            _events.emit(MainScreenEvent.NavigateTo(type))
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
    fun getTabs(): List<TabItem> {
        return listOf(
            TabItem(
                type = TabType.DISCOVER,
                title = stringResource(id = R.string.discover),
                iconEnabled = R.drawable.ic_home_selected,
                iconDisabled = R.drawable.ic_home_unselect,
            ),
            TabItem(
                type = TabType.CUSTOMIZE,
                title = stringResource(id = R.string.my_creative),
                iconEnabled = R.drawable.ic_creative_selected,
                iconDisabled = R.drawable.ic_create_unselect,
            ),
        )
    }

    fun initAppData() {
        viewModelScope.launch(Dispatchers.IO) {
            async {
                try {
                    stickerRepo.syncStickers()
                } catch (_: Exception) {

                }
            }
            async {
                try {
                    patternRepo.syncPatterns()
                } catch (_: Exception) {

                }
            }
        }
    }
}

sealed class MainScreenEvent {
    data class NavigateToTab(val tabType: TabType) : MainScreenEvent()

    data class NavigateTo(val type: FeatureType) : MainScreenEvent()
}

enum class TabType {
    DISCOVER, CUSTOMIZE
}

enum class FeatureType {
    COLLAGE, FREE_STYLE, REMOVE_BACKGROUND, AI_ENHANCE, REMOVE_OBJECT, EDIT_PHOTO
}

data class TabItem(
    val type: TabType,
    val title: String,
    val iconEnabled: Int,
    val iconDisabled: Int,
)