package com.avnsoft.photoeditor.photocollage.ui.activities.mycreate

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Surface
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.main.FeatureType
import com.avnsoft.photoeditor.photocollage.ui.activities.main.MainViewModel
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.ui.theme.BackgroundGray
import com.avnsoft.photoeditor.photocollage.ui.theme.BackgroundLight
import com.avnsoft.photoeditor.photocollage.ui.theme.BackgroundWhite
import com.avnsoft.photoeditor.photocollage.ui.theme.Primary500
import com.basesource.base.utils.clickableWithAlphaEffect
import org.koin.androidx.compose.koinViewModel

@Composable
fun MyCreateUI(
    viewModel: MyCreateViewModel = koinViewModel(),
    mainViewModel: MainViewModel? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        MyCreateHeader(mainViewModel)
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Primary500
                )
            }
        } else if (uiState.projects.isEmpty()) {
            MyCreateEmptyState(
                onStartEditing = {
                    mainViewModel?.navigateFeature(FeatureType.EDIT_PHOTO)
                }
            )
        } else {
            MyCreateProjectGrid(
                projects = uiState.projects,
                onProjectClick = { project ->
                    // TODO: Handle project click - open project editor
                }
            )
        }
    }
}

@Composable
fun MyCreateHeader(viewModel: MainViewModel?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Icon(
            modifier = Modifier
                .size(24.dp)
                .clickableWithAlphaEffect {
                    viewModel?.navigateFeature(FeatureType.SETTING)
                },
            painter = painterResource(R.drawable.ic_menu),
            contentDescription = ""
        )
        Text(
            text = "My Creative",
            style = AppStyle.h5().bold().Color_101828(),
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        )
        Image(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .size(56.dp, 28.dp),
            painter = painterResource(R.drawable.btn_pro),
            contentDescription = ""
        )
        Icon(
            modifier = Modifier
                .size(24.dp)
                .clickableWithAlphaEffect {
                    viewModel?.navigateFeature(FeatureType.STORE)
                },
            painter = painterResource(R.drawable.ic_market_black),
            contentDescription = ""
        )
    }
}

@Composable
fun MyCreateEmptyState(
    onStartEditing: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Camera icon
        Image(
            modifier = Modifier.height(120.dp),
            painter = painterResource(R.drawable.ic_camera_create),
            contentDescription = null,
            contentScale = ContentScale.FillWidth
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Title
        Text(
            text = "No creations yet!",
            style = AppStyle.title1().semibold().Color_101828(),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        // Description
        Text(
            textAlign = TextAlign.Center,
            text = "Start your first project and let your creativity shine.",
            style = AppStyle.body1().medium().Color_667085(),
            modifier = Modifier.padding(bottom = 20.dp)
        )
        
        // Start Editing button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Primary500)
                .clickableWithAlphaEffect { onStartEditing() }
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(R.drawable.ic_add_square),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Start Editing",
                style = AppStyle.buttonLarge().semibold().white()
            )
        }
    }
}

@Composable
fun MyCreateProjectGrid(
    projects: List<MyCreateItem>,
    onProjectClick: (MyCreateItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(projects) { project ->
            MyCreateProjectItem(
                project = project,
                onClick = { onProjectClick(project) }
            )
        }
    }
}

@Composable
fun MyCreateProjectItem(
    project: MyCreateItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = project.thumbnailPath,
            contentDescription = project.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_empty_image),
            error = painterResource(R.drawable.ic_empty_image)
        )
        
        // Title overlay (if exists)
        project.title?.let { title ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = title,
                    style = AppStyle.body2().semibold().white(),
                    maxLines = 1
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MyCreateUIPreview() {
    Surface {
        MyCreateUI()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MyCreateUIEmptyStatePreview() {
    Surface {
        MyCreateEmptyState(
            onStartEditing = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MyCreateUIHeaderPreview() {
    Surface {
        MyCreateHeader(null)
    }
}

@Preview(showBackground = true)
@Composable
fun MyCreateProjectItemPreview() {
    Surface {
        MyCreateProjectItem(
            project = MyCreateItem(
                id = "1",
                thumbnailPath = "",
                title = "My Project"
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MyCreateProjectGridPreview() {
    Surface {
        MyCreateProjectGrid(
            projects = listOf(
                MyCreateItem(
                    id = "1",
                    thumbnailPath = "",
                    title = "Project 1"
                ),
                MyCreateItem(
                    id = "2",
                    thumbnailPath = "",
                    title = "Project 2"
                ),
                MyCreateItem(
                    id = "3",
                    thumbnailPath = "",
                    title = "Project 3"
                ),
                MyCreateItem(
                    id = "4",
                    thumbnailPath = "",
                    title = "Project 4"
                ),
                MyCreateItem(
                    id = "5",
                    thumbnailPath = "",
                    title = "Project 5"
                ),
                MyCreateItem(
                    id = "6",
                    thumbnailPath = "",
                    title = "Project 6"
                )
            ),
            onProjectClick = {}
        )
    }
}

