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

// Data model mock
private val mockTemplates = listOf(
    TemplateItem(1, "Modern Collage", android.R.drawable.ic_menu_gallery),
    TemplateItem(2, "Classic Mix", android.R.drawable.ic_menu_gallery),
    TemplateItem(3, "Fresh Layout", android.R.drawable.ic_menu_gallery),
)

@Composable
fun DiscoverTemplates(onSeeAll: () -> Unit = {}, onTemplateClick: (TemplateItem) -> Unit = {}) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp, start = 20.dp, end = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Templates", style = AppStyle.title2().bold().black(), modifier = Modifier.weight(1f))
            Text(text = "See All", style = AppStyle.body2().regular().primaryLight(), modifier = Modifier)
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            mockTemplates.forEach { template ->
                TemplateCard(item = template, modifier = Modifier.weight(1f).padding(end = 8.dp), onClick = { onTemplateClick(template) })
            }
        }
    }
}

@Composable
fun TemplateCard(item: TemplateItem, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Column(
        modifier = modifier
            .height(80.dp)
            .background(Color.White, RoundedCornerShape(12.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(4.dp))
        Image(
            painter = painterResource(id = item.imageRes),
            contentDescription = item.name,
            modifier = Modifier.size(56.dp)
        )
        Text(text = item.name, style = AppStyle.caption1().semibold().primaryLight(), modifier = Modifier.padding(2.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun DiscoverTemplatesPreview() {
    Surface { DiscoverTemplates() }
}
