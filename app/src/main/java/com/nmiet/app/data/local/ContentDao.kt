package com.nmiet.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentDao {
    @Query("SELECT * FROM contents ORDER BY publishedAt DESC")
    fun getAllContents(): Flow<List<ContentEntity>>

    @Query("SELECT * FROM contents WHERE id = :id LIMIT 1")
    fun getContentById(id: String): Flow<ContentEntity?>

    @Query("SELECT * FROM contents WHERE id = :id LIMIT 1")
    fun getContentByIdOnce(id: String): ContentEntity?

    @Query("SELECT * FROM contents WHERE isSavedOffline = 1 OR isReceivedViaNearby = 1 ORDER BY publishedAt DESC")
    fun getSavedOfflineContents(): Flow<List<ContentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(entities: List<ContentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(entity: ContentEntity)

    @Query("UPDATE contents SET isSavedOffline = :isSaved WHERE id = :id")
    fun updateSavedOffline(id: String, isSaved: Boolean)

    @Query("SELECT COUNT(*) FROM contents")
    fun countAll(): Int

    @Query("SELECT COUNT(*) FROM contents WHERE isSavedOffline = 1 OR isReceivedViaNearby = 1")
    fun countSavedOffline(): Int

    @Query("DELETE FROM contents")
    fun clearAll()
}
