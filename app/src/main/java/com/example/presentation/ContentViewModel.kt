package com.example.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.ContentRepository
import com.example.data.repository.SyncResult
import com.example.domain.model.ContentArticle
import com.example.domain.model.SortOrder
import com.example.util.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContentViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = ContentRepository(database)
    private val networkMonitor = NetworkMonitor(application)

    private val _uiState = MutableStateFlow(ContentState(isLoading = true))
    val uiState: StateFlow<ContentState> = _uiState.asStateFlow()

    init {
        // Initialize room pre-seed if brand new install
        viewModelScope.launch {
            repository.initializeCacheIfEmpty()
        }

        // Observe network state
        viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                _uiState.update { current ->
                    val nowOnline = online && !current.isSimulatedOffline
                    current.copy(
                        isDeviceOnline = online,
                        activeBannerMessage = if (!nowOnline) {
                            "Offline Mode • Seamlessly serving local Room cache"
                        } else if (current.activeBannerMessage?.contains("Offline") == true) {
                            "Network Restored • Cloud connection established"
                        } else {
                            current.activeBannerMessage
                        },
                        isBannerError = !nowOnline
                    )
                }

                // If transitioned to online, perform automatic background cloud sync
                if (online && !_uiState.value.isSimulatedOffline) {
                    performCloudSync(isRefresh = false)
                }
            }
        }

        // Observe Room database stream
        viewModelScope.launch {
            repository.articlesStream.collect { rawArticles ->
                _uiState.update { state ->
                    val filtered = applyFilterAndSort(
                        articles = rawArticles,
                        query = state.searchQuery,
                        category = state.selectedCategory,
                        sort = state.sortOrder
                    )
                    state.copy(
                        articles = rawArticles,
                        filteredArticles = filtered,
                        isLoading = false
                    )
                }
            }
        }

        // Initial sync attempt
        performCloudSync(isRefresh = false)
    }

    fun dispatch(event: ContentEvent) {
        when (event) {
            is ContentEvent.Refresh -> performCloudSync(isRefresh = true)
            is ContentEvent.RetryConnection -> performCloudSync(isRefresh = true)
            is ContentEvent.SelectCategory -> onCategorySelected(event.category)
            is ContentEvent.UpdateSearchQuery -> onSearchQueryChanged(event.query)
            is ContentEvent.ChangeSortOrder -> onSortOrderChanged(event.sortOrder)
            is ContentEvent.ToggleBookmark -> onToggleBookmark(event.articleId, event.currentStatus)
            is ContentEvent.SelectArticle -> onSelectArticle(event.article)
            is ContentEvent.ToggleOfflineMode -> onToggleOfflineMode(event.simulateOffline)
            is ContentEvent.DismissNotification -> dismissNotification()
            is ContentEvent.ToggleViewJsonPayload -> _uiState.update { it.copy(isViewingJsonPayload = event.show) }
        }
    }

    private fun performCloudSync(isRefresh: Boolean) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val isOffline = currentState.isSimulatedOffline || !currentState.isDeviceOnline

            _uiState.update {
                it.copy(
                    isRefreshing = isRefresh,
                    isLoading = !isRefresh && it.articles.isEmpty(),
                    telemetry = it.telemetry.copy(
                        totalRequests = it.telemetry.totalRequests + 1
                    )
                )
            }

            val result = repository.syncFromRemote(isOfflineSimulated = isOffline)

            when (result) {
                is SyncResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isRefreshing = false,
                            isLoading = false,
                            activeBannerMessage = "Successfully fetched ${result.count} remote records (${result.latencyMs}ms)",
                            isBannerError = false,
                            telemetry = state.telemetry.copy(
                                lastSyncLatencyMs = result.latencyMs,
                                lastSyncTimestamp = result.timestamp,
                                payloadItemCount = result.count,
                                successfulSyncs = state.telemetry.successfulSyncs + 1
                            )
                        )
                    }
                }
                is SyncResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            isRefreshing = false,
                            isLoading = false,
                            activeBannerMessage = if (isOffline) {
                                "Offline Mode Active • Displaying ${result.fallbackCount} cached payloads from Room"
                            } else {
                                "Network issue: ${result.errorMessage}. Using cached data."
                            },
                            isBannerError = true,
                            telemetry = state.telemetry.copy(
                                lastSyncTimestamp = result.timestamp,
                                cachedFallbacks = state.telemetry.cachedFallbacks + 1
                            )
                        )
                    }
                }
            }
        }
    }

    private fun onCategorySelected(category: String) {
        _uiState.update { state ->
            val filtered = applyFilterAndSort(state.articles, state.searchQuery, category, state.sortOrder)
            state.copy(selectedCategory = category, filteredArticles = filtered)
        }
    }

    private fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            val filtered = applyFilterAndSort(state.articles, query, state.selectedCategory, state.sortOrder)
            state.copy(searchQuery = query, filteredArticles = filtered)
        }
    }

    private fun onSortOrderChanged(sortOrder: SortOrder) {
        _uiState.update { state ->
            val filtered = applyFilterAndSort(state.articles, state.searchQuery, state.selectedCategory, sortOrder)
            state.copy(sortOrder = sortOrder, filteredArticles = filtered)
        }
    }

    private fun onToggleBookmark(articleId: Int, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.toggleBookmark(articleId, currentStatus)
        }
    }

    private fun onSelectArticle(article: ContentArticle?) {
        _uiState.update { it.copy(selectedArticle = article, isViewingJsonPayload = false) }
    }

    private fun onToggleOfflineMode(simulateOffline: Boolean) {
        _uiState.update { state ->
            state.copy(
                isSimulatedOffline = simulateOffline,
                activeBannerMessage = if (simulateOffline) {
                    "Simulated Offline Active • Network requests blocked, using local cache"
                } else {
                    "Online Mode Resumed • Syncing with remote cloud endpoint..."
                },
                isBannerError = simulateOffline
            )
        }
        if (!simulateOffline && _uiState.value.isDeviceOnline) {
            performCloudSync(isRefresh = true)
        }
    }

    private fun dismissNotification() {
        _uiState.update { it.copy(activeBannerMessage = null) }
    }

    private fun applyFilterAndSort(
        articles: List<ContentArticle>,
        query: String,
        category: String,
        sort: SortOrder
    ): List<ContentArticle> {
        var result = articles

        // Filter by category / bookmark
        if (category == "Bookmarked") {
            result = result.filter { it.isBookmarked }
        } else if (category != "All") {
            result = result.filter { it.category.equals(category, ignoreCase = true) }
        }

        // Filter by search query
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter { article ->
                article.title.lowercase().contains(q) ||
                    article.body.lowercase().contains(q) ||
                    article.authorName.lowercase().contains(q) ||
                    article.category.lowercase().contains(q) ||
                    article.tags.any { it.lowercase().contains(q) }
            }
        }

        // Sort
        result = when (sort) {
            SortOrder.DEFAULT -> result.sortedBy { it.id }
            SortOrder.MOST_LIKED -> result.sortedByDescending { it.likes }
            SortOrder.MOST_VIEWED -> result.sortedByDescending { it.views }
        }

        return result
    }
}
