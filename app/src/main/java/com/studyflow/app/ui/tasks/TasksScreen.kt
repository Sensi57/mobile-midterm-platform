package com.studyflow.app.ui.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studyflow.app.data.TaskFilter
import com.studyflow.app.ui.components.ChoiceChipRow
import com.studyflow.app.ui.components.EmptyState
import com.studyflow.app.ui.components.GradientCard
import com.studyflow.app.ui.components.ProgressRing
import com.studyflow.app.ui.components.ScreenHeader
import com.studyflow.app.ui.components.TaskCard
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun TasksRoute(
    viewModel: TasksViewModel,
    snackbarHostState: SnackbarHostState,
    onEdit: (Long) -> Unit,
    onFocus: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is TasksEvent.TaskDeleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "\"${event.task.title}\" deleted",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.onUndoDelete(event.task)
                }
            }
        }
    }

    TasksScreen(
        state = state,
        query = query,
        onQueryChange = viewModel::onQueryChange,
        onFilterChange = viewModel::onFilterChange,
        onToggle = viewModel::onToggle,
        onDelete = viewModel::onDelete,
        onEdit = onEdit,
        onFocus = onFocus,
        modifier = modifier
    )
}

@Composable
fun TasksScreen(
    state: TasksUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterChange: (TaskFilter) -> Unit,
    onToggle: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onEdit: (Long) -> Unit,
    onFocus: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val greeting = remember { greetingForNow() }
    val dateText = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM")) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "header") {
            ScreenHeader(title = greeting, subtitle = dateText)
        }
        item(key = "hero") {
            ProgressHero(done = state.doneCount, total = state.totalCount, progress = state.progress)
        }
        item(key = "search") {
            SearchField(query = query, onQueryChange = onQueryChange)
        }
        item(key = "filters") {
            ChoiceChipRow(
                options = TaskFilter.entries,
                selected = state.filter,
                label = { it.label },
                onSelected = onFilterChange
            )
        }
        if (state.tasks.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    icon = Icons.Rounded.TaskAlt,
                    title = if (query.isNotBlank()) "No matches" else "Nothing here yet",
                    message = if (query.isNotBlank()) {
                        "Try a different search term."
                    } else {
                        "Tap \"New task\" to plan your next study session."
                    }
                )
            }
        }
        items(items = state.tasks, key = { it.id }) { task ->
            TaskCard(
                task = task,
                onToggle = { onToggle(task.id) },
                onDelete = { onDelete(task.id) },
                onClick = { onEdit(task.id) },
                onFocus = { onFocus(task.id) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Composable
private fun ProgressHero(done: Int, total: Int, progress: Float) {
    GradientCard {
        ProgressRing(
            progress = progress,
            modifier = Modifier.size(84.dp),
            strokeWidth = 9.dp,
            trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f),
            progressColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(modifier = Modifier.width(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Daily progress",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = "$done of $total tasks completed",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            )
            Text(
                text = motivation(total, progress),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search tasks") },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                }
            }
        },
        singleLine = true,
        shape = CircleShape,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
    )
}

private fun greetingForNow(): String = when (LocalTime.now().hour) {
    in 5..11 -> "Good morning"
    in 12..17 -> "Good afternoon"
    else -> "Good evening"
}

private fun motivation(total: Int, progress: Float): String = when {
    total == 0 -> "Add your first task"
    progress >= 1f -> "All done, great work!"
    progress >= 0.5f -> "More than halfway there"
    else -> "Let's get started"
}
