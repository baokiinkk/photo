package com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_background.onboard

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.ai_enhance.AIEnhanceActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_background.RemoveBackgroundActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.store.HeaderStore
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.ui.theme.LoadingScreen
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.clickableWithAlphaEffect
import com.basesource.base.utils.launchActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class OnboardRemoveBackgroundActivity : BaseActivity() {

    private val screenInput: ToolInput? by lazy {
        intent.getInput()
    }

    private val viewmodel: OnboardRemoveBackgroundViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel.initData(screenInput)
        setContent {
            val uiState by viewmodel.uiState.collectAsStateWithLifecycle()

            Scaffold(
                containerColor = Color.White
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding())
                        .background(AppColor.backgroundAppColor)
                ) {
                    val title = when (screenInput?.type) {
                        ToolInput.TYPE.REMOVE_BACKGROUND -> {
                            getString(R.string.remove_background)
                        }

                        ToolInput.TYPE.ENHANCE -> {
                            getString(R.string.ai_enhance)
                        }

                        else -> {
                            ""
                        }
                    }
                    HeaderStore(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 16.dp),
                        title = title//AI Enhance
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 40.dp, vertical = 20.dp)
                    ) {
                        uiState.bitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(24.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Text(
                        text = stringResource(R.string.enhance_with_magic_ai),
                        style = AppStyle.title1().bold().Color_101828(),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "97.89%",
                            style = AppStyle.title3().medium().primary500()
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = stringResource(R.string.of_photos_achieved_significant_results),
                            style = AppStyle.title3().medium().gray800()
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ImageWidget(
                            resId = R.drawable.button_onboard,
                            modifier = Modifier
                                .fillMaxSize(),
                            contentScale = ContentScale.FillWidth
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.go_premium),
                                style = AppStyle.title2().semibold().white()
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.no_ads_unlimited_plays),
                                style = AppStyle.caption2().medium().primary100()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickableWithAlphaEffect {
                                when (screenInput?.type) {
                                    ToolInput.TYPE.REMOVE_BACKGROUND -> {
                                        launchActivity(
                                            toActivity = RemoveBackgroundActivity::class.java,
                                            input = ToolInput(pathBitmap = screenInput?.pathBitmap)
                                        )
                                    }

                                    ToolInput.TYPE.ENHANCE -> {
                                        launchActivity(
                                            toActivity = AIEnhanceActivity::class.java,
                                            input = ToolInput(pathBitmap = screenInput?.pathBitmap),
                                        )
                                    }

                                    else -> {

                                    }
                                }
                            }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ImageWidget(
                            resId = R.drawable.button_view_ads,
                            modifier = Modifier
                                .fillMaxSize(),
                            contentScale = ContentScale.FillWidth
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            val text = when (screenInput?.type) {
                                ToolInput.TYPE.REMOVE_BACKGROUND -> {
                                    stringResource(R.string.remove_background)
                                }

                                ToolInput.TYPE.ENHANCE -> {
                                    stringResource(R.string.enhance_now)
                                }

                                else -> {
                                    ""
                                }
                            }

                            Text(
                                text = text,
                                style = AppStyle.title2().semibold().primary500()
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.watch_an_ads),
                                style = AppStyle.caption2().medium().primary300()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                }

                if (uiState.isLoading) {
                    LoadingScreen()
                }
            }
        }
    }
}
