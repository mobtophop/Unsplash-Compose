package com.evernetica.unsplashjetpack.data

import com.evernetica.unsplashjetpack.api.UnsplashApiService

class RemoteDataSource(private val apiService: UnsplashApiService) {
    suspend fun getLatestImages(page: String?, perPage: String?) = apiService.getLatestImages(page = page, perPage = perPage)
    suspend fun getSearchImages(page: String, perPage: String, query: String) = apiService.getSearchImages(page = page, perPage = perPage, query =  query)
}