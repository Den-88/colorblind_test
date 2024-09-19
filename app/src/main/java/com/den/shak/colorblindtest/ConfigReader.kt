package com.den.shak.colorblindtest
import android.content.Context
import android.util.Log
import java.io.IOException
import java.util.Properties

object ConfigReader {
    private const val TAG = "ConfigReader"

    fun getAdUnitId(context: Context): String? {
        val properties = Properties()
        try {
            // Открываем поток для чтения файла config.properties из ресурсов приложения
            val inputStream = context.resources.openRawResource(R.raw.config)
            // Загружаем свойства из файла в объект Properties
            properties.load(inputStream)
            // Возвращаем значение свойства "API_KEY"
            return properties.getProperty("AD-UNIT-ID")
        } catch (e: IOException) {
            // Если произошла ошибка при чтении файла, логируем её
            Log.e(TAG, "Error reading config file", e)
        }
        // Возвращаем null, если чтение не удалось
        return null
    }

    fun getAdRuStoreUnitId(context: Context): String? {
        val properties = Properties()
        try {
            // Открываем поток для чтения файла config.properties из ресурсов приложения
            val inputStream = context.resources.openRawResource(R.raw.config)
            // Загружаем свойства из файла в объект Properties
            properties.load(inputStream)
            // Возвращаем значение свойства "API_KEY"
            return properties.getProperty("AD-UNIT-ID-RUSTORE")
        } catch (e: IOException) {
            // Если произошла ошибка при чтении файла, логируем её
            Log.e(TAG, "Error reading config file", e)
        }
        // Возвращаем null, если чтение не удалось
        return null
    }

    fun getAdInterstitiaId(context: Context): String? {
        val properties = Properties()
        try {
            // Открываем поток для чтения файла config.properties из ресурсов приложения
            val inputStream = context.resources.openRawResource(R.raw.config)
            // Загружаем свойства из файла в объект Properties
            properties.load(inputStream)
            // Возвращаем значение свойства "API_KEY"
            return properties.getProperty("AD-FULLSCREEN-ID")
        } catch (e: IOException) {
            // Если произошла ошибка при чтении файла, логируем её
            Log.e(TAG, "Error reading config file", e)
        }
        // Возвращаем null, если чтение не удалось
        return null
    }

    fun getAdRuStoreInterstitiaId(context: Context): String? {
        val properties = Properties()
        try {
            // Открываем поток для чтения файла config.properties из ресурсов приложения
            val inputStream = context.resources.openRawResource(R.raw.config)
            // Загружаем свойства из файла в объект Properties
            properties.load(inputStream)
            // Возвращаем значение свойства "API_KEY"
            return properties.getProperty("AD-FULLSCREEN-ID-RUSTORE")
        } catch (e: IOException) {
            // Если произошла ошибка при чтении файла, логируем её
            Log.e(TAG, "Error reading config file", e)
        }
        // Возвращаем null, если чтение не удалось
        return null
    }
}