package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.example.ui.screens.StretchDisplayScreen
import com.example.ui.theme.PathOfChampionsTheme
import com.example.viewmodel.StretchViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w850dp-h390dp-land")
class StretchViewModelTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `stretchViewModel has predefined exercises and getRandomExercise updates state`() {
        val viewModel = StretchViewModel(random = Random(42))
        assertTrue(viewModel.exercises.size >= 5)
        viewModel.exercises.forEach { exercise ->
            assertTrue(exercise.name.isNotBlank())
            assertTrue(exercise.instructions.isNotEmpty())
            assertTrue(exercise.imageRes != 0)
        }

        val initial = viewModel.currentExercise.value
        val next = viewModel.getRandomExercise()
        assertTrue(viewModel.exercises.contains(next))
        assertNotEquals(initial.id, next.id)
        assertEquals(next, viewModel.currentExercise.value)
    }

    @Test
    fun `stretchDisplayScreen renders exercise image and instructions and switches on button tap`() {
        val viewModel = StretchViewModel(random = Random(123))
        val firstExercise = viewModel.currentExercise.value

        composeTestRule.setContent {
            PathOfChampionsTheme {
                StretchDisplayScreen(
                    viewModel = viewModel,
                    onBack = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("stretch_display_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("stretch_image").assertIsDisplayed()
        composeTestRule.onNodeWithTag("stretch_title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("stretch_title").assertTextEquals(firstExercise.name)
        composeTestRule.onNodeWithTag("stretch_instructions").assertIsDisplayed()
        composeTestRule.onNodeWithTag("next_stretch_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("stretch_back_button").assertIsDisplayed()

        composeTestRule.onNodeWithTag("next_stretch_button").performClick()
        composeTestRule.waitForIdle()

        val updatedExercise = viewModel.currentExercise.value
        assertNotEquals(firstExercise.id, updatedExercise.id)
        composeTestRule.onNodeWithTag("stretch_title").assertTextEquals(updatedExercise.name)
    }

    @Test
    @Config(qualifiers = "w360dp-h640dp-port")
    fun `stretchDisplayScreen renders cleanly in narrow portrait width without nested scroll crash`() {
        val viewModel = StretchViewModel(random = Random(99))

        composeTestRule.setContent {
            PathOfChampionsTheme {
                StretchDisplayScreen(
                    viewModel = viewModel,
                    onBack = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("stretch_display_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("stretch_image").assertIsDisplayed()
        composeTestRule.onNodeWithTag("stretch_title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("stretch_instructions").assertIsDisplayed()
    }
}
