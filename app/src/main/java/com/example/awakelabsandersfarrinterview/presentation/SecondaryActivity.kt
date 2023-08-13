package com.example.awakelabsandersfarrinterview.presentation

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
import com.example.awakelabsandersfarrinterview.presentation.theme.AwakeLabsAndersFarrInterviewTheme

class SecondaryActivity : ComponentActivity() {
    var backEndWorking = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp("Polling Health data...\nUse toggle for back-end connection", "Main\nScreen", "Toggle Data Loss")
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

