package com.basesource.base.ui.base

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.basesource.base.utils.LanguageManager
import com.basesource.base.utils.LanguageType
import com.basesource.base.utils.toJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

abstract class BaseActivity : ComponentActivity() {

    private var _activityResultManager: ActivityResultManager? = null
    private var _currentLanguage: LanguageType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Lưu ngôn ngữ hiện tại khi tạo activity
        _currentLanguage = LanguageManager.getCurrentLanguage(this)
    }

    override fun attachBaseContext(newBase: Context) {
        val language = LanguageManager.getCurrentLanguage(newBase)
        val locale = Locale(language.code)
        Locale.setDefault(locale)

        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onResume() {
        super.onResume()
        setupFullScreen()

        // Kiểm tra xem ngôn ngữ có thay đổi không
        val newLanguage = LanguageManager.getCurrentLanguage(this)
        if (_currentLanguage != newLanguage) {
            // Ngôn ngữ đã thay đổi, restart activity để áp dụng
            _currentLanguage = newLanguage
            recreate()
        }
    }

    private fun setupFullScreen() {
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Set status bar transparent
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+)
            val controller = window.insetsController
            controller?.let {
                // Hide navigation bar
                it.hide(WindowInsets.Type.navigationBars())
                // Set navigation bar behavior to show only when swiped up
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                // Set status bar appearance - dark content on light background
                it.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }
        } else {
            // Android 10 and below
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    )
        }
    }

    /**
     * ActivityResultManager được khởi tạo sớm để tránh lỗi lifecycle
     */
    val activityResultManager: ActivityResultManager
        get() {
            if (_activityResultManager == null) {
                _activityResultManager = ActivityResultManager(this)
            }
            return _activityResultManager!!
        }

    override fun onDestroy() {
        super.onDestroy()
        _activityResultManager?.clearCallbacks()
        _activityResultManager = null
    }

    fun <D> navigateTo(
        startClazz: Class<D>,
        input: IScreenData? = null,
        addFlags: (Intent.() -> Unit)? = null,
    ) {
        val navigateIntent = Intent(this, startClazz)

        if (addFlags != null) {
            navigateIntent.addFlags()
        }

        if (input != null) {
            navigateIntent.putExtra("screen_input_key", input.toJson())
        }
        startActivity(navigateIntent)
    }
}

interface IScreenData


