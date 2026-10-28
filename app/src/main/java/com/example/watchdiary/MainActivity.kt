package com.example.watchdiary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.watchdiary.ui.components.WatchDiaryBottomBar
import com.example.watchdiary.data.export.ExportManager
import com.example.watchdiary.ui.navigation.NavRoutes
import com.example.watchdiary.ui.screens.AddScreen
import com.example.watchdiary.ui.screens.DetailsScreen
import com.example.watchdiary.ui.screens.EditScreen
import com.example.watchdiary.ui.screens.ListScreen
import com.example.watchdiary.ui.theme.WatchDiaryTheme
import com.example.watchdiary.viewmodel.AddEntryViewModel
import com.example.watchdiary.viewmodel.DetailsViewModel
import com.example.watchdiary.viewmodel.EditEntryViewModel
import com.example.watchdiary.viewmodel.ListViewModel
import com.example.watchdiary.viewmodel.ViewModelFactory

class MainActivity : AppCompatActivity() {

    private val repository by lazy {
        (application as WatchDiaryApp).repository
    }

    private val factory by lazy {
        ViewModelFactory(repository)
    }

    private val exportManager by lazy { ExportManager(applicationContext, repository) }

    private val listViewModel: ListViewModel by viewModels { factory }
    private val addEntryViewModel: AddEntryViewModel by viewModels { factory }
    private val detailsViewModel: DetailsViewModel by viewModels { factory }
    private val editEntryViewModel: EditEntryViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Migrate the language chosen in older WatchDiary versions once.
        val preferences = getSharedPreferences("watch_diary_prefs", MODE_PRIVATE)
        if (!preferences.getBoolean("locale_migrated", false)) {
            val previousLanguage = preferences.getString("language", "ru") ?: "ru"
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(previousLanguage))
            preferences.edit().putBoolean("locale_migrated", true).apply()
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WatchDiaryTheme {
                WatchDiaryRoot(
                    listViewModel = listViewModel,
                    exportManager = exportManager,
                    addEntryViewModel = addEntryViewModel,
                    detailsViewModel = detailsViewModel,
                    editEntryViewModel = editEntryViewModel
                )
            }
        }
    }
}

@Composable
private fun WatchDiaryRoot(
    listViewModel: ListViewModel,
    exportManager: ExportManager,
    addEntryViewModel: AddEntryViewModel,
    detailsViewModel: DetailsViewModel,
    editEntryViewModel: EditEntryViewModel
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (currentRoute == NavRoutes.List.route || currentRoute == NavRoutes.Add.route) {
                WatchDiaryBottomBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.List.route
        ) {
            composable(NavRoutes.List.route) {
                ListScreen(
                    viewModel = listViewModel,
                    exportManager = exportManager,
                    paddingValues = paddingValues,
                    snackbarHostState = snackbarHostState,
                    onEntryClick = { id ->
                        navController.navigate(NavRoutes.Details.createRoute(id))
                    },
                    onEditClick = { id ->
                        navController.navigate(NavRoutes.Edit.createRoute(id))
                    }
                )
            }

            composable(NavRoutes.Add.route) {
                AddScreen(
                    viewModel = addEntryViewModel,
                    paddingValues = paddingValues
                )
            }

            composable(
                route = NavRoutes.Details.route,
                arguments = listOf(
                    navArgument("entryId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getInt("entryId") ?: return@composable
                DetailsScreen(
                    entryId = entryId,
                    viewModel = detailsViewModel,
                    paddingValues = paddingValues,
                    onBack = { navController.popBackStack() },
                    onDeletedOrBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = NavRoutes.Edit.route,
                arguments = listOf(
                    navArgument("entryId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getInt("entryId") ?: return@composable
                EditScreen(
                    entryId = entryId,
                    viewModel = editEntryViewModel,
                    paddingValues = paddingValues,
                    snackbarHostState = snackbarHostState,
                    onBack = { navController.popBackStack() },
                    onSaved = {
                        navController.navigate(NavRoutes.List.route) {
                            popUpTo(NavRoutes.List.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
