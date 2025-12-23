package com.example.fitsteps

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataDeleteRequest
import java.time.Instant
import java.util.concurrent.TimeUnit

object GoogleFitWriter {

    fun upsertStepsAtSecond(
        context: Context,
        googleAccount: GoogleSignInAccount,
        instant: Instant,
        steps: Int,
        onResult: (Boolean, String) -> Unit
    ) {
        val historyClient = Fitness.getHistoryClient(context, googleAccount)

        val startSec = instant.epochSecond
        val endSec = startSec + 1   // 1 秒時間桶 [startSec, endSec)

        // 先刪除同一秒內舊的步數資料（只會刪到你這個 App 寫入的）
        val deleteRequest = DataDeleteRequest.Builder()
            .setTimeInterval(startSec, endSec, TimeUnit.SECONDS)
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .build()

        val dataSource = DataSource.Builder()
            .setAppPackageName(context)
            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .setType(DataSource.TYPE_RAW)
            .build()

        val dataPoint = DataPoint.builder(dataSource)
            .setTimeInterval(startSec, endSec, TimeUnit.SECONDS)
            .setField(Field.FIELD_STEPS, steps)
            .build()

        val dataSet = DataSet.builder(dataSource)
            .add(dataPoint)
            .build()

        historyClient.deleteData(deleteRequest)
            .addOnSuccessListener {
                historyClient.insertData(dataSet)
                    .addOnSuccessListener {
                        onResult(true, "時間桶 $startSec–$endSec，步數 = $steps")
                    }
                    .addOnFailureListener { e ->
                        onResult(false, "插入失敗：${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                onResult(false, "刪除舊資料失敗：${e.message}")
            }
    }
}
