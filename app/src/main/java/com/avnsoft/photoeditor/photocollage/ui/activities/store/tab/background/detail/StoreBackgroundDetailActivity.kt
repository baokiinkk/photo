package com.avnsoft.photoeditor.photocollage.ui.activities.store.tab.background.detail

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.data.model.pattern.PatternModel
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.CollageTool
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.EditorActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.EditorInput
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.ImagePickerActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.ImagePickerActivity.Companion.RESULT_URI
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.ImageRequest
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.TypeSelect
import com.avnsoft.photoeditor.photocollage.ui.activities.store.HeaderStore
import com.avnsoft.photoeditor.photocollage.ui.activities.store.tab.sticker.detail.ButtonUnlockPack
import com.avnsoft.photoeditor.photocollage.ui.activities.store.tab.sticker.detail.ButtonUsePack
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.ui.image.LoadImage
import com.basesource.base.utils.clickableWithAlphaEffect
import com.basesource.base.utils.fromJson
import com.basesource.base.utils.fromJsonTypeToken
import com.basesource.base.utils.launchActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class StoreBackgroundDetailActivity : BaseActivity() {

    private val viewmodel: StoreBackgroundDetailViewModel by viewModel()

    private val screenInput: PatternModel? by lazy {
        intent.getInput()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel.initData(screenInput)
        setContent {
            val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
            Scaffold(
                containerColor = AppColor.White
            ) { inner ->
                Column(
                    modifier = Modifier
                        .padding(
                            top = inner.calculateTopPadding(),
                            bottom = inner.calculateBottomPadding(),
                            start = 16.dp,
                            end = 16.dp
                        )
                ) {
                    HeaderStore(
                        title = stringResource(R.string.background_pack)
                    )
                    uiState.item?.let { data ->
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF8FAFC))
                        ) {
                            LazyVerticalGrid(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                columns = GridCells.Fixed(2),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 16.dp)
                            ) {
                                uiState.item?.let { data ->
                                    items(data.content) { item ->
                                        LoadImage(
                                            model = item.urlThumb,
                                            modifier = Modifier
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(20.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }

                            if (uiState.item?.isUsed == true) {
                                ButtonUsePack(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp)
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 24.dp)
                                        .clickableWithAlphaEffect {
                                            gotoEditPhoto()
                                        }
                                )
                            } else {
                                ButtonUnlockPack(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp)
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 24.dp)
                                        .clickableWithAlphaEffect {
                                            viewmodel.updateIsUsedById(data.eventId)
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun gotoEditPhoto() {
        launchActivity(
            toActivity = ImagePickerActivity::class.java,
            ImageRequest(TypeSelect.SINGLE)
        ) { result ->
            val data: List<String>? = result.data?.getStringExtra(RESULT_URI)?.fromJsonTypeToken()
            data?.firstOrNull()?.let {
                launchActivity(
                    toActivity = EditorActivity::class.java,
                    input = EditorInput(
                        pathBitmap = it,
                        tool = CollageTool.BACKGROUND
                    ),
                )
            }
        }
    }
}