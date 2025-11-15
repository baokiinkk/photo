package com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import androidx.core.graphics.createBitmap
import androidx.lifecycle.viewModelScope
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.data.repository.RemoveObjectRepoImpl
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object.lib.DrawingView
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object.lib.ObjAuto
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object.lib.RemoveObjState
import com.basesource.base.viewmodel.BaseViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.io.File
import java.io.FileOutputStream

@KoinViewModel
class RemoveObjectViewModel(
    private val context: Application,
    private val removeObjectRepoImpl: RemoveObjectRepoImpl
) : BaseViewModel() {

    val uiState = MutableStateFlow(RemoveObjectUIState())
    val composeUIState = MutableStateFlow(RemoveObjectComposeUIState())

    val tabs = listOf(
        RemoveObjectTab(
            tab = RemoveObjectTab.TAB.AUTO,
            stringResId = R.string.auto,
            icon = R.drawable.ic_remove_object_ai
        ),
        RemoveObjectTab(
            tab = RemoveObjectTab.TAB.BRUSH,
            stringResId = R.string.brush,
            icon = R.drawable.ic_remove_object_brush
        ),
        RemoveObjectTab(
            tab = RemoveObjectTab.TAB.LASSO,
            stringResId = R.string.lasso,
            icon = R.drawable.ic_remove_object_lasso
        )
    )

    init {
        initData()
    }

    val listPathImgRemoved = ArrayList<String>()


    fun setOriginalBitmap(
        bitmap: Bitmap?,
        newPathBitmap: String,
    ) {
        viewModelScope.launch {
            if (bitmap != null) {
                FileOutputStream(newPathBitmap, false).use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                }
                listPathImgRemoved.add(newPathBitmap)
            }
            uiState.update {
                it.copy(
                    bitmap = bitmap
                )
            }
        }
    }

    fun initData() {
        composeUIState.update {
            it.copy(
                tabs = tabs
            )
        }
    }

    fun updateBlurBrush(blur: Float) {
        composeUIState.update {
            it.copy(blurBrush = blur)
        }
    }

    fun updateTabIndex(tab: RemoveObjectTab.TAB) {
        composeUIState.update {
            it.copy(tab = tab)
        }
    }

    private val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        .also { it.eraseColor(Color.parseColor("#E6EDFF")) }
    private val rect = RectF()

    private val listObjDefault = buildList {
        (0..7).onEach {
            add(
                ObjAuto(
                    nameObj = "",
                    bitmapMask = bitmap,
                    rectBitmapMask = rect,
                    bitmapMaskPreview = bitmap
                )
            )
        }
    }
    private val _listObjDetected = MutableStateFlow(listObjDefault)
    val listObjDetected = _listObjDetected.asStateFlow()

    fun isListObjDetected() = _listObjDetected.value != listObjDefault

    interface AMGUtilCallBack {
        fun decrypt(source: String?): String

        fun encryptFile(
            source: String?,
            file: File?
        ): String
    }

    private var jobDetectObjAuto: Job? = null
    private val _removeObjState = MutableStateFlow<RemoveObjState>(RemoveObjState.None)
    val removeObjState = _removeObjState.asStateFlow()
    private var currIndexImg = 0


    @SuppressLint("StringFormatMatches")
    fun getObjDetectedAuto(
        context: Context,
        drawingView: DrawingView,
        tokenApi: String,
    ) {

        if (!isListObjDetected()) {
            val start = System.currentTimeMillis()
            _removeObjState.value = RemoveObjState.ScanningObj
            jobDetectObjAuto?.cancel()
            jobDetectObjAuto = viewModelScope.launch(Dispatchers.IO) {
                try {
                    val fileOrigin = File(listPathImgRemoved[currIndexImg])
                    val response = removeObjectRepoImpl.genAutoDetect(fileOrigin)
                    resquestResponseAutoDetech(
                        id = response.id,
                        drawingView = drawingView,
                        tokenApi = tokenApi
                    )
                } catch (e: Exception) {
                    Log.i("TAG", "requestImagsdfesdfsdf: 2 $e ")
//                    sendEvent("Auto", "fail", start)
                    _removeObjState.value = RemoveObjState.Error(e)
                    cancel()
                }
            }
        }
    }

    private fun resquestResponseAutoDetech(
        id: String,
        drawingView: DrawingView,
        tokenApi: String,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val gson = Gson()
            val bitmapOriginal = listPathImgRemoved[currIndexImg].getBitmapOriginal()
            val canvas = Canvas()
            var continueRequest = true

            while (continueRequest) {
                try {
                    val responsePostAI = removeObjectRepoImpl.getProgress(id)
                    val startDownload = System.currentTimeMillis()
                    if (responsePostAI != null) {
                        continueRequest = false
                        launch {
                            buildList {

                                val matrix = Matrix()
                                List(responsePostAI.result.size) { index ->
                                    val result = responsePostAI.result[index]
                                    launch {
                                        context.getBitmapFromUrl(
                                            result.maskImg
                                        )?.let { bitmapMask ->
                                            val bitmapDraw = createBitmap(
                                                bitmapOriginal.width,
                                                bitmapOriginal.height,
                                                bitmapOriginal.config!!
                                            )

                                            canvas.setBitmap(bitmapDraw)
                                            canvas.drawBitmap(
                                                bitmapMask,
                                                result.boxRect[0].toFloat(),
                                                result.boxRect[1].toFloat(),
                                                null
                                            )
                                            val bitmapMaskScale =
                                                Bitmap.createScaledBitmap(
                                                    bitmapDraw,
                                                    drawingView.widthImg,
                                                    drawingView.heightImg,
                                                    true
                                                )

                                            val bitmapMaskPreview =
                                                Bitmap.createBitmap(
                                                    bitmapOriginal,
                                                    result.boxRect[0],
                                                    result.boxRect[1],
                                                    result.boxRect[2] - result.boxRect[0],
                                                    result.boxRect[3] - result.boxRect[1]
                                                )

                                            bitmapDraw.recycle()

                                            val scaleX =
                                                bitmapMaskScale.width.toFloat() / bitmapOriginal.width.toFloat()
                                            val scaleY =
                                                bitmapMaskScale.height.toFloat() / bitmapOriginal.height.toFloat()
                                            val rectBitmapMaskScale = RectF(
                                                result.boxRect[0].toFloat() * scaleX,
                                                result.boxRect[1].toFloat() * scaleY,
                                                result.boxRect[2].toFloat() * scaleX,
                                                result.boxRect[3].toFloat() * scaleY
                                            )

                                            matrix.reset()
                                            matrix.setTranslate(
                                                drawingView.minx.toFloat(),
                                                drawingView.miny.toFloat()
                                            )
                                            matrix.mapRect(rectBitmapMaskScale)

                                            add(
                                                ObjAuto(
                                                    nameObj = result.objName,
                                                    bitmapMask = bitmapMaskScale,
                                                    rectBitmapMask = rectBitmapMaskScale,
                                                    bitmapMaskPreview = bitmapMaskPreview
                                                )
                                            )
                                        }
                                    }

                                }.joinAll()
                            }.let {

                                _listObjDetected.value = it
                            }


                            Log.i(
                                "TAG_REMOVE_OBJ_AUTO",
                                "time downloaded: ${
                                    (System.currentTimeMillis() - startDownload)
//                                    / 1000
                                }"
                            )

                            _removeObjState.value = RemoveObjState.DoneScanning
                            cancel()
                        }
                    } else {
                        cancel()
                    }
                } catch (ex: Exception) {
                    continueRequest = false
                }
            }

        }
    }
}

data class RemoveObjectUIState(
    val bitmap: Bitmap? = null,
)

data class RemoveObjectComposeUIState(
    val blurBrush: Float = 50f,
    val tabs: List<RemoveObjectTab> = emptyList(),
    val tab: RemoveObjectTab.TAB = RemoveObjectTab.TAB.AUTO
)

data class RemoveObjectTab(
    val tab: TAB,
    val stringResId: Int,
    val icon: Int
) {
    enum class TAB {
        AUTO,
        BRUSH,
        LASSO
    }
}

fun String.getBitmapOriginal(): Bitmap {
    return BitmapFactory.decodeFile(this)
}

fun Context.getBitmapFromUrl(
    url: String, size: Pair<Int, Int> = Pair(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
): Bitmap? {
    return try {
        Glide.with(this).asBitmap().load(url).submit(size.first, size.second)
            .get()
    } catch (e: Exception) {
        Log.i("TAG", "getBitmapFromUrlaergae: $e")
        null
    }
}


