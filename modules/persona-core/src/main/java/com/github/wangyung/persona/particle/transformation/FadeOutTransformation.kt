package com.github.wangyung.persona.particle.transformation

import android.util.Log
import androidx.annotation.FloatRange
import com.github.wangyung.persona.particle.MutableParticle
import java.lang.Float.max
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * The transformation that translates (x, y) by the speed. It only modifies the current particle
 * state when everytime the [transform] is invoked.
 * It would have different result even invokes [transform] multiple times with the same iteration.
 */
class FadeOutTransformation(
    val coefficient: Int = 100, // lower number results in faster fade out
) : ParticleTransformation {
    override fun transform(particle: MutableParticle, iteration: Long) {
        particle.alpha = max(particle.alpha - (Random.nextFloat()/coefficient)/(particle.alpha),0f)
    }
}
