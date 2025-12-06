package com.avnsoft.photoeditor.photocollage.ui.activities.freestyle

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.CollageActivity.Companion.EXTRA_URIS
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.tools.CollageTool
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.TEXT_TYPE
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.DrawableSticker
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.Sticker
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.StickerView
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.edittext.EditTextStickerActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.AddTextProperties
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.TextSticker
import com.avnsoft.photoeditor.photocollage.ui.activities.export_image.ExportImageResultActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.FreeStyleSticker
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.FreeStyleStickerView
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.ImagePickerActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.ImagePickerActivity.Companion.RESULT_URI
import com.avnsoft.photoeditor.photocollage.ui.dialog.DiscardChangesDialog
import com.avnsoft.photoeditor.photocollage.ui.dialog.NetworkDialog
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.ui.theme.BackgroundWhite
import com.avnsoft.photoeditor.photocollage.ui.theme.Primary500
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.clickableWithAlphaEffect
import com.basesource.base.utils.fromJsonTypeToken
import com.basesource.base.utils.launchActivity
import com.basesource.base.utils.toJson
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class FreeStyleActivity : BaseActivity() {
    companion object {
        fun start(context: Context, uris: List<Uri>) {
            val i = Intent(context, FreeStyleActivity::class.java)
            i.putParcelableArrayListExtra(EXTRA_URIS, ArrayList(uris))
            context.startActivity(i)
        }
    }

    private lateinit var stickerView: FreeStyleStickerView

    private val viewmodel: FreeStyleViewModel by viewModel()

    val list by lazy {
        intent.getParcelableArrayListExtra<Uri>(EXTRA_URIS) ?: arrayListOf()
    }

    override fun onBackPressed() {
        viewmodel.showDiscardDialog()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        observerData()
        viewmodel.initData(list)
        setContent {
            Scaffold(
                containerColor = Color.White
            ) { inner ->
                val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    FreeStyleScreen(
                        modifier = Modifier.fillMaxSize(),
                        viewmodel = viewmodel,
                        stickerView = stickerView,
                        onBack = {
                            viewmodel.showDiscardDialog()
                        },
                        onDownloadSuccess = {
                            launchActivity(
                                toActivity = ExportImageResultActivity::class.java,
                                input = it
                            )
                        },
                        onToolClick = {
                            when (it) {
                                CollageTool.RATIO -> {
                                    viewmodel.showRatioTool()
                                }

                                CollageTool.STICKER -> {
                                    viewmodel.showStickerTool()
                                }

                                CollageTool.TEXT -> {
                                    viewmodel.showTextSticker()
                                }

                                CollageTool.BACKGROUND -> {
                                    viewmodel.showBackgroundTool()
                                }

                                CollageTool.FRAME -> {
                                    viewmodel.showFrameTool()
                                }

                                CollageTool.ADD_PHOTO -> {
                                    launchActivity(toActivity = ImagePickerActivity::class.java) { result ->
                                        val data: List<String>? =
                                            result.data?.getStringExtra(RESULT_URI)
                                                ?.fromJsonTypeToken()
                                        viewmodel.addMorePhoto(data)
                                    }
                                }

                                else -> {

                                }
                            }
                        },
                    )

                    DiscardChangesDialog(
                        isVisible = uiState.showDiscardDialog,
                        onDiscard = {
                            finish()
                        },
                        onCancel = {
                            viewmodel.hideDiscardDialog()
                        }
                    )
                }
            }
        }
    }

    private fun observerData() {
        viewmodel.freeStyleSticker.observe(this) {
            Log.d("ssss", "data nek ${it.toJson()}")
            stickerView.addSticker(it, Sticker.Position.CENTER, 1f)
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewmodel.removeSticker.collect {
                    stickerView.remove(it)
                }
            }
        }
    }

    private fun initView() {
        stickerView = FreeStyleStickerView(this)
        stickerView.setLocked(false)
        stickerView.setConstrained(true)
        stickerView.configDefaultIcons()
        stickerView.setOnStickerOperationListener(object : StickerView.OnStickerOperationListener {
            public override fun onTextStickerEdit(param1Sticker: Sticker) {
                if (param1Sticker is TextSticker) {
                    viewmodel.showEditTextSticker(param1Sticker.getAddTextProperties())
                    val intent = EditTextStickerActivity.newIntent(
                        this@FreeStyleActivity,
                        param1Sticker.getAddTextProperties()?.text
                    )
                    activityResultManager.launchActivity(intent, null) {
                        if (it.resultCode == Activity.RESULT_OK) {
                            val textResult =
                                it.data?.getStringExtra(EditTextStickerActivity.EXTRA_TEXT)
                                    .orEmpty()
                            val widthResult =
                                it.data?.getIntExtra(EditTextStickerActivity.EXTRA_WIDTH, 0)
                            val heightResult =
                                it.data?.getIntExtra(EditTextStickerActivity.EXTRA_HEIGHT, 0)

                            val paramAddTextProperties = param1Sticker.getAddTextProperties()
                                ?: AddTextProperties.defaultProperties
                            paramAddTextProperties.apply {
                                this.text = textResult
                                this.textWidth = widthResult ?: 0
                                this.textHeight = heightResult ?: 0
                            }
                            stickerView.replace(
                                TextSticker(
                                    stickerView.context,
                                    paramAddTextProperties
                                )
                            )
                            viewmodel.hideEditTextSticker()
                        }
                    }
                }
            }

            public override fun onStickerAdded(sticker: Sticker) {
                Log.d("stickerView", "onStickerAdded")
                if (sticker is TextSticker) {
                    stickerView.configDefaultIcons()
                } else if (sticker is DrawableSticker || sticker is FreeStyleSticker) {
                    stickerView.configStickerIcons()
                }
                stickerView.invalidate()
            }

            public override fun onStickerClicked(sticker: Sticker) {
            }

            public override fun onStickerDeleted(sticker: Sticker) {
            }

            public override fun onStickerDragFinished(sticker: Sticker) {
            }


            public override fun onStickerZoomFinished(sticker: Sticker) {
            }

            public override fun onTouchDownForBeauty(param1Float1: Float, param1Float2: Float) {
            }

            public override fun onTouchDragForBeauty(param1Float1: Float, param1Float2: Float) {
            }

            public override fun onTouchUpForBeauty(param1Float1: Float, param1Float2: Float) {
            }


            public override fun onStickerFlipped(sticker: Sticker) {
            }

            public override fun onStickerTouchOutside(param1Sticker: Sticker?) {
            }

            public override fun onStickerTouchedDown(sticker: Sticker) {
                Log.d("stickerView", "onStickerTouchedDown")
                stickerView.setShowFocus(true)
                if (sticker is TextSticker) {
                    stickerView.configDefaultIcons()
                } else if (sticker is DrawableSticker) {
                    stickerView.configStickerIcons()
                } else if (sticker is FreeStyleSticker) {
                }
                stickerView.swapLayers()
                stickerView.invalidate()
            }

            public override fun onStickerDoubleTapped(sticker: Sticker) {
            }
        })
    }
}


@Composable
fun HeaderSave(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    isActionRight: Boolean = true,
    onActionRight: () -> Unit,
    textRight: String = stringResource(R.string.save),
    type: TEXT_TYPE = TEXT_TYPE.ROUND,
) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(BackgroundWhite)
    )
    Row(
        modifier = modifier,
    ) {
        ImageWidget(
            modifier = Modifier
                .clickableWithAlphaEffect(onClick = onBack),
            resId = R.drawable.ic_arrow_left
        )
        Spacer(modifier = Modifier.weight(1f))
        when (type) {
            TEXT_TYPE.TEXT -> {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .clickableWithAlphaEffect {
                            if (isActionRight) onActionRight.invoke()
                        },
                    text = textRight,
                    textAlign = TextAlign.Center,
                    style = if (isActionRight) {
                        AppStyle.buttonMedium().semibold().primary500()
                    } else {
                        AppStyle.buttonMedium().semibold().gray300()
                    }
                )
            }

            TEXT_TYPE.ROUND -> {
                Text(
                    modifier = Modifier
                        .background(Primary500, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .clickableWithAlphaEffect(onClick = onActionRight),
                    text = textRight,
                    textAlign = TextAlign.Center,
                    style = AppStyle.button().semibold().white()
                )
            }
        }
    }
}