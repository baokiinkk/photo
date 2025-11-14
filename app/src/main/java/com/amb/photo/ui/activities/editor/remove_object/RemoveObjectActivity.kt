package com.amb.photo.ui.activities.editor.remove_object

import android.os.Bundle
import android.util.Log
import android.view.View
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
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.amb.photo.R
import com.amb.photo.databinding.ActivityRemoveObjectBinding
import com.amb.photo.ui.activities.collage.components.FeaturePhotoHeader
import com.amb.photo.ui.activities.editor.blur.BrushShapeSlider
import com.amb.photo.ui.activities.editor.crop.ToolInput
import com.amb.photo.ui.activities.editor.remove_object.lib.RemoveObjState
import com.amb.photo.ui.activities.editor.remove_object.lib.Type
import com.amb.photo.ui.theme.AppColor
import com.amb.photo.ui.theme.AppStyle
import com.amb.photo.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.clickableWithAlphaEffect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class RemoveObjectActivity : BaseActivity() {

    private val viewmodel: RemoveObjectViewModel by viewModel()

    private val screenInput: ToolInput? by lazy {
        intent.getInput()
    }

    private val binding by lazy {
        ActivityRemoveObjectBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel.setOriginalBitmap(
            bitmap = screenInput?.getBitmap(this),
            newPathBitmap = cacheDir.absolutePath + "/BitmapOriginal_For_remove_obj.jpeg"
        )
        setContentView(binding.root)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewmodel.uiState.collect {
                    if (it.bitmap != null) {
                        binding.viewRemoveObject.registerView(it.bitmap)
                        binding.viewRemoveObject.setType(Type.BRUSH)
                    }
                }

            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewmodel.removeObjState.collect {
                    when (it) {
                        is RemoveObjState.DoneRemoving -> {
//                            removingDialog.dismiss()
//                            binding.btRevemoObj.isEnabled = false
//                            binding.btRevemoObj.isVisible = false
//                            drawingView?.setBitmapDraw(it.bitmapResult)
//                            drawingView?.setListObjSelected(null)
//                            removeObjVM?.updateListObjDetected(objAdapter?.listObjSelected)
//                            objAdapter?.listObjSelected?.clear()
//                            binding.btChangeImg.visibility = View.VISIBLE
                        }

                        is RemoveObjState.RemovingObj -> {
                            Log.e("RemoveObjStateRemovingObj", "removingDialog.show: ", )
//                            removingDialog.setContent(getString(R.string.content_removing_object))
//                            removingDialog.show(supportFragmentManager, removingDialog.tag)
                        }

                        is RemoveObjState.Error -> {
//                            removingDialog.dismiss()
                            Toast.makeText(this@RemoveObjectActivity, it.getErrorMessage(), Toast.LENGTH_SHORT).show()

                        }

                        is RemoveObjState.None -> Unit
                        is RemoveObjState.DoneScanning -> {
//                            removingDialog.dismiss()
//                            setAutoDetectedObjLayoutState()
                        }

                        is RemoveObjState.ScanningObj -> {
//                            removingDialog.setContent(getString(R.string.content_detecting_object))
//                            removingDialog.show(supportFragmentManager, removingDialog.tag)
                        }
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
            onAddLasso = {
                binding.viewRemoveObject.setType(Type.LASSO_BRUSH)
            },
            onSubtractLasso = {
                binding.viewRemoveObject.setType(Type.LASSO_ERASE)
            },
            onTabSelected = {
                when(it){
                    RemoveObjectTab.TAB.AUTO -> {
                        binding.viewRemoveObject.setType(Type.SELECT_OBJ)
                    }
                    RemoveObjectTab.TAB.BRUSH -> {
                        binding.viewRemoveObject.setType(Type.BRUSH)
                    }
                    RemoveObjectTab.TAB.LASSO -> {
                        binding.viewRemoveObject.setType(Type.LASSO_BRUSH)
                    }
                }
            }
        )
    }

    private fun headerUI() {
        binding.composeHeader.setContent {
            FeaturePhotoHeader(
                onUndo = {},
                onRedo = {},
                onSave = {},
                canUndo = false,
                canRedo = false
            )
        }
    }

    private fun footerUI(
        viewModel: RemoveObjectViewModel,
        onSliderBrushChange: (Float) -> Unit,
        onAddLasso: () -> Unit,
        onSubtractLasso: () -> Unit,
        onTabSelected: (RemoveObjectTab.TAB) -> Unit
    ) {
        binding.composeFooter.setContent {
            val uiState by viewModel.composeUIState.collectAsStateWithLifecycle()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                            onSliderBrushChange = onSliderBrushChange
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
    onSliderBrushChange: (Float) -> Unit
) {
    BrushShapeSlider(
        value = uiState.blurBrush,
        onValueChange = onSliderBrushChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
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

