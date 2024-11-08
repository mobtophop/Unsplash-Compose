package com.evernetica.unsplashjetpack.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UnsplashSearchResult(
    var results: List<UnsplashPhotoData>,
)

@Serializable
data class UnsplashPhotoData(
    var id: String?,
    var color: String?,
    var urls: UnsplashPhotoLinks,
    var user: UnsplashUserInfo,
)

@Serializable
data class UnsplashPhotoLinks(
    var raw: String?,
    var full: String?,
    var regular: String?,
    var small: String?,
    var thumb: String?,
)

@Serializable
data class UnsplashUserInfo(
    var id: String?,
    var name: String?,
    @SerialName("profile_image") var profileImage: UnsplashUserPfp,
)

@Serializable
data class UnsplashUserPfp(
    var small: String?,
    var medium: String?,
    var large: String?,
)