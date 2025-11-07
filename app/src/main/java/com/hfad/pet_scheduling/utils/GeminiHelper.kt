package com.hfad.pet_scheduling.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class QuotaExceededException(message: String) : Exception(message)

class GeminiHelper(private val apiKey: String) {
    // First, try to list available models, then use the first available one
    private suspend fun getAvailableModel(): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                android.util.Log.d("GeminiHelper", "Models list response: $response")
                val jsonResponse = org.json.JSONObject(response)
                val models = jsonResponse.getJSONArray("models")
                
                android.util.Log.d("GeminiHelper", "Found ${models.length()} models")
                
                // Look for a model that supports generateContent
                for (i in 0 until models.length()) {
                    val model = models.getJSONObject(i)
                    val name = model.getString("name")
                    val supportedMethods = model.optJSONArray("supportedGenerationMethods")
                    
                    android.util.Log.d("GeminiHelper", "Model: $name, Methods: $supportedMethods")
                    
                    if (supportedMethods != null) {
                        for (j in 0 until supportedMethods.length()) {
                            if (supportedMethods.getString(j) == "generateContent") {
                                android.util.Log.d("GeminiHelper", "Found available model: $name")
                                return@withContext name
                            }
                        }
                    }
                }
            } else {
                val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                android.util.Log.e("GeminiHelper", "Error listing models: $responseCode - $errorResponse")
            }
            null
        } catch (e: Exception) {
            android.util.Log.e("GeminiHelper", "Error listing models", e)
            null
        }
    }
    
    // Try different model names - AI Studio keys might use different endpoints
    private val apiEndpoints = listOf(
        // Try with full model path format
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent",
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent",
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.0-pro:generateContent",
        "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent",
        // Try without models/ prefix (some APIs use different format)
        "https://generativelanguage.googleapis.com/v1beta/gemini-1.5-flash:generateContent",
        "https://generativelanguage.googleapis.com/v1beta/gemini-1.5-pro:generateContent"
    )

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
        // First, try to get an available model
        val availableModel = getAvailableModel()
        if (availableModel != null) {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/$availableModel:generateContent"
            android.util.Log.d("GeminiHelper", "Using discovered model: $endpoint")
            val result = tryCallAPI(endpoint, prompt)
            if (result != null) {
                return@withContext result
            }
        }
        
        // Fallback: Try each endpoint until one works
        for (endpoint in apiEndpoints) {
            android.util.Log.d("GeminiHelper", "Trying endpoint: $endpoint")
            val result = tryCallAPI(endpoint, prompt)
            if (result != null) {
                android.util.Log.d("GeminiHelper", "Success with endpoint: $endpoint")
                return@withContext result
            }
        }
        android.util.Log.e("GeminiHelper", "All API endpoints failed")
        null
    }
    
    private suspend fun tryCallAPI(apiUrl: String, prompt: String): String? = withContext(Dispatchers.IO) {
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
            
            android.util.Log.d("GeminiHelper", "Calling API: $apiUrl")
            android.util.Log.d("GeminiHelper", "Request body: $requestBody")
            
            connection.outputStream.use { output ->
                output.write(requestBody.toString().toByteArray())
            }
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                android.util.Log.d("GeminiHelper", "API response: $response")
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
                android.util.Log.e("GeminiHelper", "API error ($apiUrl): $responseCode - $errorResponse")
                
                // Handle quota errors (429) - these indicate the API key works but quota is exceeded
                if (responseCode == 429) {
                    android.util.Log.w("GeminiHelper", "Quota exceeded for endpoint: $apiUrl")
                    // This means the model exists and API key works, but free tier quota is 0
                    // Return a special indicator so we can show a helpful message
                    throw QuotaExceededException("Free tier quota exceeded. Please wait or set up billing.")
                }
            }
            null
        } catch (e: Exception) {
            android.util.Log.e("GeminiHelper", "Error calling Gemini API ($apiUrl)", e)
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

