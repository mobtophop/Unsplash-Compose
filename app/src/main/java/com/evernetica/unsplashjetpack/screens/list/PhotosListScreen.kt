package com.evernetica.unsplashjetpack.screens.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.evernetica.unsplashjetpack.R
import com.evernetica.unsplashjetpack.nav.NavRoute
import com.evernetica.unsplashjetpack.nav.navigate
import com.evernetica.unsplashjetpack.ui.view.SearchField
import com.evernetica.unsplashjetpack.ui.view.nav_bar.NavTopBar
import com.evernetica.unsplashjetpack.utils.Constants.PHOTO_KEY
import com.evernetica.unsplashjetpack.view_model.PhotosListViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotosListScreenView(
    modifier: Modifier = Modifier,
    viewModel: PhotosListViewModel = koinViewModel(),
    navController: NavHostController
) {
    val response = viewModel.photos.collectAsLazyPagingItems()
    val searchQuery by viewModel.queryText.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Scaffold(
        topBar = {
            NavTopBar(
                title = stringResource(R.string.main_title),
                canNavigateBack = false,
            )
        },
        content = { padding ->
            Surface(
                modifier = Modifier.padding(padding),
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SearchField(
                            searchQuery = searchQuery ?: "",
                            onQueryChanged = viewModel::onQueryTextChanged,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )
                    }
                    Spacer(Modifier.size(4.dp))
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.refresh() },
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()) {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            modifier = modifier.fillMaxSize(),
                            verticalItemSpacing = 4.dp,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            items(response.itemCount) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(response[it]?.imageUrl ?: "-")
                                        .crossfade(true)
                                        .build(),
                                    placeholder = painterResource(R.drawable.ic_launcher_foreground),
                                    contentDescription = "",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .clickable {
                                            navController.navigate(
                                                route = NavRoute.PhotosDetailScreen.route,
                                                args = bundleOf(PHOTO_KEY to response[it])
                                            )
                                        }
                                )
                            }
                        }
                    }
                }
            }
        })
    response.apply {
        when {
            (loadState.refresh is LoadState.Loading || loadState.append is LoadState.Loading) && response.itemCount == 0 -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            loadState.refresh is LoadState.Error || loadState.append is LoadState.Error -> {
                Text(modifier = Modifier.fillMaxSize(), text = "Error")
            }

            loadState.refresh is LoadState.NotLoading -> {
            }
        }
    }

}
