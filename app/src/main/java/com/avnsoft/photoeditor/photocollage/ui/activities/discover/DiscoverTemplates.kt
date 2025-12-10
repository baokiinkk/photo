package com.avnsoft.photoeditor.photocollage.ui.activities.discover

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateModel
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.basesource.base.ui.image.LoadImage
import com.basesource.base.utils.clickableWithAlphaEffect
import com.basesource.base.utils.Shimmer

@Composable
fun DiscoverTemplates(
    templates: List<TemplateModel> = emptyList(),
    onSeeAll: () -> Unit = {},
    onTemplateClick: (TemplateModel) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, start = 20.dp, end = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.templates), style = AppStyle.title1().bold().gray900())
            Image(
                painterResource(R.drawable.ic_template), contentDescription = "", modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(24.dp)
            )
            Text(
                text = stringResource(R.string.see_all),
                style = AppStyle.caption1().medium().primary500(),
                modifier = Modifier
                    .weight(1f)
                    .clickableWithAlphaEffect {
                        onSeeAll.invoke()
                    },
                textAlign = TextAlign.End
            )
        }
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (templates.isNotEmpty()) {
                templates.forEach { template ->
                    LoadImage(
                        model = template.previewUrl,
                        modifier = Modifier
                            .size(130.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickableWithAlphaEffect {
                                onTemplateClick.invoke(template)
                            },
                        contentScale = ContentScale.Crop
                    )

                }
            } else {
                Shimmer(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(RoundedCornerShape(16.dp))
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











