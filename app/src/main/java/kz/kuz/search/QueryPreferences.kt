package kz.kuz.search

import android.content.Context
import android.preference.PreferenceManager

object QueryPreferences {
    fun getStoredQuery(context: Context?): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString("searchQuery", null)
    }

    fun setStoredQuery(context: Context?, query: String?) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString("searchQuery", query)
                .apply()
    }
}