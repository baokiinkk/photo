package com.basesource.base.utils

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun <T> ChunkedPagerList(
    items: List<T>,
    chunked: Int,
    itemSpacing: Dp = 8.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp),
    modifier: Modifier = Modifier.fillMaxWidth(),
    content: @Composable RowScope.(T) -> Unit
) {

    val groupedItems = items.chunked(chunked)

    val pagerState = rememberPagerState(pageCount = { groupedItems.size })

    HorizontalPager(
        state = pagerState,
        pageSpacing = 12.dp,
        contentPadding = contentPadding,
        userScrollEnabled = groupedItems.size > 1,
        modifier = modifier,
    ) { page ->
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        ) {
            groupedItems[page].forEach { item ->
                content(item)
            }
            if (groupedItems[page].size == 1) {
                repeat(2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ImageWidget(
    modifier: Modifier = Modifier,
    @DrawableRes resId: Int,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null
) = Image(
    modifier = modifier,
    painter = painterResource(resId),
    contentDescription = null,
    alignment = alignment,
    contentScale = contentScale,
    alpha = alpha,
    colorFilter = colorFilter,
)

