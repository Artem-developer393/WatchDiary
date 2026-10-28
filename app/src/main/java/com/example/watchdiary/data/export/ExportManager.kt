package com.example.watchdiary.data.export

import android.content.Context
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.watchdiary.data.repository.DiaryRepository
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportManager(
    private val context: Context,
    private val repository: DiaryRepository
) {

    suspend fun exportToUri(uri: Uri) = withContext(Dispatchers.IO) {
        val file = exportToJson()
        context.contentResolver.openOutputStream(uri)?.use { output ->
            file.inputStream().use { it.copyTo(output) }
        } ?: error("Cannot open selected file for writing")
    }

    /** Android 10+ scoped-storage Downloads: no storage permission required. */
    suspend fun saveToDownloads(): String = withContext(Dispatchers.IO) {
        require(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        val filename = "watch_diary_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.json"
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, filename)
            put(MediaStore.Downloads.MIME_TYPE, "application/json")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: error("Cannot create file in Downloads")
        try {
            exportToUri(uri)
            resolver.update(uri, ContentValues().apply {
                put(MediaStore.Downloads.IS_PENDING, 0)
            }, null, null)
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            throw e
        }
        filename
    }

    suspend fun exportToJson(): File = withContext(Dispatchers.IO) {

        val entries = repository.getAllEntriesOnce()

        val exportList = entries.map {
            ExportDiaryEntry(
                id = it.id,
                title = it.title,
                contentType = it.contentType.name,
                watchDate = it.watchDate.toString(),
                rating = it.rating,
                comment = it.comment
            )
        }

        val json = GsonBuilder().setPrettyPrinting().create().toJson(exportList)

        val file = File(
            context.cacheDir,
            "watch_diary_export.json"
        )

        file.writeText(json)

        file
    }
}
