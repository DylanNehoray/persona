package com.github.wangyung.persona.particle.generator

import android.util.Log
import android.util.Size
import com.github.wangyung.persona.particle.Instinct
import com.github.wangyung.persona.particle.MutableParticle
import com.github.wangyung.persona.particle.ParticleShape
import com.github.wangyung.persona.particle.fastForEach
import com.github.wangyung.persona.particle.generator.parameter.InitialConstraints
import com.github.wangyung.persona.particle.generator.parameter.ParticleGeneratorParameters
import com.github.wangyung.persona.particle.generator.parameter.SourceEdge
import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random
import kotlin.random.nextInt

/**
 * The particle generator that generates the particle randomly in the given [Size].
 */
class RandomizeParticleGenerator(
    private val parameters: ParticleGeneratorParameters,
    private val dimension: Size,
    private val shapeProvider: ShapeProvider,
) : ParticleGenerator {

    private val sourceEdgeSet: List<SourceEdge> = parameters.sourceEdges.toList()
    private var autoIncrementId: AtomicLong = AtomicLong(0)
    private val initialConstraints: List<InitialConstraints>? =
        parameters.constraints?.filterIsInstance(InitialConstraints::class.java)

    override fun resetParticle(particle: MutableParticle) {
        val (x, y) = getRandomXY(particle.instinct, initialConstraints = initialConstraints)
        particle.x = x
        particle.y = y
        val shape = particle.instinct.shape
        val scale = getRandomFloatSafely(parameters.scaleRange)
        val particleInstinct = Instinct(
            speed = getRandomFloatSafely(parameters.speedRange),
            angle = getRandomFloatSafely(parameters.angleRange),
            scaleX = scale,//getRandomFloatSafely(parameters.scaleRange),
            scaleY = scale,//getRandomFloatSafely(parameters.scaleRange),
            width = getRandomWidth(shape),
            height = getRandomHeight(shape),
            xRotationalSpeed = getRandomFloatSafely(parameters.xRotationalSpeedRange),
            zRotationalSpeed = getRandomFloatSafely(parameters.zRotationalSpeedRange),
            shape = shape
        )
        particle.rotation = 0f
        particle.xRotationWidth = particleInstinct.shape.width.toFloat()
        particle.instinct = particleInstinct
        particle.alpha = 1f
        particle.scaleX = scale
        particle.scaleY = scale
    }

    override fun createParticles(): List<MutableParticle> {
        val mutableParticles: MutableList<MutableParticle> = mutableListOf()
        repeat(parameters.count) {
            mutableParticles.add(createParticle(parameters.randomizeInitialXY))
        }
        return mutableParticles
    }

    private fun createParticle(randomizeInitialXY: Boolean): MutableParticle {
        val shape = shapeProvider.provide()
        val scale = getRandomFloatSafely(parameters.scaleRange)
        val instinct = Instinct(
            width = getRandomWidth(shape),
            height = getRandomHeight(shape),
            speed = getRandomFloatSafely(parameters.speedRange)*2,
            angle = getRandomFloatSafely(parameters.angleRange),
            xRotationalSpeed = getRandomFloatSafely(parameters.xRotationalSpeedRange),
            zRotationalSpeed = getRandomFloatSafely(parameters.zRotationalSpeedRange),
            scaleX = scale,//getRandomFloatSafely(parameters.scaleRange),
            scaleY = scale,//getRandomFloatSafely(parameters.scaleRange),
            startOffset = Random.nextInt(parameters.startOffsetRange),
            shape = shape
        )
        val (x, y) = if (randomizeInitialXY) {
            Pair(getRandomX().toFloat(), getRandomY().toFloat())
        } else {
            getRandomXY(instinct, initialConstraints = initialConstraints)
        }
        return MutableParticle(
            id = autoIncrementId.getAndIncrement(),
            x = x,
            y = y,
            scaleX = instinct.scaleX,
            scaleY = instinct.scaleY,
            alpha = 0f,
            instinct = instinct,
        )
    }

    @Suppress("ForbiddenComment")
    private fun getRandomXY(
        instinct: Instinct,
        initialConstraints: List<InitialConstraints>?
    ): Pair<Float, Float> {
        val halfWidth = instinct.width / 2
        val halfHeight = instinct.height / 2

        // If source edge set is empty, use TOP as fallback.
        if (sourceEdgeSet.isEmpty()) {
            return Pair(getRandomX().coerceIn(halfWidth, dimension.width).toFloat(), 0f)
        }

        val edgeCount = sourceEdgeSet.count()
        val index = if (edgeCount > 1) {
            Random.nextInt(0, edgeCount)
        } else {
            edgeCount - 1
        }
        // TODO: Support multiple constraints later.
        return when (sourceEdgeSet[index]) {
            SourceEdge.TOP -> {
                Pair(
                    if (initialConstraints.isNullOrEmpty()) {
                        (getRandomX() - halfWidth).toFloat()
                    } else {
                        (getRandomFloatSafely(initialConstraints[0].limitRange) * dimension.width)
                    },
                    -halfHeight.toFloat()
                )
            }
            SourceEdge.BOTTOM -> {
                Pair(
                    //(getRandomX() - halfWidth).toFloat(),
                    if (initialConstraints.isNullOrEmpty()) {
                        (getRandomX() - halfWidth).toFloat()
                    } else {
                        (getRandomFloatSafely(initialConstraints[0].limitRange) * dimension.width)
                    },
                    dimension.height.toFloat()
                )
            }
            SourceEdge.LEFT -> {
                Pair(
                    0f,
                    if (initialConstraints == null) {
                        (getRandomY() - halfHeight).toFloat()
                    } else {
                        getRandomFloatSafely(initialConstraints[0].limitRange) * dimension.height
                    }
                )
            }
            SourceEdge.RIGHT -> {
                Pair(
                    dimension.width.toFloat(),
                    (getRandomY() - halfHeight).toFloat()
                )
            }
            SourceEdge.CUSTOM -> {
                Pair(
                    if (initialConstraints.isNullOrEmpty()) {
                        (getRandomX() - halfWidth).toFloat()
                    } else {
                        (getRandomFloatSafely(initialConstraints[0].limitRange) * dimension.width)
                    },
                    if (initialConstraints.isNullOrEmpty() || initialConstraints.size < 2) {
                        (getRandomY() - halfHeight).toFloat()
                    } else {
                        (getRandomFloatSafely(initialConstraints[1].limitRange) * dimension.height)
                    }
                )
            }
        }
    }

    private fun getRandomX(): Int = Random.nextInt(dimension.width)
    private fun getRandomY(): Int = Random.nextInt(dimension.height)

    private fun getRandomWidth(shape: ParticleShape): Int =
        when (shape) {
            is ParticleShape.Text,
            is ParticleShape.Path,
            is ParticleShape.Image,
            is ParticleShape.Circle -> {
                shape.width
            }
            else -> Random.nextInt(parameters.particleWidthRange)
        }

    private fun getRandomHeight(shape: ParticleShape): Int =
        when (shape) {
            is ParticleShape.Text,
            is ParticleShape.Path,
            is ParticleShape.Image,
            is ParticleShape.Circle -> {
                shape.height
            }
            else -> Random.nextInt(parameters.particleHeightRange)
        }

    @Suppress("SwallowedException")
    private fun getRandomFloatSafely(floatRange: ClosedFloatingPointRange<Float>): Float =
        try {
            floatRange.nextFloat()
        } catch (e: IllegalArgumentException) {
            floatRange.start
        }
}
