package com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object

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
import com.basesource.base.utils.toJson
import com.basesource.base.viewmodel.BaseViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel
import java.io.File
import java.io.FileOutputStream
import java.net.URL

enum class ButtonState {

    CAN_PREV, CAN_NEXT, CAN_PREV_AND_NEXT, NONE, CAN_SAVE, CAN_NOT_SAVE
}

@KoinViewModel
class RemoveObjectViewModel(
    private val context: Application,
    private val removeObjectRepoImpl: RemoveObjectRepoImpl
) : BaseViewModel() {

    val bitmapUIState = MutableStateFlow(BitmapUIState())
    val composeUIState = MutableStateFlow(RemoveObjectComposeUIState())

    val undoRedoState = MutableStateFlow((UndoRedoState()))

    val canSaveState = MutableStateFlow(false)

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
    private val folderTemp = context.cacheDir.absolutePath + "/ImageRemoveObjTemp"


    init {
        initData()
        val folder = File(folderTemp)
        folder.deleteRecursively()
        if (!folder.exists()) {
            folder.mkdirs()
        }

    }

    val listPathImgRemoved = ArrayList<String>()

    private val _buttonState = MutableStateFlow(ButtonState.NONE)
    val buttonState = _buttonState.asStateFlow()

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
            bitmapUIState.update {
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

    private var jobDetectObjAuto: Job? = null
    private val _removeObjState = MutableStateFlow<RemoveObjState>(RemoveObjState.None)
    val removeObjState = _removeObjState.asStateFlow()
    private var currIndexImg = 0

    private var originalPathBitmap: String? = null


    fun getObjDetectedAuto(
        drawingView: DrawingView?,
    ) {
        if (drawingView == null) return
        if (!isListObjDetected()) {
            val start = System.currentTimeMillis()
            _removeObjState.value = RemoveObjState.ScanningObj
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val fileOrigin = File(listPathImgRemoved[currIndexImg])
                    val response = removeObjectRepoImpl.genAutoDetect(fileOrigin)
                    Log.d("aaa", "${response.toJson()}")
                    resquestResponseAutoDetech(
                        id = response.id,
                        drawingView = drawingView,
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
                } catch (ex: Exception) {
//                    continueRequest = false
//                    _removeObjState.value = RemoveObjState.Error(ex)
                }
                if (continueRequest) {
                    delay(4000)
                }
            }

        }
    }

    fun refreshTokenFirebase() {
        viewModelScope.launch(Dispatchers.IO) {
            removeObjectRepoImpl.getTokenFirebase()
        }
    }

    var isRemoveObj = false
        private set

    fun removeObj(
        drawingView: DrawingView,
        listObjSelected: ArrayList<ObjAuto>?,
    ) {
        Log.e("RemoveObjStateRemovingObj", "call removeObj:2 ")
        isRemoveObj = true
        Log.e("RemoveObjStateRemovingObj", "call removeObj:3 ")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.e("RemoveObjStateRemovingObj", "call removeObj:4 ")
                Log.e("RemoveObjStateRemovingObj", "call removeObj:5")

                _removeObjState.value = RemoveObjState.RemovingObj
                val mask = drawingView.getMask()
                val canvas = Canvas(mask)
                Log.d("TAG", "removeObj: ${listPathImgRemoved.size}")
                val originalBitmap = listPathImgRemoved[currIndexImg].getBitmapOriginal()
                val originalBitmapWidth = originalBitmap.width
                val originalBitmapHeight = originalBitmap.height
                originalBitmap.recycle()
                listObjSelected?.onEach { objAuto ->
                    if (!objAuto.isRemoved) {
                        val bitmapMaskScale = Bitmap.createScaledBitmap(
                            objAuto.bitmapMask, originalBitmapWidth, originalBitmapHeight, true
                        )
                        canvas.drawBitmap(bitmapMaskScale, 0f, 0f, null)
                        bitmapMaskScale.recycle()
                    }
                }

                val pathSaveMask = context.cacheDir.absolutePath + "/BitmapMask.jpeg"

                FileOutputStream(pathSaveMask, false).use {
                    mask.compress(Bitmap.CompressFormat.JPEG, 100, it)
                }
                val fileMask = File(pathSaveMask)

                Log.d("TAG", "removeObj: ${listPathImgRemoved[currIndexImg]}")
                val fileOrigin = File(listPathImgRemoved[currIndexImg])

                val response = removeObjectRepoImpl.genRemoveObject(
                    fileMask = fileMask,
                    fileOrigin = fileOrigin
                )

                resquestResponseRemoveObj(response.id)
            } catch (e: Exception) {
                Log.i("TAG", "requestImagesdfsdf: 2 $e ")
                _removeObjState.value = RemoveObjState.Error(e)
                cancel()
            }
        }
    }


    private fun resquestResponseRemoveObj(
        id: String,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            var continueRequest = true
            while (continueRequest) {
                try {
                    val responsePostAI = removeObjectRepoImpl.getProgressRemoveObject(id)
                    continueRequest = false
                    val pathSave = folderTemp + "/${System.currentTimeMillis()}.jpeg"
                    responsePostAI.result.url.downloadAndSaveToFile(pathSave)
                    val bitmapRemoved = pathSave.getBitmapOriginal()
                    listPathImgRemoved.add(pathSave)
                    currIndexImg = listPathImgRemoved.size - 1
                    _buttonState.value = ButtonState.CAN_SAVE
                    delay(100)
                    _buttonState.value = ButtonState.CAN_PREV
                    _removeObjState.value = RemoveObjState.DoneRemoving(bitmapRemoved)
                    cancel()
                } catch (ex: Exception) {

                }
                if (continueRequest) {
                    delay(4000)
                }
            }
        }
    }

    fun updateListObjDetected(listObjSelected: List<ObjAuto>?) {
        listObjSelected?.let { listObjAuto ->
            _listObjDetected.value = _listObjDetected.value.map {
                if (listObjAuto.contains(it)) {
                    it.copy(isRemoved = true)
                } else {
                    it
                }
            }
        }
    }

    fun updateUndoRedoState(canUndo: Boolean, canRedo: Boolean) {
        undoRedoState.update {
            it.copy(
                canUndo = canUndo,
                canRedo = canRedo
            )
        }
    }

    fun setCurrImageIndex(isNext: Boolean, onDone: (Bitmap) -> Unit) {

        viewModelScope.launch {
            val size = listPathImgRemoved.size

            if (size == 1) {
                _buttonState.value = ButtonState.NONE
                return@launch
            }

            if (isNext && currIndexImg < size - 1) {
                currIndexImg++
            } else if (!isNext && currIndexImg > 0) {
                currIndexImg--
            }

            if (currIndexImg == 0) {
                _buttonState.value = ButtonState.CAN_NOT_SAVE
            } else {
                _buttonState.value = ButtonState.CAN_SAVE
            }
            withContext(Dispatchers.IO) {
                listPathImgRemoved[currIndexImg].getBitmapOriginal()
            }.let(onDone)

            if (currIndexImg == 0) {
                _buttonState.value = ButtonState.CAN_NEXT
            } else if (size > 1 && currIndexImg == size - 1) {
                _buttonState.value = ButtonState.CAN_PREV
            } else {
                _buttonState.value = ButtonState.CAN_PREV_AND_NEXT
            }
        }
    }

    fun saveImg(onDone: (pathBitmap: String?) -> Unit) {
        onDone.invoke(listPathImgRemoved[currIndexImg])
//        viewModelScope.launch(Dispatchers.IO) {
//            val bitmapSave: Bitmap? = listPathImgRemoved[currIndexImg].getBitmapOriginal()
//            onDone.invoke(bitmapSave)
//        }
    }

}

suspend fun String.downloadAndSaveToFile(pathSave: String) = withContext(Dispatchers.IO) {
    val url = URL(this@downloadAndSaveToFile)
    val connection = url.openConnection()
    connection.connect()
    url.openStream().use input@{ input ->
        FileOutputStream(pathSave, false).use { output ->
            input.copyTo(output)
        }
    }
}

data class BitmapUIState(
    val bitmap: Bitmap? = null,
)

data class UndoRedoState(
    val canUndo: Boolean = false,
    val canRedo: Boolean = false
)


data class RemoveObjectComposeUIState(
    val blurBrush: Float = 50f,
    val tabs: List<RemoveObjectTab> = emptyList(),
    val tab: RemoveObjectTab.TAB = RemoveObjectTab.TAB.BRUSH
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


