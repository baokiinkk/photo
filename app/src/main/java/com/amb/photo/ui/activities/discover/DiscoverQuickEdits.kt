package com.amb.photo.ui.activities.discover

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amb.photo.ui.theme.AppStyle

private val mockQuickEdits = listOf(
    QuickEditItem(1, "Background", android.R.drawable.ic_menu_gallery),
    QuickEditItem(2, "Filter", android.R.drawable.ic_menu_gallery),
    QuickEditItem(3, "Sticker", android.R.drawable.ic_menu_gallery),
    QuickEditItem(4, "Add Text", android.R.drawable.ic_menu_gallery),
    QuickEditItem(5, "Frame", android.R.drawable.ic_menu_gallery),
    QuickEditItem(6, "Doodle", android.R.drawable.ic_menu_gallery),
)

@Composable
fun DiscoverQuickEdits(onItemClick: (QuickEditItem) -> Unit = {}) {
    Column(Modifier.fillMaxWidth().padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 12.dp)) {
        Text(text = "Quick Edits ✨", style = AppStyle.title2().bold().black())
        Spacer(modifier = Modifier.height(8.dp))
        Column {
            for (row in mockQuickEdits.chunked(2)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (item in row) {
                        QuickEditCard(item, Modifier.weight(1f), onClick = { onItemClick(item) })
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun QuickEditCard(item: QuickEditItem, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(Color.White, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(painter = painterResource(id = item.imageRes), contentDescription = item.title, modifier = Modifier.size(56.dp))
            Spacer(Modifier.weight(1f))
            Text(
                text = item.title,
                style = AppStyle.body2().bold().primary(),
                modifier = Modifier.background(Color(0xFFF6F7FA), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DiscoverQuickEditsPreview() {
    Surface { DiscoverQuickEdits() }
}
