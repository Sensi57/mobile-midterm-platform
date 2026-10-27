package com.studyflow.app.ui

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.studyflow.app.data.AppContainer
import com.studyflow.app.ui.focus.FocusViewModel
import com.studyflow.app.ui.stats.StatsViewModel
import com.studyflow.app.ui.tasks.TasksViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { TasksViewModel(AppContainer.taskRepository) }
        initializer { FocusViewModel(AppContainer.taskRepository) }
        initializer { StatsViewModel(AppContainer.taskRepository) }
    }
}
