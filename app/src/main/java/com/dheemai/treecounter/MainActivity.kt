package com.dheemai.treecounter

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dheemai.treecounter.data.FarmPlot
import com.dheemai.treecounter.data.Tree
import com.dheemai.treecounter.ui.screens.*
import com.dheemai.treecounter.ui.theme.TreeCounterTheme
import com.dheemai.treecounter.viewmodel.TreeViewModel

private const val PREFS_NAME = "treecounter_prefs"
private const val KEY_USER_NAME = "user_name"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TreeCounterTheme {
                TreeCounterNavHost()
            }
        }
    }
}

@Composable
fun TreeCounterNavHost() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val savedName = remember { prefs.getString(KEY_USER_NAME, "") ?: "" }

    val vm: TreeViewModel = viewModel()
    var screen by remember {
        mutableStateOf<Screen>(if (savedName.isBlank()) Screen.Welcome else Screen.Home)
    }
    var userName by remember { mutableStateOf(savedName) }

    when (val s = screen) {
        Screen.Welcome -> WelcomeScreen(
            onDone = { name, farmPlotName ->
                prefs.edit().putString(KEY_USER_NAME, name).apply()
                userName = name
                if (farmPlotName.isNotBlank()) vm.addFarmPlot(FarmPlot(name = farmPlotName))
                screen = Screen.Home
            }
        )
        Screen.Home -> HomeScreen(
            viewModel = vm,
            userName = userName,
            onFarmPlotClick = { farmPlot -> screen = Screen.FarmPlotDetail(farmPlot) }
        )
        is Screen.FarmPlotDetail -> PlotDetailScreen(
            farmPlot = s.farmPlot,
            userName = userName,
            viewModel = vm,
            onBack = { screen = Screen.Home },
            onAddTree = { screen = Screen.Add(s.farmPlot) },
            onOpenMap = { screen = Screen.FarmPlotMap(s.farmPlot) },
            onOpenCanvas = { screen = Screen.PlotCanvas(s.farmPlot) },
            onTreeClick = { tree -> screen = Screen.Detail(tree, s.farmPlot) }
        )
        is Screen.Add -> AddTreeScreen(
            viewModel = vm,
            farmPlot = s.farmPlot,
            onBack = { screen = Screen.FarmPlotDetail(s.farmPlot) }
        )
        is Screen.FarmPlotMap -> MapScreen(
            viewModel = vm,
            plotId = s.farmPlot.id,
            onBack = { screen = Screen.FarmPlotDetail(s.farmPlot) }
        )
        is Screen.PlotCanvas -> PlotCanvasScreen(
            farmPlot = s.farmPlot,
            viewModel = vm,
            onBack = { screen = Screen.FarmPlotDetail(s.farmPlot) }
        )
        is Screen.Detail -> TreeDetailScreen(
            tree = s.tree,
            viewModel = vm,
            onBack = { screen = Screen.FarmPlotDetail(s.farmPlot) }
        )
    }
}

sealed class Screen {
    object Welcome : Screen()
    object Home : Screen()
    data class FarmPlotDetail(val farmPlot: FarmPlot) : Screen()
    data class Add(val farmPlot: FarmPlot) : Screen()
    data class FarmPlotMap(val farmPlot: FarmPlot) : Screen()
    data class PlotCanvas(val farmPlot: FarmPlot) : Screen()
    data class Detail(val tree: Tree, val farmPlot: FarmPlot) : Screen()
}
