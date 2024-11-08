package com.evernetica.unsplashjetpack.nav

sealed class NavRoute(val route:String) {
    data object PhotosListScreen: NavRoute(LIST_PHOTO_ROUTE)
    data object PhotosDetailScreen: NavRoute(PHOTO_DETAIL_ROUTE)
}

private const val LIST_PHOTO_ROUTE = "list photos screen"
private const val PHOTO_DETAIL_ROUTE = "photos detail screen"
