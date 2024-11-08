package com.evernetica.unsplashjetpack.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.evernetica.unsplashjetpack.model.ImagePreviewData
import com.evernetica.unsplashjetpack.use_case.UnsplashUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class PhotosListViewModel(private val unsplashUseCase: UnsplashUseCase, application: Application) :
    AndroidViewModel(application) {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _queryText = MutableStateFlow<String?>("")
    val queryText = _queryText.asStateFlow()

    private val _photos: MutableStateFlow<PagingData<ImagePreviewData>> =
        MutableStateFlow(PagingData.empty())
    var photos = _photos.asStateFlow()
        private set

    init {
        viewModelScope.launch {
            _queryText
                .debounce(700)
                .distinctUntilChanged()
                .mapLatest { query ->
                    query?.let {
                        unsplashUseCase.getImages(query).cachedIn(viewModelScope).collect {
                            _photos.value = it
                            _isRefreshing.tryEmit(false)
                        }
                    }
                }
                .flowOn(Dispatchers.IO)
                .collect()
        }
    }

    fun onQueryTextChanged(query: String) {
        _queryText.tryEmit(query)
    }

    fun refresh() {
        _isRefreshing.tryEmit(true)
        val query = _queryText.value
        _queryText.tryEmit(null)
        viewModelScope.launch {
            delay(800)
            _queryText.emit(query)
        }
    }
}