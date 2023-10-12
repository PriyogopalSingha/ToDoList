package com.dullgames.todolist.ui.taskdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dullgames.todolist.ADD_TASK_RESULT_OK
import com.dullgames.todolist.EDIT_TASK_RESULT_OK
import com.dullgames.todolist.data.Task
import com.dullgames.todolist.data.TaskDao
import dagger.assisted.Assisted
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddOrEditTaskViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val state: SavedStateHandle
    ) : ViewModel() {

    val task = state.get<Task>("task")

    var taskName = state.get<String>("taskName") ?: task?.name ?: ""
        set(value) {
            field = value
            state.set("taskName", value)
        }

    var taskImportance = state.get<Boolean>("taskImportant") ?: task?.important ?: false
        set(value) {
            field = value
            state.set("taskImportance", value)
        }

    private val addTaskEventChannel = Channel<AddTaskEvent>()
    val addTaskEvent = addTaskEventChannel.receiveAsFlow()
    fun saveNewTask() {
        if(taskName.isBlank()){
            showInvalidMessage("Invalid Task!")
            return
        }
        if (task != null) {
            val updatedTask = task.copy(name = taskName, important =  taskImportance)
            updateTask(updatedTask)
        } else {
            val newTask = Task(name = taskName, important = taskImportance)
            createTask(newTask)
        }
    }

    private fun showInvalidMessage(s: String) {
         viewModelScope.launch { addTaskEventChannel.send(AddTaskEvent.ShowInvalidSnackbar(s)) }
    }

    private fun createTask(newTask: Task) = viewModelScope.launch {
        taskDao.insert(newTask)
        addTaskEventChannel.send(AddTaskEvent.ShowTaskCreatedOrEditedSnackbar(ADD_TASK_RESULT_OK))
    }


    private fun updateTask(updatedTask: Task) = viewModelScope.launch {
        taskDao.update(updatedTask)
        addTaskEventChannel.send(AddTaskEvent.ShowTaskCreatedOrEditedSnackbar(EDIT_TASK_RESULT_OK))
    }

    sealed class AddTaskEvent{
        data class ShowInvalidSnackbar(val msg: String): AddTaskEvent()
        data class ShowTaskCreatedOrEditedSnackbar(val code: Int): AddTaskEvent()
    }


}