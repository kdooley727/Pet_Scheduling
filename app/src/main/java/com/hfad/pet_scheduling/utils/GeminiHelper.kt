package com.hfad.pet_scheduling.utils

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiHelper(private val apiKey: String) {
    private val model: GenerativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-pro",
            apiKey = apiKey
        )
    }

    /**
     * Generate a suggested schedule for a pet based on pet information
     */
    suspend fun generatePetSchedule(
        petName: String,
        petType: String,
        petBreed: String? = null,
        petAge: String? = null
    ): String? = withContext(Dispatchers.IO) {
        try {
            val prompt = buildString {
                append("I have a pet named $petName")
                append(" (Type: $petType")
                if (petBreed != null) append(", Breed: $petBreed")
                if (petAge != null) append(", Age: $petAge")
                append("). ")
                append("Please suggest a daily care schedule for this pet. ")
                append("Include feeding times, exercise, grooming, and any other important care tasks. ")
                append("Format the response as a JSON array of tasks with fields: title, description, category, suggestedTime (HH:mm format), and recurrencePattern. ")
                append("Categories should be one of: ${Constants.TaskCategory.ALL_CATEGORIES.joinToString(", ")}")
            }

            val response = model.generateContent(prompt)
            response.text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Get AI-generated suggestions for task completion notes
     */
    suspend fun generateTaskNotes(
        taskTitle: String,
        petName: String,
        completedAt: Long
    ): String? = withContext(Dispatchers.IO) {
        try {
            val prompt = "Generate a brief note for completing task '$taskTitle' for pet '$petName' at ${DateTimeUtils.formatDateTime(completedAt)}. Keep it concise and friendly."
            val response = model.generateContent(prompt)
            response.text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Get pet care tips based on pet information
     */
    suspend fun getPetCareTips(
        petType: String,
        petBreed: String? = null
    ): String? = withContext(Dispatchers.IO) {
        try {
            val prompt = buildString {
                append("Provide helpful care tips for a $petType")
                if (petBreed != null) append(" (specifically $petBreed)")
                append(". Include information about diet, exercise, grooming, and health monitoring.")
            }
            val response = model.generateContent(prompt)
            response.text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

