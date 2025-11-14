package com.hfad.pet_scheduling.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.hfad.pet_scheduling.data.local.entities.CompletedTask
import com.hfad.pet_scheduling.data.local.entities.Pet
import com.hfad.pet_scheduling.data.local.entities.ScheduleTask
import com.hfad.pet_scheduling.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper class for exporting pet schedules and details
 */
class ExportHelper(private val context: Context) {

    /**
     * Export pet data to HTML (can be printed to PDF)
     */
    suspend fun exportToHTML(
        pet: Pet,
        allTasks: List<ScheduleTask>,
        completedTasks: List<CompletedTask>
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            // Filter active tasks for the schedule section
            val activeTasks = allTasks.filter { it.isActive }
            val htmlContent = generateHTMLReport(pet, activeTasks, allTasks, completedTasks)
            return@withContext saveHTMLFile(htmlContent, pet.name)
        } catch (e: Exception) {
            android.util.Log.e("ExportHelper", "Error creating HTML", e)
            return@withContext null
        }
    }

    /**
     * Export pet data to CSV
     */
    suspend fun exportToCSV(
        pet: Pet,
        allTasks: List<ScheduleTask>,
        completedTasks: List<CompletedTask>
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            // Filter active tasks for the schedule section
            val activeTasks = allTasks.filter { it.isActive }
            val csvContent = generateCSVReport(pet, activeTasks, allTasks, completedTasks)
            return@withContext saveCSVFile(csvContent, pet.name)
        } catch (e: Exception) {
            android.util.Log.e("ExportHelper", "Error creating CSV", e)
            return@withContext null
        }
    }

    /**
     * Generate HTML report content
     */
    private fun generateHTMLReport(
        pet: Pet,
        activeTasks: List<ScheduleTask>,
        allTasks: List<ScheduleTask>,
        completedTasks: List<CompletedTask>
    ): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        
        val petAge = pet.birthDate?.let {
            val ageInDays = (System.currentTimeMillis() - it) / (1000 * 60 * 60 * 24)
            when {
                ageInDays < 30 -> "$ageInDays days"
                ageInDays < 365 -> "${ageInDays / 30} months"
                else -> "${ageInDays / 365} years"
            }
        } ?: "Unknown"

        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        margin: 20px;
                        color: #333;
                        line-height: 1.6;
                    }
                    h1 {
                        color: #6200EE;
                        border-bottom: 3px solid #6200EE;
                        padding-bottom: 10px;
                    }
                    h2 {
                        color: #3700B3;
                        margin-top: 30px;
                        border-bottom: 2px solid #E0E0E0;
                        padding-bottom: 5px;
                    }
                    .pet-info {
                        background-color: #F5F5F5;
                        padding: 15px;
                        border-radius: 5px;
                        margin: 20px 0;
                    }
                    .info-row {
                        margin: 8px 0;
                    }
                    .info-label {
                        font-weight: bold;
                        display: inline-block;
                        width: 150px;
                    }
                    table {
                        width: 100%;
                        border-collapse: collapse;
                        margin: 20px 0;
                    }
                    th, td {
                        border: 1px solid #DDD;
                        padding: 12px;
                        text-align: left;
                    }
                    th {
                        background-color: #6200EE;
                        color: white;
                        font-weight: bold;
                    }
                    tr:nth-child(even) {
                        background-color: #F9F9F9;
                    }
                    .category-badge {
                        display: inline-block;
                        padding: 4px 8px;
                        border-radius: 4px;
                        font-size: 12px;
                        font-weight: bold;
                        background-color: #E0E0E0;
                    }
                    .footer {
                        margin-top: 40px;
                        padding-top: 20px;
                        border-top: 2px solid #E0E0E0;
                        text-align: center;
                        color: #666;
                        font-size: 12px;
                    }
                </style>
            </head>
            <body>
                <h1>Pet Care Report: ${escapeHtml(pet.name)}</h1>
                
                <div class="pet-info">
                    <h2>Pet Information</h2>
                    <div class="info-row">
                        <span class="info-label">Name:</span>
                        <span>${escapeHtml(pet.name)}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">Type:</span>
                        <span>${escapeHtml(Constants.PetType.getDisplayName(pet.type))}</span>
                    </div>
                    ${pet.breed?.let { """
                    <div class="info-row">
                        <span class="info-label">Breed:</span>
                        <span>${escapeHtml(it)}</span>
                    </div>
                    """ } ?: ""}
                    ${pet.birthDate?.let { """
                    <div class="info-row">
                        <span class="info-label">Birth Date:</span>
                        <span>${dateFormat.format(Date(it))}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">Age:</span>
                        <span>$petAge</span>
                    </div>
                    """ } ?: ""}
                    ${pet.notes?.let { """
                    <div class="info-row">
                        <span class="info-label">Notes:</span>
                        <span>${escapeHtml(it)}</span>
                    </div>
                    """ } ?: ""}
                </div>

                <h2>Active Schedule</h2>
                ${if (activeTasks.isEmpty()) {
                    "<p>No active tasks scheduled.</p>"
                } else {
                    """
                    <table>
                        <thead>
                            <tr>
                                <th>Task</th>
                                <th>Category</th>
                                <th>Time</th>
                                <th>Repeat</th>
                                <th>Reminder</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${activeTasks.joinToString("") { task ->
                                """
                                <tr>
                                    <td><strong>${escapeHtml(task.title)}</strong><br>
                                        ${task.description?.let { "<small>${escapeHtml(it)}</small>" } ?: ""}
                                    </td>
                                    <td><span class="category-badge">${escapeHtml(Constants.TaskCategory.getDisplayName(task.category))}</span></td>
                                    <td>${timeFormat.format(Date(task.startTime))}</td>
                                    <td>${escapeHtml(Constants.RecurrencePattern.getDisplayName(task.recurrencePattern))}</td>
                                    <td>${task.reminderMinutesBefore} minutes before</td>
                                </tr>
                                """
                            }}
                        </tbody>
                    </table>
                    """
                }}

                <h2>Task Completion History</h2>
                ${if (completedTasks.isEmpty()) {
                    "<p>No completed tasks recorded.</p>"
                } else {
                    """
                    <table>
                        <thead>
                            <tr>
                                <th>Task</th>
                                <th>Completed Date</th>
                                <th>Scheduled Time</th>
                                <th>Notes</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${completedTasks.sortedByDescending { it.completedAt }.joinToString("") { completed ->
                                val task = allTasks.find { it.taskId == completed.taskId }
                                val taskTitle = task?.title ?: "Task (ID: ${completed.taskId.take(8)}...)"
                                """
                                <tr>
                                    <td><strong>${escapeHtml(taskTitle)}</strong></td>
                                    <td>${dateTimeFormat.format(Date(completed.completedAt))}</td>
                                    <td>${completed.scheduledTime?.let { dateTimeFormat.format(Date(it)) } ?: "N/A"}</td>
                                    <td>${completed.notes?.let { escapeHtml(it) } ?: "-"}</td>
                                </tr>
                                """
                            }}
                        </tbody>
                    </table>
                    """
                }}

                <div class="footer">
                    <p>Generated on ${dateTimeFormat.format(Date())}</p>
                    <p>Pet Scheduling App - Pet Care Management System</p>
                </div>
            </body>
            </html>
        """.trimIndent()

        return html
    }

    /**
     * Generate CSV report content
     */
    private fun generateCSVReport(
        pet: Pet,
        activeTasks: List<ScheduleTask>,
        allTasks: List<ScheduleTask>,
        completedTasks: List<CompletedTask>
    ): String {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateTimeFormat = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault())

        val csv = StringBuilder()

        // Pet Information Section
        csv.appendLine("PET INFORMATION")
        csv.appendLine("Name,${pet.name}")
        csv.appendLine("Type,${Constants.PetType.getDisplayName(pet.type)}")
        pet.breed?.let { csv.appendLine("Breed,$it") }
        pet.birthDate?.let { csv.appendLine("Birth Date,${dateFormat.format(Date(it))}") }
        pet.notes?.let { csv.appendLine("Notes,\"$it\"") }
        csv.appendLine()

        // Active Tasks Section
        csv.appendLine("ACTIVE SCHEDULE")
        csv.appendLine("Task Title,Category,Time,Repeat,Reminder (minutes),Description")
        activeTasks.forEach { task ->
            csv.appendLine(
                "\"${task.title}\"," +
                "${Constants.TaskCategory.getDisplayName(task.category)}," +
                "${timeFormat.format(Date(task.startTime))}," +
                "${Constants.RecurrencePattern.getDisplayName(task.recurrencePattern)}," +
                "${task.reminderMinutesBefore}," +
                "\"${task.description ?: ""}\""
            )
        }
        csv.appendLine()

        // Completed Tasks Section
        csv.appendLine("COMPLETION HISTORY")
        csv.appendLine("Task Title,Completed Date,Scheduled Time,Notes")
        completedTasks.sortedByDescending { it.completedAt }.forEach { completed ->
            val task = allTasks.find { it.taskId == completed.taskId }
            val taskTitle = task?.title ?: "Task (ID: ${completed.taskId.take(8)}...)"
            csv.appendLine(
                "\"$taskTitle\"," +
                "${dateTimeFormat.format(Date(completed.completedAt))}," +
                "${completed.scheduledTime?.let { dateTimeFormat.format(Date(it)) } ?: "N/A"}," +
                "\"${completed.notes ?: ""}\""
            )
        }
        csv.appendLine()

        csv.appendLine("Generated,${dateTimeFormat.format(Date())}")

        return csv.toString()
    }

    /**
     * Save HTML file and return URI
     */
    private fun saveHTMLFile(htmlContent: String, petName: String): Uri? {
        return try {
            val fileName = "Pet_Schedule_${petName}_${System.currentTimeMillis()}.html"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            FileWriter(file).use { writer ->
                writer.write(htmlContent)
            }

            // Use FileProvider to share the file
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } else {
                Uri.fromFile(file)
            }

            uri
        } catch (e: Exception) {
            android.util.Log.e("ExportHelper", "Error saving HTML", e)
            null
        }
    }

    /**
     * Save CSV file and return URI
     */
    private fun saveCSVFile(csvContent: String, petName: String): Uri? {
        return try {
            val fileName = "Pet_Schedule_${petName}_${System.currentTimeMillis()}.csv"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            FileWriter(file).use { writer ->
                writer.write(csvContent)
            }

            // Use FileProvider to share the file
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } else {
                Uri.fromFile(file)
            }

            uri
        } catch (e: Exception) {
            android.util.Log.e("ExportHelper", "Error saving CSV", e)
            null
        }
    }

    /**
     * Share file via Android share intent
     */
    fun shareFile(uri: Uri, fileName: String, mimeType: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, fileName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Pet Schedule"))
    }

    /**
     * Escape HTML special characters
     */
    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}

