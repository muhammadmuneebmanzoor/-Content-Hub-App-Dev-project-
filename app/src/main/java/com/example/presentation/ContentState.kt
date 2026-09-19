package com.example.presentation

import com.example.domain.model.ContentArticle
import com.example.domain.model.SortOrder

sealed interface ContentEvent {
    object Refresh : ContentEvent
    data class SelectCategory(val category: String) : ContentEvent
    data class UpdateSearchQuery(val query: String) : ContentEvent
    data class ToggleBookmark(val articleId: Int, val currentStatus: Boolean) : ContentEvent
    data class SelectArticle(val article: ContentArticle?) : ContentEvent
    data class ToggleOfflineMode(val simulateOffline: Boolean) : ContentEvent
    data class ChangeSortOrder(val sortOrder: SortOrder) : ContentEvent
    object DismissNotification : ContentEvent
    object RetryConnection : ContentEvent
    data class ToggleViewJsonPayload(val show: Boolean) : ContentEvent
}

data class TelemetryStats(
    val lastSyncLatencyMs: Long = 0,
    val lastSyncTimestamp: Long = 0,
    val payloadItemCount: Int = 0,
    val totalRequests: Int = 0,
    val successfulSyncs: Int = 0,
    val cachedFallbacks: Int = 0,
    val endpointUrl: String = "https://dummyjson.com/posts"
)

data class ContentState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val articles: List<ContentArticle> = emptyList(),
    val filteredArticles: List<ContentArticle> = emptyList(),
    val categories: List<String> = listOf("All", "Bookmarked", "Engineering", "Cloud & Data", "Architecture", "Design", "Security"),
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.DEFAULT,
    val selectedArticle: ContentArticle? = null,
    val isDeviceOnline: Boolean = true,
    val isSimulatedOffline: Boolean = false,
    val activeBannerMessage: String? = null,
    val isBannerError: Boolean = false,
    val telemetry: TelemetryStats = TelemetryStats(),
    val isViewingJsonPayload: Boolean = false
) {
    val isEffectivelyOnline: Boolean get() = isDeviceOnline && !isSimulatedOffline
}
