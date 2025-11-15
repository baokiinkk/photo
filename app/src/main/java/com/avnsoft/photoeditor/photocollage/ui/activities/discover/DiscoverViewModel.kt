package com.avnsoft.photoeditor.photocollage.ui.activities.discover

import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DiscoverViewModel : BaseViewModel() {
    // State cho Templates (future có thể lấy từ API hoặc local DB)
    private val _templates = MutableStateFlow(
        listOf(
            TemplateItem(1, "Modern Collage", android.R.drawable.ic_menu_gallery),
            TemplateItem(2, "Classic Mix", android.R.drawable.ic_menu_gallery),
            TemplateItem(3, "Fresh Layout", android.R.drawable.ic_menu_gallery)
        )
    )
    val templates: StateFlow<List<TemplateItem>> = _templates

    // State cho QuickEdit (future có thể lấy từ API hoặc local DB)
    private val _quickEdits = MutableStateFlow(
        listOf(
            QuickEditItem(1, "Background", android.R.drawable.ic_menu_gallery),
            QuickEditItem(2, "Filter", android.R.drawable.ic_menu_gallery),
            QuickEditItem(3, "Sticker", android.R.drawable.ic_menu_gallery),
            QuickEditItem(4, "Add Text", android.R.drawable.ic_menu_gallery),
            QuickEditItem(5, "Frame", android.R.drawable.ic_menu_gallery),
            QuickEditItem(6, "Doodle", android.R.drawable.ic_menu_gallery),
        )
    )
    val quickEdits: StateFlow<List<QuickEditItem>> = _quickEdits

    // Có thể bổ sung các function load/update data động ở future
}

// Định nghĩa TemplateItem/QuickEditItem trong package discover để dùng chung
// Nếu đã có ở file khác thì import hoặc chuyển về đây.
data class TemplateItem(val id: Int, val name: String, val imageRes: Int)
data class QuickEditItem(val id: Int, val title: String, val imageRes: Int)











