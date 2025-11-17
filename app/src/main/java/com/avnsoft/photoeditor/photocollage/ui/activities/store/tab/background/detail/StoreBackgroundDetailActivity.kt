package com.avnsoft.photoeditor.photocollage.ui.activities.store.tab.background.detail

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.data.model.pattern.PatternModel
import com.avnsoft.photoeditor.photocollage.data.model.sticker.StickerModel
import com.avnsoft.photoeditor.photocollage.ui.activities.store.HeaderStore
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.ui.image.LoadImage
import com.basesource.base.utils.ImageWidget
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
                        .background(Color(0xFFF8FAFC))
                ) {
                    HeaderStore(
                        title = stringResource(R.string.background_pack)
                    )
                    Box {
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

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                ,
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                            ) {
                                ImageWidget(
                                    resId = R.drawable.button_gradient_shadow,
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp, start = 14.dp, end = 18.dp)
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                ,
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                ImageWidget(resId = R.drawable.ic_store_star)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.unlock_sticker_pack),
                                    style = AppStyle.buttonLarge().semibold().white(),
                                )
                            }
                        }

                    }
                }
            }
        }
    }
}