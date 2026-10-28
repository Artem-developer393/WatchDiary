package com.example.watchdiary.util

import androidx.annotation.StringRes
import com.example.watchdiary.R

/** Stable, localized error identifiers without holding an Android Context. */
enum class AppError(@StringRes val messageRes: Int) {
    LOAD(R.string.could_not_load_entries),
    SAVE(R.string.could_not_save_entry),
    DELETE(R.string.could_not_delete_entry),
    IMPORT(R.string.could_not_import_entries),
    EXPORT(R.string.could_not_export_file),
    FILE_READ(R.string.could_not_open_json_file),
    FILE_WRITE(R.string.could_not_save_file);

    fun message() = UiMessage(messageRes)
}
