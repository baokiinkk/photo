package com.avnsoft.photoeditor.photocollage.ui.activities.collage.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.data.model.gradient.GradientGroup
import com.avnsoft.photoeditor.photocollage.data.model.gradient.GradientItem
import com.avnsoft.photoeditor.photocollage.data.model.pattern.PatternContentModel
import com.avnsoft.photoeditor.photocollage.data.model.pattern.PatternModel
import com.avnsoft.photoeditor.photocollage.data.repository.GradientRepository
import com.avnsoft.photoeditor.photocollage.data.repository.PatternRepository
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor.Companion.Gray100
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor.Companion.Gray500
import com.avnsoft.photoeditor.photocollage.ui.theme.AppColor.Companion.Gray900
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.utils.loadPatternAssetPainter
import com.basesource.base.components.ColorPickerUI
import com.basesource.base.utils.clickableWithAlphaEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject
import kotlin.math.roundToInt

enum class BackgroundTab {
    SOLID,
    PATTERN,
    GRADIENT,
}

enum class PatternState {
    GROUP,
    CHILD
}

@Serializable
sealed class BackgroundSelection {
    @Serializable
    data class Solid(val color: String) : BackgroundSelection()

    @Serializable
    data class Pattern(val item: PatternContentModel, val group: PatternModel) :
        BackgroundSelection()

    @Serializable
    data class Gradient(val item: GradientItem, val group: GradientGroup) : BackgroundSelection()

    @Serializable
    data class BackgroundTransparent(
        val resId: Int,
    ) : BackgroundSelection()
}

@Composable
fun BackgroundSheet(
    selectedTab: BackgroundTab = BackgroundTab.SOLID,
    selectedBackgroundSelection: BackgroundSelection? = null,
    onBackgroundSelect: (BackgroundTab, BackgroundSelection) -> Unit,
    onClose: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    isShowFooter: Boolean = true
) {
    var currentTab by remember(selectedTab) { mutableStateOf(selectedTab) }
    var showColorWheel by remember { mutableStateOf(false) }
    var patternState by remember { mutableStateOf(PatternState.GROUP) }
    var selectedPatternGroup by remember { mutableStateOf<PatternModel?>(null) }

    // Extract color from selectedBackgroundSelection for color picker
    var initSelectColor by remember {
        mutableStateOf(
            (selectedBackgroundSelection as? BackgroundSelection.Solid)?.color?.let {
                try {
                    Color((if (it.startsWith("#")) it else "#$it").toColorInt())
                } catch (e: Exception) {
                    Color.White
                }
            } ?: Color.White
        )
    }
    var currentSelectedColor by remember {
        mutableStateOf(
            (selectedBackgroundSelection as? BackgroundSelection.Solid)?.color?.let {
                try {
                    Color((if (it.startsWith("#")) it else "#$it").toColorInt())
                } catch (e: Exception) {
                    Color.White
                }
            } ?: Color.White
        )
    }

    // Update currentSelectedColor when selectedBackgroundSelection changes externally
    LaunchedEffect(selectedBackgroundSelection) {
        (selectedBackgroundSelection as? BackgroundSelection.Solid)?.color?.let {
            try {
                initSelectColor = Color((if (it.startsWith("#")) it else "#$it").toColorInt())
                currentSelectedColor = Color((if (it.startsWith("#")) it else "#$it").toColorInt())
            } catch (e: Exception) {
                // Keep current color if parsing fails
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
    ) {
        if (showColorWheel) {
            ColorPickerUI(
                onColorSelected = { color ->
                    currentSelectedColor = color
                    val colorHex = currentSelectedColor.colorToHex()
                    onBackgroundSelect(BackgroundTab.SOLID, BackgroundSelection.Solid(colorHex))
                },
                selectedColor = initSelectColor ?: Color.White,
                onDismiss = { showColorWheel = false },
                textStyle = AppStyle.body1().medium().gray900(),
                confirmText = R.string.confirm,
                cancelText = R.string.cancel
            )
        } else {
            if (currentTab == BackgroundTab.PATTERN && patternState == PatternState.CHILD && selectedPatternGroup != null) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 16.dp, horizontal = 16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(onClick = {
                        patternState = PatternState.GROUP
                        selectedPatternGroup = null
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_left),
                            contentDescription = "Back",
                            tint = Gray900
                        )
                    }
                    Text(
                        text = selectedPatternGroup?.tabName ?: "",
                        style = AppStyle.body1().medium().gray900(),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        BackgroundTab.SOLID,
                        BackgroundTab.PATTERN,
                        BackgroundTab.GRADIENT
                    ).forEach { tab ->
                        val tabText = when (tab) {
                            BackgroundTab.SOLID -> "Solid"
                            BackgroundTab.PATTERN -> "Pattern"
                            BackgroundTab.GRADIENT -> "Gradient"
                        }
                        Text(
                            text = tabText,
                            style = AppStyle.body2().medium().let {
                                if (currentTab == tab) it.white() else it.gray900()
                            },
                            modifier = Modifier
                                .background(
                                    if (currentTab == tab) Color(0xFF9747FF) else Color(0xFFF3F4F6),
                                    RoundedCornerShape(24.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                .clickableWithAlphaEffect {
                                    currentTab = tab
                                    if (tab == BackgroundTab.PATTERN) {
                                        patternState = PatternState.GROUP
                                        selectedPatternGroup = null
                                    }
                                }
                        )
                        if (tab != BackgroundTab.GRADIENT) {
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
            when (currentTab) {
                BackgroundTab.SOLID -> {
                    SolidBackgroundTab(
                        selectedColor = currentSelectedColor,
                        onColorSelect = { color ->
                            currentSelectedColor = color
                            val colorHex = currentSelectedColor.colorToHex()
                            onBackgroundSelect(
                                BackgroundTab.SOLID,
                                BackgroundSelection.Solid(colorHex)
                            )
                        },
                        onColorWheelClick = { showColorWheel = true }
                    )
                }

                BackgroundTab.PATTERN -> {
                    PatternBackgroundTab(
                        patternState = patternState,
                        selectedGroup = selectedPatternGroup,
                        onGroupClick = { group ->
                            selectedPatternGroup = group
                            patternState = PatternState.CHILD
                        },
                        onPatternSelect = { item, group ->
                            onBackgroundSelect(
                                BackgroundTab.PATTERN,
                                BackgroundSelection.Pattern(item, group)
                            )
                        }
                    )
                }

                BackgroundTab.GRADIENT -> {
                    GradientBackgroundTab(
                        onGradientSelect = { item, group, urlRoot ->
                            onBackgroundSelect(
                                BackgroundTab.GRADIENT,
                                BackgroundSelection.Gradient(item, group)
                            )
                        }
                    )
                }
            }
        }

        if (isShowFooter) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Gray100)
            )
            // Bottom Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            )
            {
                IconButton(onClick = onClose) {
                    Icon(
                        modifier = Modifier.size(28.dp),
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "Close",
                        tint = Gray500
                    )
                }

                Text(
                    text = stringResource(R.string.color),
                    style = AppStyle.title2().semibold().gray900()
                )

                IconButton(onClick = onConfirm) {
                    Icon(
                        modifier = Modifier.size(28.dp),
                        painter = painterResource(R.drawable.ic_confirm),
                        contentDescription = "Confirm",
                        tint = Gray900
                    )
                }
            }
        }
    }
}

@Composable
private fun SolidBackgroundTab(
    selectedColor: Color,
    onColorSelect: (Color) -> Unit,
    onColorWheelClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // Color swatches grid
        val solidColors = listOf(
            Color(0xFFFFFFFF), // White
            Color(0xFF000000), // Black
            Color(0xFFE5E7EB), // Gray
            Color(0xFF9CA3AF), // Dark Gray
            Color(0xFFEF4444), // Red
            Color(0xFFF59E0B), // Orange
            Color(0xFFEAB308), // Yellow
            Color(0xFF84CC16), // Green
            Color(0xFF06B6D4), // Cyan
            Color(0xFF3B82F6), // Blue
            Color(0xFF8B5CF6), // Purple
            Color(0xFFEC4899), // Pink
            Color(0xFFF97316), // Orange Red
            Color(0xFF14B8A6), // Teal
            Color(0xFF6366F1), // Indigo
        )

        LazyVerticalGrid(
            columns = GridCells.FixedSize(32.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Image(painter = painterResource(R.drawable.ic_color_picker),
                    contentDescription = "",
                    modifier = Modifier.size(32.dp).clickableWithAlphaEffect{
                        onColorWheelClick.invoke()
                    }
                )
            }
            items(solidColors) { color ->
                SolidColorSwatch(
                    color = color,
                    isSelected = selectedColor.toArgb() == color.toArgb(),
                    onClick = { onColorSelect(color) }
                )
            }
        }
    }
}

@Composable
private fun SolidColorSwatch(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(
                color = color,
                shape = CircleShape
            )
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = Color(0xFF9747FF),
                        shape = CircleShape
                    )
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = Color(0xFFE5E7EB),
                        shape = CircleShape
                    )
                }
            )
            .clickableWithAlphaEffect(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = if (color.toArgb() == Color.White.toArgb() || color.toArgb() == Color(
                        0xFFE5E7EB
                    ).toArgb()
                ) Color.Black else Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

fun Color.colorToHex(includeAlpha: Boolean = true): String {
    val a = (alpha * 255).roundToInt().coerceIn(0, 255)
    val r = (red * 255).roundToInt().coerceIn(0, 255)
    val g = (green * 255).roundToInt().coerceIn(0, 255)
    val b = (blue * 255).roundToInt().coerceIn(0, 255)

    return if (includeAlpha) {
        String.format("#%02X%02X%02X%02X", a, r, g, b)  // #AARRGGBB
    } else {
        String.format("#%02X%02X%02X", r, g, b)         // #RRGGBB
    }
}

@Composable
private fun PatternBackgroundTab(
    patternState: PatternState,
    selectedGroup: PatternModel?,
    onGroupClick: (PatternModel) -> Unit,
    onPatternSelect: ((PatternContentModel, PatternModel) -> Unit)? = null
) {
    val patternRepository: PatternRepository = koinInject()

    var patterns by remember { mutableStateOf<List<PatternModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            isLoading = true
            error = null
            when (val result = patternRepository.getNewPatterns()) {
                is com.basesource.base.result.Result.Success -> {
                    patterns = result.data
                    isLoading = false
                }

                is com.basesource.base.result.Result.Error -> {
                    error = result.exception?.message
                    isLoading = false
                }

                else -> {
                    isLoading = false
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading patterns...",
                        style = AppStyle.body1().medium().gray600()
                    )
                }
            }

            error != null -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: $error",
                        style = AppStyle.body1().medium().gray600()
                    )
                }
            }

            patterns.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No patterns available",
                        style = AppStyle.body1().medium().gray600()
                    )
                }
            }

            patternState == PatternState.GROUP -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(patterns) { group ->
                        PatternGroupCard(
                            patternGroup = group,
                            onGroupClick = { onGroupClick(group) }
                        )
                    }
                }
            }

            patternState == PatternState.CHILD && selectedGroup != null -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(selectedGroup.content) { item ->
                        PatternItemCard(
                            patternItem = item,
                            patternGroup = selectedGroup,
                            onPatternSelect = { patternItem, patternGroup ->
                                onPatternSelect?.invoke(patternItem, patternGroup)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PatternGroupCard(
    patternGroup: PatternModel,
    onGroupClick: () -> Unit
) {
    val context = LocalContext.current
    val fallbackPainter = remember(patternGroup.content) {
        loadPatternAssetPainter(context, patternGroup.tabName)
    }
    var urlLoadFailed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .width(100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickableWithAlphaEffect {
                    onGroupClick()
                },
            contentAlignment = Alignment.Center
        ) {
            if (urlLoadFailed && fallbackPainter != null) {
                Image(
                    painter = fallbackPainter,
                    contentDescription = patternGroup.tabName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(patternGroup.urlThumb)
                        .build(),
                    contentDescription = patternGroup.tabName,
                    contentScale = ContentScale.Crop,
                    error = fallbackPainter,
                    placeholder = fallbackPainter,
                    onError = {
                        urlLoadFailed = true
                    },
                    onSuccess = {
                        urlLoadFailed = false
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                )
            }
            if (patternGroup.isPro) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(
                            Color(0xFF9747FF),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "PRO",
                        style = AppStyle.body2().medium().white()
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = patternGroup.tabName,
            style = AppStyle.caption2().medium().gray(),
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PatternItemCard(
    patternItem: PatternContentModel,
    patternGroup: PatternModel,
    onPatternSelect: ((PatternContentModel, PatternModel) -> Unit)? = null
) {
    val context = LocalContext.current

    // Load fallback from assets using patternItem.name (e.g., "item_1.jpg")
    val fallbackPainter = remember(patternItem.name) {
        loadPatternAssetPainter(context, patternItem.name)
    }

    // Track if URL load failed to show fallback
    var urlLoadFailed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .width(100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickableWithAlphaEffect {
                    onPatternSelect?.invoke(patternItem, patternGroup)
                },
            contentAlignment = Alignment.Center
        ) {
            if (urlLoadFailed && fallbackPainter != null) {
                // Show fallback from assets
                Image(
                    painter = fallbackPainter,
                    contentDescription = patternItem.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(patternItem.urlThumb)
                        .build(),
                    contentDescription = patternItem.title,
                    contentScale = ContentScale.Crop,
                    error = fallbackPainter,
                    placeholder = fallbackPainter,
                    onError = {
                        urlLoadFailed = true
                    },
                    onSuccess = {
                        urlLoadFailed = false
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            // Show PRO badge if needed
            if (patternGroup.isPro) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(
                            color = Color(0xFF9747FF),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "PRO",
                        style = AppStyle.title1().medium().white(),
                    )
                }
            }
        }

        // Pattern title below image
        Text(
            text = patternItem.title,
            style = AppStyle.body2().medium().gray900(),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun GradientBackgroundTab(
    onGradientSelect: ((GradientItem, GradientGroup, String) -> Unit)? = null
) {
    val context = LocalContext.current
    val gradientRepository: GradientRepository = koinInject()

    var gradients by remember { mutableStateOf<List<GradientGroup>>(emptyList()) }
    var urlRoot by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        when (val result = gradientRepository.getGradients()) {
            is com.basesource.base.result.Result.Success -> {
                gradients = result.data.groups
                urlRoot = result.data.urlRoot
                isLoading = false
            }

            is com.basesource.base.result.Result.Error -> {
                error = result.exception?.message
                isLoading = false
            }

            else -> {
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading gradients...",
                        style = AppStyle.body1().medium().gray500()
                    )
                }
            }

            error != null -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: $error",
                        style = AppStyle.body1().medium().gray500()
                    )
                }
            }

            gradients.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No gradients available",
                        style = AppStyle.body1().medium().gray500()
                    )
                }
            }

            else -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    gradients.forEach { group ->
                        items(group.content) { item ->
                            GradientItemCard(
                                gradientItem = item,
                                gradientGroup = group,
                                urlRoot = urlRoot,
                                onGradientSelect = { gradientItem, gradientGroup ->
                                    onGradientSelect?.invoke(gradientItem, gradientGroup, urlRoot)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GradientItemCard(
    gradientItem: GradientItem,
    gradientGroup: GradientGroup,
    urlRoot: String,
    onGradientSelect: ((GradientItem, GradientGroup) -> Unit)? = null
) {
    // Parse colors from hex strings
    val colors = remember(gradientItem.colors) {
        gradientItem.colors.mapNotNull { colorHex ->
            try {
                Color(colorHex.toColorInt())
            } catch (e: Exception) {
                null
            }
        }
    }

    Column(
        modifier = Modifier
            .width(100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    brush = if (colors.size >= 2) {
                        androidx.compose.ui.graphics.Brush.verticalGradient(colors)
                    } else {
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(Color.White, Color.Gray)
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .clickableWithAlphaEffect {
                    onGradientSelect?.invoke(gradientItem, gradientGroup)
                },
            contentAlignment = Alignment.Center
        ) {
            // Show PRO badge if needed
            if (gradientGroup.isPro || gradientItem.isPro == true) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(
                            color = Color(0xFF9747FF),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "PRO",
                        style = AppStyle.title1().medium().white(),
                    )
                }
            }
        }

        // Gradient title below
        Text(
            text = gradientItem.title,
            style = AppStyle.body2().medium().gray900(),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun BackgroundSheetPreview() {
    BackgroundSheet(
        selectedTab = BackgroundTab.SOLID,
        onBackgroundSelect = { _, _ -> },
        onClose = {},
        onConfirm = {}
    )
}

