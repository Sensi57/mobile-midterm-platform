package com.studyflow.app.data

import java.time.LocalDate

internal object SampleData {

    private const val HOUR = 3_600_000L

    fun tasks(): List<Task> {
        val now = System.currentTimeMillis()
        return listOf(
            Task(
                id = 1,
                title = "Linear algebra problem set",
                notes = "Chapters 4-5, exercises 12-30",
                category = Category.STUDY,
                priority = Priority.HIGH,
                plannedSessions = 4,
                completedSessions = 1,
                createdAt = now - 6 * HOUR
            ),
            Task(
                id = 2,
                title = "Prepare for algorithms midterm",
                notes = "Sorting, graphs, dynamic programming",
                category = Category.EXAM,
                priority = Priority.HIGH,
                plannedSessions = 6,
                createdAt = now - 5 * HOUR
            ),
            Task(
                id = 3,
                title = "Android midterm project",
                notes = "Compose UI, navigation, ViewModel",
                category = Category.PROJECT,
                priority = Priority.MEDIUM,
                plannedSessions = 5,
                completedSessions = 2,
                createdAt = now - 4 * HOUR
            ),
            Task(
                id = 4,
                title = "Read operating systems chapter 7",
                notes = "Memory management and paging",
                category = Category.READING,
                priority = Priority.MEDIUM,
                plannedSessions = 2,
                createdAt = now - 3 * HOUR
            ),
            Task(
                id = 5,
                title = "Plan the week",
                notes = "",
                category = Category.PERSONAL,
                priority = Priority.LOW,
                plannedSessions = 1,
                completedSessions = 1,
                isDone = true,
                createdAt = now - 2 * HOUR
            ),
            Task(
                id = 6,
                title = "English essay draft",
                notes = "Introduction and first two paragraphs",
                category = Category.STUDY,
                priority = Priority.LOW,
                plannedSessions = 3,
                completedSessions = 3,
                isDone = true,
                createdAt = now - HOUR
            )
        )
    }

    fun sessions(): List<FocusSession> {
        val today = LocalDate.now()
        val sessionsPerDay = mapOf(6L to 2, 5L to 3, 4L to 1, 3L to 4, 2L to 2, 1L to 3, 0L to 1)
        return sessionsPerDay.flatMap { (daysAgo, count) ->
            List(count) { FocusSession(taskId = null, minutes = 25, date = today.minusDays(daysAgo)) }
        }
    }
}
