package com.lehaine.lab

import com.lehaine.kiwi.korge.InputController
import com.lehaine.kiwi.korge.getByPrefix
import com.lehaine.kiwi.korge.view.*
import com.soywiz.klock.milliseconds
import com.soywiz.klock.seconds
import com.soywiz.korev.GameButton
import com.soywiz.korev.Key
import com.soywiz.korge.input.keys
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.*
import com.soywiz.korim.atlas.readAtlas
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korma.geom.Anchor

sealed class Input {
    object MoveHorizontal : Input()
    object MoveVertical : Input()
}

class TestScene : Scene() {

    private val testContainer = Container()
    override suspend fun Container.sceneInit() {

        val sceneText = text("SELECT SCENE (0-9)")
        addChild(testContainer)
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

        fun createLight(color: RGBA): Container {
            val lightContainer = Container()
            val lightCore = lightContainer.enhancedSprite(atlas.getByPrefix("fxSpotLightCore"), smoothing = false) {
                blendMode = BlendMode.ADD
                smoothing = true
                anchor(Anchor.MIDDLE_CENTER)
                colorMul = color
                alpha = 0.4
                scale = 0.5
            }
            val coreHalo = lightContainer.enhancedSprite(atlas.getByPrefix("fxSpotLight"), smoothing = false) {
                blendMode = BlendMode.ADD
                anchor(Anchor.MIDDLE_CENTER)
                colorMul = color
                scale = 0.3
                alpha = 0.36
            }

            val largeHalo = lightContainer.enhancedSprite(atlas.getByPrefix("fxSpotLight"), smoothing = false) {
                blendMode = BlendMode.ADD
                anchor(Anchor.MIDDLE_CENTER)
                colorMul = color
                scale = 0.5
                alpha = 0.2
            }

            return lightContainer
        }

        createLight(Colors.GREEN)
            .addToLayer(layers, 1)
            .apply {
                x = 150.0
                y = 100.0
            }

        createLight(Colors.PURPLE)
            .addToLayer(layers, 1)
            .apply {
                x = 300.0
                y = 100.0
            }

        val sprite1 = layers.enhancedSprite(layer = 0) {
            smoothing = false
            playAnimationLooped(atlas.createEnhancedSpriteAnimation("heroRun") {
                frames(0..1, frameTime = 2.seconds)
                frames(2..3, frameTime = 500.milliseconds)
                frames(1..2, frameTime = 100.milliseconds)
            })
            scale(5, 5)
            position(50, 50)
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
}