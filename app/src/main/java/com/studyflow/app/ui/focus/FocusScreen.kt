package com.studyflow.app.ui.focus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studyflow.app.data.Task
import com.studyflow.app.ui.components.ChoiceChipRow
import com.studyflow.app.ui.components.ProgressRing
import com.studyflow.app.ui.components.ScreenHeader
import com.studyflow.app.ui.components.SectionTitle
import com.studyflow.app.ui.components.StatTile
import com.studyflow.app.ui.components.SurfaceCard

@Composable
fun FocusRoute(
    viewModel: FocusViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FocusScreen(
        state = state,
        onModeSelected = viewModel::selectMode,
        onToggle = viewModel::toggleTimer,
        onReset = viewModel::reset,
        onSkip = viewModel::skip,
        onTaskSelected = { viewModel.selectTask(it?.id) },
        modifier = modifier
    )
}

@Composable
fun FocusScreen(
    state: FocusUiState,
    onModeSelected: (FocusMode) -> Unit,
    onToggle: () -> Unit,
    onReset: () -> Unit,
    onSkip: () -> Unit,
    onTaskSelected: (Task?) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    DisposableEffect(state.isRunning) {
        view.keepScreenOn = state.isRunning
        onDispose { view.keepScreenOn = false }
    }

    val ringColor = if (state.mode == FocusMode.FOCUS) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.tertiary
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ScreenHeader(
            title = "Focus",
            subtitle = "One thing at a time. You've got this."
        )

        ChoiceChipRow(
            options = FocusMode.entries,
            selected = state.mode,
            label = { "${it.label} ${it.minutes}m" },
            onSelected = onModeSelected,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        ProgressRing(
            progress = state.progress,
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.CenterHorizontally),
            strokeWidth = 16.dp,
            trackColor = ringColor.copy(alpha = 0.15f),
            progressColor = ringColor,
            animationMillis = 1000
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = state.timeLabel,
                    style = MaterialTheme.typography.displayMedium.merge(TextStyle(fontFeatureSettings = "tnum")),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (state.mode == FocusMode.FOCUS) {
                        "Session ${state.positionInCycle} of 4"
                    } else {
                        state.mode.label
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedIconButton(onClick = onReset, modifier = Modifier.size(52.dp)) {
                Icon(Icons.Rounded.Replay, contentDescription = "Reset timer")
            }
            FilledIconButton(
                onClick = onToggle,
                modifier = Modifier.size(84.dp),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = if (state.isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (state.isRunning) "Pause timer" else "Start timer",
                    modifier = Modifier.size(44.dp)
                )
            }
            OutlinedIconButton(onClick = onSkip, modifier = Modifier.size(52.dp)) {
                Icon(Icons.Rounded.SkipNext, contentDescription = "Skip to next phase")
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle("Working on")
            ChoiceChipRow(
                options = listOf<Task?>(null) + state.availableTasks,
                selected = state.selectedTask,
                label = { it?.title?.take(24) ?: "Free focus" },
                onSelected = onTaskSelected
            )
            state.selectedTask?.let { SelectedTaskCard(it) }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                icon = Icons.Rounded.TaskAlt,
                value = state.sessionsToday.toString(),
                label = "Sessions today",
                accent = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                icon = Icons.Rounded.Schedule,
                value = "${state.minutesToday} min",
                label = "Focused today",
                accent = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SelectedTaskCard(task: Task) {
    SurfaceCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${task.completedSessions}/${task.plannedSessions}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End
                )
            }
            LinearProgressIndicator(
                progress = { task.sessionProgress },
                modifier = Modifier.fillMaxWidth(),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
