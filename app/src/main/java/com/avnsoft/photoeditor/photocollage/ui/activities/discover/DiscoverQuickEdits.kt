package com.avnsoft.photoeditor.photocollage.ui.activities.discover

import androidx.annotation.RawRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.basesource.base.utils.clickableWithAlphaEffect
data class StaggeredItem(
    val id: String,
    val title: String,
    @RawRes val imageRes: LottieCompositionSpec.RawRes,
)

@Composable
fun DiscoverQuickEdits(onItemClick: (StaggeredItem) -> Unit) {
    val data by remember {
        mutableStateOf(
            listOf(
                StaggeredItem("1", "Background", LottieCompositionSpec.RawRes(R.raw.background)),
                StaggeredItem("2", "Filter", LottieCompositionSpec.RawRes(R.raw.filter)),
                StaggeredItem("3", "Sticker", LottieCompositionSpec.RawRes(R.raw.stickers)),
                StaggeredItem("4", "Add Text", LottieCompositionSpec.RawRes(R.raw.text)),
                StaggeredItem("5", "Frame", LottieCompositionSpec.RawRes(R.raw.frame)),
                StaggeredItem("6", "Doodle", LottieCompositionSpec.RawRes(R.raw.doodle))
            )
        )
    }
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 12.dp)
    ) {
        Row {
            Text(text = stringResource(R.string.quick_edits), style = AppStyle.title1().bold().gray900())
            Image(
                painterResource(R.drawable.ic_quicks), contentDescription = "", modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        QuickEditsStaggeredGrid(
            data = data,
            onItemClick = onItemClick,
            contentPadding = PaddingValues(bottom = 28.dp)
        )
    }
}

@Composable
fun QuickEditsStaggeredGrid(
    data: List<StaggeredItem>,
    onItemClick: (StaggeredItem) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        verticalItemSpacing = 16.dp,
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = contentPadding,
        modifier = Modifier.fillMaxSize().heightIn(max = 9999.dp)
    ) {
        items(data) { item ->
            QuickEditCardStaggered(
                item = item,
                onClick = remember(item.id) { { onItemClick(item) } },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun QuickEditCardStaggered(
    item: StaggeredItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val anim by rememberLottieComposition(item.imageRes)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickableWithAlphaEffect { onClick.invoke() }
    ) {

        LottieAnimation(
            modifier = Modifier.fillMaxSize(),
            composition = anim,
            iterations = LottieConstants.IterateForever
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DiscoverQuickEditsPreview() {
    Surface { DiscoverQuickEdits() {} }
}











