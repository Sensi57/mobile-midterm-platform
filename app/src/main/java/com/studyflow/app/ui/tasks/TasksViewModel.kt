package com.studyflow.app.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyflow.app.data.Task
import com.studyflow.app.data.TaskDraft
import com.studyflow.app.data.TaskFilter
import com.studyflow.app.data.TaskRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class TasksUiState(
    val tasks: List<Task> = emptyList(),
    val filter: TaskFilter = TaskFilter.ALL,
    val doneCount: Int = 0,
    val totalCount: Int = 0
) {
    val progress: Float
        get() = if (totalCount == 0) 0f else doneCount.toFloat() / totalCount
}

sealed interface TasksEvent {
    data class TaskDeleted(val task: Task) : TasksEvent
}

class TasksViewModel(private val repository: TaskRepository) : ViewModel() {

    private val filter = MutableStateFlow(TaskFilter.ALL)
    private val _query = MutableStateFlow("")
    private val _events = Channel<TasksEvent>(Channel.BUFFERED)

    val query: StateFlow<String> = _query.asStateFlow()
    val events: Flow<TasksEvent> = _events.receiveAsFlow()

    val uiState: StateFlow<TasksUiState> = combine(repository.tasks, filter, _query, ::buildState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = buildState(repository.tasks.value, TaskFilter.ALL, "")
        )

    fun onQueryChange(value: String) = _query.update { value }

    fun onFilterChange(value: TaskFilter) = filter.update { value }

    fun onToggle(id: Long) = repository.toggleDone(id)

    fun onDelete(id: Long) {
        repository.remove(id)?.let { _events.trySend(TasksEvent.TaskDeleted(it)) }
    }

    fun onUndoDelete(task: Task) = repository.restore(task)

    fun find(id: Long): Task? = repository.find(id)

    fun save(id: Long?, draft: TaskDraft) {
        if (id == null) repository.add(draft) else repository.update(id, draft)
    }

    private fun buildState(tasks: List<Task>, filter: TaskFilter, query: String): TasksUiState {
        val needle = query.trim()
        val visible = tasks
            .filter { task ->
                when (filter) {
                    TaskFilter.ALL -> true
                    TaskFilter.ACTIVE -> !task.isDone
                    TaskFilter.DONE -> task.isDone
                }
            }
            .filter { task ->
                needle.isEmpty() ||
                    task.title.contains(needle, ignoreCase = true) ||
                    task.notes.contains(needle, ignoreCase = true)
            }
            .sortedWith(
                compareBy<Task> { it.isDone }
                    .thenByDescending { it.priority.weight }
                    .thenByDescending { it.createdAt }
            )
        return TasksUiState(
            tasks = visible,
            filter = filter,
            doneCount = tasks.count { it.isDone },
            totalCount = tasks.size
        )
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
