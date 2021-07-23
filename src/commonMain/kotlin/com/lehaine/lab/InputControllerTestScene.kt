package com.lehaine.lab

import com.lehaine.kiwi.korge.InputController
import com.lehaine.kiwi.korge.view.createEnhancedSpriteAnimation
import com.lehaine.kiwi.korge.view.enhancedSprite
import com.soywiz.klock.milliseconds
import com.soywiz.klock.seconds
import com.soywiz.korev.GameButton
import com.soywiz.korev.Key
import com.soywiz.korge.input.keys
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.*
import com.soywiz.korim.atlas.readAtlas
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korio.file.std.resourcesVfs

/**
 * @author Colton Daily
 * @date 7/23/2021
 */
class InputControllerTestScene : Scene() {

    override suspend fun Container.sceneInit() {
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

        keys {
            down(Key.ESCAPE) {
                launchImmediately { sceneContainer.changeTo<SceneSelector>() }
            }
        }

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


        text("Input Controller Scene")

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
}