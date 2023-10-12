package com.dullgames.todolist.ui.taskdetails

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.dullgames.todolist.R
import com.dullgames.todolist.databinding.TaskDetailsLayoutBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddOrEditTaskFragment: Fragment(R.layout.task_details_layout) {

    private val viewModel: AddOrEditTaskViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = TaskDetailsLayoutBinding.bind(view)
        binding.apply {
            taskEditText.setText(viewModel.taskName)
            checkboxImportant.isChecked = viewModel.taskImportance
            dateCreatedTextView.isVisible = viewModel.task != null
            dateCreatedTextView.text = "Created: ${viewModel.task?.createdDateFormatted}"

            taskEditText.addTextChangedListener {
                viewModel.taskName = it.toString()
            }
            checkboxImportant.setOnCheckedChangeListener { _, isChecked ->
                viewModel.taskImportance = isChecked
            }
            fabSaveTask.setOnClickListener {
                viewModel.saveNewTask()
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.addTaskEvent.collect{ event ->
                    when(event){
                        is AddOrEditTaskViewModel.AddTaskEvent.ShowInvalidSnackbar -> {
                            Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                        }

                        is AddOrEditTaskViewModel.AddTaskEvent.ShowTaskCreatedOrEditedSnackbar -> {
                            binding.taskEditText.clearFocus()
                            setFragmentResult("add_new_request", bundleOf("result_code" to event.code))
                            findNavController().popBackStack()

                        }
                    }
                }
            }
        }


    }
}