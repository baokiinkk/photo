package com.amb.photo.ui.activities.collage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amb.photo.data.repository.CollageTemplateRepository
import com.amb.photo.data.model.collage.CollageTemplate
import com.basesource.base.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class CollageViewModel(
    private val repository: CollageTemplateRepository
) : ViewModel() {

    private val _templates = MutableStateFlow<List<CollageTemplate>>(emptyList())
    val templates: StateFlow<List<CollageTemplate>> = _templates.asStateFlow()

    private val _selected = MutableStateFlow<CollageTemplate?>(null)
    val selected: StateFlow<CollageTemplate?> = _selected.asStateFlow()

    fun load(count: Int) {
        viewModelScope.launch {
            when (val res = repository.getTemplates()) {
                is Result.Success -> {
                    val all = res.data
                    val filtered = all.filter { it.cells.size == count }
                    val options = filtered.ifEmpty { all }
                    _templates.value = options
                    _selected.value = options.firstOrNull()
                }
                else -> { /* no-op */ }
            }
        }
    }

    fun select(template: CollageTemplate) {
        _selected.value = template
    }
}


