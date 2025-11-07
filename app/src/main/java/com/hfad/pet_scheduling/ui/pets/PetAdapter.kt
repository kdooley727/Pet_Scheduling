package com.hfad.pet_scheduling.ui.pets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hfad.pet_scheduling.data.local.entities.Pet
import com.hfad.pet_scheduling.databinding.ItemPetBinding
import com.hfad.pet_scheduling.utils.Constants
import com.hfad.pet_scheduling.utils.DateTimeUtils

class PetAdapter(
    private val onPetClick: (Pet) -> Unit,
    private val onPetLongClick: (Pet) -> Unit
) : ListAdapter<Pet, PetAdapter.PetViewHolder>(PetDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val binding = ItemPetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PetViewHolder(
        private val binding: ItemPetBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pet: Pet) {
            binding.apply {
                tvPetName.text = pet.name
                tvPetType.text = Constants.PetType.getDisplayName(pet.type)
                pet.breed?.let {
                    tvPetBreed.text = it
                    tvPetBreed.visibility = ViewGroup.VISIBLE
                } ?: run {
                    tvPetBreed.visibility = ViewGroup.GONE
                }

                pet.birthDate?.let {
                    tvPetAge.text = "Born: ${DateTimeUtils.formatDate(it)}"
                    tvPetAge.visibility = ViewGroup.VISIBLE
                } ?: run {
                    tvPetAge.visibility = ViewGroup.GONE
                }
                
                // Load pet photo if available
                pet.photoUrl?.let { photoUrl ->
                    if (photoUrl.isNotEmpty()) {
                        Glide.with(binding.root.context)
                            .load(photoUrl)
                            .circleCrop()
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .into(ivPetPhoto)
                    } else {
                        ivPetPhoto.setImageResource(android.R.drawable.ic_menu_gallery)
                    }
                } ?: run {
                    ivPetPhoto.setImageResource(android.R.drawable.ic_menu_gallery)
                }

                root.setOnClickListener {
                    onPetClick(pet)
                }
                
                root.setOnLongClickListener {
                    onPetLongClick(pet)
                    true
                }
            }
        }
    }

    class PetDiffCallback : DiffUtil.ItemCallback<Pet>() {
        override fun areItemsTheSame(oldItem: Pet, newItem: Pet): Boolean {
            return oldItem.petId == newItem.petId
        }

        override fun areContentsTheSame(oldItem: Pet, newItem: Pet): Boolean {
            return oldItem == newItem
        }
    }
}

