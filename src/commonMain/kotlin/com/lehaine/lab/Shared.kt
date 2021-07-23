package com.lehaine.lab

import com.lehaine.ldtk.LDtkProject

/**
 * @author Colton Daily
 * @date 7/23/2021
 */
@LDtkProject("world.ldtk", name = "World")
class _World

fun create(num: Int, createParticle: (index: Int) -> Unit) {
    for (i in 0 until num) {
        createParticle(i)
    }
}

sealed class Input {
    object MoveHorizontal : Input()
    object MoveVertical : Input()
}