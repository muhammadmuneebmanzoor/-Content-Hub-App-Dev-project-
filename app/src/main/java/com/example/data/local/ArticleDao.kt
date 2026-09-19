package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY id ASC")
    fun getAllArticlesFlow(): Flow<List<ArticleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<ArticleEntity>)

    @Query("UPDATE articles SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun setBookmark(id: Int, isBookmarked: Boolean)

    @Query("SELECT isBookmarked FROM articles WHERE id = :id")
    suspend fun isBookmarked(id: Int): Boolean?

    @Query("SELECT id FROM articles WHERE isBookmarked = 1")
    suspend fun getBookmarkedIds(): List<Int>

    @Query("SELECT COUNT(*) FROM articles")
    suspend fun getCount(): Int

    @Query("DELETE FROM articles")
    suspend fun clearAll()
}
