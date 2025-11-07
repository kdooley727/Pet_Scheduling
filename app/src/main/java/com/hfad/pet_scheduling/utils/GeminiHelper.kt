package com.hfad.pet_scheduling.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class GeminiHelper(private val apiKey: String) {
    private val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent"

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
                append("Categories should be one of: ${Constants.TaskCategory.ALL_CATEGORIES.joinToString(", ")}. ")
                append("Return ONLY valid JSON, no markdown, no code blocks, just the JSON array.")
            }

            android.util.Log.d("GeminiHelper", "Sending prompt to Gemini: $prompt")
            
            val text = callGeminiAPI(prompt)
            if (text != null && text.isNotEmpty()) {
                android.util.Log.d("GeminiHelper", "Received response from Gemini: $text")
                return@withContext text
            }
            
            android.util.Log.w("GeminiHelper", "Empty response from Gemini")
            null
        } catch (e: Exception) {
            android.util.Log.e("GeminiHelper", "Error generating content", e)
            e.printStackTrace()
            null
        }
    }

    /**
     * Helper method to call Gemini API via REST
     */
    private suspend fun callGeminiAPI(prompt: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$apiUrl?key=$apiKey")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            
            // Build request body according to Gemini API format
            val requestBody = JSONObject().apply {
                put("contents", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", org.json.JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }
            
            connection.outputStream.use { output ->
                output.write(requestBody.toString().toByteArray())
            }
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = org.json.JSONObject(response)
                val candidates = jsonResponse.getJSONArray("candidates")
                if (candidates.length() > 0) {
                    val content = candidates.getJSONObject(0).getJSONObject("content")
                    val parts = content.getJSONArray("parts")
                    if (parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).getString("text")
                    }
                }
            } else {
                val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                android.util.Log.e("GeminiHelper", "API error: $responseCode - $errorResponse")
            }
            null
        } catch (e: Exception) {
            android.util.Log.e("GeminiHelper", "Error calling Gemini API", e)
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
            callGeminiAPI(prompt)
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
            callGeminiAPI(prompt)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

