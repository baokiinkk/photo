package com.avnsoft.photoeditor.photocollage.ui.activities.store.tab.sticker.detail

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.data.model.sticker.StickerModel
import com.avnsoft.photoeditor.photocollage.ui.activities.collage.components.CollageTool
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.EditorActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.EditorInput
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.ImagePickerActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.ImagePickerActivity.Companion.RESULT_URI
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.ImageRequest
import com.avnsoft.photoeditor.photocollage.ui.activities.imagepicker.TypeSelect
import com.avnsoft.photoeditor.photocollage.ui.activities.store.HeaderStore
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.ui.image.LoadImage
import com.basesource.base.utils.Effects
import com.basesource.base.utils.ImageWidget
import com.basesource.base.utils.backgroundLinearGradient
import com.basesource.base.utils.clickableWithAlphaEffect
import com.basesource.base.utils.figmaShadow
import com.basesource.base.utils.fromJson
import com.basesource.base.utils.fromJsonTypeToken
import com.basesource.base.utils.launchActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class StoreStickerDetailActivity : BaseActivity() {

    private val viewModel: StoreStickerDetailViewModel by viewModel()

    private val screenInput: StickerModel? by lazy {
        intent.getInput()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initData(screenInput)
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
                        title = stringResource(R.string.sticker_pack)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    uiState.item?.let { item ->
                        LoadImage(
                            model = item.bannerUrl,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(20.dp))
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Box {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (item.isUsed) Color(0xFFFFF1E5) else Color(
                                            0xFFF3F2F2
                                        ),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                            ) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(4),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(10.dp)
                                        .alpha(if (item.isUsed) 1f else 0.5f),
                                ) {
                                    items(item.content) { item ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                        ) {
                                            LoadImage(
                                                model = item.urlThumb,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .aspectRatio(1f),
                                                contentScale = ContentScale.Fit
                                            )
                                        }
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
                                            viewModel.updateIsUsedById(item.eventId)
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
                        tool = CollageTool.STICKER
                    ),
                )
            }
        }
    }
}

@Composable
fun ButtonUsePack(
    modifier: Modifier,
    text: String = stringResource(R.string.use_sticker_pack)
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color(0x33101828),
                ambientColor = Color(0x33101828)
            )
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 16.dp
        ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = AppStyle.buttonLarge().semibold().primary500()
            )
        }
    }
}

@Composable
fun ButtonUnlockPack(
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.unlock_sticker_pack)
) {
    Row(
        modifier = modifier
//            .shadow(
//                elevation = 16.dp,
//                shape = RoundedCornerShape(12.dp),
//                spotColor = Color(0x666425F3),
//                ambientColor = Color(0x666425F3)
//            )
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .backgroundLinearGradient(
                colors = listOf(
                    Color(0xFFF7ACEF),
                    Color(0xFF6425F3)
                )
            )
//            .background(
//                brush = Brush.linearGradient(
//                    colors = listOf(
//                        Color(0xFFF7ACEF),
//                        Color(0xFF6425F3)
//                    )
//                )
//            )
        ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        ImageWidget(resId = R.drawable.ic_store_star)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = AppStyle.buttonLarge().semibold().white(),
        )
    }
}
