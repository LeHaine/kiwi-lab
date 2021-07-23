package com.lehaine.lab

import com.lehaine.kiwi.korge.InputController
import com.lehaine.kiwi.korge.getByPrefix
import com.lehaine.kiwi.korge.getRandomByPrefix
import com.lehaine.kiwi.korge.particle.FParticleContainer
import com.lehaine.kiwi.korge.particle.FParticleSimulator
import com.lehaine.kiwi.korge.particle.Particle
import com.lehaine.kiwi.korge.particle.ParticleSimulator
import com.lehaine.kiwi.korge.view.*
import com.lehaine.kiwi.korge.view.ldtk.ldtkMapView
import com.lehaine.kiwi.korge.view.ldtk.toLDtkLevel
import com.lehaine.kiwi.random
import com.lehaine.kiwi.randomd
import com.lehaine.ldtk.LDtkProject
import com.soywiz.klock.milliseconds
import com.soywiz.klock.seconds
import com.soywiz.korev.GameButton
import com.soywiz.korev.Key
import com.soywiz.korge.input.keys
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.*
import com.soywiz.korge.view.fast.*
import com.soywiz.korim.atlas.readAtlas
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korma.geom.Anchor
import kotlin.math.PI
import kotlin.random.Random

sealed class Input {
    object MoveHorizontal : Input()
    object MoveVertical : Input()
}

@LDtkProject("world.ldtk", name = "World")
class _World

class TestScene : Scene() {

    private val testContainer = Container()
    override suspend fun Container.sceneInit() {

        addChild(testContainer)
        val sceneText = text("SELECT SCENE (0-9)")
        keys {
            down {
                when (it.key) {
                    Key.N0, Key.N1, Key.N2, Key.N3, Key.N4,
                    Key.N5, Key.N6, Key.N7, Key.N8, Key.N9 -> testContainer.removeChildren()
                    else -> {
                        // do nothing
                    }
                }

            }
            down(Key.N1) {
                sceneText.text = "Test Input Controller"
                testContainer.testInputController()
            }
            down(Key.N2) {
                sceneText.text = "Test Enhanced Sprite"
                testContainer.testEnhancedSprites()
            }
            down(Key.N3) {
                sceneText.text = "Test Lights"
                testContainer.testLights()
            }
            down(Key.N4) {
                sceneText.text = "CPU Particles"
                testContainer.testCPUParticles()
            }
            down(Key.N5) {
                sceneText.text = "Instanced Particles"
                testContainer.testGPUParticles()
            }
            down(Key.ESCAPE) {
                stage?.views?.debugViews = false
                stage?.gameWindow?.run {
                    debug = false
                    close()
                }
            }
        }
    }

    private suspend fun Container.testLights() {
        val world = World().apply { loadAsync() }
        val level = world.allLevels[0].toLDtkLevel()
        val atlas = resourcesVfs["tiles.atlas.json"].readAtlas()
        val layers = layers()

        val controller = InputController<Input>(views)

        controller.addAxis(
            Input.MoveHorizontal,
            listOf(Key.D, Key.RIGHT),
            listOf(GameButton.LX),
            listOf(Key.A, Key.LEFT),
            listOf(GameButton.LX)
        )
        val ca1 = controller.createAccess("test", true)

        layers.ldtkMapView(level, layer = 0)

        fun createLight(color: RGBA, intensity: Double = 1.0): Container {
            val lightContainer = Container()
            val darkerLight = Colors["#6841cd"]
            val lightCore = lightContainer.enhancedSprite(atlas.getByPrefix("fxSpotLight"), smoothing = true) {
                blendMode = BlendMode.ADD
                smoothing = true
                anchor(Anchor.MIDDLE_CENTER)
                colorMul = color.interpolateWith(0.2, darkerLight)
                alpha = intensity
                // scale = 0.5
            }

            return lightContainer
        }

        createLight(Colors["#fffd7a"])
            .addToLayer(layers, 2)
            .apply {
                x = 75.0
                y = 200.0
            }

        createLight(Colors.GREEN, 0.9)
            .addToLayer(layers, 2)
            .apply {
                x = 150.0
                y = 200.0
            }

        createLight(Colors.PURPLE, 0.75)
            .addToLayer(layers, 2)
            .apply {
                x = 225.0
                y = 200.0
            }

        createLight(Colors["#524680"], 0.5)
            .addToLayer(layers, 2)
            .apply {
                x = 300.0
                y = 200.0
            }

        createLight(Colors["#ff9937"], intensity = 0.25)
            .addToLayer(layers, 2)
            .apply {
                x = 375.0
                y = 200.0
                scale = 1.5
            }

        val sprite1 = layers.enhancedSprite(layer = 1) {
            smoothing = false
            playAnimationLooped(atlas.getEnhancedSpriteAnimation("heroRun", 100.milliseconds))
            anchor(Anchor.MIDDLE_CENTER)
            position(50, 220)
            scale = 2.0
        }

        addUpdater {
            controller.update()
            if (ca1.down(Input.MoveHorizontal)) {
                sprite1.x += ca1.strength(Input.MoveHorizontal) * 3
            }
        }
    }

    private suspend fun Container.testInputController() {
        val atlas = resourcesVfs["tiles.atlas.json"].readAtlas()
        val controller = InputController<Input>(views)

        controller.addAxis(
            Input.MoveHorizontal,
            listOf(Key.D, Key.RIGHT),
            listOf(GameButton.LX),
            listOf(Key.A, Key.LEFT),
            listOf(GameButton.LX)
        )
        val ca1 = controller.createAccess("test", true)
        val ca2 = controller.createAccess("test2")

        val sprite1 = enhancedSprite {
            smoothing = false
            playAnimationLooped(atlas.createEnhancedSpriteAnimation("heroRun") {
                frames(0..1, frameTime = 2.seconds)
                frames(2..3, frameTime = 500.milliseconds)
                frames(1..2, frameTime = 100.milliseconds)
            })
            scale(5, 5)
            position(50, 50)
        }

        val sprite2 = enhancedSprite {
            smoothing = false
            playAnimationLooped(atlas.createEnhancedSpriteAnimation("heroRun") {
                frames(0..1, frameTime = 2.seconds)
                frames(2..3, frameTime = 500.milliseconds)
                frames(1..2, frameTime = 100.milliseconds)
            })
            scale(5, 5)
            position(100, 50)
        }

        addUpdater {
            controller.update()
            if (ca1.down(Input.MoveHorizontal)) {
                sprite1.x += ca1.strength(Input.MoveHorizontal) * 3
            }
            if (ca2.down(Input.MoveHorizontal)) {
                sprite2.x += ca2.strength(Input.MoveHorizontal) * 3
            }
        }
    }

    private suspend fun Container.testEnhancedSprites() {
        val atlas = resourcesVfs["tiles.atlas.json"].readAtlas()
        enhancedSprite {
            smoothing = false
            playAnimationLooped(atlas.createEnhancedSpriteAnimation("heroRun") {
                frames(0..1, frameTime = 2.seconds)
                frames(2..3, frameTime = 500.milliseconds)
                frames(1..2, frameTime = 100.milliseconds)
            })
            scale(5, 5)
            position(50, 50)
        }


        enhancedSprite {
            smoothing = false
            playAnimationLooped(atlas.createEnhancedSpriteAnimation("heroIdle") {
                frames(0, frameTime = 1.seconds)
                frames(1, frameTime = 500.milliseconds)
                frames(0, frameTime = 100.milliseconds)
                frames(1, frameTime = 400.milliseconds)
                frames(0, frameTime = 200.milliseconds)
                frames(1, frameTime = 2000.milliseconds)
            })
            scale(5, 5)
            position(100, 50)
        }

        enhancedSprite {
            smoothing = false
            playAnimation(atlas.getEnhancedSpriteAnimation("heroDie", 100.milliseconds)) {
                playAnimationLooped(atlas.getEnhancedSpriteAnimation("heroSleep", 100.milliseconds))
            }
            scale(5, 5)
            position(150, 50)
        }
    }

    private suspend fun Container.testCPUParticles() {
        val world = World().apply { loadAsync() }
        val level = world.allLevels[0]
        val layers = layers()
        val atlas = resourcesVfs["tiles.atlas.json"].readAtlas()
        val particleContainer = FastSpriteContainer(useRotation = true, smoothing = false).also {
            layers.add(it, layer = 1)
        }
        val particleSimulator = ParticleSimulator(2048)
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
            create(500) {
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

        addUpdater { dt ->
            if (views.input.mouseButtons != 0) {
                val point = localMouseXY(views)
                val dir = if (Random.nextFloat() > 0.5) 1 else -1
                gutsSplatter(point.x, point.y, dir)
            }
            particleSimulator.simulate(dt)

        }
    }

    private suspend fun Container.testGPUParticles() {
        val world = World().apply { loadAsync() }
        val level = world.allLevels[0]
        val layers = layers()
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
                    //     p.color = RGBA((111..255).random(), 0, 0, (0..255).random())
                    p.xDelta = dir * (3f..7f).random()
                    p.yDelta = (-1f..0f).random()
                    p.gravityY = (0.07f..0.1f).random()
                    //   p.rotationRadians = (0.0f..PI.toFloat() * 2f).random()
                    p.friction = (0.92f..0.96f).random()
                    //    p.scale(0.7f)
                    p.life = (5f..10f).random().seconds
                    //   p.onUpdate = ::bloodPhysics
                    p.data3 = 5
                }
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

    private fun create(num: Int, createParticle: (index: Int) -> Unit) {
        for (i in 0 until num) {
            createParticle(i)
        }
    }
}