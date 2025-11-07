package com.hfad.pet_scheduling.ui.schedules

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hfad.pet_scheduling.data.local.entities.CompletedTask
import com.hfad.pet_scheduling.databinding.ItemCompletedTaskBinding
import com.hfad.pet_scheduling.utils.DateTimeUtils

class CompletedTaskAdapter : ListAdapter<CompletedTask, CompletedTaskAdapter.CompletedTaskViewHolder>(
    CompletedTaskDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompletedTaskViewHolder {
        val binding = ItemCompletedTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CompletedTaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CompletedTaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CompletedTaskViewHolder(
        private val binding: ItemCompletedTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(completedTask: CompletedTask) {
            binding.apply {
                tvCompletedDate.text = DateTimeUtils.formatDateTime(completedTask.completedAt)
                
                completedTask.notes?.let {
                    tvCompletedNotes.text = it
                    tvCompletedNotes.visibility = ViewGroup.VISIBLE
                } ?: run {
                    tvCompletedNotes.visibility = ViewGroup.GONE
                }
            }
        }
    }

    class CompletedTaskDiffCallback : DiffUtil.ItemCallback<CompletedTask>() {
        override fun areItemsTheSame(oldItem: CompletedTask, newItem: CompletedTask): Boolean {
            return oldItem.completedTaskId == newItem.completedTaskId
        }

        override fun areContentsTheSame(oldItem: CompletedTask, newItem: CompletedTask): Boolean {
            return oldItem == newItem
        }
    }
}

