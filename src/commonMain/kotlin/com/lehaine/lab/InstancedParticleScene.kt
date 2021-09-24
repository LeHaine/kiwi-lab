package com.lehaine.lab

import com.lehaine.kiwi.korge.getRandomByPrefix
import com.lehaine.kiwi.korge.particle.FParticleContainer
import com.lehaine.kiwi.korge.particle.FParticleSimulator
import com.lehaine.kiwi.korge.view.addToLayer
import com.lehaine.kiwi.korge.view.layers
import com.lehaine.kiwi.korge.view.ldtk.ldtkMapView
import com.lehaine.kiwi.korge.view.ldtk.toLDtkLevel
import com.lehaine.kiwi.random
import com.soywiz.klock.seconds
import com.soywiz.korev.Key
import com.soywiz.korge.input.keys
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.addUpdater
import com.soywiz.korge.view.fast.FSprite
import com.soywiz.korge.view.text
import com.soywiz.korim.atlas.readAtlas
import com.soywiz.korim.color.RGBA
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korio.file.std.resourcesVfs
import kotlin.math.PI

/**
 * @author Colton Daily
 * @date 7/23/2021
 */
class InstancedParticleScene : Scene() {
    override suspend fun Container.sceneInit() {
        val world = World().apply { loadAsync() }
        val level = world.allLevels[0]
        val layers = layers()
        text("Instanced Particle Scene").addToLayer(layers, 3)
        val atlas = resourcesVfs["tiles.atlas.json"].readAtlas()
        val particleContainer = FParticleContainer(100_000)
        val particleSimulator = FParticleSimulator()
        val collisionLayers = intArrayOf(1)
        val collisionLayer = level.layerCollisions
        val gridSize = 16

        layers.ldtkMapView(level.toLDtkLevel(), layer = 0)
        layers.add(particleContainer.createView(atlas.texture.bmp), 1)

        fun hasCollision(cx: Int, cy: Int): Boolean {
            return if (collisionLayer.isCoordValid(cx, cy)) {
                collisionLayers.contains(collisionLayer.getInt(cx, cy))
            } else {
                true
            }
        }

        fun FSprite.isColliding(particleContainer: FParticleContainer, offsetX: Int = 0, offsetY: Int = 0) =
            particleContainer.run {
                hasCollision(((x + offsetX) / gridSize).toInt(), ((y + offsetY) / gridSize).toInt())
            }

        fun FParticleContainer.bloodPhysics(particle: FSprite) {
            if (particle.isColliding(this) && particle.data0 != 1) {
                particle.data0 = 1
                particle.xDelta *= 0.4f
                particle.yDelta = 0.0f
                particle.gravityY = (0.0f..0.001f).random()
                particle.friction = (0.5f..0.7f).random()
                particle.scaleDeltaY = (0.0f..0.001f).random()
                particle.radiansf = 0.0f
                particle.rotationDelta = 0.0f
                if (particle.isColliding(this, -5) || particle.isColliding(this, 5)) {
                    particle.scaleY *= (1.0f..1.25f).random()
                }
                if (particle.isColliding(this, offsetY = -5) || particle.isColliding(this, offsetY = 5)) {
                    particle.scaleX *= (1.0f..1.25f).random()
                }
            }
        }

        fun gutsSplatter(x: Float, y: Float, dir: Int) {
            create(50) {
                val p = particleSimulator.alloc(particleContainer, atlas.getRandomByPrefix("fxDot"), x, y)
                particleContainer.run {
                    p.colorMul = RGBA((111..255).random(), 0, 0, (0..255).random())
                    p.xDelta = dir * (3f..7f).random()
                    p.yDelta = (-1f..0f).random()
                    p.gravityY = (0.07f..0.1f).random()
                    p.radiansf = (0.0f..PI.toFloat() * 2f).random()
                    p.friction = (0.92f..0.96f).random()
                    p.scale(0.7f)
                    p.life = (5..10).random().seconds
                    p.data3 = 5
                }
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
                val dir = if ((0f..1f).random() > 0.5) 1 else -1
                gutsSplatter(point.xf, point.yf, dir)
            }

            particleSimulator.simulate(particleContainer, dt) { p ->
                if (p.data3 == 5) {
                    bloodPhysics(p)
                }
            }

        }
    }
}