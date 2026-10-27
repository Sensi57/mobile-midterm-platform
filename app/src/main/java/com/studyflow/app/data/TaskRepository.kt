package com.studyflow.app.data

import java.time.LocalDate
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TaskRepository {

    private val idGenerator = AtomicLong(100)
    private val _tasks = MutableStateFlow(SampleData.tasks())
    private val _sessions = MutableStateFlow(SampleData.sessions())

    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()
    val sessions: StateFlow<List<FocusSession>> = _sessions.asStateFlow()

    fun find(id: Long): Task? = _tasks.value.firstOrNull { it.id == id }

    fun add(draft: TaskDraft) {
        val task = Task(
            id = idGenerator.incrementAndGet(),
            title = draft.title,
            notes = draft.notes,
            category = draft.category,
            priority = draft.priority,
            plannedSessions = draft.plannedSessions
        )
        _tasks.update { it + task }
    }

    fun update(id: Long, draft: TaskDraft) {
        _tasks.update { list ->
            list.map { task ->
                if (task.id == id) {
                    task.copy(
                        title = draft.title,
                        notes = draft.notes,
                        category = draft.category,
                        priority = draft.priority,
                        plannedSessions = draft.plannedSessions
                    )
                } else {
                    task
                }
            }
        }
    }

    fun toggleDone(id: Long) {
        _tasks.update { list ->
            list.map { task -> if (task.id == id) task.copy(isDone = !task.isDone) else task }
        }
    }

    fun remove(id: Long): Task? {
        val removed = find(id)
        _tasks.update { list -> list.filterNot { it.id == id } }
        return removed
    }

    fun restore(task: Task) {
        _tasks.update { it + task }
    }

    fun recordSession(taskId: Long?, minutes: Int) {
        _sessions.update { it + FocusSession(taskId, minutes, LocalDate.now()) }
        if (taskId != null) {
            _tasks.update { list ->
                list.map { task ->
                    if (task.id == taskId) task.copy(completedSessions = task.completedSessions + 1) else task
                }
            }
        }
    }
}
