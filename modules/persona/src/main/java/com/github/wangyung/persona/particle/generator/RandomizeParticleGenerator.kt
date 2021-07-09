package com.github.wangyung.persona.particle.generator

import com.github.wangyung.persona.particle.Instinct
import com.github.wangyung.persona.particle.MutableParticle
import com.github.wangyung.persona.particle.ParticleShape
import com.github.wangyung.persona.particle.generator.parameter.RandomizeParticleGeneratorParameters
import com.github.wangyung.persona.particle.generator.parameter.SourceEdge
import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random
import kotlin.random.nextInt

/**
 * The particle generator that generates the particle randomly in the given
 * [width] and [height].
 */
class RandomizeParticleGenerator(
    private val parameters: RandomizeParticleGeneratorParameters,
    private val width: Int,
    private val height: Int,
) : ParticleGenerator {

    private val sourceEdgeSet: List<SourceEdge> = parameters.sourceEdges.toList()
    private var autoIncrementId: AtomicLong = AtomicLong(0)

    override fun resetParticle(particle: MutableParticle) {
        val (x, y) = getRandomXY(particle.instinct)
        particle.x = x
        particle.y = y
        val shape = particle.instinct.shape
        val particleInstinct = Instinct(
            speed = Random.nextFloat(parameters.minSpeed, parameters.maxSpeed),
            angle = getRandomAngle(parameters.angleRange),
            scaleX = Random.nextFloat(parameters.minScale, parameters.maxScale),
            scaleY = Random.nextFloat(parameters.minScale, parameters.maxScale),
            width = getRandomWidth(shape),
            height = getRandomHeight(shape),
            rotationalSpeed =
            Random.nextFloat(parameters.minRotationalSpeed, parameters.maxRotationalSpeed),
            shape = shape
        )
        particle.rotation = 0f
        particle.instinct = particleInstinct
    }

    override fun createParticles(): List<MutableParticle> {
        val mutableParticles: MutableList<MutableParticle> = mutableListOf()
        repeat(parameters.count) {
            mutableParticles.add(createParticle(parameters.randomizeInitialXY))
        }
        return mutableParticles
    }

    private fun createParticle(randomizeInitialXY: Boolean): MutableParticle {
        val shape = parameters.shapeProvider.invoke()
        val instinct = Instinct(
            width = getRandomWidth(shape),
            height = getRandomHeight(shape),
            speed = Random.nextFloat(parameters.minSpeed, parameters.maxSpeed),
            angle = getRandomAngle(parameters.angleRange),
            rotationalSpeed = Random.nextFloat(
                parameters.minRotationalSpeed,
                parameters.maxRotationalSpeed
            ),
            scaleX = Random.nextFloat(parameters.minScale, parameters.maxScale),
            scaleY = Random.nextFloat(parameters.minScale, parameters.maxScale),
            startOffset = Random.nextInt(parameters.startOffsetRange),
            shape = shape
        )
        val (x, y) = if (randomizeInitialXY) {
            Pair(getRandomX().toFloat(), getRandomY().toFloat())
        } else {
            getRandomXY(instinct)
        }
        return MutableParticle(
            id = autoIncrementId.getAndIncrement(),
            x = x,
            y = y,
            instinct = instinct
        )
    }

    private fun getRandomXY(instinct: Instinct): Pair<Float, Float> {
        val halfWidth = instinct.width / 2
        val halfHeight = instinct.height / 2

        // If source edge set is empty, use TOP as fallback.
        if (sourceEdgeSet.isEmpty()) {
            return Pair(getRandomX().coerceIn(halfWidth, width).toFloat(), 0f)
        }

        val edgeCount = sourceEdgeSet.count()
        val index = if (edgeCount > 1) {
            Random.nextInt(0, edgeCount)
        } else {
            edgeCount - 1
        }
        return when (sourceEdgeSet[index]) {
            SourceEdge.TOP -> {
                Pair(getRandomX().coerceIn(halfWidth, width).toFloat(), 0f)
            }
            SourceEdge.BOTTOM -> {
                Pair(
                    getRandomX().coerceIn(halfWidth, width).toFloat(),
                    height.toFloat()
                )
            }
            SourceEdge.LEFT -> {
                Pair(
                    0f,
                    getRandomY().coerceIn(halfHeight, height - halfHeight).toFloat()
                )
            }
            SourceEdge.RIGHT -> {
                Pair(
                    width.toFloat(),
                    getRandomY().coerceIn(halfHeight, height - halfHeight).toFloat()
                )
            }
        }
    }

    private fun getRandomX(): Int = Random.nextInt(width)
    private fun getRandomY(): Int = Random.nextInt(height)

    private fun getRandomWidth(shape: ParticleShape): Int =
        when (shape) {
            is ParticleShape.Text -> {
                shape.nativePaint.getTextBounds(shape.text, 0, shape.text.count(), shape.textBounds)
                shape.textBounds.width()
            }
            is ParticleShape.Path -> {
                shape.path.getBounds().width.toInt()
            }
            is ParticleShape.Image -> {
                if (shape.useImageSize) {
                    shape.image.width
                } else {
                    Random.nextInt(parameters.particleWidthRange)
                }
            }
            is ParticleShape.Circle -> {
                shape.radius
            }
            else -> Random.nextInt(parameters.particleWidthRange)
        }

    private fun getRandomHeight(shape: ParticleShape): Int =
        when (shape) {
            is ParticleShape.Text -> {
                shape.nativePaint.getTextBounds(shape.text, 0, shape.text.count(), shape.textBounds)
                shape.textBounds.height()
            }
            is ParticleShape.Path -> {
                shape.path.getBounds().height.toInt()
            }
            is ParticleShape.Image -> {
                if (shape.useImageSize) {
                    shape.image.height
                } else {
                    Random.nextInt(parameters.particleWidthRange)
                }
            }
            is ParticleShape.Circle -> {
                shape.radius
            }
            else -> Random.nextInt(parameters.particleHeightRange)
        }

    private fun getRandomAngle(angleRange: IntRange): Int =
        try {
            Random.nextInt(angleRange)
        } catch (e: IllegalArgumentException) {
            Random.nextInt(IntRange(angleRange.first, parameters.angleRange.first))
        }
}