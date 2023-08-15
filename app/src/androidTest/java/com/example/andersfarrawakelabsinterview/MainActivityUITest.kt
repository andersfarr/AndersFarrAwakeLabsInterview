package com.example.andersfarrawakelabsinterview
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.awakelabsandersfarrinterview.presentation.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityUITest {

    @get:Rule
    val rule = createComposeRule()
    @Test
    fun testUI(){
        val greeting = "hello"
        val buttonOneString = "buttonOne"
        val buttonTwoString = "buttonTwo"
        rule.setContent{
            MainActivity().WearApp(greeting, buttonOneString, buttonTwoString)
        }
        rule.onNodeWithText("hello").assertExists()
        rule.onNodeWithText("buttonOne").assertExists()
        rule.onNodeWithText("buttonOne").assertHasClickAction()
        rule.onNodeWithText("buttonTwo").assertExists()
        rule.onNodeWithText("buttonTwo").assertHasClickAction()
        //Since mocked instance, we can't perform navigation to different activities
    }

}