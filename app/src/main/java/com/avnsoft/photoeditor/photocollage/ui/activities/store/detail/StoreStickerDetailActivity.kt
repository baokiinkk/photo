package com.avnsoft.photoeditor.photocollage.ui.activities.store.detail

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.data.model.sticker.StickerModel
import com.avnsoft.photoeditor.photocollage.ui.activities.store.HeaderStore
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor
import com.avnsoft.photoeditor.photocollage.utils.getInput
import com.basesource.base.components.CustomButton
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.ui.image.LoadImage
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
                    LoadImage(
                        model = uiState.item?.bannerUrl,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFFFFF1E5),
                                shape = RoundedCornerShape(20.dp)
                            )
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                        ) {
                            items(uiState.item?.content ?: emptyList()) { item ->
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

                }
            }
        }
    }
}

@Composable
fun ItemStoreSticker(
    modifier: Modifier
) {

}