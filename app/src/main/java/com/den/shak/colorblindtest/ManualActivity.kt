package com.den.shak.colorblindtest

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ManualActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual)

        // Показать кнопку "Назад" в ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Установка отступов для главного View с учетом системных панелей
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.manual)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Завершает текущее активити и возвращает назад
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}