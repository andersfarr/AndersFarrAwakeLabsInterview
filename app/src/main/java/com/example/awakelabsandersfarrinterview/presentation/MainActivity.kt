package com.example.awakelabsandersfarrinterview.presentation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import android.app.RemoteInput
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.wear.input.RemoteInputIntentHelper
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.input.RemoteInputIntentHelper.Companion.putRemoteInputsExtra
import com.example.awakelabsandersfarrinterview.presentation.theme.AwakeLabsAndersFarrInterviewTheme
import java.io.File



class MainActivity : ComponentActivity() {
    private val uiKey = "given_name"
    private val uiHandler = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val givenName = result.data?.getStringExtra(uiKey)
            if(givenName != null){
                //Placeholder file - didn't know what else to do with a given name.
                File("names.txt").writeText(givenName)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            this.WearApp("Hello Awake Labs! Welcome to my App", "Health\nData", "Name\nEntry")
        }
    }

    @Composable
    fun WearApp(greetingName: String, healthText: String, uiText: String) {
        AwakeLabsAndersFarrInterviewTheme {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Greeting(greetingName = greetingName)
                Row{
                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                    ){
                        SecondActivityButton(healthText = healthText)
                    }
                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                    ) {
                        UserInputButton(uiText = uiText)
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
    fun SecondActivityButton(healthText: String) {
        Button(
            onClick = {toSecondaryActivity()},
            enabled = true,
            content = {
                Text(
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.secondary,
                    text = healthText
                )
            }
        )
    }

    private fun toSecondaryActivity() {
        val secondaryActivityIntent = Intent(this, SecondaryActivity::class.java)
        startActivity(secondaryActivityIntent)
    }

    @Composable
    fun UserInputButton(uiText: String) {
        Button(
            onClick = {getUserInput()},
            enabled = true,
            content = {
                Text(
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.secondary,
                    text = uiText
                )
            }
        )
    }

    private fun getUserInput() {
        val remoteInputs: List<RemoteInput> = listOf(
            RemoteInput.Builder(uiKey).setLabel("Enter your Name").build()
        )
        val intent: Intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
        putRemoteInputsExtra(intent, remoteInputs)

        uiHandler.launch(intent)
    }

}