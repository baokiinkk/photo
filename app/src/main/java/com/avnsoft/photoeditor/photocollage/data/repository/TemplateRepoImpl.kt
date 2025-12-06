package com.avnsoft.photoeditor.photocollage.data.repository

import android.content.Context
import com.avnsoft.photoeditor.photocollage.data.local.sharedPref.EditorSharedPref
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateCategoryModel
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateContentModel
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateModel
import com.avnsoft.photoeditor.photocollage.data.model.template.TemplateResponse
import com.basesource.base.network.CollageApiService
import com.basesource.base.network.safeApiCall
import com.basesource.base.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single
class TemplateRepoImpl(
    private val context: Context,
    private val api: CollageApiService,
) {
    suspend fun getPreviewTemplates(): Flow<List<TemplateCategoryModel>> = flow {
        val response = safeApiCall<TemplateResponse>(
            context = context,
            apiCallMock = { api.getMockTemplates() },
            apiCall = { api.getTemplates() }
        )
        
        when (response) {
            is Result.Success -> {
                val urlRoot = response.data.urlRoot ?: ""
                
                // Helper function to convert px to ratio (0-1)
                fun convertToRatio(value: Float?, bound: Float): Float? {
                    return value?.let { 
                        // If bound is valid (> 0) and value > 1, assume it's in px
                        if (bound > 0f && it > 1f) {
                            // Convert px to ratio
                            it / bound
                        } else {
                            // If value <= 1 or bound is invalid, assume it's already a ratio
                            it
                        }
                    }
                }
                
                val categoryModels = response.data.data.map { categoryData ->
                    val templates = categoryData.content?.map { templateItem ->
                        // Get template bounds (width and height)
                        val templateWidth = templateItem.width?.toFloat() ?: 1f
                        val templateHeight = templateItem.height?.toFloat() ?: 1f
                        
                        // Use layer for content, fallback to placeholder if layer is empty
                        val layerContents = templateItem.layer?.map { layerItem ->
                            val url = layerItem.urlThumb?.let {
                                if (it.startsWith("http") || it.startsWith("file://")) it
                                else "${urlRoot}${it}"
                            }
                            TemplateContentModel(
                                urlThumb = url,
                                x = convertToRatio(layerItem.x, templateWidth),
                                y = convertToRatio(layerItem.y, templateHeight),
                                width = convertToRatio(layerItem.width, templateWidth),
                                height = convertToRatio(layerItem.height, templateHeight),
                                rotate = layerItem.rotate
                            )
                        } ?: emptyList()

                        // If layer is empty, use placeholder data
                        val contents = templateItem.placeholder?.map { placeholderItem ->
                            TemplateContentModel(
                                urlThumb = null,
                                x = convertToRatio(placeholderItem.x, templateWidth),
                                y = convertToRatio(placeholderItem.y, templateHeight),
                                width = convertToRatio(placeholderItem.width, templateWidth),
                                height = convertToRatio(placeholderItem.height, templateHeight),
                                rotate = placeholderItem.rotate
                            )
                        } ?: emptyList()

                        // Build URLs with urlRoot if needed
                        val bannerUrl = templateItem.bannerUrl?.let {
                            if (it.startsWith("http") || it.startsWith("file://")) it
                            else "${urlRoot}${it}"
                        }
                        val previewUrl = templateItem.previewUrl?.let {
                            if (it.startsWith("http") || it.startsWith("file://")) it
                            else "${urlRoot}${it}"
                        }
                        val frameUrl = templateItem.frameUrl?.let {
                            if (it.startsWith("http") || it.startsWith("file://")) it
                            else "${urlRoot}${it}"
                        }

                        TemplateModel(
                            bannerUrl = bannerUrl,
                            previewUrl = previewUrl,
                            frameUrl = frameUrl ?: "",
                            cells = contents,
                            layer = layerContents,
                            timeCreate = System.currentTimeMillis().toString(),
                            isUsed = templateItem.isUsed ?: false,
                            isPro = templateItem.isPro ?: false,
                            isReward = templateItem.isReward ?: false,
                            isFree = templateItem.isFree ?: false,
                            width = templateItem.width,
                            height = templateItem.height
                        )
                    }

                    TemplateCategoryModel(
                        category = categoryData.categoryName,
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
                emit(result)
            }
            else -> {
                emit(emptyList())
            }
        }
    }

}