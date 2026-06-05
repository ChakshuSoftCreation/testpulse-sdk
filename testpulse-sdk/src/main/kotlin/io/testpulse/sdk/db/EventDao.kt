package io.testpulse.sdk.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface EventDao {

    @Insert
    suspend fun insert(event: EventEntity): Long

    @Insert
    suspend fun insertAll(events: List<EventEntity>): List<Long>

    @Query("SELECT * FROM events WHERE isSynced = 0 ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getUnsynced(limit: Int = 100): List<EventEntity>

    @Query("SELECT COUNT(*) FROM events WHERE isSynced = 0")
    suspend fun getUnsyncedCount(): Int

    @Query("UPDATE events SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("DELETE FROM events WHERE isSynced = 1 AND createdAt < :threshold")
    suspend fun deleteOldSynced(threshold: Long = System.currentTimeMillis() - 86_400_000)

    @Query("DELETE FROM events")
    suspend fun deleteAll()
}
