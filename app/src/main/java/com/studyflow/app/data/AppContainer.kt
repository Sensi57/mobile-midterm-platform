package com.studyflow.app.data

object AppContainer {
    val taskRepository: TaskRepository by lazy { TaskRepository() }
}
