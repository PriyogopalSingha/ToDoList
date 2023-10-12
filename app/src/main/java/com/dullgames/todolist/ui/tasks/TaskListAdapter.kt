package com.dullgames.todolist.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dullgames.todolist.data.Task
import com.dullgames.todolist.databinding.SingleTaskLayoutBinding

class TaskListAdapter(private val listener: OnItemClickListener): ListAdapter<Task, TaskListAdapter.TaskViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = SingleTaskLayoutBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val currentTask = getItem(position)
        holder.bind(currentTask)
    }

    inner class TaskViewHolder(private val binding: SingleTaskLayoutBinding) : RecyclerView.ViewHolder(binding.root){
        init{
            binding.apply {
                root.setOnClickListener{
                    val position = adapterPosition
                    if(position != RecyclerView.NO_POSITION){
                        val task = getItem(position)
                        listener.onItemClick(task)
                    }
                }
                checkBox.setOnClickListener {
                    val position = adapterPosition
                    if(position != RecyclerView.NO_POSITION){
                        val task = getItem(position)
                        listener.onItemChecked(task,checkBox.isChecked)
                    }
                }
            }
        }
        fun bind(task: Task){
            binding.apply {
                checkBox.isChecked = task.completed
                taskNameTextView.text = task.name
                taskNameTextView.paint.isStrikeThruText = task.completed
                importantIcon.isVisible = task.important
            }
        }

    }

    class DiffCallback: DiffUtil.ItemCallback<Task>(){
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }

    }
    interface OnItemClickListener{
        fun onItemClick(task: Task)
        fun onItemChecked(task: Task, onChecked: Boolean)
    }
}

