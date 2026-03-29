package com.dheemai.treecounter.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dheemai.treecounter.data.FarmPlot
import com.dheemai.treecounter.data.Tree
import com.dheemai.treecounter.data.TreeDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TreeViewModel(app: Application) : AndroidViewModel(app) {

    private val db = TreeDatabase.getInstance(app)
    private val treeDao = db.treeDao()
    private val farmPlotDao = db.farmPlotDao()

    val trees = treeDao.getAllTrees()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val farmPlots = farmPlotDao.getAllFarmPlots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getTreesForPlot(plotId: Long) = treeDao.getTreesByPlot(plotId)

    fun addFarmPlot(farmPlot: FarmPlot) = viewModelScope.launch { farmPlotDao.insert(farmPlot) }

    fun deleteFarmPlot(farmPlot: FarmPlot) = viewModelScope.launch {
        treeDao.deleteTreesByPlotId(farmPlot.id)
        farmPlotDao.delete(farmPlot)
    }

    fun addTree(tree: Tree) = viewModelScope.launch { treeDao.insert(tree) }
    fun updateTree(tree: Tree) = viewModelScope.launch { treeDao.update(tree) }
    fun deleteTree(tree: Tree) = viewModelScope.launch { treeDao.delete(tree) }
}
