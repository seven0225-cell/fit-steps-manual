package com.example.fitsteps

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.auth.api.signin.GoogleSignIn
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

        val editDate = findViewById<android.widget.EditText>(R.id.editDate)
        val editTime = findViewById<android.widget.EditText>(R.id.editTime)
        val editSteps = findViewById<android.widget.EditText>(R.id.editSteps)
        val btnSubmit = findViewById<android.widget.Button>(R.id.btnSubmit)
        val txtStatus = findViewById<android.widget.TextView>(R.id.txtStatus)

        editDate.setOnClickListener {
            val now = LocalDate.now()
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    val date = LocalDate.of(y, m + 1, d)
                    editDate.setText(date.toString())
                },
                now.year, now.monthValue - 1, now.dayOfMonth
            ).show()
        }

        editTime.setOnClickListener {
            val now = LocalTime.now()
            TimePickerDialog(
                this,
                { _, h, min ->
                    val time = LocalTime.of(h, min, 0)
                    editTime.setText(time.toString())
                },
                now.hour, now.minute, true
            ).show()
        }

        btnSubmit.setOnClickListener {
            val dateText = editDate.text.toString()
            val timeText = editTime.text.toString()
            val stepsText = editSteps.text.toString()

            if (dateText.isBlank() || timeText.isBlank() || stepsText.isBlank()) {
                txtStatus.text = "請輸入完整的日期、時間與步數"
                return@setOnClickListener
            }

            val steps = stepsText.toIntOrNull()
            if (steps == null || steps < 0) {
                txtStatus.text = "步數必須是非負整數"
                return@setOnClickListener
            }

            val ldt = try {
                LocalDateTime.parse("${dateText}T$timeText")
            } catch (e: Exception) {
                txtStatus.text = "日期或時間格式錯誤"
                return@setOnClickListener
            }

            val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
            if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                GoogleSignIn.requestPermissions(
                    this,
                    1001,
                    account,
                    fitnessOptions
                )
                return@setOnClickListener
            }

            val zoneId = ZoneId.systemDefault()
            val instant = ldt.atZone(zoneId).toInstant()

            txtStatus.text = "寫入中..."
            GoogleFitWriter.upsertStepsAtSecond(
                context = this,
                instant = instant,
                steps = steps
            ) { success, message ->
                runOnUiThread {
                    txtStatus.text = if (success) {
                        "成功寫入：$message"
                    } else {
                        "寫入失敗：$message"
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 授權結束後再按一次寫入即可
    }
}
