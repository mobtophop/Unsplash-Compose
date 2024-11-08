package com.evernetica.unsplashjetpack.nav

import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.evernetica.unsplashjetpack.model.ImagePreviewData
import com.evernetica.unsplashjetpack.screens.detail.PhotosDetailsScreenView
import com.evernetica.unsplashjetpack.screens.list.PhotosListScreenView
import com.evernetica.unsplashjetpack.utils.Constants.PHOTO_KEY

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = NavRoute.PhotosListScreen.route) {
        composable(route = NavRoute.PhotosListScreen.route) {
            PhotosListScreenView(navController = navController)
        }
        composable(route = NavRoute.PhotosDetailScreen.route) { bundle ->
            PhotosDetailsScreenView(
                navController = navController,
                photoItem = bundle.arguments?.parcelable<ImagePreviewData>(PHOTO_KEY)
            )
        }
    }
}

fun NavController.navigate(
    route: String,
    args: Bundle,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null
) {
    val nodeId = graph.findNode(route = route)?.id
    if (nodeId != null) {
        navigate(nodeId, args, navOptions, navigatorExtras)
    }
}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}