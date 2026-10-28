package com.example.watchdiary.ui.components
import androidx.compose.ui.platform.LocalContext
import com.example.watchdiary.R

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.watchdiary.ui.navigation.NavRoutes

@Composable
fun WatchDiaryBottomBar(
    navController: NavHostController
) {
    val context = LocalContext.current
    val items = listOf(
        NavRoutes.List,
        NavRoutes.Add
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        NavigationBar(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route

                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(NavRoutes.List.route)
                            launchSingleTop = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(),
                    icon = {
                        when (item) {
                            NavRoutes.List -> Icon(Icons.Outlined.ListAlt, contentDescription = null)
                            NavRoutes.Add -> Icon(Icons.Outlined.AddCircle, contentDescription = null)
                            else -> {}
                        }
                    },

                    label = {
                        Text(
                            when (item) {
                                NavRoutes.List -> context.getString(R.string.list)
                                NavRoutes.Add -> context.getString(R.string.add)
                                else -> ""
                            }
                        )
                    }
                )
            }
        }
    }
}
