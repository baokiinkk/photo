package com.avnsoft.photoeditor.photocollage.ui.activities.collage

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.CollageScreen
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.DrawableSticker
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.Sticker
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.StickerView
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.edittext.EditTextStickerActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.AddTextProperties
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.TextSticker
import com.avnsoft.photoeditor.photocollage.ui.activities.export_image.ExportImageResultActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.FreeStyleViewModel
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.FreeStyleSticker
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.FreeStyleStickerView
import com.avnsoft.photoeditor.photocollage.ui.theme.MainTheme
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.launchActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class CollageActivity : BaseActivity() {
    private val vm: CollageViewModel by viewModel()

    private lateinit var stickerView: FreeStyleStickerView

    private val freeStyleViewModel: FreeStyleViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        // Set stickerView vào ViewModel để có thể lấy/restore stickers
        vm.stickerView = stickerView
        val list = intent.getParcelableArrayListExtra<Uri>(EXTRA_URIS) ?: arrayListOf()
        setContent {
            MainTheme {
                CollageScreen(
                    list,
                    vm,
                    freeStyleViewModel = freeStyleViewModel,
                    stickerView = stickerView,
                    onBack = { finish() }, onDownloadSuccess = {
                        launchActivity(
                            toActivity = ExportImageResultActivity::class.java, input = it
                        )
                    }
                    )
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
                    freeStyleViewModel.showEditTextSticker(param1Sticker.getAddTextProperties())
                    val intent = EditTextStickerActivity.newIntent(
                        this@CollageActivity,
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
                            freeStyleViewModel.hideEditTextSticker()
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
                // Unselect tất cả images trong CollagePreview
                vm.triggerUnselectAllImages()
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

    companion object {
        const val EXTRA_URIS = "extra_uris"
        fun start(context: Context, uris: List<Uri>) {
            val i = Intent(context, CollageActivity::class.java)
            i.putParcelableArrayListExtra(EXTRA_URIS, ArrayList(uris))
            context.startActivity(i)
        }
    }
}

