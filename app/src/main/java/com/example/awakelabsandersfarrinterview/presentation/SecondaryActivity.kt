package com.example.awakelabsandersfarrinterview.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.MaterialTheme
import androidx.health.services.client.HealthServices
import androidx.health.services.client.PassiveListenerCallback
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveListenerConfig
import androidx.lifecycle.lifecycleScope
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.awakelabsandersfarrinterview.presentation.theme.AwakeLabsAndersFarrInterviewTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Queue
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue

class SecondaryActivity : ComponentActivity() {
    var backEndWorking = true

    val dbTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    var lastTimeDBWorking: String = LocalDateTime.now().format(dbTimeFormatter)

    //Data to be logged in database
    @Entity
    data class HealthDataPoint(
        @PrimaryKey val id: UUID,
        @ColumnInfo(name = "time") val time: String?,
        @ColumnInfo(name = "heart_rate_bpm") val heartRateBPM: Double?,
        @ColumnInfo(name = "absolute_elevation") val absoluteElevation: Double?,
        @ColumnInfo(name = "speed") val speed: Double?
    )

    var cacheOfData: Queue<HealthDataPoint> = ConcurrentLinkedQueue<HealthDataPoint>()
    @Dao
    interface HealthDao {
        @Insert
        fun insert(datapoint: HealthDataPoint)

    }

    @Database(entities = [HealthDataPoint::class], version = 1)
    abstract class AppDatabase : RoomDatabase() {
        abstract fun userDao(): HealthDao

        private class HealthCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        val healthDao = database.userDao()
                        val startDataPoint = HealthDataPoint(
                            UUID.randomUUID(),
                            "0",
                            0.0,
                            0.0,
                            0.0,
                        )
                        healthDao.insert(startDataPoint)
                    }
                }
            }
        }

        companion object {
            @Volatile
            private var INSTANCE: AppDatabase? = null

            fun getDatabase(context: Context,
                            scope: CoroutineScope): AppDatabase {
                // if the INSTANCE is not null, then return it,
                // if it is, then create the database
                return INSTANCE ?: synchronized(this) {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "health_database"
                    ).addCallback(HealthCallback(scope))
                        .build()
                    INSTANCE = instance
                    // return instance
                    return instance
                }

            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //building health polling information
        val healthClient = HealthServices.getClient(this)
        val passiveMonitoringClient = healthClient.passiveMonitoringClient

        //Grabbing heart rate, elevation, and speed as stats for initial prototype
        val passiveListenerConfig = PassiveListenerConfig.builder()
            .setDataTypes(setOf(DataType.HEART_RATE_BPM, DataType.ABSOLUTE_ELEVATION, DataType.SPEED))
            .build()
        val database by lazy{AppDatabase.getDatabase(this, this.lifecycleScope)}
        val userDao by lazy{database.userDao()}

        // logging data in database whenever new data is received from HealthServicesClient
        val passiveListenerCallback: PassiveListenerCallback = object : PassiveListenerCallback {
            override fun onRegistered() {
                super.onRegistered()
                val startDataPoint = HealthDataPoint(
                    UUID.randomUUID(),
                    "0",
                    0.0,
                    0.0,
                    0.0,
                )
                userDao.insert(startDataPoint)
            }
            override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
                val current = LocalDateTime.now().format(dbTimeFormatter)
                val datapoint = HealthDataPoint(
                                    UUID.randomUUID(),
                                    current,
                                    dataPoints.getData(DataType.HEART_RATE_BPM)[0].value,
                                    dataPoints.getData(DataType.ABSOLUTE_ELEVATION)[0].value,
                                    dataPoints.getData(DataType.SPEED)[0].value)
                if(backEndWorking){
                    while(!cacheOfData.isEmpty()){
                        userDao.insert(cacheOfData.remove())
                    }
                    userDao.insert(datapoint)
                    lastTimeDBWorking = current
                }
                else{
                    //if it hasn't been working for 4 hours, inform and don't add to cache
                    // would need more testing to determine perfect cache size, just say 4 hours for now
                    if(current.substring(11, 13).toInt() >= lastTimeDBWorking.substring(11,13).toInt() +4){
                        setContent{
                            WearApp("DB not updated for more than 4 hours!","Main\nScreen", "Toggle Backend Loss" )
                        }
                    }
                    else{
                        cacheOfData.add(datapoint)
                    }
                }
            }
        }

        passiveMonitoringClient.setPassiveListenerCallback(
            passiveListenerConfig,
            passiveListenerCallback
        )

        setContent {
            WearApp("Polling Health data...", "Main\nScreen", "Toggle\nBackend\nLoss")
        }
    }

    @Composable
    fun WearApp(greetingName: String, homeText: String, stopDataText: String) {
        AwakeLabsAndersFarrInterviewTheme {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Greeting(greetingName = greetingName)
                Row {
                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                    ) {
                        HomeButton(homeText = homeText)
                    }
                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                    ) {
                        SimulateDataLossButton(stopDataText = stopDataText)
                    }
                }
            }
        }
    }
    @Composable
    fun Greeting(greetingName: String) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary,
            text = greetingName
        )
    }

    @Composable
    fun HomeButton(homeText: String){
        Button(
            onClick = {toHome()},
            enabled = true,
            content = {
                Text(
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.secondary,
                    text = homeText
                )
            }
        )
    }

    private fun toHome() {
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(mainActivityIntent)
    }

    @Composable
    fun SimulateDataLossButton(stopDataText: String){
        Button(
            onClick = {toggleDataLoss()},
            enabled = true,
            content = {
                Text(
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.secondary,
                    text = stopDataText
                )
            }
        )
    }

    private fun toggleDataLoss(){
        backEndWorking = !backEndWorking
    }
}

