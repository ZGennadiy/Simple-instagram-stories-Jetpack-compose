package com.example.storiesjetpackcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.storiesjetpackcompose.ui.theme.StoriesJetpackComposeTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import kotlin.math.max
import kotlin.math.min

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StoriesJetpackComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Stories()
                }
            }
        }
    }
}

@Preview
@Composable
fun Stories() {
    val images = remember {
        listOf(
            R.drawable.image_1,
            R.drawable.image_2,
            R.drawable.image_3,
            R.drawable.image_4,
            R.drawable.image_5,
            R.drawable.image_6,
            R.drawable.image_7,
            R.drawable.image_8,
        )
    }

    val stepCount = images.size
    val currentStep = remember { mutableStateOf(0) }
    val isPaused = remember { mutableStateOf(false) }
    val goToPreviousScreen = { currentStep.value = max(0, currentStep.value - 1) }
    val goToNextScreen = { currentStep.value = min(stepCount - 1, currentStep.value + 1) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val imageModifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        val pressStartTime = System.currentTimeMillis()



                        try {
                            isPaused.value = true
                            awaitRelease()
                            val pressEndTime = System.currentTimeMillis()
                            val totalPressTime = pressEndTime - pressStartTime
                            if (totalPressTime < 200) {
                                val isTapOnRightTwoTiers = (offset.x > (constraints.maxWidth / 4))
                                if (isTapOnRightTwoTiers) {
                                    goToNextScreen()
                                } else {
                                    goToPreviousScreen()
                                }
                            }
                        } finally {
                            isPaused.value = false
                        }
                    }
                )
            }
        Image(
            painter = painterResource(id = images[currentStep.value]),
            contentDescription = "StoryImage",
            contentScale = ContentScale.FillHeight,
            modifier = imageModifier
        )
        ProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            stepCount = stepCount,
            stepDuration = 2_000,
            unSelectedColor = Color.LightGray,
            selectedColor = Color.White,
            currentStep = currentStep.value,
            onStepChanged = { currentStep.value = it },
            isPaused = isPaused.value,
            onComplete = { }
        )
    }
}

@Composable
fun ProgressIndicator(
    modifier: Modifier = Modifier,
    stepCount: Int,
    stepDuration: Int,
    unSelectedColor: Color,
    selectedColor: Color,
    currentStep: Int,
    onStepChanged: (Int) -> Unit,
    isPaused: Boolean = false,
    onComplete: () -> Unit,
) {
    val currentStepState = remember(currentStep) { mutableStateOf(currentStep) }
    val progress = remember(currentStep) { Animatable(0f) }

    Row(
        modifier = modifier
    ) {
        for (i in 0 until stepCount) {
            val stepProgress = when {
                i == currentStepState.value -> progress.value
                i > currentStepState.value -> 0f
                else -> 1f
            }
            LinearProgressIndicator(
                color = selectedColor,
                backgroundColor = unSelectedColor,
                progress = stepProgress,
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp)
                    .height(2.dp) //Indicator height
            )
        }
    }

    LaunchedEffect(
        isPaused, currentStep
    ) {
        if (isPaused) {
            progress.stop()
        } else {
            for (i in currentStep until stepCount) {
                progress.animateTo(
                    1f,
                    animationSpec = tween(
                        durationMillis = ((1f - progress.value) * stepDuration).toInt(),
                        easing = LinearEasing
                    )
                )
                if (currentStepState.value + 1 <= stepCount - 1) {
                    progress.snapTo(0f)
                    currentStepState.value += 1
                    onStepChanged(currentStepState.value)
                } else {
                    onComplete()
                }
            }
        }
    }
}

