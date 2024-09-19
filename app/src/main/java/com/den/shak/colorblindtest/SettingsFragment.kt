package com.den.shak.colorblindtest

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Подключаем файл preferences.xml, который содержит настройки
        setPreferencesFromResource(R.xml.preferences, rootKey)

        // Обновляем сводку для ListPreference
        updatePreferenceSummaries()

        // Устанавливаем слушатель изменений для ListPreference
        val listPreference = findPreference<ListPreference>("list_preference")
        listPreference?.setOnPreferenceChangeListener { preference, newValue ->
            // Получаем записи и находим индекс нового значения
            val entries = (preference as ListPreference).entries
            val entryIndex = preference.findIndexOfValue(newValue.toString())

            // Если индекс действителен, обновляем сводку
            if (entryIndex >= 0) {
                preference.summary = entries[entryIndex]
            }

            // Перезапускаем активити, чтобы применить изменения (например, тему)
            activity?.recreate() // Перезапуск активити с новой конфигурацией
            true
        }
    }

    private fun updatePreferenceSummaries() {
        // Обновляем сводку для текущего значения ListPreference
        val listPreference = findPreference<ListPreference>("list_preference")
        listPreference?.let {
            // Получаем текущее значение и устанавливаем его как сводку
            val currentValue = it.value
            val entries = it.entries
            val entryIndex = it.findIndexOfValue(currentValue)

            // Если индекс действителен, обновляем сводку
            if (entryIndex >= 0) {
                it.summary = entries[entryIndex]
            }
        }
    }
}
