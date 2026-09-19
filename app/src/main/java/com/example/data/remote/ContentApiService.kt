package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class ReactionsDto(
    @Json(name = "likes") val likes: Int = 0,
    @Json(name = "dislikes") val dislikes: Int = 0
)

@JsonClass(generateAdapter = true)
data class RemotePost(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "body") val body: String,
    @Json(name = "tags") val tags: List<String> = emptyList(),
    @Json(name = "views") val views: Int = 0,
    @Json(name = "userId") val userId: Int = 1
)

@JsonClass(generateAdapter = true)
data class PostsResponse(
    @Json(name = "posts") val posts: List<RemotePost> = emptyList(),
    @Json(name = "total") val total: Int = 0,
    @Json(name = "skip") val skip: Int = 0,
    @Json(name = "limit") val limit: Int = 0
)

interface ContentApiService {
    @GET("posts")
    suspend fun getPosts(
        @Query("limit") limit: Int = 30
    ): Response<PostsResponse>
}
