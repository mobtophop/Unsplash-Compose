package com.evernetica.unsplashjetpack.use_case

import androidx.paging.PagingData
import androidx.paging.map
import com.evernetica.unsplashjetpack.data.UnsplashRepository
import com.evernetica.unsplashjetpack.model.ImagePreviewData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UnsplashUseCase(
    private val unsplashRepository: UnsplashRepository
) {
    fun getImages(query: String?):
            Flow<PagingData<ImagePreviewData>> {
        return unsplashRepository.getImages(query).map {
            it.map { data ->
                ImagePreviewData(
                    id = data.id ?: "",
                    imageUrl = data.urls.regular ?: "",
                    fullImageUrl = data.urls.full ?: "",
                    color = data.color ?: "#000000",
                    authorName = data.user.name ?: "",
                    authorPfp = data.user.profileImage.medium ?: "",
                )
            }
        }
    }
}