package com.example.ui.theme

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

object ThemeManager {
    private const val PREFS_NAME = "study_ai_theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"

    private var prefs: SharedPreferences? = null
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun initialize(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedMode = prefs?.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
            _themeMode.value = try {
                ThemeMode.valueOf(savedMode)
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs?.edit()?.putString(KEY_THEME_MODE, mode.name)?.apply()
    }

    fun toggleDarkMode(currentIsDark: Boolean) {
        val nextMode = if (currentIsDark) ThemeMode.LIGHT else ThemeMode.DARK
        setThemeMode(nextMode)
    }
}
