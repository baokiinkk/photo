package com.avnsoft.photoeditor.photocollage.ui.activities.main

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.CollageActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.EditorActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.EditorInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.ai_enhance.AIEnhanceActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.copyImageToAppStorage
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_background.RemoveBackgroundActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object.RemoveObjectActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.FreeStyleActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.ImagePickerActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.ImagePickerActivity.Companion.RESULT_URI
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.ImageRequest
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.TypeSelect
import com.avnsoft.photoeditor.photocollage.ui.activities.settings.SettingsActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.store.StoreActivity
import com.avnsoft.photoeditor.photocollage.ui.theme.BackgroundWhite
import com.avnsoft.photoeditor.photocollage.ui.theme.MainTheme
import com.avnsoft.photoeditor.photocollage.utils.FileUtil
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.fromJsonTypeToken
import com.basesource.base.utils.launchActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class MainActivity : BaseActivity() {

    companion object {
        fun newScreen(context: Context) {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(intent)
        }
    }

    private val viewModel: MainViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getTokenFirebase()
        deleteFolderTmp()
        getSignature()
        observerData()
        setContent {
            val selectedTab by viewModel.selectedTab.collectAsState()
            MainTheme {
                MainScreenUI(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .background(BackgroundWhite),
                    selectedTab = selectedTab,
                    viewModel = viewModel,
                )
            }
        }
    }

    private fun observerData() {
        lifecycleScope.launch {
            viewModel.events.collect { event ->
                when (event) {
                    is MainScreenEvent.NavigateToTab -> {
                        viewModel.setSelectedTab(event.tabType)
                    }

                    is MainScreenEvent.NavigateTo -> {
                        when (event.type) {
                            FeatureType.COLLAGE -> {
                                gotoCollage()
                            }

                            FeatureType.REMOVE_BACKGROUND -> {
                                gotoRemoveBackground()
                            }

                            FeatureType.AI_ENHANCE -> {
                                gotoAIEnhanceActivity()
                            }

                            FeatureType.REMOVE_OBJECT -> {
                                gotoRemoveObject()
                            }

                            FeatureType.EDIT_PHOTO -> {
                                gotoEditPhoto()
                            }

                            FeatureType.SETTING -> {
                                gotoSetting()
                            }

                            FeatureType.FREE_STYLE -> {
                                gotoFreeStyle()
                            }

                            FeatureType.STORE -> {
                                gotoStore()
                            }

                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun gotoCollage() {
        launchActivity(toActivity = ImagePickerActivity::class.java) { result ->
            val result: List<String>? = result.data?.getStringExtra(RESULT_URI)?.fromJsonTypeToken()
            val uris = result?.map { it.toUri() } ?: emptyList()
            if (uris.isNotEmpty()) {
                CollageActivity.start(this, uris)
            }
        }
    }

    private fun gotoFreeStyle() {
        launchActivity(toActivity = ImagePickerActivity::class.java) { result ->
            val result: List<String>? = result.data?.getStringExtra(RESULT_URI)?.fromJsonTypeToken()
            val uris = result?.map { it.toUri() } ?: emptyList()
            if (uris.isNotEmpty()) {
                FreeStyleActivity.start(this, uris)
            }
        }
    }

    private fun gotoStore() {
        launchActivity(toActivity = StoreActivity::class.java)
    }

    private fun gotoEditPhoto() {
        launchActivity(
            toActivity = ImagePickerActivity::class.java,
            ImageRequest(TypeSelect.SINGLE)
        ) { result ->
            val data: List<String>? = result.data?.getStringExtra(RESULT_URI)?.fromJsonTypeToken()
            data?.firstOrNull()?.let {
                launchActivity(
                    toActivity = EditorActivity::class.java,
                    input = EditorInput(pathBitmap = it),
                )
            }
        }
    }

    private fun gotoSetting() {
        launchActivity(SettingsActivity::class.java)
    }

    private fun gotoRemoveObject() {
        launchActivity(
            toActivity = ImagePickerActivity::class.java,
            ImageRequest(TypeSelect.SINGLE)
        ) { result ->
            val data: List<String>? = result.data?.getStringExtra(RESULT_URI)?.fromJsonTypeToken()
            data?.firstOrNull()?.let {
                lifecycleScope.launch {
                    val pathBitmap = copyImageToAppStorage(this@MainActivity, it.toUri())
                    launchActivity(
                        toActivity = RemoveObjectActivity::class.java,
                        input = ToolInput(pathBitmap = pathBitmap),
                    )
                }
            }
        }
    }

    private fun gotoRemoveBackground() {
        launchActivity(
            toActivity = ImagePickerActivity::class.java,
            ImageRequest(TypeSelect.SINGLE)
        ) { result ->
            val data: List<String>? = result.data?.getStringExtra(RESULT_URI)?.fromJsonTypeToken()
            data?.firstOrNull()?.let {
                lifecycleScope.launch {
                    val pathBitmap = copyImageToAppStorage(this@MainActivity, it.toUri())
                    launchActivity(
                        toActivity = RemoveBackgroundActivity::class.java,
                        input = ToolInput(pathBitmap = pathBitmap),
                    )
                }
            }
        }
    }

    private fun gotoAIEnhanceActivity() {
        launchActivity(
            toActivity = ImagePickerActivity::class.java,
            ImageRequest(TypeSelect.SINGLE)
        ) { result ->
            val data: List<String>? = result.data?.getStringExtra(RESULT_URI)?.fromJsonTypeToken()
            data?.firstOrNull()?.let {
                lifecycleScope.launch {
                    val pathBitmap = copyImageToAppStorage(this@MainActivity, it.toUri())
                    launchActivity(
                        toActivity = AIEnhanceActivity::class.java,
                        input = ToolInput(pathBitmap = pathBitmap),
                    )
                }
            }
        }
    }

    private fun getSignature() {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("DEPRECATION") packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNATURES
                )
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION") packageInfo.signatures
            }

            val md = MessageDigest.getInstance("SHA")
            signatures?.get(0)?.toByteArray()?.let { signatureBytes ->
                md.update(signatureBytes)
            }
            val sig = Base64.encodeToString(md.digest(), Base64.DEFAULT)
            Log.d("quocbao", sig)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
    }

    fun deleteFolderTmp() {
        try {
            val folder = FileUtil.getCacheFolder(this)
            folder.deleteRecursively()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}