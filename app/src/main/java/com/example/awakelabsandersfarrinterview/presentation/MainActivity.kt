package com.example.awakelabsandersfarrinterview.presentation

import android.app.Activity
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.input.RemoteInputIntentHelper
import androidx.wear.input.RemoteInputIntentHelper.Companion.putRemoteInputsExtra
import com.example.awakelabsandersfarrinterview.presentation.theme.AwakeLabsAndersFarrInterviewTheme

/**Main User Landing Screen
 * Has two buttons which either go to the health data activity or allow User Input
 * User Input is done using RemoteInput Package
 */
class MainActivity : ComponentActivity() {
    private val uiKey = "given_name"
    private val tag = "MainActivity"

    //Handle returned data from UI - Wasnt exactly sure what to do with it
    //but implemented the handler
    private val uiHandler = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val givenName = result.data?.getStringExtra(uiKey)
            if(givenName != null){
                //just logging given name for now, not much else to do with it
                Log.v(tag, "givenName = $givenName")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            this.WearApp("Hello Awake Labs! Welcome to my App", "Health\nData", "Name\nEntry")
        }
    }

    //Top-level application holding all of the required data
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

    //Basic greeting
    @Composable
    fun Greeting(greetingName: String) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary,
            text = greetingName
        )
    }

    //Brings to the second activity (Health Activity)
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

    //Moves to secondary activity
    private fun toSecondaryActivity() {
        val secondaryActivityIntent = Intent(this, SecondaryActivity::class.java)
        startActivity(secondaryActivityIntent)
    }

    //Button to begin UI
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

    //Getting User Input using the remoteInputs class
    private fun getUserInput() {
        val remoteInputs: List<RemoteInput> = listOf(
            RemoteInput.Builder(uiKey).setLabel("Enter your Name").build()
        )
        val intent: Intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
        putRemoteInputsExtra(intent, remoteInputs)

        uiHandler.launch(intent)
    }

}