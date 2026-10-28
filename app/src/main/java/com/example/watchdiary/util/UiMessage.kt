package com.example.watchdiary.util

import android.content.Context
import androidx.annotation.StringRes

/** A localized message resolved only where a Context is available (the UI). */
class UiMessage(@StringRes val resId: Int, vararg val args: Any) {
    fun resolve(context: Context): String = context.getString(resId, *args)
}
