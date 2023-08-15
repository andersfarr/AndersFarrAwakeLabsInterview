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
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.awakelabsandersfarrinterview.presentation.theme.AwakeLabsAndersFarrInterviewTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Queue
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Activity which sets up DB and monitoring for health data
 * Also has a button which toggles whether the "back-end" (i.e. the database) is accessible
 */
class SecondaryActivity : ComponentActivity() {
    var backEndWorking = true
    val dbTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    var lastTimeDBWorking: String = LocalDateTime.now().format(dbTimeFormatter)

    //Data to be logged in database
    @Entity
    data class HealthDataPoint(
        @PrimaryKey val id: String,
        @ColumnInfo(name = "time") val time: String?,
        @ColumnInfo(name = "heart_rate_bpm") val heartRateBPM: Double?,
        @ColumnInfo(name = "absolute_elevation") val absoluteElevation: Double?,
        @ColumnInfo(name = "speed") val speed: Double?
    )

    //Queue used for caching data if back-end DB goes down
    var cacheOfData: Queue<HealthDataPoint> = ConcurrentLinkedQueue<HealthDataPoint>()
    //Dao object for database interaction, only insert operations right now
    @Dao
    interface HealthDao {
        @Insert
        fun insert(datapoint: HealthDataPoint)

    }

    //AppDatabase - extends room database SQLite implementation
    @Database(entities = [HealthDataPoint::class], version = 1)
    abstract class AppDatabase : RoomDatabase() {
        abstract fun userDao(): HealthDao

        //Callback, initializes DB with mock data
        private class HealthCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        val healthDao = database.userDao()
                        val startDataPoint = HealthDataPoint(
                            UUID.randomUUID().toString(),
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

        //Singleton implementation as recommended by
        //https://developer.android.com/codelabs/android-room-with-a-view-kotlin#13
        companion object {
            @Volatile
            private var INSTANCE: AppDatabase? = null

            //function used to create DB
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


    //Create and set up all required information for health and DB
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
            override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
                val current = LocalDateTime.now().format(dbTimeFormatter)
                val datapoint = HealthDataPoint(
                                    UUID.randomUUID().toString(),
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

        //Set up passive health monitoring using above set-up
        passiveMonitoringClient.setPassiveListenerCallback(
            passiveListenerConfig,
            passiveListenerCallback
        )

        setContent {
            WearApp("Polling Health data...", "Main\nScreen", "Toggle\nBackend\nLoss")
        }
    }

    //Main app containing all of the UI
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
    //Polling info greeting
    @Composable
    fun Greeting(greetingName: String) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary,
            text = greetingName
        )
    }

    //Button to go back to the landing page
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

    //Moves back to the main activity
    private fun toHome() {
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(mainActivityIntent)
    }

    //toggles the backendWorking value
    //that the back-end database isn't working
    @Composable
    fun SimulateDataLossButton(stopDataText: String){
        Button(
            onClick = {toggleDataLoss()},
            enabled = true,
            modifier = Modifier.fillMaxWidth(),
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