package com.example.watchdiary.ui.navigation

sealed class NavRoutes(val route: String) {
    data object List : NavRoutes("list")
    data object Add : NavRoutes("add")
    data object Details : NavRoutes("details/{entryId}") {
        fun createRoute(entryId: Int) = "details/$entryId"
    }
    data object Edit : NavRoutes("edit/{entryId}") {
        fun createRoute(entryId: Int) = "edit/$entryId"
    }
}