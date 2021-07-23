package com.lehaine.lab

import com.lehaine.kiwi.korge.getRandomByPrefix
import com.lehaine.kiwi.korge.particle.Particle
import com.lehaine.kiwi.korge.particle.ParticleSimulator
import com.lehaine.kiwi.korge.view.addToLayer
import com.lehaine.kiwi.korge.view.layers
import com.lehaine.kiwi.korge.view.ldtk.ldtkMapView
import com.lehaine.kiwi.korge.view.ldtk.toLDtkLevel
import com.lehaine.kiwi.random
import com.lehaine.kiwi.randomd
import com.soywiz.klock.seconds
import com.soywiz.korev.Key
import com.soywiz.korge.input.keys
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.addUpdater
import com.soywiz.korge.view.fast.*
import com.soywiz.korge.view.text
import com.soywiz.korim.atlas.readAtlas
import com.soywiz.korim.color.RGBA
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korio.file.std.resourcesVfs
import kotlin.math.PI
import kotlin.random.Random

/**
 * @author Colton Daily
 * @date 7/23/2021
 */
class CPUParticleScene : Scene() {
    override suspend fun Container.sceneInit() {


        val world = World().apply { loadAsync() }
        val level = world.allLevels[0]
        val layers = layers()
        text("CPU Particle Scene").addToLayer(layers, 3)
        val atlas = resourcesVfs["tiles.atlas.json"].readAtlas()
        val particleContainer = FastSpriteContainer(useRotation = true, smoothing = false).also {
            layers.add(it, layer = 1)
        }
        val particleSimulator = ParticleSimulator(100_000)
        val collisionLayers = intArrayOf(1)
        val collisionLayer = level.layerCollisions
        val gridSize = 16

        layers.ldtkMapView(level.toLDtkLevel(), layer = 0)

        fun hasCollision(cx: Int, cy: Int): Boolean {
            return if (collisionLayer.isCoordValid(cx, cy)) {
                collisionLayers.contains(collisionLayer.getInt(cx, cy))
            } else {
                true
            }
        }

        fun Particle.isColliding(offsetX: Int = 0, offsetY: Int = 0) =
            hasCollision(((x + offsetX) / gridSize).toInt(), ((y + offsetY) / gridSize).toInt())

        fun bloodPhysics(particle: Particle) {
            if (particle.isColliding() && particle.data0 != 1) {
                particle.data0 = 1
                particle.xDelta *= 0.4
                particle.yDelta = 0.0
                particle.gravityY = (0.0..0.001).random()
                particle.friction = (0.5..0.7).random()
                particle.scaleDeltaY = (0.0..0.001).random()
                particle.rotation = 0.0
                particle.rotationDelta = 0.0
                if (particle.isColliding(-5) || particle.isColliding(5)) {
                    particle.scaleY *= (1.0..1.25).random()
                }
                if (particle.isColliding(offsetY = -5) || particle.isColliding(offsetY = 5)) {
                    particle.scaleX *= (1.0..1.25).random()
                }
            }
        }

        fun gutsSplatter(x: Double, y: Double, dir: Int) {
            create(50) {
                val p = particleSimulator.alloc(particleContainer, atlas.getRandomByPrefix("fxDot"), x, y)
                p.color = RGBA((111..255).random(), 0, 0, (0..255).random())
                p.xDelta = dir * (3..7).randomd()
                p.yDelta = (-1..0).randomd()
                p.gravityY = (0.07..0.1).random()
                p.rotation = (0.0..PI * 2).random()
                p.friction = (0.92..0.96).random()
                p.rotation = (0.0..PI * 2).random()
                p.scale(0.7)
                p.life = (3..10).random().seconds
                p.onUpdate = ::bloodPhysics

            }
        }

        keys {
            down(Key.ESCAPE) {
                launchImmediately { sceneContainer.changeTo<SceneSelector>() }
            }
        }

        addUpdater { dt ->
            if (views.input.mouseButtons != 0) {
                val point = localMouseXY(views)
                val dir = if (Random.nextFloat() > 0.5) 1 else -1
                gutsSplatter(point.x, point.y, dir)
            }
            particleSimulator.simulate(dt)

        }
    }
}