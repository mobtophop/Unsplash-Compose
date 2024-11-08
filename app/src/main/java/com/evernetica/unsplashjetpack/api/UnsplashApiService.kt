package com.evernetica.unsplashjetpack.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface UnsplashApiService {

    @GET("photos")
    suspend fun getLatestImages(
        @Query("page") page: String?, // Paging 3
        @Query("per_page") perPage: String?,
    ): Response<List<UnsplashPhotoData>>

    @GET("search/photos")
    suspend fun getSearchImages(
        @Query("query") query: String?,
        @Query("page") page: String?, // Paging 3
        @Query("per_page") perPage: String?,
    ): Response<UnsplashSearchResult>
}