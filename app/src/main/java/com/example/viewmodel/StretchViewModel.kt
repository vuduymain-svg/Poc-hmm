package com.example.viewmodel

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

data class StretchExercise(
    val id: String,
    val name: String,
    val targetArea: String,
    val durationSeconds: Int,
    @DrawableRes val imageRes: Int,
    val instructions: List<String>,
    val breathingTip: String
)

class StretchViewModel(
    private val random: Random = Random.Default
) : ViewModel() {

    val exercises: List<StretchExercise> = listOf(
        StretchExercise(
            id = "stretch_neck_tilt",
            name = "Lateral Neck & Shoulder Tilt",
            targetArea = "Neck & Upper Traps",
            durationSeconds = 30,
            imageRes = R.drawable.img_stretch_neck_roll,
            instructions = listOf(
                "Sit tall with both shoulders relaxed and pulled slightly away from your ears.",
                "Gently tilt your right ear toward your right shoulder until you feel a mild stretch.",
                "Hold for 15 seconds, return to center, and repeat on the left side."
            ),
            breathingTip = "Inhale slowly as you lengthen your spine; exhale to let your shoulder drop."
        ),
        StretchExercise(
            id = "stretch_chest_opener",
            name = "Seated Chest & Spine Opener",
            targetArea = "Chest & Front Shoulders",
            durationSeconds = 30,
            imageRes = R.drawable.img_stretch_chest_opener,
            instructions = listOf(
                "Interlace your fingers behind your back or hold the back edges of your chair.",
                "Roll your shoulder blades down and together while lifting your sternum upward.",
                "Hold the open chest position for 30 seconds while keeping your chin level."
            ),
            breathingTip = "Take deep rib-expanding breaths to open tight pectoral muscles."
        ),
        StretchExercise(
            id = "stretch_wrist_flexor",
            name = "Forearm & Wrist Flexor Stretch",
            targetArea = "Wrists & Forearms",
            durationSeconds = 25,
            imageRes = R.drawable.img_stretch_wrist_flexor,
            instructions = listOf(
                "Extend one arm straight in front of you with your palm facing forward and fingers up.",
                "Use your opposite hand to gently draw your fingers back toward your body.",
                "Hold for 25 seconds per arm, keeping your elbow soft and shoulder relaxed."
            ),
            breathingTip = "Keep steady nasal breaths and ease off immediately if you feel sharp tension."
        ),
        StretchExercise(
            id = "stretch_cat_cow",
            name = "Cat-Cow Spinal Mobilizer",
            targetArea = "Upper & Lower Spine",
            durationSeconds = 40,
            imageRes = R.drawable.img_stretch_cat_cow,
            instructions = listOf(
                "Start on hands and knees with wrists under shoulders and knees beneath hips.",
                "Inhale as you drop your belly, lift your chest, and gently arch your back (Cow).",
                "Exhale as you round your spine toward the ceiling and tuck your chin (Cat). Repeat smoothly."
            ),
            breathingTip = "Synchronize one full breath with each spinal transition."
        ),
        StretchExercise(
            id = "stretch_hamstring_fold",
            name = "Seated Forward Hamstring Fold",
            targetArea = "Hamstrings & Lower Back",
            durationSeconds = 35,
            imageRes = R.drawable.img_stretch_hamstring_fold,
            instructions = listOf(
                "Sit with both legs extended forward and flex your toes gently back toward you.",
                "Lengthen your spine upright, then hinge forward from the hips reaching toward your shins or toes.",
                "Hold for 35 seconds with a neutral back—avoid forcing your knees locked."
            ),
            breathingTip = "Exhale gently as you hinge a little deeper without rounding your upper shoulders."
        )
    )

    private val _currentExercise = MutableStateFlow(exercises[random.nextInt(exercises.size)])
    val currentExercise: StateFlow<StretchExercise> = _currentExercise.asStateFlow()

    /**
     * Picks a random stretching exercise from the predefined list, updates [currentExercise],
     * and returns the chosen [StretchExercise].
     */
    fun getRandomExercise(excludeCurrent: Boolean = true): StretchExercise {
        val candidates = if (excludeCurrent && exercises.size > 1) {
            exercises.filter { it.id != _currentExercise.value.id }
        } else {
            exercises
        }
        val chosen = candidates[random.nextInt(candidates.size)]
        _currentExercise.value = chosen
        return chosen
    }
}
