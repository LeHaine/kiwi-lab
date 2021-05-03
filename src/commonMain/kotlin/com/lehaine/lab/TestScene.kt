package com.lehaine.lab

import com.lehaine.kiwi.korge.InputController
import com.lehaine.kiwi.korge.view.createEnhancedSpriteAnimation
import com.lehaine.kiwi.korge.view.enhancedSprite
import com.lehaine.kiwi.korge.view.getEnhancedSpriteAnimation
import com.soywiz.klock.milliseconds
import com.soywiz.klock.seconds
import com.soywiz.korev.GameButton
import com.soywiz.korev.Key
import com.soywiz.korge.input.keys
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.addUpdater
import com.soywiz.korge.view.position
import com.soywiz.korge.view.scale
import com.soywiz.korim.atlas.readAtlas
import com.soywiz.korio.file.std.resourcesVfs

sealed class Input {
    object MoveHorizontal : Input()
    object MoveVertical : Input()
}

class TestScene : Scene() {

    override suspend fun Container.sceneInit() {
        val atlas = resourcesVfs["tiles.atlas.json"].readAtlas()
        val controller = InputController<Input>(views)

        controller.addAxis(
            Input.MoveHorizontal,
            listOf(Key.D),
            listOf(GameButton.LX),
            listOf(Key.A),
            listOf(GameButton.LX)
        )
        val sprite = enhancedSprite {
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
            val moveDir = controller.strength(Input.MoveHorizontal)
            if (controller.down(Input.MoveHorizontal)) {
                sprite.x += moveDir * 3
            }
        }


        keys {
            down(Key.ESCAPE) {
                stage?.views?.debugViews = false
                stage?.gameWindow?.run {
                    debug = false
                    close()
                }
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