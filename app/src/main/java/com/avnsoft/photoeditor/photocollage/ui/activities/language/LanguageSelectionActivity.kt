package com.avnsoft.photoeditor.photocollage.ui.activities.language

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.avnsoft.photoeditor.photocollage.ui.activities.language.component.LanguageSelectionScreen
import com.avnsoft.photoeditor.photocollage.ui.theme.MainTheme
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.LanguageType

class LanguageSelectionActivity : BaseActivity(), LanguageListener {

    private lateinit var viewModel: LanguageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = LanguageViewModel()
        viewModel.initializeLanguage(this)

        setContent {
            MainTheme {
                val uiState by viewModel.uiState.collectAsState()

                LanguageSelectionScreen(
                    uiState = uiState,
                    listener = this@LanguageSelectionActivity
                )
            }
        }
    }

    override fun onBackClicked() {
        finish()
    }

    override fun onLanguageSelected(language: LanguageType) {
        // Chỉ lưu vào state, không cập nhật ngay
        viewModel.selectLanguage(language)
    }

    override fun onLanguageChanged(language: LanguageType) {
        // Click dấu tích - cập nhật ngôn ngữ và đóng activity
        viewModel.changeLanguage(this, language)

        // Trả về kết quả thành công
        setResult(RESULT_OK)
        finish()
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, LanguageSelectionActivity::class.java)
        }
    }
}
