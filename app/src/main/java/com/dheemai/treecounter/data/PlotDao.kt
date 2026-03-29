package com.dheemai.treecounter.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmPlotDao {
    @Query("SELECT * FROM plots ORDER BY timestamp ASC")
    fun getAllFarmPlots(): Flow<List<FarmPlot>>

    @Insert
    suspend fun insert(farmPlot: FarmPlot): Long

    @Delete
    suspend fun delete(farmPlot: FarmPlot)
}
