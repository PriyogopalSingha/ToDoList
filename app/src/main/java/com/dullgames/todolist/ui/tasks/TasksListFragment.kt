package com.dullgames.todolist.ui.tasks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dullgames.todolist.ADD_TASK_RESULT_OK
import com.dullgames.todolist.R
import com.dullgames.todolist.data.SortOrder
import com.dullgames.todolist.data.Task
import com.dullgames.todolist.databinding.TasksListBinding
import com.dullgames.todolist.util.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasksListFragment : Fragment(R.layout.tasks_list), TaskListAdapter.OnItemClickListener {
    private val viewModel: TaskViewModel by viewModels()
    private lateinit var searchView: SearchView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = TasksListBinding.bind(view)
        val taskListAdapter = TaskListAdapter(this)

        binding.apply {
            tasksRecyclerView.apply {
                adapter = taskListAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ){
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val task = taskListAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onTaskSwiping(task)
                }
            }).attachToRecyclerView(tasksRecyclerView)

            fabAddTask.setOnClickListener {
                viewModel.addNewTask()
            }
        }
        setFragmentResultListener("add_new_request"){ _, bundle ->
            val code = bundle.getInt("result_code")
            viewModel.showCompletedMessage(code)

        }


        viewModel.tasksLiveData.observe(viewLifecycleOwner) {
            taskListAdapter.submitList(it)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tasksEvent.collect { event ->
                    when(event) {
                        is TaskViewModel.TaskEvent.ShowUndoDeleteTaskMessage -> {
                            Snackbar.make(requireView(), "Task Deleted", Snackbar.LENGTH_LONG)
                                .setAction("UNDO") {
                                    viewModel.onUndoDeleteTask(event.task)
                                }.show()
                        }

                        is TaskViewModel.TaskEvent.AddNewTask -> {
                            val action = TasksListFragmentDirections.actionTasksListFragmentToAddOrEditTaskFragment(task = null, title = "New Task")
                            findNavController().navigate(action)

                        }
                        is TaskViewModel.TaskEvent.EditTask -> {
                            val action = TasksListFragmentDirections.actionTasksListFragmentToAddOrEditTaskFragment(task = event.task, title = "Edit Task")
                            findNavController().navigate(action)

                        }

                        is TaskViewModel.TaskEvent.ShowTaskCreatedOrEditedMessage -> {
                            Snackbar.make(requireView(),event.msg, Snackbar.LENGTH_LONG).show()
                        }

                        is TaskViewModel.TaskEvent.DeleteCompletedTasks -> {
                            val action = DeleteCompletedTaskDirections.actionGlobalDeleteCompletedTask()
                            findNavController().navigate(action)
                        }
                    }
                }
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_tasks, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }

        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.hide_completed_action).isChecked =
                viewModel.preferencesFlow.first().hideCompleted
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.hide_completed_action -> {
                item.isChecked = !item.isChecked
                viewModel.updateHideCompleted(item.isChecked)
                true
            }

            R.id.sort_by_date -> {
                viewModel.updateSortOrder(SortOrder.BY_DATE)
                true
            }

            R.id.sort_by_name -> {
                viewModel.updateSortOrder(SortOrder.BY_NAME)
                true
            }

            R.id.delete_completed_action -> {
                viewModel.deleteCompletedTasks()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClick(task: Task) {
        viewModel.onSelectedTask(task)
    }

    override fun onItemChecked(task: Task, onChecked: Boolean) {
        viewModel.checkAndUpdateTask(task, onChecked)
    }
}