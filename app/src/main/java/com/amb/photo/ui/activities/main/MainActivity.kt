package com.amb.photo.ui.activities.main

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
import androidx.lifecycle.lifecycleScope
import com.amb.photo.ui.theme.BackgroundWhite
import com.basesource.base.ui.base.BaseActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class MainActivity : BaseActivity() {
    private val viewModel: MainViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //getSignature()
        observerData()
        setContent {
            val selectedTab by viewModel.selectedTab.collectAsState()
            MainScreenUI(
                modifier = Modifier
                    .navigationBarsPadding()
                    .background(BackgroundWhite),
                selectedTab = selectedTab,
                viewModel = viewModel,
            )

        }
    }

    private fun observerData() {
        lifecycleScope.launch {
            viewModel.events.collect { event ->
                when (event) {
                    is MainScreenEvent.LaunchActivity -> {
                        startActivity(Intent(this@MainActivity, event.cls).apply {
                            val bundle = event.bundle
                            putExtra("arg", bundle)
                        })
                    }

                    is MainScreenEvent.NavigateToTab -> {
                        viewModel.setSelectedTab(event.tabType)
                    }
                }
            }
        }
    }

    private fun getSignature() {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("DEPRECATION") packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
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