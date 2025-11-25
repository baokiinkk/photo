package com.avnsoft.photoeditor.photocollage.ui.activities.mycreate

import androidx.lifecycle.viewModelScope
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class MyCreateViewModel : BaseViewModel() {
    
    private val _uiState = MutableStateFlow(MyCreateUIState())
    val uiState: StateFlow<MyCreateUIState> = _uiState.asStateFlow()

    init {
        loadProjects()
    }

    fun loadProjects() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Simulate loading delay
            delay(500)
            
            // TODO: Load actual data from local storage/database
            // For now, using mock data
            val mockProjects = getMockProjects()
            
            _uiState.value = _uiState.value.copy(
                projects = mockProjects,
                isLoading = false
            )
        }
    }

    fun refreshProjects() {
        loadProjects()
    }

    private fun getMockProjects(): List<MyCreateItem> {
        // Return empty list for now to show empty state
        // TODO: Replace with actual data loading
        return emptyList()
    }
}

data class MyCreateUIState(
    val projects: List<MyCreateItem> = emptyList(),
    val isLoading: Boolean = false
)

data class MyCreateItem(
    val id: String,
    val thumbnailPath: String,
    val title: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

