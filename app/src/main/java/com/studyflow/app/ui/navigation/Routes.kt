package com.studyflow.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val TASKS = "tasks"
    const val FOCUS = "focus"
    const val STATS = "stats"
    const val ARG_TASK_ID = "taskId"
    const val EDITOR_PATTERN = "editor?$ARG_TASK_ID={$ARG_TASK_ID}"
    const val NO_TASK = -1L

    fun editor(taskId: Long?): String = "editor?$ARG_TASK_ID=${taskId ?: NO_TASK}"
}

enum class TopDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    TASKS(Routes.TASKS, "Tasks", Icons.Rounded.Checklist),
    FOCUS(Routes.FOCUS, "Focus", Icons.Rounded.Timer),
    STATS(Routes.STATS, "Stats", Icons.Rounded.BarChart)
}
