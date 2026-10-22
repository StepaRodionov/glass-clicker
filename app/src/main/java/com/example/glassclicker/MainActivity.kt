package com.example.glassclicker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val fieldX = findViewById<EditText>(R.id.et_field_x)
        val fieldY = findViewById<EditText>(R.id.et_field_y)
        val text = findViewById<EditText>(R.id.et_text)
        val btnX = findViewById<EditText>(R.id.et_btn_x)
        val btnY = findViewById<EditText>(R.id.et_btn_y)
        val interval = findViewById<EditText>(R.id.et_interval)

        fun show(v: EditText, key: String) {
            val n = prefs.getInt(key, -1)
            if (n >= 0) v.setText(n.toString())
        }
        show(fieldX, KEY_FIELD_X); show(fieldY, KEY_FIELD_Y)
        show(btnX, KEY_BTN_X); show(btnY, KEY_BTN_Y)
        interval.setText(prefs.getInt(KEY_INTERVAL, 1000).toString())
        text.setText(prefs.getString(KEY_TEXT, ""))

        findViewById<Button>(R.id.btn_save).setOnClickListener {
            prefs.edit()
                .putInt(KEY_FIELD_X, fieldX.text.toString().toIntOrNull() ?: -1)
                .putInt(KEY_FIELD_Y, fieldY.text.toString().toIntOrNull() ?: -1)
                .putInt(KEY_BTN_X, btnX.text.toString().toIntOrNull() ?: -1)
                .putInt(KEY_BTN_Y, btnY.text.toString().toIntOrNull() ?: -1)
                .putInt(KEY_INTERVAL, (interval.text.toString().toIntOrNull() ?: 1000).coerceAtLeast(1000))
                .putString(KEY_TEXT, text.text.toString())
                .apply()
            Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_access).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    companion object {
        const val PREFS = "glass_clicker"
        const val KEY_FIELD_X = "field_x"
        const val KEY_FIELD_Y = "field_y"
        const val KEY_BTN_X = "btn_x"
        const val KEY_BTN_Y = "btn_y"
        const val KEY_INTERVAL = "interval"
        const val KEY_TEXT = "text"
    }
}
