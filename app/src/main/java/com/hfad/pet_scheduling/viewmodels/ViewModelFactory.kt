package com.hfad.pet_scheduling.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hfad.pet_scheduling.PetSchedulingApplication
import com.hfad.pet_scheduling.data.repository.PetRepository
import com.hfad.pet_scheduling.data.repository.ScheduleRepository

class ViewModelFactory(
    private val petRepository: PetRepository,
    private val scheduleRepository: ScheduleRepository,
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val cloudSyncManager = (application as? PetSchedulingApplication)?.cloudSyncManager
        return when {
            modelClass.isAssignableFrom(PetViewModel::class.java) -> {
                PetViewModel(petRepository, cloudSyncManager, scheduleRepository) as T
            }
            modelClass.isAssignableFrom(ScheduleViewModel::class.java) -> {
                ScheduleViewModel(scheduleRepository, petRepository, application) as T
            }
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel() as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

