package com.avnsoft.photoeditor.photocollage.ui.activities.freestyle

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.CollageActivity.Companion.EXTRA_URIS
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.CollageTool
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.DrawableSticker
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.Sticker
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.sticker.lib.StickerView
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.text_sticker.lib.TextSticker
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.FreeStyleSticker
import com.avnsoft.photoeditor.photocollage.ui.activities.freestyle.lib.FreeStyleStickerView
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.toJson
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        observerData()
        viewmodel.initData(list)
        setContent {
            Scaffold(
                containerColor = Color(0xFFF2F4F8)
            ) { inner ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    FreeStyleScreen(
                        modifier = Modifier.fillMaxSize(),
                        viewmodel = viewmodel,
                        stickerView = stickerView,
                        onToolClick = {
                            when (it) {
                                CollageTool.STICKER -> {
                                    viewmodel.showStickerTool()
                                }

                                CollageTool.TEXT ->{
                                    viewmodel.showTextSticker()
                                }
                                else -> {

                                }
                            }
                        },
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
    }

    private fun initView() {
        stickerView = FreeStyleStickerView(this)
        stickerView.setLocked(false)
        stickerView.setConstrained(true)
        stickerView.configDefaultIcons()
        stickerView.setOnStickerOperationListener(object : StickerView.OnStickerOperationListener {
            public override fun onTextStickerEdit(param1Sticker: Sticker) {
                if (param1Sticker is TextSticker) {
//                    param1Sticker.isShow = false
//                    stickerView.setHandlingSticker(null)
                    viewmodel.showEditTextSticker(param1Sticker.getAddTextProperties())
//                    textEditorDialogFragment = TextEditorDialogFragment.show(
//                        this@FreeStyleActivity,
//                        (sticker as TextSticker).getAddTextProperties()
//                    )
//                    textEditor = object : TextEditor() {
//                        fun onDone(addTextProperties: AddTextProperties?) {
//                            stickerView.remove(sticker)
//                            stickerView.addSticker(
//                                TextSticker(
//                                    this@FreeStyleActivity,
//                                    addTextProperties
//                                )
//                            )
//
//                            showToolBar()
//                        }
//
//                        fun onBackButton() {
//                            showToolBar()
//                        }
//                    }
//                    textEditorDialogFragment.setOnTextEditorListener(textEditor)
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
                if (sticker is FreeStyleSticker) {
                    val freeStyleSticker: FreeStyleSticker = sticker as FreeStyleSticker

//                    index = listPhotoSelected.indexOf(freeStyleSticker.getPhoto())
//                    LogUtils.logD(FreeStyleActivity.TAG, "onStickerClicked", index)
//                    if (currentFunction === Function.EDITOR_IMAGE) {
//                        return
//                    }
//                    if (currentFunction !== Function.NONE) {
//                        checkCurrentFunction()
//                    }
//
//                    currentFunction = Function.EDITOR_IMAGE
//                    if (editImageView.getVisibility() !== View.VISIBLE) {
//                        hideToolbar()
//                        editImageView.show()
//                    }
                }
            }

            public override fun onStickerDeleted(sticker: Sticker) {
                if (sticker is FreeStyleSticker) {
//                    listPhotoSelected.removeAt(index)
//                    LogUtils.logD(FreeStyleActivity.TAG, "onStickerDeleted", index)
//                    checkNumberOfImageSelected()
//                    if (currentFunction === Function.EDITOR_IMAGE) onBackPressed()
                }
            }

            public override fun onStickerDragFinished(sticker: Sticker) {
                if (sticker is FreeStyleSticker) {
//                    val freeStyleSticker: FreeStyleSticker = sticker as FreeStyleSticker
//                    index = listPhotoSelected.indexOf(freeStyleSticker.getPhoto())
//                    LogUtils.logD(FreeStyleActivity.TAG, "onStickerDragFinished", index)
                }
            }


            public override fun onStickerZoomFinished(sticker: Sticker) {
//                Log.d(FreeStyleActivity.TAG, "onStickerZoomFinished")
            }

            public override fun onTouchDownForBeauty(param1Float1: Float, param1Float2: Float) {
            }

            public override fun onTouchDragForBeauty(param1Float1: Float, param1Float2: Float) {
            }

            public override fun onTouchUpForBeauty(param1Float1: Float, param1Float2: Float) {
            }


            public override fun onStickerFlipped(sticker: Sticker) {
//                Log.d(FreeStyleActivity.TAG, "onStickerFlipped")
            }

            public override fun onStickerTouchOutside(param1Sticker: Sticker?) {
                param1Sticker?.isShow = true
                Log.d("stickerView", "onStickerTouchOutside")
//                if (currentFunction === Function.EDITOR_IMAGE) {
//                    index = -1
//                    LogUtils.logD(FreeStyleActivity.TAG, "onStickerTouchOutside", index)
//                    onBackPressed()
//                }
            }

            public override fun onStickerTouchedDown(sticker: Sticker) {
                Log.d("stickerView", "onStickerTouchedDown")
                stickerView.setShowFocus(true)
                if (sticker is TextSticker) {
                    stickerView.configDefaultIcons()
                } else if (sticker is DrawableSticker) {
                    stickerView.configStickerIcons()
                } else if (sticker is FreeStyleSticker) {
//                    val freeStyleSticker: FreeStyleSticker = (sticker as FreeStyleSticker)
//                    index = listPhotoSelected.indexOf(freeStyleSticker.getPhoto())
//                    LogUtils.logD(FreeStyleActivity.TAG, "onStickerTouchedDown", index)
                }
                stickerView.swapLayers()
                stickerView.invalidate()
//                if (currentFunction === Function.EDITOR_IMAGE && sticker !is FreeStyleSticker) {
//                    onBackPressed()
//                }
            }

            public override fun onStickerDoubleTapped(sticker: Sticker) {
//                Log.d(FreeStyleActivity.TAG, "onDoubleTapped: double tap will be with two click")
//                if (stickerView.getCurrentSticker() is TextSticker) {
//                }
            }
        })
    }
}