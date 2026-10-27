package com.studyflow.app.data

import java.time.LocalDate

enum class Priority(val label: String, val weight: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3)
}

enum class Category(val label: String) {
    STUDY("Study"),
    EXAM("Exam"),
    PROJECT("Project"),
    READING("Reading"),
    PERSONAL("Personal")
}

enum class TaskFilter(val label: String) {
    ALL("All"),
    ACTIVE("Active"),
    DONE("Done")
}

data class Task(
    val id: Long,
    val title: String,
    val notes: String,
    val category: Category,
    val priority: Priority,
    val plannedSessions: Int,
    val completedSessions: Int = 0,
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val sessionProgress: Float
        get() = if (plannedSessions == 0) 0f else (completedSessions.toFloat() / plannedSessions).coerceIn(0f, 1f)
}

data class TaskDraft(
    val title: String,
    val notes: String,
    val category: Category,
    val priority: Priority,
    val plannedSessions: Int
)

data class FocusSession(
    val taskId: Long?,
    val minutes: Int,
    val date: LocalDate
)
