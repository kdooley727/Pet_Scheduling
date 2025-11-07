package com.hfad.pet_scheduling.ui.schedules

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hfad.pet_scheduling.data.local.entities.ScheduleTask
import com.hfad.pet_scheduling.databinding.ItemTaskBinding
import com.hfad.pet_scheduling.utils.Constants
import com.hfad.pet_scheduling.utils.DateTimeUtils

class TaskAdapter(
    private val onTaskClick: (ScheduleTask) -> Unit,
    private val onMarkComplete: (ScheduleTask) -> Unit
) : ListAdapter<ScheduleTask, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: ScheduleTask) {
            binding.apply {
                tvTaskTitle.text = task.title
                tvTaskCategory.text = Constants.TaskCategory.getDisplayName(task.category)
                
                // Format time and recurrence
                val timeText = buildString {
                    append(DateTimeUtils.formatTime(task.startTime))
                    if (task.recurrencePattern != Constants.RecurrencePattern.NONE) {
                        append(" • ")
                        append(Constants.RecurrencePattern.getDisplayName(task.recurrencePattern))
                    }
                }
                tvTaskTime.text = timeText

                // Show description if available
                task.description?.let {
                    tvTaskDescription.text = it
                    tvTaskDescription.visibility = ViewGroup.VISIBLE
                } ?: run {
                    tvTaskDescription.visibility = ViewGroup.GONE
                }

                // Set status icon (active/inactive)
                if (task.isActive) {
                    ivTaskStatus.setImageResource(android.R.drawable.ic_menu_recent_history)
                    ivTaskStatus.alpha = 1.0f
                } else {
                    ivTaskStatus.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                    ivTaskStatus.alpha = 0.5f
                }

                root.setOnClickListener {
                    onTaskClick(task)
                }

                binding.btnMarkComplete.setOnClickListener {
                    onMarkComplete(task)
                }
            }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<ScheduleTask>() {
        override fun areItemsTheSame(oldItem: ScheduleTask, newItem: ScheduleTask): Boolean {
            return oldItem.taskId == newItem.taskId
        }

        override fun areContentsTheSame(oldItem: ScheduleTask, newItem: ScheduleTask): Boolean {
            return oldItem == newItem
        }
    }
}

