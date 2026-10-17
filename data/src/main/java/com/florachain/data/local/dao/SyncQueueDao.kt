package com.florachain.data.local.dao

import androidx.room.*
import com.florachain.data.local.entity.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {

    @Query("SELECT COUNT(*) FROM sync_queue")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC")
    suspend fun getAllPendingActions(): List<SyncQueueEntity>

    @Insert
    suspend fun insertAction(action: SyncQueueEntity): Long

    @Update
    suspend fun updateAction(action: SyncQueueEntity)

    @Delete
    suspend fun deleteAction(action: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE queueId = :id")
    suspend fun deleteById(id: Long)
}
