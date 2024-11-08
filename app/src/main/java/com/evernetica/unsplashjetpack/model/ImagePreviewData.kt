package com.evernetica.unsplashjetpack.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImagePreviewData(
    val id: String,
    val imageUrl: String,
    val fullImageUrl: String,
    val color: String,
    val authorName: String,
    val authorPfp: String,
) : Parcelable