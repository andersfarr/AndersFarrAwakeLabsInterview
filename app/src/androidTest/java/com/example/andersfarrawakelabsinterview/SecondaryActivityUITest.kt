package com.example.andersfarrawakelabsinterview
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.awakelabsandersfarrinterview.presentation.SecondaryActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)

class SecondaryActivityUITest {
    @get:Rule
    val rule = createComposeRule()
    @Test
    fun testUI(){
        val greeting = "hello"
        val buttonOneString = "home"
        val buttonTwoString = "stop"
        rule.setContent{
            SecondaryActivity().WearApp(greeting, buttonOneString, buttonTwoString)
        }
        rule.onNodeWithText("hello").assertExists()
        rule.onNodeWithText("home").assertExists()
        rule.onNodeWithText("home").assertHasClickAction()
        rule.onNodeWithText("stop").assertExists()
        //Since mocked instance, we can't perform navigation to different actions
        rule.onNodeWithText("stop").assertHasClickAction()
        //Perform click changes private variable, so we can't check it's output
        //Testing to make sure click doens't throw errors
        rule.onNodeWithText("stop").performClick()
    }

}