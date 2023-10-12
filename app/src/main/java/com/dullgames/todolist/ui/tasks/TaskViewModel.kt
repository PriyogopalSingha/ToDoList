package com.dullgames.todolist.ui.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dullgames.todolist.ADD_TASK_RESULT_OK
import com.dullgames.todolist.EDIT_TASK_RESULT_OK
import com.dullgames.todolist.data.PreferenceManager
import com.dullgames.todolist.data.SortOrder
import com.dullgames.todolist.data.Task
import com.dullgames.todolist.data.TaskDao
import com.google.android.material.snackbar.Snackbar
import dagger.assisted.Assisted
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val preferenceManager: PreferenceManager,
    private val state: SavedStateHandle
) : ViewModel() {

    val searchQuery = state.getLiveData("searchQuery", "")

    val preferencesFlow = preferenceManager.preferencesFlow

    private val taskEventChannel = Channel<TaskEvent>()

    val tasksEvent = taskEventChannel.receiveAsFlow()

    private val tasksFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        taskDao.getTasks(query, filterPreferences.sortOrder, filterPreferences.hideCompleted)
    }

    val tasksLiveData = tasksFlow.asLiveData()

    fun updateSortOrder(sortOrder: SortOrder) =
        viewModelScope.launch { preferenceManager.updateSortOrder(sortOrder) }

    fun updateHideCompleted(hideCompleted: Boolean) =
        viewModelScope.launch { preferenceManager.updateHideCompleted(hideCompleted) }

    fun checkAndUpdateTask(task: Task, isChecked: Boolean) =
        viewModelScope.launch { taskDao.update(task.copy(completed = isChecked)) }

    fun onUndoDeleteTask(task: Task) =
        viewModelScope.launch {
            taskDao.insert(task)
        }


    fun onSelectedTask(task: Task) =
        viewModelScope.launch { taskEventChannel.send(TaskEvent.EditTask(task)) }

    fun onTaskSwiping(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
        taskEventChannel.send(TaskEvent.ShowUndoDeleteTaskMessage(task))
    }

    fun addNewTask() = viewModelScope.launch { taskEventChannel.send(TaskEvent.AddNewTask) }
    fun showCompletedMessage(code: Int) {
        when (code) {
            ADD_TASK_RESULT_OK -> showTaskCreatedOrEditedMessage("Added New Task")
            EDIT_TASK_RESULT_OK -> showTaskCreatedOrEditedMessage("Edited the Task")
        }
    }

    private fun showTaskCreatedOrEditedMessage(text: String) {
        viewModelScope.launch {
            taskEventChannel.send(TaskEvent.ShowTaskCreatedOrEditedMessage(text))
        }
    }

    fun deleteCompletedTasks() {
        viewModelScope.launch {
            taskEventChannel.send(TaskEvent.DeleteCompletedTasks)
        }
    }


    sealed class TaskEvent {
        object AddNewTask : TaskEvent()
        data class EditTask(val task: Task) : TaskEvent()
        data class ShowUndoDeleteTaskMessage(val task: Task) : TaskEvent()
        data class ShowTaskCreatedOrEditedMessage(val msg: String) : TaskEvent()
        object DeleteCompletedTasks : TaskEvent()
    }
}
