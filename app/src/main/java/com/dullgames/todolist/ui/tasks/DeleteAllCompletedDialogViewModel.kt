package com.dullgames.todolist.ui.tasks

import androidx.lifecycle.ViewModel
import com.dullgames.todolist.data.TaskDao
import com.dullgames.todolist.di.ApplicationScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteAllCompletedDialogViewModel @Inject constructor(
   private val taskDao: TaskDao,
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel() {
    fun deleteAllCompleted(){
        applicationScope.launch {
            taskDao.deleteAllCompletedTasks()
        }
    }
}