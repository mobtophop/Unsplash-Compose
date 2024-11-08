package com.evernetica.unsplashjetpack.screens.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.evernetica.unsplashjetpack.R
import com.evernetica.unsplashjetpack.model.ImagePreviewData
import com.evernetica.unsplashjetpack.ui.view.nav_bar.NavTopBar
import com.evernetica.unsplashjetpack.ui.view.zoom.rememberZoomState
import com.evernetica.unsplashjetpack.ui.view.zoom.zoomable

@Composable
fun PhotosDetailsScreenView(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    photoItem: ImagePreviewData?
) {
    Scaffold(
        topBar = {
            NavTopBar(
                title = photoItem?.authorName ?: stringResource(R.string.main_title),
                canNavigateBack = true,
                navigateUp = { navController.popBackStack() }
            )
        },
        content = { padding ->
            Surface(
                modifier = Modifier.padding(padding),
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoItem?.imageUrl ?: "-")
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().zoomable(rememberZoomState())
                )
            }
        })
}