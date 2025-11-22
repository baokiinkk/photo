package com.avnsoft.photoeditor.photocollage.ui.activities.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.avnsoft.photoeditor.photocollage.ui.activities.language.LanguageSelectionActivity
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.ui.theme.BackgroundWhite
import com.avnsoft.photoeditor.photocollage.ui.theme.MainTheme
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.LanguageManager
import com.basesource.base.utils.clickableWithAlphaEffect
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainTheme {
                SettingsScreen(onLanguageClick = {
                    startActivity(Intent(this@SettingsActivity, LanguageSelectionActivity::class.java))
                }, onRate = {}, onFeedback = {
                    sendFeedback(this@SettingsActivity)
                }, onShareApp = {
                    shareApp(this@SettingsActivity)
                }, onManagerSubscription = {
                    // TODO: Implement subscription management
                }, onGdpr = {
                    openGdpr(this@SettingsActivity)
                }, onCheckUpdate = {
                    // TODO: Implement app update check
                }, onPrivacyPolicy = {
                    openPrivacyPolicy(this@SettingsActivity)
                }, onRestorePurchase = {
                    // TODO: Implement restore purchase
                })
            }
        }
    }

}

@Composable
fun SettingsScreen(
    onLanguageClick: () -> Unit = {},
    onRate: () -> Unit = {},
    onFeedback: () -> Unit = {},
    onShareApp: () -> Unit = {},
    onManagerSubscription: () -> Unit = {},
    onGdpr: () -> Unit = {},
    onCheckUpdate: () -> Unit = {},
    onPrivacyPolicy: () -> Unit = {},
    onRestorePurchase: () -> Unit = {}
) {

    val context = LocalContext.current
    val packageInfo = LocalContext.current.packageManager.getPackageInfo(LocalContext.current.packageName, 0)
    val versionName = packageInfo.versionName
    var showRatingDialog by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, BackgroundWhite, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .weight(1f),
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SettingsGroup {
                    SettingsItem(
                        icon = R.drawable.ic_language_setting,
                        title = stringResource(id = R.string.setting_language),
                        value = LanguageManager.getCurrentLanguage(LocalContext.current).displayName,
                        onClick = onLanguageClick,
                        showArrow = false
                    )
                    SettingsItem(
                        icon = R.drawable.ic_rating_setting, title = stringResource(id = R.string.rate_us), onClick = {
                            showRatingDialog = true
                        })
                    SettingsItem(
                        icon = R.drawable.ic_feedback_setting, title = stringResource(id = R.string.feedback), onClick = onFeedback
                    )
                    SettingsItem(
                        icon = R.drawable.ic_share_setting, title = stringResource(id = R.string.share_app), onClick = onShareApp
                    )
                }

                SettingsGroup {
                    SettingsItem(
                        icon = R.drawable.ic_subscription_setting,
                        title = stringResource(id = R.string.manager_subscription),
                        onClick = onManagerSubscription
                    )
                    SettingsItem(
                        icon = R.drawable.ic_gdpr_setting, title = stringResource(id = R.string.gdpr), onClick = onGdpr
                    )
                    SettingsItem(
                        icon = R.drawable.ic_update_setting, title = stringResource(id = R.string.check_app_update), onClick = onCheckUpdate
                    )
                }

                SettingsGroup {
                    SettingsItem(
                        icon = R.drawable.ic_policy_setting, title = stringResource(id = R.string.privacy_policy), onClick = onPrivacyPolicy
                    )
                    SettingsItem(
                        icon = R.drawable.ic_restore_setting, title = stringResource(id = R.string.restore_purchase), onClick = onRestorePurchase
                    )
                }
            }
        }

        // Join the Community Section
        Text(
            text = stringResource(id = R.string.join_the_community),
            style = AppStyle.buttonLarge().medium().neutral01(),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )

        // Social Media Icons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 54.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ImageWidget(
                resId = R.drawable.ic_facebook, modifier = Modifier.size(48.dp)
            )
            ImageWidget(
                resId = R.drawable.ic_instagram, modifier = Modifier.size(48.dp)
            )
            ImageWidget(
                resId = R.drawable.ic_tiktok, modifier = Modifier.size(48.dp)
            )
            ImageWidget(
                resId = R.drawable.ic_youtube, modifier = Modifier.size(48.dp)
            )
        }
        Text(
            text = stringResource(id = R.string.version_with_team, versionName ?: ""),
            style = AppStyle.body2().medium().grayScale05(),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        )
    }
    RatingDialog(isVisible = showRatingDialog, onDismiss = { showRatingDialog = false }, onRatingSubmitted = { rating ->
        if (rating <= 3) {
            //showToast = true
        } else {
            openPlayStore(context, context.packageName)
        }
    })
}

@Composable
fun SettingsGroup(
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun SettingsItem(
    icon: Int, title: String, value: String? = null, onClick: () -> Unit, showArrow: Boolean = true
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .clickableWithAlphaEffect { onClick() }
        .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ImageWidget(
                resId = icon, modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title, style = AppStyle.buttonLarge().medium().grayScale09()
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value != null) {
                Text(
                    text = value,
                    style = AppStyle.body1().semibold().purple700(),
                )
            }
            if (showArrow) {
                ImageWidget(
                    resId = R.drawable.ic_arrow_right, modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SocialIcon(iconRes: Int) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickableWithAlphaEffect { /* TODO: Handle social media click */ },
        contentAlignment = Alignment.Center
    ) {
        ImageWidget(
            resId = iconRes, modifier = Modifier.size(24.dp)
        )
    }
}

// Helper functions for settings actions
fun sendFeedback(context: Context) {
    try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionName = packageInfo.versionName
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode.toString()
        } else {
            @Suppress("DEPRECATION") packageInfo.versionCode.toString()
        }
        val appName = context.getString(R.string.app_name)

        val deviceInfo = """
            App Version: $versionName ($versionCode)
            Device: ${Build.MANUFACTURER} ${Build.MODEL}
            Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
            Locale: ${Locale.getDefault().displayName}
            Date & Time: ${
            SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
            ).format(Date())
        }
        """.trimIndent()

        val subject = context.getString(R.string.feedback_subject, appName)
        val body = context.getString(R.string.feedback_body, appName, deviceInfo)

        val encodedSubject = Uri.encode(subject)
        val encodedBody = Uri.encode(body)
        val mailtoUri = "mailto:support@example.com?subject=$encodedSubject&body=$encodedBody"

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = mailtoUri.toUri()
        }

        context.startActivity(Intent.createChooser(intent, context.getString(R.string.send_feedback_via_email)))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun shareApp(context: Context) {
    try {
        val appName = context.getString(R.string.app_name)
        val packageName = context.packageName
        val shareText = context.getString(R.string.share_app_title, appName) + "\n\n" + context.getString(R.string.share_app_body, packageName)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.check_out_app, appName))
        }

        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_app_chooser, appName)))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun openGdpr(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, context.getString(R.string.gdpr_url).toUri())
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun openPrivacyPolicy(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, context.getString(R.string.privacy_policy_url).toUri())
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}