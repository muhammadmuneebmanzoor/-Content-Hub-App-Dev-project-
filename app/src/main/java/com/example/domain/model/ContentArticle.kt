package com.example.domain.model

enum class SortOrder(val displayName: String) {
    DEFAULT("Featured"),
    MOST_LIKED("Most Liked"),
    MOST_VIEWED("Most Viewed")
}

data class ContentArticle(
    val id: Int,
    val title: String,
    val body: String,
    val tags: List<String>,
    val likes: Int,
    val dislikes: Int,
    val views: Int,
    val userId: Int,
    val authorName: String,
    val readTimeMinutes: Int,
    val category: String,
    val isBookmarked: Boolean,
    val cachedAtTimestamp: Long,
    val rawJsonPayload: String = ""
)
