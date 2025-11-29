package com.avnsoft.photoeditor.photocollage.ui.activities.mycreate

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.avnsoft.photoeditor.photocollage.R
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.EditorActivity
import com.avnsoft.photoeditor.photocollage.ui.activities.editor.EditorInput
import com.avnsoft.photoeditor.photocollage.ui.activities.export_image.shareFile
import com.avnsoft.photoeditor.photocollage.ui.activities.main.FeatureType
import com.avnsoft.photoeditor.photocollage.ui.activities.main.MainViewModel
import com.avnsoft.photoeditor.photocollage.ui.dialog.ConfirmDeletePhotoDialog
import com.avnsoft.photoeditor.photocollage.ui.theme.AppStyle
import com.avnsoft.photoeditor.photocollage.ui.theme.BackgroundGray
import com.avnsoft.photoeditor.photocollage.ui.theme.BackgroundWhite
import com.avnsoft.photoeditor.photocollage.ui.theme.Primary500
import com.basesource.base.ui.base.BaseActivity
import com.basesource.base.utils.clickableWithAlphaEffect
import org.koin.androidx.compose.koinViewModel

@Composable
fun MyCreateUI(
    viewModel: MyCreateViewModel = koinViewModel(),
    mainViewModel: MainViewModel? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    var isShowConfirmDeletePhotoDialog by remember { mutableStateOf(false) }
    var projectItem by remember { mutableStateOf(MyCreateItem()) }
    var isShowMyCreateUIBottomSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
                    projectItem = project
                    isShowMyCreateUIBottomSheet = true
                }
            )
        }

        ConfirmDeletePhotoDialog(
            isVisible = isShowConfirmDeletePhotoDialog,
            onKeep = {
                isShowConfirmDeletePhotoDialog = false
            },
            onDelete = {
                viewModel.deleteProject(projectItem.id)
                isShowConfirmDeletePhotoDialog = false
            }
        )

        MyCreateUIBottomSheet(
            isVisible = isShowMyCreateUIBottomSheet,
            pathBitmap = projectItem.thumbnailPath,
            onDismissRequest = {
                isShowMyCreateUIBottomSheet = false
            },
            onClose = {
                isShowMyCreateUIBottomSheet = false
            },
            onEdit = {
                isShowMyCreateUIBottomSheet = false
                EditorActivity.newScreen(
                    context = context,
                    input = EditorInput(
                        pathBitmap = projectItem.thumbnailPath
                    )
                )
            },
            onDelete = {
                isShowMyCreateUIBottomSheet = false
                isShowConfirmDeletePhotoDialog = true
            },
            onShare = {
                isShowMyCreateUIBottomSheet = false
                val uri = projectItem.thumbnailPath.toUri()
                (context as? BaseActivity)?.shareFile(uri)
            }
        )
    }
}

@Composable
fun MyCreateHeader(viewModel: MainViewModel?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundWhite)
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
            text = stringResource(R.string.my_creative),
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
            text = stringResource(R.string.no_creations_yet),
            style = AppStyle.title1().semibold().Color_101828(),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Description
        Text(
            textAlign = TextAlign.Center,
            text = stringResource(R.string.start_first_project),
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
                text = stringResource(R.string.start_editing),
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

    AsyncImage(
        model = project.thumbnailPath,
        contentDescription = project.title,
        modifier = Modifier
            .fillMaxWidth()
            .height(156.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickableWithAlphaEffect { onClick() },
        contentScale = ContentScale.Crop,
        placeholder = painterResource(R.drawable.ic_empty_image),
        error = painterResource(R.drawable.ic_empty_image)
    )

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
                id = "1".toLong(),
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
                    id = "1".toLong(),
                    thumbnailPath = "",
                    title = "Project 1"
                ),
                MyCreateItem(
                    id = "2".toLong(),
                    thumbnailPath = "",
                    title = "Project 2"
                ),
                MyCreateItem(
                    id = "3".toLong(),
                    thumbnailPath = "",
                    title = "Project 3"
                ),
                MyCreateItem(
                    id = "4".toLong(),
                    thumbnailPath = "",
                    title = "Project 4"
                ),
                MyCreateItem(
                    id = "5".toLong(),
                    thumbnailPath = "",
                    title = "Project 5"
                ),
                MyCreateItem(
                    id = "6".toLong(),
                    thumbnailPath = "",
                    title = "Project 6"
                )
            ),
            onProjectClick = {}
        )
    }
}

