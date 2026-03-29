package com.dheemai.treecounter.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plots")
data class FarmPlot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val timestamp: Long = System.currentTimeMillis()
)
