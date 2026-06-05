package io.testpulse.sdk.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val sessionUuid: String?,
    val jsonPayload: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
