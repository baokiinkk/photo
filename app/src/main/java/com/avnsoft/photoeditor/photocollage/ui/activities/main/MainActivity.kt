package com.avnsoft.photoeditor.photocollage.ui.activities.main

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
import androidx.lifecycle.lifecycleScope
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.ImagePickerActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.ImagePickerActivity.Companion.RESULT_URI
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.ImageRequest
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.TypeSelect
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.CollageActivity
import com.avnsoft.photoeditor.photocollage.ui.theme.BackgroundWhite
import com.avnsoft.photoeditor.photocollage.ui.theme.MainTheme
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.fromJson
import com.basesource.base.utils.fromJsonTypeToken
import com.basesource.base.utils.launchActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import androidx.core.net.toUri
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.CollageTool
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.EditorActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.EditorInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.ai_enhance.AIEnhanceActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.copyImageToAppStorage
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_background.RemoveBackgroundActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object.RemoveObjectActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.store.StoreActivity

class MainActivity : BaseActivity() {
    private val viewModel: MainViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getTokenFirebase()
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

                            FeatureType.FREE_STYLE -> TODO()
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

    private fun gotoEditPhoto() {
        launchActivity(
            toActivity = ImagePickerActivity::class.java,
            ImageRequest(TypeSelect.SINGLE)
        ) { result ->
            val result: String? = result.data?.getStringExtra(RESULT_URI)?.fromJson()
            result?.let {
                launchActivity(
                    toActivity = EditorActivity::class.java,
                    input = EditorInput(pathBitmap = it),
                )
            }
        }
    }

    private fun gotoRemoveObject(){
        launchActivity(
            toActivity = ImagePickerActivity::class.java,
            ImageRequest(TypeSelect.SINGLE)
        ) { result ->
            val result: String? = result.data?.getStringExtra(RESULT_URI)?.fromJson()
            result?.let {
                lifecycleScope.launch {
                    val pathBitmap =copyImageToAppStorage(this@MainActivity, result.toUri())
                    launchActivity(
                        toActivity = RemoveObjectActivity::class.java,
                        input = ToolInput(pathBitmap = pathBitmap),
                    )
                }
            }
        }
    }

    private fun gotoRemoveBackground(){
        launchActivity(
            toActivity = ImagePickerActivity::class.java,
            ImageRequest(TypeSelect.SINGLE)
        ) { result ->
            val result: String? = result.data?.getStringExtra(RESULT_URI)?.fromJson()
            result?.let {
                lifecycleScope.launch {
                    val pathBitmap =copyImageToAppStorage(this@MainActivity, result.toUri())
                    launchActivity(
                        toActivity = RemoveBackgroundActivity::class.java,
                        input = ToolInput(pathBitmap = pathBitmap),
                    )
                }
            }
        }
    }

    private fun gotoAIEnhanceActivity(){
        launchActivity(
            toActivity = ImagePickerActivity::class.java,
            ImageRequest(TypeSelect.SINGLE)
        ) { result ->
            val result: String? = result.data?.getStringExtra(RESULT_URI)?.fromJson()
            result?.let {
                lifecycleScope.launch {
                    val pathBitmap =copyImageToAppStorage(this@MainActivity, result.toUri())
                    launchActivity(
                        toActivity = AIEnhanceActivity::class.java,
                        input = ToolInput(pathBitmap = pathBitmap),
                    )
                }
            }
        }
    }

    private fun gotoStorePhoto() {
        launchActivity(
            toActivity = StoreActivity::class.java,
        )
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
}