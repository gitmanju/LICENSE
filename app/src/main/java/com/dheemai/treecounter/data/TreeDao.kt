package com.dheemai.treecounter.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TreeDao {
    @Query("SELECT * FROM trees ORDER BY timestamp DESC")
    fun getAllTrees(): Flow<List<Tree>>

    @Insert
    suspend fun insert(tree: Tree): Long

    @Update
    suspend fun update(tree: Tree)

    @Delete
    suspend fun delete(tree: Tree)

    @Query("SELECT * FROM trees WHERE id = :id")
    suspend fun getById(id: Long): Tree?

    @Query("SELECT * FROM trees WHERE plotId = :plotId ORDER BY timestamp DESC")
    fun getTreesByPlot(plotId: Long): Flow<List<Tree>>

    @Query("DELETE FROM trees WHERE plotId = :plotId")
    suspend fun deleteTreesByPlotId(plotId: Long)
}
