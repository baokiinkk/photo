package com.avnsoft.photoeditor.photocollage.ui.activities.discover

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.basesource.base.utils.clickableWithAlphaEffect

// Data model mock
private val mockTemplates = listOf(
    TemplateItem(1, "Modern Collage", android.R.drawable.ic_menu_gallery),
    TemplateItem(2, "Classic Mix", android.R.drawable.ic_menu_gallery),
    TemplateItem(3, "Fresh Layout", android.R.drawable.ic_menu_gallery),
)

@Composable
fun DiscoverTemplates(onSeeAll: () -> Unit = {}, onTemplateClick: (TemplateItem) -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, start = 20.dp, end = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Templates", style = AppStyle.title1().bold().gray900())
            Image(
                painterResource(R.drawable.ic_template), contentDescription = "", modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(24.dp)
            )
            Text(
                text = "See All", style = AppStyle.caption1().medium().primary500(), modifier = Modifier.weight(1f), textAlign = TextAlign.End
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())) {
            mockTemplates.forEach { template ->
                Image(
                    painterResource(template.imageRes),
                    contentDescription = "",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .size(130.dp)
                        .clickableWithAlphaEffect {
                            onTemplateClick(template)
                        },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DiscoverTemplatesPreview() {
    Surface { DiscoverTemplates() }
}











