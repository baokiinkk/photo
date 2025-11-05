package com.amb.photo.ui.activities.main

import android.content.pm.PackageManager
import android.net.Uri
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
import com.amb.photo.ui.activities.imagepicker.ImagePickerActivity
import com.amb.photo.ui.activities.imagepicker.ImagePickerActivity.Companion.RESULT_URI
import com.amb.photo.ui.activities.imagepicker.ImageRequest
import com.amb.photo.ui.activities.imagepicker.TypeSelect
import com.amb.photo.ui.activities.collage.CollageActivity
import com.amb.photo.ui.theme.BackgroundWhite
import com.amb.photo.ui.theme.MainTheme
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.fromJson
import com.basesource.base.utils.fromJsonTypeToken
import com.basesource.base.utils.launchActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.io.File
import androidx.core.net.toUri
import com.amb.photo.ui.activities.editor.EditorActivity
import com.amb.photo.ui.activities.editor.EditorInput

class MainActivity : BaseActivity() {
    private val viewModel: MainViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //getSignature()
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
                            FeatureType.REMOVE_BACKGROUND -> TODO()
                            FeatureType.AI_ENHANCE -> TODO()
                            FeatureType.REMOVE_OBJECT -> TODO()
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
            Log.d("quocbao", result.toString())
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
}