package com.studyflow.app.ui.stats

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studyflow.app.ui.components.ScreenHeader
import com.studyflow.app.ui.components.SectionTitle
import com.studyflow.app.ui.components.StatTile
import com.studyflow.app.ui.components.SurfaceCard
import com.studyflow.app.ui.components.icon

@Composable
fun StatsRoute(
    viewModel: StatsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    StatsScreen(state = state, modifier = modifier)
}

@Composable
fun StatsScreen(
    state: StatsUiState,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader(title = "Statistics", subtitle = "See how your study habits add up.")

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                icon = Icons.Rounded.TaskAlt,
                value = "${state.doneTasks}/${state.totalTasks}",
                label = "Tasks completed",
                accent = colors.primary,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                icon = Icons.Rounded.TrendingUp,
                value = "${state.completionPercent}%",
                label = "Completion rate",
                accent = colors.secondary,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                icon = Icons.Rounded.Schedule,
                value = formatMinutes(state.totalMinutes),
                label = "Total focus time",
                accent = colors.tertiary,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                icon = Icons.Rounded.LocalFireDepartment,
                value = "${state.streak} ${if (state.streak == 1) "day" else "days"}",
                label = "Current streak",
                accent = colors.error,
                modifier = Modifier.weight(1f)
            )
        }

        SurfaceCard {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle("Last 7 days")
                    Text(
                        text = formatMinutes(state.weekMinutes),
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.primary
                    )
                }
                WeeklyChart(days = state.week)
            }
        }

        SurfaceCard {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionTitle("Progress by category")
                if (state.categories.isEmpty()) {
                    Text(
                        text = "Add tasks to see your progress by category.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                }
                state.categories.forEach { CategoryRow(it) }
            }
        }
    }
}

@Composable
private fun WeeklyChart(days: List<DayStat>, modifier: Modifier = Modifier) {
    val maxMinutes = (days.maxOfOrNull { it.minutes } ?: 0).coerceAtLeast(1)
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(190.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        days.forEach { day ->
            val fraction by animateFloatAsState(
                targetValue = day.minutes.toFloat() / maxMinutes,
                animationSpec = tween(durationMillis = 700),
                label = "bar-${day.label}"
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.62f)
                            .fillMaxHeight(fraction.coerceAtLeast(0.03f))
                            .clip(
                                RoundedCornerShape(
                                    topStart = 10.dp,
                                    topEnd = 10.dp,
                                    bottomStart = 4.dp,
                                    bottomEnd = 4.dp
                                )
                            )
                            .background(
                                when {
                                    day.minutes == 0 -> colors.outlineVariant
                                    day.isToday -> colors.primary
                                    else -> colors.primary.copy(alpha = 0.35f)
                                }
                            )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (day.minutes > 0) day.minutes.toString() else "-",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
                )
                Text(
                    text = day.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (day.isToday) colors.primary else colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(stat: CategoryStat) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = stat.category.icon,
                contentDescription = null,
                tint = colors.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stat.category.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onBackground
                )
                Text(
                    text = "${stat.done}/${stat.total}",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurfaceVariant
                )
            }
            LinearProgressIndicator(
                progress = { stat.progress },
                modifier = Modifier.fillMaxWidth(),
                trackColor = colors.surfaceVariant
            )
        }
    }
}

private fun formatMinutes(minutes: Int): String =
    if (minutes >= 60) "${minutes / 60}h ${minutes % 60}m" else "${minutes}m"
