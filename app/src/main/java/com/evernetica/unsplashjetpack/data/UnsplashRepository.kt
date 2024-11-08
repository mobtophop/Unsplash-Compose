package com.evernetica.unsplashjetpack.data

import androidx.paging.Pager
import androidx.paging.PagingConfig

class UnsplashRepository(private val remoteDataSource: RemoteDataSource) {
    fun getImages(query: String?) =
        Pager(
            config = PagingConfig(
                10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { UnsplashDataSource(remoteDataSource, query) }
        ).flow
}