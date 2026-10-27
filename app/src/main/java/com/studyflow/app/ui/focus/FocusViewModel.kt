package com.studyflow.app.ui.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyflow.app.data.Task
import com.studyflow.app.data.TaskRepository
import java.time.LocalDate
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FocusMode(val label: String, val seconds: Int) {
    FOCUS("Focus", 25 * 60),
    SHORT_BREAK("Short break", 5 * 60),
    LONG_BREAK("Long break", 15 * 60);

    val minutes: Int get() = seconds / 60
}

data class FocusUiState(
    val mode: FocusMode = FocusMode.FOCUS,
    val remainingSeconds: Int = FocusMode.FOCUS.seconds,
    val isRunning: Boolean = false,
    val cycle: Int = 0,
    val selectedTask: Task? = null,
    val availableTasks: List<Task> = emptyList(),
    val sessionsToday: Int = 0,
    val minutesToday: Int = 0
) {
    val progress: Float
        get() = 1f - remainingSeconds.toFloat() / mode.seconds

    val timeLabel: String
        get() = String.format(Locale.US, "%02d:%02d", remainingSeconds / 60, remainingSeconds % 60)

    val positionInCycle: Int
        get() = cycle % SESSIONS_BEFORE_LONG_BREAK + 1
}

sealed interface FocusEvent {
    data class TimerFinished(val mode: FocusMode) : FocusEvent
}

private const val SESSIONS_BEFORE_LONG_BREAK = 4

private data class TimerState(
    val mode: FocusMode = FocusMode.FOCUS,
    val remaining: Int = FocusMode.FOCUS.seconds,
    val running: Boolean = false,
    val cycle: Int = 0
)

class FocusViewModel(private val repository: TaskRepository) : ViewModel() {

    private val timer = MutableStateFlow(TimerState())
    private val selectedTaskId = MutableStateFlow<Long?>(null)
    private val _events = Channel<FocusEvent>(Channel.BUFFERED)
    private var tickerJob: Job? = null

    val events: Flow<FocusEvent> = _events.receiveAsFlow()

    val uiState: StateFlow<FocusUiState> = combine(
        timer,
        selectedTaskId,
        repository.tasks,
        repository.sessions
    ) { timerState, selectedId, tasks, sessions ->
        val today = LocalDate.now()
        val todaySessions = sessions.filter { it.date == today }
        val activeTasks = tasks.filterNot { it.isDone }
        FocusUiState(
            mode = timerState.mode,
            remainingSeconds = timerState.remaining,
            isRunning = timerState.running,
            cycle = timerState.cycle,
            selectedTask = activeTasks.firstOrNull { it.id == selectedId },
            availableTasks = activeTasks,
            sessionsToday = todaySessions.size,
            minutesToday = todaySessions.sumOf { it.minutes }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = FocusUiState()
    )

    fun selectTask(id: Long?) = selectedTaskId.update { id }

    fun selectMode(mode: FocusMode) {
        tickerJob?.cancel()
        timer.update { it.copy(mode = mode, remaining = mode.seconds, running = false) }
    }

    fun toggleTimer() {
        if (timer.value.running) pause() else start()
    }

    fun reset() {
        tickerJob?.cancel()
        timer.update { it.copy(remaining = it.mode.seconds, running = false) }
    }

    fun skip() {
        tickerJob?.cancel()
        advance(completed = false)
    }

    private fun start() {
        if (timer.value.running) return
        timer.update { it.copy(running = true) }
        tickerJob = viewModelScope.launch {
            while (timer.value.remaining > 0) {
                delay(TICK_MILLIS)
                timer.update { it.copy(remaining = it.remaining - 1) }
            }
            advance(completed = true)
        }
    }

    private fun pause() {
        tickerJob?.cancel()
        timer.update { it.copy(running = false) }
    }

    private fun advance(completed: Boolean) {
        val current = timer.value
        var cycle = current.cycle
        val next = if (current.mode == FocusMode.FOCUS) {
            if (completed) {
                repository.recordSession(selectedTaskId.value, current.mode.minutes)
                cycle++
            }
            if (completed && cycle % SESSIONS_BEFORE_LONG_BREAK == 0) {
                FocusMode.LONG_BREAK
            } else {
                FocusMode.SHORT_BREAK
            }
        } else {
            FocusMode.FOCUS
        }
        timer.value = TimerState(mode = next, remaining = next.seconds, running = false, cycle = cycle)
        if (completed) _events.trySend(FocusEvent.TimerFinished(current.mode))
    }

    private companion object {
        const val TICK_MILLIS = 1_000L
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
