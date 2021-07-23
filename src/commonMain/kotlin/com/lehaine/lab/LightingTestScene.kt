package com.lehaine.lab

import com.lehaine.kiwi.korge.InputController
import com.lehaine.kiwi.korge.getByPrefix
import com.lehaine.kiwi.korge.view.addToLayer
import com.lehaine.kiwi.korge.view.enhancedSprite
import com.lehaine.kiwi.korge.view.getEnhancedSpriteAnimation
import com.lehaine.kiwi.korge.view.layers
import com.lehaine.kiwi.korge.view.ldtk.ldtkMapView
import com.lehaine.kiwi.korge.view.ldtk.toLDtkLevel
import com.soywiz.klock.milliseconds
import com.soywiz.korev.GameButton
import com.soywiz.korev.Key
import com.soywiz.korge.input.keys
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.*
import com.soywiz.korim.atlas.readAtlas
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korma.geom.Anchor

/**
 * @author Colton Daily
 * @date 7/23/2021
 */
class LightingTestScene : Scene() {

    override suspend fun Container.sceneInit() {
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


        text("Lighting Scene").addToLayer(layers, 3)

        keys {
            down(Key.ESCAPE) {
                launchImmediately { sceneContainer.changeTo<SceneSelector>() }
            }
        }

        addUpdater {
            controller.update()
            if (ca1.down(Input.MoveHorizontal)) {
                sprite1.x += ca1.strength(Input.MoveHorizontal) * 3
            }
        }
    }
}