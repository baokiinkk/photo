package com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.databinding.ActivityRemoveObjectBinding
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.FeaturePhotoHeader
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.TEXT_TYPE
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.blur.BrushShapeSlider
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.crop.ToolInput
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object.lib.DialogAIGenerate
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object.lib.DrawingView
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object.lib.ObjAdapter
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object.lib.ObjAuto
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object.lib.RemoveObjState
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.remove_object.lib.Type
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.ui.base.BaseNativeActivity
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.clickableWithAlphaEffect
import com.basesource.base.utils.toJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class RemoveObjectActivity : BaseNativeActivity() {

    private val viewmodel: RemoveObjectViewModel by viewModel()

    private val screenInput: ToolInput? by lazy {
        intent.getInput()
    }

    private val binding by lazy {
        ActivityRemoveObjectBinding.inflate(layoutInflater)
    }

    private val objAdapter by lazy {
        ObjAdapter(this).apply {
            eventClickObj = {
                onObjAutoSelected(it)
            }
        }
    }

    private val btRemoveObState = MutableStateFlow(false)

    var removingDialog: DialogAIGenerate = DialogAIGenerate()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel.setOriginalBitmap(
            bitmap = screenInput?.getBitmap(this),
            newPathBitmap = cacheDir.absolutePath + "/BitmapOriginal_For_remove_obj.jpeg"
        )
        setContentView(binding.root)

        binding.recyclerV.adapter = objAdapter
        binding.recyclerV.itemAnimator = object : DefaultItemAnimator() {
            override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder) = true
        }

        binding.tvAll.isSelected = true

        binding.btAll.setOnClickListener {
            objAdapter.selectAll {
                binding.viewRemoveObject.drawingView?.setListObjSelected(it.filter { objAuto -> !objAuto.isRemoved })
                if (it.isNotEmpty()) {
                    btRemoveObState.value = true
                    binding.btRevemoObj.isEnabled = true
                    binding.btRevemoObj.isVisible = true
                }
            }
        }

        binding.btRevemoObj.setOnClickListener {

            Log.e("RemoveObjStateRemovingObj", "call removeObj:1.0")
            //doRemoveObj()

            Log.e("RemoveObjStateRemovingObj", "call removeObj:1.1")
            Log.e("RemoveObjStateRemovingObj", "call removeObj:1.2")

            binding.viewRemoveObject.drawingView?.let {
                doRemoveObj(it)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewmodel.listObjDetected.collect {
                    binding.tvAll.text = "All" + " (${it.size})"
                    objAdapter?.differ?.submitList(null)
                    objAdapter?.differ?.submitList(it)
                    Log.d("thanhc", " All ${it.size}")
                    Log.d("thanhc", " submitList ${it.first().toJson()}")
                    binding.viewRemoveObject.drawingView?.setListObjAuto(it)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewmodel.bitmapUIState.collect {
                    if (it.bitmap != null) {
                        binding.viewRemoveObject.registerView(
                            mBitmap = it.bitmap,
                            onDrawView = {
                                binding.btRevemoObj.isEnabled = false
                                binding.btRevemoObj.isVisible = false
                            },
                            onFinishDrawView = { isBrush ->
                                if (!isBrush) {
                                    binding.btRevemoObj.isEnabled = false
                                    binding.btRevemoObj.isVisible = false
                                } else {
                                    btRemoveObState.value = true
                                    binding.btRevemoObj.isEnabled = true
                                    binding.btRevemoObj.isVisible = true
                                }
                            },
                            eventClickObjView = { item ->
                                onObjAutoSelected(item)
                            }
                        )
                        activeTabFirst()
//                        binding.viewRemoveObject.setType(Type.BRUSH)
                    }
                }

            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewmodel.removeObjState.collect {
                    when (it) {
                        is RemoveObjState.DoneRemoving -> {
                            removingDialog.dismiss()
                            if (viewmodel.composeUIState.value.tab == RemoveObjectTab.TAB.AUTO) {
                                binding.frameTool.isVisible = true
                            }
                            binding.btRevemoObj.isEnabled = false
                            binding.btRevemoObj.isVisible = false
                            binding.viewRemoveObject.drawingView?.setBitmapDraw(it.bitmapResult)
                            binding.viewRemoveObject.drawingView?.setListObjSelected(null)
                            viewmodel.updateListObjDetected(objAdapter.listObjSelected)
                            objAdapter.listObjSelected?.clear()
//                            binding.btChangeImg.visibility = View.VISIBLE
                        }

                        is RemoveObjState.RemovingObj -> {
                            Log.e("RemoveObjStateRemovingObj", "removingDialog.show: ")
                            removingDialog.setContent(getString(R.string.content_removing_object))
                            removingDialog.show(supportFragmentManager, removingDialog.tag)
                        }

                        is RemoveObjState.Error -> {
                            removingDialog.dismiss()
                            Toast.makeText(
                                this@RemoveObjectActivity,
                                it.getErrorMessage(),
                                Toast.LENGTH_SHORT
                            ).show()

                        }

                        is RemoveObjState.None -> Unit
                        is RemoveObjState.DoneScanning -> {
                            removingDialog.dismiss()
                        }

                        is RemoveObjState.ScanningObj -> {
                            removingDialog.setContent(getString(R.string.content_removing_object))
                            removingDialog.show(supportFragmentManager, removingDialog.tag)
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewmodel.buttonState.collect {
                Log.i("TAG", "observerDataChangeứefwef: ${it}")

                when (it) {
                    ButtonState.CAN_PREV -> {
//                        setButtonState(binding.btNext, enabled = false)
//                        setButtonState(binding.btPrev, enabled = true)
                        viewmodel.updateUndoRedoState(
                            canUndo = true,
                            canRedo = false
                        )
//                        binding.btnReverse.isEnabled = true
                    }

                    ButtonState.CAN_NEXT -> {
//                        setButtonState(binding.btNext, enabled = true)
//                        setButtonState(binding.btPrev, enabled = false)
                        viewmodel.updateUndoRedoState(
                            canUndo = false,
                            canRedo = true
                        )
//                        binding.btnReverse.isEnabled = false
                    }

                    ButtonState.CAN_PREV_AND_NEXT -> {
//                        setButtonState(binding.btNext, enabled = true)
//                        setButtonState(binding.btPrev, enabled = true)
                        viewmodel.updateUndoRedoState(
                            canUndo = true,
                            canRedo = true
                        )
//                        binding.btnReverse.isEnabled = true
                    }

                    ButtonState.NONE -> {
//                        setButtonState(binding.btNext, false)
//                        setButtonState(binding.btPrev, false)
                        viewmodel.updateUndoRedoState(
                            canUndo = false,
                            canRedo = false
                        )
//                        binding.btnReverse.isEnabled = false
                    }

                    ButtonState.CAN_SAVE -> {
                        setButtonSaveState(true)
                    }

                    ButtonState.CAN_NOT_SAVE -> {
                        setButtonSaveState(false)
                    }
                }
            }
        }

        headerUI()
        footerUI(
            viewModel = viewmodel,
            onSliderBrushChange = {
                binding.viewRemoveObject.setStrokeWidth(strokeWidth = it, true)
                viewmodel.updateBlurBrush(it)
            },
            onValueChangeFinished = {
                binding.viewRemoveObject.setStrokeWidth(
                    strokeWidth = viewmodel.composeUIState.value.blurBrush,
                    false
                )
            },
            onAddLasso = {
                binding.viewRemoveObject.setType(Type.LASSO_BRUSH)
            },
            onSubtractLasso = {
                binding.viewRemoveObject.setType(Type.LASSO_ERASE)
            },
            onTabSelected = {
                when (it) {
                    RemoveObjectTab.TAB.AUTO -> {
                        binding.viewRemoveObject.setType(Type.SELECT_OBJ)
                        viewmodel.getObjDetectedAuto(binding.viewRemoveObject.drawingView)
                        binding.frameTool.isVisible = true
                    }

                    RemoveObjectTab.TAB.BRUSH -> {
                        binding.viewRemoveObject.setType(Type.BRUSH)
                        binding.frameTool.isInvisible = true
                    }

                    RemoveObjectTab.TAB.LASSO -> {
                        binding.viewRemoveObject.setType(Type.LASSO_BRUSH)
                        binding.frameTool.isInvisible = true
                    }
                }
            }
        )
    }

    private fun activeTabFirst() {
//        binding.viewRemoveObject.setType(Type.SELECT_OBJ)
//        viewmodel.getObjDetectedAuto(binding.viewRemoveObject.drawingView)
        binding.frameTool.isInvisible = true
        binding.viewRemoveObject.setType(Type.BRUSH)
    }

    private fun setButtonSaveState(isEnable: Boolean) {
        viewmodel.canSaveState.value = isEnable
//        binding.btSave.isEnabled = isEnable
//        if (isEnable) {
//            binding.btSave.setTextColor(android.graphics.Color.parseColor("#615BFD"))
//        } else {
//            binding.btSave.setTextColor(android.graphics.Color.parseColor("#BABABB"))
//        }
    }

    private fun setButtonState(view: ImageView, enabled: Boolean) {
        view.apply {
            isEnabled = enabled
        }
    }

    private fun onObjAutoSelected(objAuto: ObjAuto) {
        if (objAuto.isRemoved) {
            return
        }
        if (objAdapter?.listObjSelected?.contains(objAuto) == true) {
            objAdapter?.listObjSelected?.remove(objAuto)
        } else {
            objAdapter?.listObjSelected?.add(objAuto)
            objAdapter?.differ?.currentList?.indexOf(objAuto)?.let {
                if (it != -1) {
                    binding.recyclerV.smoothScrollToPosition(it)
                }
            }
        }
        objAdapter?.notifyItemRangeChanged(
            0, objAdapter!!.differ.currentList.size
        )
        binding.viewRemoveObject.drawingView?.setListObjSelected(objAdapter!!.listObjSelected)
        if (objAdapter!!.listObjSelected.isNotEmpty()) {
            btRemoveObState.value = true
            binding.btRevemoObj.isEnabled = true
            binding.btRevemoObj.isVisible = true
        }
    }

    private fun doRemoveObj(drawingView: DrawingView) {
        Log.e("RemoveObjStateRemovingObj", "call removeObj:1 ")
        viewmodel.removeObj(
            drawingView,
            listObjSelected = objAdapter.listObjSelected,
        )
    }

    private fun headerUI() {
        binding.composeHeader.setContent {
            val undoRedoState by viewmodel.undoRedoState.collectAsStateWithLifecycle()
            val canSaveState by viewmodel.canSaveState.collectAsStateWithLifecycle()

            FeaturePhotoHeader(
                onBack = {
                    finish()
                },
                onUndo = {
                    viewmodel.setCurrImageIndex(false) { bm ->
                        binding.viewRemoveObject.drawingView?.setBitmapDraw(bm)
                    }

                },
                onRedo = {
                    viewmodel.setCurrImageIndex(true) { bm ->
                        binding.viewRemoveObject.drawingView?.setBitmapDraw(bm)
                    }
                },
                onSave = {
                    viewmodel.saveImg { pathSave ->
                        Log.d("aaa", "----- $pathSave")
                        if (pathSave != null) {
                            val intent = Intent()
                            intent.putExtra("pathBitmap", "$pathSave")
                            setResult(RESULT_OK, intent)
                            finish()
                        } else {
                            finish()
                        }
                    }
                },
                canUndo = undoRedoState.canUndo,
                canRedo = undoRedoState.canRedo,
                type = TEXT_TYPE.TEXT,
                canSave = canSaveState
            )
        }
    }

    private fun footerUI(
        viewModel: RemoveObjectViewModel,
        onSliderBrushChange: (Float) -> Unit,
        onAddLasso: () -> Unit,
        onSubtractLasso: () -> Unit,
        onTabSelected: (RemoveObjectTab.TAB) -> Unit,
        onValueChangeFinished: (() -> Unit)? = null
    ) {
        binding.composeFooter.setContent {
            val uiState by viewModel.composeUIState.collectAsStateWithLifecycle()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(top = 16.dp)
                    .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        uiState.tabs.forEach { item ->
                            RemoveObjectTab(
                                item = item,
                                isSelected = item.tab == uiState.tab,
                                onClick = {
                                    viewmodel.updateTabIndex(item.tab)
                                    onTabSelected.invoke(item.tab)
                                }
                            )
                        }
                    }
                }

                when (uiState.tab) {
                    RemoveObjectTab.TAB.AUTO -> {

                    }

                    RemoveObjectTab.TAB.BRUSH -> {
                        Spacer(modifier = Modifier.height(36.dp))
                        TabBrush(
                            uiState = uiState,
                            onSliderBrushChange = onSliderBrushChange,
                            onValueChangeFinished = onValueChangeFinished
                        )
                    }

                    RemoveObjectTab.TAB.LASSO -> {
                        Spacer(modifier = Modifier.height(24.dp))
                        TabLasso(
                            onAddLasso = onAddLasso,
                            onSubtractLasso = onSubtractLasso
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabLasso(
    onAddLasso: () -> Unit,
    onSubtractLasso: () -> Unit
) {
    var tabIndex by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (tabIndex == 0) Color(0xFFF2F4F7) else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .clickableWithAlphaEffect {
                        tabIndex = 0
                        onAddLasso.invoke()
                    },
                contentAlignment = Alignment.Center
            ) {
                ImageWidget(
                    resId = R.drawable.ic_lasso_add
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (tabIndex == 1) Color(0xFFF2F4F7) else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .clickableWithAlphaEffect {
                        tabIndex = 1
                        onSubtractLasso.invoke()
                    },
                contentAlignment = Alignment.Center
            ) {
                ImageWidget(resId = R.drawable.ic_lasso_subtract)
            }
        }
    }

}

@Composable
fun TabBrush(
    uiState: RemoveObjectComposeUIState,
    onSliderBrushChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null
) {
    BrushShapeSlider(
        value = uiState.blurBrush,
        onValueChange = onSliderBrushChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        onValueChangeFinished = onValueChangeFinished
    )
}

@Composable
fun RemoveObjectTab(
    item: RemoveObjectTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = if (isSelected) {
        AppColor.Primary500
    } else {
        Color(0xFF1D2939)
    }
    Column(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickableWithAlphaEffect(onClick = onClick)
    ) {
        ImageWidget(
            resId = item.icon,
            modifier = Modifier.size(32.dp),
            tintColor = color
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(item.stringResId),
            style = if (isSelected) AppStyle.caption2().medium()
                .primary500() else AppStyle.caption2().medium().Color_1D2939(),
        )
    }
}

