package com.studyflow.app.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyflow.app.data.Category
import com.studyflow.app.data.FocusSession
import com.studyflow.app.data.Task
import com.studyflow.app.data.TaskRepository
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DayStat(val label: String, val minutes: Int, val isToday: Boolean)

data class CategoryStat(val category: Category, val total: Int, val done: Int) {
    val progress: Float
        get() = if (total == 0) 0f else done.toFloat() / total
}

data class StatsUiState(
    val totalTasks: Int = 0,
    val doneTasks: Int = 0,
    val totalMinutes: Int = 0,
    val weekMinutes: Int = 0,
    val streak: Int = 0,
    val week: List<DayStat> = emptyList(),
    val categories: List<CategoryStat> = emptyList()
) {
    val completionPercent: Int
        get() = if (totalTasks == 0) 0 else doneTasks * 100 / totalTasks
}

class StatsViewModel(private val repository: TaskRepository) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(repository.tasks, repository.sessions, ::buildState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = buildState(repository.tasks.value, repository.sessions.value)
        )

    private fun buildState(tasks: List<Task>, sessions: List<FocusSession>): StatsUiState {
        val today = LocalDate.now()
        val minutesByDate = sessions
            .groupBy { it.date }
            .mapValues { (_, daySessions) -> daySessions.sumOf { it.minutes } }

        val week = (WEEK_LENGTH - 1 downTo 0).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            DayStat(
                label = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                minutes = minutesByDate[date] ?: 0,
                isToday = daysAgo == 0
            )
        }

        var streak = 0
        var cursor = if (minutesByDate.getOrDefault(today, 0) > 0) today else today.minusDays(1)
        while (minutesByDate.getOrDefault(cursor, 0) > 0) {
            streak++
            cursor = cursor.minusDays(1)
        }

        val categories = Category.entries
            .map { category ->
                val inCategory = tasks.filter { it.category == category }
                CategoryStat(category, inCategory.size, inCategory.count { it.isDone })
            }
            .filter { it.total > 0 }

        return StatsUiState(
            totalTasks = tasks.size,
            doneTasks = tasks.count { it.isDone },
            totalMinutes = sessions.sumOf { it.minutes },
            weekMinutes = week.sumOf { it.minutes },
            streak = streak,
            week = week,
            categories = categories
        )
    }

    private companion object {
        const val WEEK_LENGTH = 7
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
