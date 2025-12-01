package com.avnsoft.photoeditor.photocollage.data.repository

import android.content.Context
import com.avnsoft.photoeditor.photocollage.data.local.room.AppDataDao
import com.avnsoft.photoeditor.photocollage.data.local.sharedPref.EditorSharedPref
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateCategoryModel
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateCategoryRoomModel
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateContentRoom
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateModel
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateResponse
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateRoomModel
import com.basesource.base.network.CollageApiService
import com.basesource.base.network.safeApiCall
import com.basesource.base.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class TemplateRepoImpl(
    private val context: Context,
    private val api: CollageApiService,
    private val appDataDao: AppDataDao,
    private val editorSharedPref: EditorSharedPref
) {
    suspend fun syncTemplates() {
        val response = safeApiCall<TemplateResponse>(
            context = context,
            apiCallMock = { api.getTemplates() },
            apiCall = { api.getTemplates() }
        )
        when (response) {
            is Result.Success -> {
                val categories = response.data.data.map { categoryData ->
                    val templates = categoryData.content?.map { templateItem ->
                        val contents = templateItem.content?.map { contentItem ->
                            TemplateContentRoom(
                                urlThumb = contentItem.urlThumb,
                                x = contentItem.x,
                                y = contentItem.y,
                                width = contentItem.width,
                                height = contentItem.height,
                                rotate = contentItem.rotate
                            )
                        }
                        
                        TemplateRoomModel(
                            bannerUrl = "file:///android_asset/${templateItem.bannerUrl}",
                            previewUrl = templateItem.previewUrl?.let { "file:///android_asset/$it" },
                            frameUrl = templateItem.frameUrl,
                            content = contents,
                            timeCreate = System.currentTimeMillis().toString(),
                            isUsed = templateItem.isUsed,
                            isPro = templateItem.isPro,
                            isReward = templateItem.isReward,
                            isFree = templateItem.isFree
                        )
                    }
                    
                    TemplateCategoryRoomModel(
                        id = System.currentTimeMillis().toString(),
                        category = categoryData.category,
                        templates = templates
                    )
                }

                appDataDao.refreshAllCategories(categories)
            }

            else -> {

            }
        }
    }
    suspend fun getPreviewTemplates(): Flow<List<TemplateCategoryModel>> {
        syncTemplates()
        val response = appDataDao.getPreviewTemplateCategories()

        val data = response.map { categories ->
            fun toTemplateModel(roomModel: TemplateRoomModel): TemplateModel {
                val cells = (roomModel.content ?: emptyList()).map { contentRoom ->
                    com.avnsoft.photoeditor.photocollage.data.model.template.TemplateContentModel(
                        x = contentRoom.x,
                        y = contentRoom.y,
                        width = contentRoom.width,
                        height = contentRoom.height,
                        rotate = contentRoom.rotate
                    )
                }
                return TemplateModel(
                    previewUrl = roomModel.previewUrl,
                    frameUrl = roomModel.frameUrl ?: "",
                    cells = cells,
                    isUsed = roomModel.isUsed ?: false,
                    isPro = roomModel.isPro ?: false,
                    isReward = roomModel.isReward ?: false,
                    isFree = roomModel.isFree ?: false,
                    bannerUrl = roomModel.bannerUrl ?: ""
                )
            }

            val categoryModels = categories.map { categoryModel ->
                val categoryName = categoryModel.category
                val templates = (categoryModel.templates ?: emptyList()).map { toTemplateModel(it) }
                
                TemplateCategoryModel(
                    category = categoryName,
                    templates = templates
                )
            }

            val allTemplates = categoryModels.flatMap { it.templates.orEmpty() }
            
            val result = mutableListOf<TemplateCategoryModel>()
            result.add(
                TemplateCategoryModel(
                    category = "All",
                    templates = allTemplates
                )
            )
            result.addAll(categoryModels)
            result
        }
        return data
    }

}