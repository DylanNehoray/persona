package com.github.wangyung.app.ui.screen.animation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.wangyung.app.model.AnimationParameterSet
import com.github.wangyung.app.model.AnimationType
import com.github.wangyung.app.model.DEFAULT_RAIN_ANGLE_FROM
import com.github.wangyung.app.model.DEFAULT_RAIN_ANGLE_TO
import com.github.wangyung.app.model.DEFAULT_RAIN_MAX_SPEED
import com.github.wangyung.app.model.DEFAULT_RAIN_MIN_SPEED

@Composable
fun RainDemo() {
    val animationType = AnimationType.Rain
    val generatorParameters = animationType.toGeneratorParameters()
    var parameterSet by remember {
        mutableStateOf(
            AnimationParameterSet(
                generatorParameters = generatorParameters,
                particleSystemParameters = animationType.toParticleSystemParameters()
            )
        )
    }
    AnimationDemo(
        animationType = animationType,
        parameterSet = AnimationParameterSet(
            generatorParameters = parameterSet.generatorParameters,
            particleSystemParameters = parameterSet.particleSystemParameters
        )
    ) {
        val modifier = Modifier.fillMaxWidth()
        Column(modifier = modifier) {
            SliderWithValueText(
                title = "Particle Count:",
                modifier = modifier,
                sliderRange = 25f..1000f,
                defaultSliderValue = parameterSet.generatorParameters.count.toFloat()
            ) { newCount ->
                parameterSet = parameterSet.copy(
                    generatorParameters = parameterSet.generatorParameters.copy(count = newCount)
                )
            }

            SliderWithValueText(
                title = "Min speed:",
                modifier = modifier,
                sliderRange = 1f..DEFAULT_RAIN_MAX_SPEED,
                defaultSliderValue = DEFAULT_RAIN_MIN_SPEED
            ) { newMinSpeed ->
                parameterSet = parameterSet.copy(
                    generatorParameters = parameterSet.generatorParameters.copy(
                        minSpeed = newMinSpeed.toFloat()
                    )
                )
            }
            SliderWithValueText(
                title = "Max speed:",
                modifier = modifier,
                sliderRange = 1f..DEFAULT_RAIN_MAX_SPEED,
                defaultSliderValue = DEFAULT_RAIN_MAX_SPEED
            ) { newMaxSpeed ->
                parameterSet = parameterSet.copy(
                    generatorParameters = parameterSet.generatorParameters.copy(
                        maxSpeed = newMaxSpeed.toFloat()
                    )
                )
            }

            SliderWithValueText(
                title = "Angle from:",
                modifier = modifier,
                sliderRange = 30f..150f,
                defaultSliderValue = DEFAULT_RAIN_ANGLE_FROM.toFloat()
            ) { newFromAngle ->
                parameterSet = parameterSet.copy(
                    generatorParameters = parameterSet.generatorParameters.copy(
                        angleRange = IntRange(
                            newFromAngle,
                            parameterSet.generatorParameters.angleRange.last
                        )
                    )
                )
            }
            SliderWithValueText(
                title = "Angle to:",
                modifier = modifier,
                sliderRange = 30f..150f,
                defaultSliderValue = DEFAULT_RAIN_ANGLE_TO.toFloat()
            ) { newToAngle ->
                parameterSet = parameterSet.copy(
                    generatorParameters = parameterSet.generatorParameters.copy(
                        angleRange = IntRange(
                            parameterSet.generatorParameters.angleRange.first,
                            newToAngle
                        )
                    )
                )
            }

            SwitchWithText(
                title = "Random initial X&Y:",
                modifier = modifier,
                isChecked = parameterSet.generatorParameters.randomizeInitialXY
            ) { checked ->
                parameterSet = parameterSet.copy(
                    generatorParameters = parameterSet.generatorParameters.copy(
                        randomizeInitialXY = checked
                    )
                )
            }
            Spacer(modifier = Modifier.padding(8.dp))

            SwitchWithText(
                title = "Auto Reset Particles:",
                modifier = modifier,
                isChecked = parameterSet.particleSystemParameters.autoResetParticles,
            ) { checked ->
                parameterSet = parameterSet.copy(
                    particleSystemParameters = parameterSet.particleSystemParameters.copy(
                        autoResetParticles = checked
                    )
                )
            }
            Spacer(modifier = Modifier.padding(8.dp))

            SwitchWithText(
                title = "Auto reset system",
                modifier = modifier,
                isChecked = parameterSet.particleSystemParameters.restartWhenAllDead,
            ) { checked ->
                parameterSet = parameterSet.copy(
                    particleSystemParameters = parameterSet.particleSystemParameters.copy(
                        restartWhenAllDead = checked
                    )
                )
            }
        }
    }
}