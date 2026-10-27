package com.studyflow.app.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.studyflow.app.ui.AppViewModelProvider
import com.studyflow.app.ui.edit.TaskEditorRoute
import com.studyflow.app.ui.focus.FocusEvent
import com.studyflow.app.ui.focus.FocusMode
import com.studyflow.app.ui.focus.FocusRoute
import com.studyflow.app.ui.focus.FocusViewModel
import com.studyflow.app.ui.stats.StatsRoute
import com.studyflow.app.ui.stats.StatsViewModel
import com.studyflow.app.ui.tasks.TasksRoute
import com.studyflow.app.ui.tasks.TasksViewModel

@Composable
fun StudyFlowApp() {
    val navController = rememberNavController()
    val tasksViewModel: TasksViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val focusViewModel: FocusViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val statsViewModel: StatsViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val snackbarHostState = remember { SnackbarHostState() }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = TopDestination.entries.any { it.route == currentRoute }

    val navigateTopLevel: (TopDestination) -> Unit = { destination ->
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    LaunchedEffect(focusViewModel) {
        focusViewModel.events.collect { event ->
            when (event) {
                is FocusEvent.TimerFinished -> snackbarHostState.showSnackbar(
                    if (event.mode == FocusMode.FOCUS) {
                        "Session complete! Time for a break."
                    } else {
                        "Break is over. Ready to focus?"
                    }
                )
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (currentRoute == Routes.TASKS) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(Routes.editor(null)) },
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    text = { Text("New task") }
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                    TopDestination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = { navigateTopLevel(destination) },
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.TASKS,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.TASKS) {
                TasksRoute(
                    viewModel = tasksViewModel,
                    snackbarHostState = snackbarHostState,
                    onEdit = { id -> navController.navigate(Routes.editor(id)) },
                    onFocus = { id ->
                        focusViewModel.selectTask(id)
                        navigateTopLevel(TopDestination.FOCUS)
                    }
                )
            }
            composable(Routes.FOCUS) {
                FocusRoute(viewModel = focusViewModel)
            }
            composable(Routes.STATS) {
                StatsRoute(viewModel = statsViewModel)
            }
            composable(
                route = Routes.EDITOR_PATTERN,
                arguments = listOf(
                    navArgument(Routes.ARG_TASK_ID) {
                        type = NavType.LongType
                        defaultValue = Routes.NO_TASK
                    }
                ),
                enterTransition = { fadeIn() + slideInVertically { it / 8 } },
                popExitTransition = { fadeOut() + slideOutVertically { it / 8 } }
            ) { entry ->
                val taskId = entry.arguments
                    ?.getLong(Routes.ARG_TASK_ID)
                    ?.takeIf { it != Routes.NO_TASK }
                TaskEditorRoute(
                    taskId = taskId,
                    viewModel = tasksViewModel,
                    onClose = { navController.popBackStack() }
                )
            }
        }
    }
}
