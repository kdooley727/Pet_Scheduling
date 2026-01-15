package com.hfad.pet_scheduling.ui.schedules

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hfad.pet_scheduling.data.entities.ScheduleTask
import com.hfad.pet_scheduling.databinding.ItemTaskBinding
import com.hfad.pet_scheduling.utils.Constants
import com.hfad.pet_scheduling.utils.DateTimeUtils

class TaskAdapter(
    private val onTaskClick: (ScheduleTask) -> Unit,
    private val onMarkComplete: (ScheduleTask) -> Unit
) : ListAdapter<ScheduleTask, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    private var completedTaskIds: Set<String> = emptySet()

    fun setCompletedTaskIds(taskIds: Set<String>) {
        completedTaskIds = taskIds
        notifyDataSetChanged()
    }

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
                val isCompleted = completedTaskIds.contains(task.taskId)
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

                root.setOnClickListener {
                    onTaskClick(task)
                }

                btnMarkComplete.text = if (isCompleted) "✓" else ""
                btnMarkComplete.isEnabled = !isCompleted
                btnMarkComplete.alpha = if (isCompleted) 1.0f else 0.6f
                btnMarkComplete.contentDescription = if (isCompleted) {
                    "Task completed"
                } else {
                    "Mark task complete"
                }

                btnMarkComplete.setOnClickListener {
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

