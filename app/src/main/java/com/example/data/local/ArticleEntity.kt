package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val body: String,
    val tags: String,
    val likes: Int,
    val dislikes: Int,
    val views: Int,
    val userId: Int,
    val authorName: String,
    val readTimeMinutes: Int,
    val category: String,
    val isBookmarked: Boolean = false,
    val cachedAtTimestamp: Long = System.currentTimeMillis(),
    val rawJsonPayload: String = ""
)
