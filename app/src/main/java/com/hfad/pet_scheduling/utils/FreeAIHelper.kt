package com.hfad.pet_scheduling.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Free AI Helper using Hugging Face Inference API
 * This provides free AI features without requiring an API key
 * Uses open-source models available on Hugging Face
 */
class FreeAIHelper {
    
    // Using Hugging Face Inference API - completely free, no API key needed
    // Using smaller, faster models that are more likely to be available
    private val apiUrl = "https://api-inference.huggingface.co/models/google/flan-t5-large"
    
    // Fallback models if primary fails (smaller models load faster)
    private val fallbackModels = listOf(
        "https://api-inference.huggingface.co/models/microsoft/DialoGPT-medium",
        "https://api-inference.huggingface.co/models/gpt2"
    )

    /**
     * Generate a suggested schedule for a pet based on pet information
     * Returns JSON string with task suggestions
     * Falls back to rule-based generation if API fails
     */
    suspend fun generatePetSchedule(
        petName: String,
        petType: String,
        petBreed: String? = null,
        petAge: String? = null
    ): String? = withContext(Dispatchers.IO) {
        try {
            // First try API-based generation
            val prompt = buildString {
                append("Generate a JSON array for pet care schedule. Pet: $petName, Type: $petType")
                if (petBreed != null) append(", Breed: $petBreed")
                if (petAge != null) append(", Age: $petAge")
                append(". Return JSON array with tasks. Each task: {title, description, category, suggestedTime (HH:mm), recurrencePattern}. ")
                append("Categories: ${Constants.TaskCategory.ALL_CATEGORIES.joinToString(", ")}. ")
                append("Include 3-5 tasks. Return ONLY JSON array.")
            }

            android.util.Log.d("FreeAIHelper", "Sending prompt: $prompt")
            
            val response = callHuggingFaceAPI(apiUrl, prompt)
            if (response != null && response.isNotEmpty()) {
                // Try to extract JSON from response
                val jsonResponse = extractJSONFromResponse(response)
                if (jsonResponse != null) {
                    android.util.Log.d("FreeAIHelper", "Received valid JSON response")
                    return@withContext jsonResponse
                }
            }
            
            // Try fallback models
            for (fallbackUrl in fallbackModels) {
                android.util.Log.d("FreeAIHelper", "Trying fallback model: $fallbackUrl")
                val fallbackResponse = callHuggingFaceAPI(fallbackUrl, prompt)
                if (fallbackResponse != null && fallbackResponse.isNotEmpty()) {
                    val jsonResponse = extractJSONFromResponse(fallbackResponse)
                    if (jsonResponse != null) {
                        android.util.Log.d("FreeAIHelper", "Success with fallback: $fallbackUrl")
                        return@withContext jsonResponse
                    }
                }
            }
            
            // Fallback to rule-based generation if API fails
            android.util.Log.w("FreeAIHelper", "API failed, using rule-based generation")
            return@withContext generateRuleBasedSchedule(petName, petType, petBreed, petAge)
        } catch (e: Exception) {
            android.util.Log.e("FreeAIHelper", "Error generating schedule", e)
            // Fallback to rule-based generation
            return@withContext generateRuleBasedSchedule(petName, petType, petBreed, petAge)
        }
    }
    
    /**
     * Extract JSON from AI response (handles various formats)
     */
    private fun extractJSONFromResponse(response: String): String? {
        try {
            // Try to find JSON array in the response
            val jsonStart = response.indexOf('[')
            val jsonEnd = response.lastIndexOf(']')
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val jsonStr = response.substring(jsonStart, jsonEnd + 1)
                // Validate it's valid JSON
                JSONArray(jsonStr)
                return jsonStr
            }
            
            // Try parsing as-is
            JSONArray(response)
            return response
        } catch (e: Exception) {
            android.util.Log.w("FreeAIHelper", "Could not extract JSON from response: $response")
            return null
        }
    }
    
    /**
     * Rule-based schedule generation (completely free, no API needed)
     */
    private fun generateRuleBasedSchedule(
        petName: String,
        petType: String,
        petBreed: String?,
        petAge: String?
    ): String {
        val tasks = mutableListOf<JSONObject>()
        val typeLower = petType.lowercase()
        
        // Base feeding schedule
        tasks.add(JSONObject().apply {
            put("title", "Morning Feeding")
            put("description", "Feed $petName breakfast")
            put("category", Constants.TaskCategory.FEEDING)
            put("suggestedTime", "08:00")
            put("recurrencePattern", Constants.RecurrencePattern.DAILY)
        })
        
        tasks.add(JSONObject().apply {
            put("title", "Evening Feeding")
            put("description", "Feed $petName dinner")
            put("category", Constants.TaskCategory.FEEDING)
            put("suggestedTime", "18:00")
            put("recurrencePattern", Constants.RecurrencePattern.DAILY)
        })
        
        // Type-specific tasks
        when {
            typeLower.contains("dog") -> {
                tasks.add(JSONObject().apply {
                    put("title", "Walk & Exercise")
                    put("description", "Take $petName for a walk and exercise")
                    put("category", Constants.TaskCategory.EXERCISE)
                    put("suggestedTime", "09:00")
                    put("recurrencePattern", Constants.RecurrencePattern.DAILY)
                })
                tasks.add(JSONObject().apply {
                    put("title", "Grooming")
                    put("description", "Brush $petName's coat")
                    put("category", Constants.TaskCategory.GROOMING)
                    put("suggestedTime", "19:00")
                    put("recurrencePattern", Constants.RecurrencePattern.WEEKLY)
                })
            }
            typeLower.contains("cat") -> {
                tasks.add(JSONObject().apply {
                    put("title", "Play Time")
                    put("description", "Interactive play session with $petName")
                    put("category", Constants.TaskCategory.EXERCISE)
                    put("suggestedTime", "10:00")
                    put("recurrencePattern", Constants.RecurrencePattern.DAILY)
                })
                tasks.add(JSONObject().apply {
                    put("title", "Litter Box Check")
                    put("description", "Clean and check $petName's litter box")
                    put("category", Constants.TaskCategory.GROOMING)
                    put("suggestedTime", "20:00")
                    put("recurrencePattern", Constants.RecurrencePattern.DAILY)
                })
            }
            typeLower.contains("bird") -> {
                tasks.add(JSONObject().apply {
                    put("title", "Cage Cleaning")
                    put("description", "Clean $petName's cage")
                    put("category", Constants.TaskCategory.GROOMING)
                    put("suggestedTime", "09:00")
                    put("recurrencePattern", Constants.RecurrencePattern.DAILY)
                })
            }
            typeLower.contains("fish") -> {
                tasks.add(JSONObject().apply {
                    put("title", "Tank Maintenance")
                    put("description", "Check water quality and clean tank")
                    put("category", Constants.TaskCategory.GROOMING)
                    put("suggestedTime", "10:00")
                    put("recurrencePattern", Constants.RecurrencePattern.WEEKLY)
                })
            }
            else -> {
                tasks.add(JSONObject().apply {
                    put("title", "Health Check")
                    put("description", "Daily health check for $petName")
                    put("category", Constants.TaskCategory.OTHER)
                    put("suggestedTime", "12:00")
                    put("recurrencePattern", Constants.RecurrencePattern.DAILY)
                })
            }
        }
        
        return JSONArray(tasks).toString()
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
                append(". Include information about diet, exercise, grooming, and health monitoring. ")
                append("Keep it concise and practical, around 200 words.")
            }
            
            callHuggingFaceAPI(apiUrl, prompt)
        } catch (e: Exception) {
            android.util.Log.e("FreeAIHelper", "Error getting pet care tips", e)
            e.printStackTrace()
            null
        }
    }

    /**
     * Generate task completion notes
     */
    suspend fun generateTaskNotes(
        taskTitle: String,
        petName: String,
        completedAt: Long
    ): String? = withContext(Dispatchers.IO) {
        try {
            val prompt = "Generate a brief, friendly note for completing task '$taskTitle' for pet '$petName'. Keep it under 50 words."
            callHuggingFaceAPI(apiUrl, prompt)
        } catch (e: Exception) {
            android.util.Log.e("FreeAIHelper", "Error generating task notes", e)
            e.printStackTrace()
            null
        }
    }

    /**
     * Call Hugging Face Inference API
     */
    private suspend fun callHuggingFaceAPI(apiUrl: String, prompt: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL(apiUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("User-Agent", "PetSchedulingApp/1.0")
            connection.doOutput = true
            connection.connectTimeout = 30000 // 30 seconds
            connection.readTimeout = 60000 // 60 seconds
            
            // Build request body for Hugging Face API
            val requestBody = JSONObject().apply {
                put("inputs", prompt)
                put("parameters", JSONObject().apply {
                    put("max_new_tokens", 500)
                    put("temperature", 0.7)
                    put("return_full_text", false)
                })
            }
            
            android.util.Log.d("FreeAIHelper", "Calling API: $apiUrl")
            android.util.Log.d("FreeAIHelper", "Request body: $requestBody")
            
            connection.outputStream.use { output ->
                output.write(requestBody.toString().toByteArray())
            }
            
            val responseCode = connection.responseCode
            android.util.Log.d("FreeAIHelper", "Response code: $responseCode")
            
            when {
                responseCode == HttpURLConnection.HTTP_OK -> {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    android.util.Log.d("FreeAIHelper", "API response: $response")
                    
                    // Parse Hugging Face response format
                    // Hugging Face typically returns an array: [{"generated_text": "..."}]
                    try {
                        val jsonArray = org.json.JSONArray(response)
                        if (jsonArray.length() > 0) {
                            val firstItem = jsonArray.getJSONObject(0)
                            if (firstItem.has("generated_text")) {
                                return@withContext firstItem.getString("generated_text").trim()
                            }
                        }
                    } catch (e: Exception) {
                        // If not an array, try as object
                        try {
                            val jsonObject = JSONObject(response)
                            if (jsonObject.has("generated_text")) {
                                return@withContext jsonObject.getString("generated_text").trim()
                            }
                        } catch (e2: Exception) {
                            android.util.Log.w("FreeAIHelper", "Could not parse response as JSON")
                        }
                    }
                }
                responseCode == 503 -> {
                    // Model is loading, wait and retry
                    android.util.Log.w("FreeAIHelper", "Model is loading, waiting...")
                    kotlinx.coroutines.delay(5000) // Wait 5 seconds
                    return@withContext callHuggingFaceAPI(apiUrl, prompt) // Retry once
                }
                else -> {
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    android.util.Log.e("FreeAIHelper", "API error: $responseCode - $errorResponse")
                }
            }
            
            null
        } catch (e: Exception) {
            android.util.Log.e("FreeAIHelper", "Error calling Hugging Face API", e)
            null
        }
    }
}

