package com.studyflow.app.ui.edit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.studyflow.app.data.Category
import com.studyflow.app.data.Priority
import com.studyflow.app.data.TaskDraft
import com.studyflow.app.ui.components.ChoiceChipRow
import com.studyflow.app.ui.components.icon
import com.studyflow.app.ui.tasks.TasksViewModel
import com.studyflow.app.ui.theme.accentColor
import kotlin.math.roundToInt

private const val TITLE_LIMIT = 60
private const val NOTES_LIMIT = 160
private const val MIN_SESSIONS = 1
private const val MAX_SESSIONS = 8
private const val SESSION_MINUTES = 25

@Composable
fun TaskEditorRoute(
    taskId: Long?,
    viewModel: TasksViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val existing = remember(taskId) { taskId?.let(viewModel::find) }

    var title by rememberSaveable(taskId) { mutableStateOf(existing?.title.orEmpty()) }
    var notes by rememberSaveable(taskId) { mutableStateOf(existing?.notes.orEmpty()) }
    var category by rememberSaveable(taskId) { mutableStateOf(existing?.category ?: Category.STUDY) }
    var priority by rememberSaveable(taskId) { mutableStateOf(existing?.priority ?: Priority.MEDIUM) }
    var sessions by rememberSaveable(taskId) { mutableStateOf(existing?.plannedSessions ?: 2) }
    var showTitleError by rememberSaveable { mutableStateOf(false) }

    TaskEditorScreen(
        isEditing = taskId != null,
        title = title,
        notes = notes,
        category = category,
        priority = priority,
        sessions = sessions,
        showTitleError = showTitleError,
        onTitleChange = {
            title = it.take(TITLE_LIMIT)
            if (title.isNotBlank()) showTitleError = false
        },
        onNotesChange = { notes = it.take(NOTES_LIMIT) },
        onCategoryChange = { category = it },
        onPriorityChange = { priority = it },
        onSessionsChange = { sessions = it },
        onSave = {
            if (title.isBlank()) {
                showTitleError = true
            } else {
                viewModel.save(
                    id = taskId,
                    draft = TaskDraft(title.trim(), notes.trim(), category, priority, sessions)
                )
                onClose()
            }
        },
        onBack = onClose,
        modifier = modifier
    )
}

@Composable
fun TaskEditorScreen(
    isEditing: Boolean,
    title: String,
    notes: String,
    category: Category,
    priority: Priority,
    sessions: Int,
    showTitleError: Boolean,
    onTitleChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onCategoryChange: (Category) -> Unit,
    onPriorityChange: (Priority) -> Unit,
    onSessionsChange: (Int) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isEditing) "Edit task" else "New task",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        FieldSection(label = "What do you need to do?") {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Finish calculus homework") },
                singleLine = true,
                isError = showTitleError,
                supportingText = {
                    Text(if (showTitleError) "Title can't be empty" else "${title.length}/$TITLE_LIMIT")
                },
                shape = MaterialTheme.shapes.medium
            )
        }

        FieldSection(label = "Notes") {
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Optional details") },
                minLines = 3,
                maxLines = 4,
                supportingText = { Text("${notes.length}/$NOTES_LIMIT") },
                shape = MaterialTheme.shapes.medium
            )
        }

        FieldSection(label = "Category") {
            ChoiceChipRow(
                options = Category.entries,
                selected = category,
                label = { it.label },
                onSelected = onCategoryChange,
                icon = { it.icon }
            )
        }

        FieldSection(label = "Priority") {
            PrioritySelector(selected = priority, onSelected = onPriorityChange)
        }

        FieldSection(label = "Focus sessions") {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$sessions ${if (sessions == 1) "session" else "sessions"}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "about ${sessions * SESSION_MINUTES} min",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Slider(
                    value = sessions.toFloat(),
                    onValueChange = { onSessionsChange(it.roundToInt()) },
                    valueRange = MIN_SESSIONS.toFloat()..MAX_SESSIONS.toFloat(),
                    steps = MAX_SESSIONS - MIN_SESSIONS - 1
                )
            }
        }

        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = CircleShape
        ) {
            Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isEditing) "Save changes" else "Create task",
                style = MaterialTheme.typography.labelLarge
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun FieldSection(
    label: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        content()
    }
}

@Composable
private fun PrioritySelector(
    selected: Priority,
    onSelected: (Priority) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Priority.entries.forEach { priority ->
            val accent = priority.accentColor()
            val isSelected = priority == selected
            Card(
                onClick = { onSelected(priority) },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        accent.copy(alpha = 0.14f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLowest
                    }
                ),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) accent else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Text(
                    text = priority.label,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
