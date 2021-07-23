package com.lehaine.lab

import com.soywiz.korev.Key
import com.soywiz.korge.input.keys
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.text
import com.soywiz.korio.async.launchImmediately

class SceneSelector : Scene() {

    override suspend fun Container.sceneInit() {
        text(
            """
            SELECT SCENE (1-9)
            1: Input Controller Test Scene
            2: Enhanced Sprite Test Scene
            3: Lighting Test Scene
            4: CPU Particle Scene
            5: Instanced Particle Scene
            Esc: navigate back to scene selector / close
        """.trimIndent()
        )
        keys {
            down(Key.N1) {
                launchImmediately {
                    sceneContainer.changeTo<InputControllerTestScene>()
                }
            }
            down(Key.N2) {
                launchImmediately {
                    sceneContainer.changeTo<EnhancedSpritesTestScene>()
                }
            }
            down(Key.N3) {
                launchImmediately {
                    sceneContainer.changeTo<LightingTestScene>()
                }
            }
            down(Key.N4) {
                launchImmediately {
                    sceneContainer.changeTo<CPUParticleScene>()
                }
            }
            down(Key.N5) {
                launchImmediately {
                    sceneContainer.changeTo<InstancedParticleScene>()
                }
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
}