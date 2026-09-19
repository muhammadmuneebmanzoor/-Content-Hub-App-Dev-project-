package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.ArticleDao
import com.example.data.local.ArticleEntity
import com.example.data.remote.ContentApiService
import com.example.data.remote.NetworkClient
import com.example.domain.model.ContentArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

sealed class SyncResult {
    data class Success(
        val count: Int,
        val latencyMs: Long,
        val timestamp: Long,
        val source: String = "Remote REST API (JSON)"
    ) : SyncResult()

    data class Failure(
        val errorMessage: String,
        val fallbackCount: Int,
        val timestamp: Long = System.currentTimeMillis()
    ) : SyncResult()
}

class ContentRepository(
    private val database: AppDatabase,
    private val apiService: ContentApiService = NetworkClient.apiService
) {
    private val dao: ArticleDao = database.articleDao()

    val articlesStream: Flow<List<ContentArticle>> = dao.getAllArticlesFlow()
        .map { entities ->
            entities.map { it.toDomainModel() }
        }
        .flowOn(Dispatchers.IO)

    suspend fun initializeCacheIfEmpty() = withContext(Dispatchers.IO) {
        val count = dao.getCount()
        if (count == 0) {
            dao.insertArticles(InitialSeedData.getPreloadedArticles())
        }
    }

    suspend fun syncFromRemote(isOfflineSimulated: Boolean): SyncResult = withContext(Dispatchers.IO) {
        if (isOfflineSimulated) {
            val cachedCount = dao.getCount()
            return@withContext SyncResult.Failure(
                errorMessage = "Network disconnected (Simulated Offline Mode)",
                fallbackCount = cachedCount
            )
        }

        var latency = 0L
        try {
            var responsePayloadCount = 0
            val fetchTime = measureTimeMillis {
                val response = apiService.getPosts(limit = 30)
                if (!response.isSuccessful || response.body() == null) {
                    throw Exception("HTTP ${response.code()}: ${response.message()}")
                }

                val remotePosts = response.body()!!.posts
                responsePayloadCount = remotePosts.size

                // Retrieve existing bookmarked IDs to preserve user choices
                val bookmarkedIds = dao.getBookmarkedIds().toSet()

                val authors = listOf(
                    "Sarah Lin", "Alex Chen", "Maya Patel", "David Kim",
                    "Jordan Hayes", "Dr. Emily Watson", "Liam O'Connor", "Elena Vance"
                )
                val categories = listOf("Engineering", "Cloud & Data", "Architecture", "Design", "Security", "AI & Systems")

                val moshi = NetworkClient.getMoshiInstance()
                val jsonAdapter = moshi.adapter(Map::class.java)

                val entities = remotePosts.mapIndexed { index, post ->
                    val author = authors[post.userId % authors.size]
                    val cat = categories[index % categories.size]
                    val words = post.body.split("\\s+".toRegex()).size
                    val readTime = (words / 35).coerceAtLeast(2)
                    val isBookmarked = bookmarkedIds.contains(post.id)

                    val payloadMap = mapOf(
                        "id" to post.id,
                        "title" to post.title,
                        "body" to post.body,
                        "tags" to post.tags,
                        "views" to post.views,
                        "userId" to post.userId,
                        "category" to cat,
                        "author" to author
                    )
                    val rawJson = try {
                        jsonAdapter.toJson(payloadMap)
                    } catch (_: Exception) {
                        "{\"id\":${post.id},\"title\":\"${post.title.replace("\"", "\\\"")}\"}"
                    }

                    ArticleEntity(
                        id = post.id,
                        title = post.title.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                        body = post.body,
                        tags = if (post.tags.isEmpty()) "tech,cloud,api" else post.tags.joinToString(","),
                        likes = (150 + (post.id * 17) % 380),
                        dislikes = ((post.id * 3) % 25),
                        views = if (post.views > 0) post.views else (600 + (post.id * 89) % 2200),
                        userId = post.userId,
                        authorName = author,
                        readTimeMinutes = readTime,
                        category = cat,
                        isBookmarked = isBookmarked,
                        cachedAtTimestamp = System.currentTimeMillis(),
                        rawJsonPayload = rawJson
                    )
                }

                dao.insertArticles(entities)
            }
            latency = fetchTime

            SyncResult.Success(
                count = responsePayloadCount,
                latencyMs = latency,
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            val cachedCount = dao.getCount()
            SyncResult.Failure(
                errorMessage = e.message ?: "Failed to connect to remote JSON endpoint",
                fallbackCount = cachedCount
            )
        }
    }

    suspend fun toggleBookmark(articleId: Int, currentStatus: Boolean) = withContext(Dispatchers.IO) {
        dao.setBookmark(articleId, !currentStatus)
    }

    private fun ArticleEntity.toDomainModel(): ContentArticle {
        return ContentArticle(
            id = id,
            title = title,
            body = body,
            tags = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() },
            likes = likes,
            dislikes = dislikes,
            views = views,
            userId = userId,
            authorName = authorName,
            readTimeMinutes = readTimeMinutes,
            category = category,
            isBookmarked = isBookmarked,
            cachedAtTimestamp = cachedAtTimestamp,
            rawJsonPayload = rawJsonPayload
        )
    }
}
