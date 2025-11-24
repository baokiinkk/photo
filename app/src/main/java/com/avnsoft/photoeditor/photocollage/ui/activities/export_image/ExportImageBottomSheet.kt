package com.avnsoft.photoeditor.photocollage.ui.activities.export_image

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avnsoft.photoeditor.photocollage.ui.theme.Gray100
import com.avnsoft.photoeditor.photocollage.ui.theme.Gray500
import com.avnsoft.photoeditor.photocollage.ui.theme.Gray900
import com.avnsoft.photoeditor.photocollage.ui.theme.Purple40
import com.avnsoft.photoeditor.photocollage.ui.theme.PurpleLight

enum class Quality {
    LOW, MEDIUM, HIGH
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportImageBottomSheet(
    onDismissRequest: () -> Unit,
    onDownload: (Quality) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = null // We have our own handle in the design
    ) {
        ExportImageScreen(
            onDownload = onDownload,
            onClose = onDismissRequest
        )
    }
}

@Composable
fun ExportImageScreen(
    onDownload: (Quality) -> Unit,
    onClose: () -> Unit
) {
    var selectedQuality by remember { mutableStateOf(Quality.MEDIUM) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with Close Button
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(Gray100, CircleShape)
                    .align(Alignment.TopCenter)
            )
            
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = Gray500
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Image Preview (Placeholder)
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Gray100)
                .border(1.dp, Gray100, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
             // In a real app, you would pass the image bitmap or uri here.
             // For now, we simulate the preview area.
             Text("Image Preview", color = Gray500)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = "Photo Quality",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Gray900
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = "Pick your preferred download resolution",
            fontSize = 14.sp,
            color = Gray500,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Quality Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QualityOption(
                text = "Low",
                isSelected = selectedQuality == Quality.LOW,
                onClick = { selectedQuality = Quality.LOW },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            QualityOption(
                text = "Medium",
                isSelected = selectedQuality == Quality.MEDIUM,
                onClick = { selectedQuality = Quality.MEDIUM },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            QualityOption(
                text = "High",
                isSelected = selectedQuality == Quality.HIGH,
                onClick = { selectedQuality = Quality.HIGH },
                isPro = true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Download Button
        Button(
            onClick = { onDownload(selectedQuality) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            contentPadding = PaddingValues(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF6425F3), Color(0xFF9C27B0))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Download",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun QualityOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isPro: Boolean = false,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) Purple40 else Color.Transparent
    val backgroundColor = if (isSelected) PurpleLight else Gray100
    val textColor = if (isSelected) Purple40 else Gray900

    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        )

        if (isPro) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFE91E63), Color(0xFF9C27B0))
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "PRO",
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
