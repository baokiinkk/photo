package com.amb.photo.ui.activities.collage.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amb.photo.data.model.collage.CellSpec
import com.amb.photo.data.model.collage.CollageTemplate
import com.amb.photo.ui.theme.AppStyle
import com.basesource.base.utils.clickableWithAlphaEffect

@Composable
fun GridsSheet(
    templates: List<CollageTemplate>,
    selectedTemplate: CollageTemplate?,
    previewImages: List<Uri>,
    onTemplateSelect: (CollageTemplate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Layouts",
            style = AppStyle.title2().medium().gray500(),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(templates) { template ->
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(
                            if (template.id == selectedTemplate?.id) Color(0xFFEEE1FF) else Color(0xFFF3F4F6),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(6.dp)
                        .clickableWithAlphaEffect { onTemplateSelect(template) }
                ) {
                    CollagePreview(
                        images = previewImages,
                        template = template,
                        gap = 2.dp,
                        corner = 6.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GridsSheetPreview() {
    val mockTemplates = listOf(
        CollageTemplate("1-full", listOf(CellSpec(0f, 0f, 1f, 1f))),
        CollageTemplate(
            "2-vertical", listOf(
                CellSpec(0f, 0f, 0.5f, 1f),
                CellSpec(0.5f, 0f, 0.5f, 1f)
            )
        ),
        CollageTemplate(
            "left-big-right-2", listOf(
                CellSpec(0f, 0f, 0.63f, 1f),
                CellSpec(0.66f, 0f, 0.34f, 0.48f),
                CellSpec(0.66f, 0.52f, 0.34f, 0.48f)
            )
        )
    )
    GridsSheet(
        templates = mockTemplates,
        selectedTemplate = mockTemplates.first(),
        previewImages = emptyList(),
        onTemplateSelect = {}
    )
}

