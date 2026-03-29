package com.dheemai.treecounter.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trees")
data class Tree(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val species: String,
    val notes: String,
    val latitude: Double,
    val longitude: Double,
    val photoPath: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val additionalNames: String = "", // comma-separated list of alternative names
    val plotName: String = "",
    val plotId: Long = 0,
    val canvasX: Float = -1f,  // normalized 0..1, -1 = not placed
    val canvasY: Float = -1f
)
