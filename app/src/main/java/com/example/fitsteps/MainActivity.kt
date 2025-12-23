package com.example.fitsteps

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.FitnessOptions
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class MainActivity : AppCompatActivity() {

    private val fitnessOptions: FitnessOptions by lazy {
        FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editDate = findViewById<EditText>(R.id.editDate)
        val editTime = findViewById<EditText>(R.id.editTime)
        val editSteps = findViewById<EditText>(R.id.editSteps)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        val txtStatus = findViewById<TextView>(R.id.txtStatus)

        // 日期選擇
        editDate.setOnClickListener {
            val now = LocalDate.now()
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    val date = LocalDate.of(y, m + 1, d)
                    editDate.setText(date.toString())   // yyyy-MM-dd
                },
                now.year, now.monthValue - 1, now.dayOfMonth
            ).show()
        }

        // 時間選擇（自動補秒）
        editTime.setOnClickListener {
            val now = LocalTime.now()
            TimePickerDialog(
                this,
                { _, h, min ->
                    val time = LocalTime.of(h, min, 0)   // 秒固定 0
                    editTime.setText(time.toString())    // HH:mm:00
                },
                now.hour, now.minute, true
            ).show()
        }

        // 按下「寫入 GOOGLE FIT」
        btnSubmit.setOnClickListener {
            val dateText = editDate.text.toString().trim()
            val timeTextRaw = editTime.text.toString().trim()
            val stepsText = editSteps.text
