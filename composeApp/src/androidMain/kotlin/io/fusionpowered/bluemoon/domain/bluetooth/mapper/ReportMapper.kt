package io.fusionpowered.bluemoon.domain.bluetooth.mapper

import io.fusionpowered.bluemoon.domain.controller.model.ControllerState
import io.fusionpowered.bluemoon.domain.keyboard.model.KeyboardState
import io.fusionpowered.bluemoon.domain.keyboard.model.KeyboardState.Key
import io.fusionpowered.bluemoon.domain.touchpad.model.TouchpadState

fun KeyboardState.toReport(): ByteArray =
    ByteArray(8).let { report ->
        var modifierByte = 0
        activeModifiers.forEach { mod ->
            modifierByte = modifierByte or when (mod) {
                Key.Modifier.LeftControl -> 0x01
                Key.Modifier.LeftShift -> 0x02
                Key.Modifier.LeftAlt -> 0x04
                Key.Modifier.LeftMeta -> 0x08
                Key.Modifier.RightControl -> 0x10
                Key.Modifier.RightShift -> 0x20
                Key.Modifier.RightAlt -> 0x40
            }
        }
        report[0] = modifierByte.toByte()

        pressedKeys.take(6).forEachIndexed { index, key ->
            val code: Int = when (key) {
                is Key.Modifier -> 0 // Already handled in Byte 0

                is Key.Letter -> when (key) {
                    Key.Letter.A -> 0x04; Key.Letter.B -> 0x05; Key.Letter.C -> 0x06
                    Key.Letter.D -> 0x07; Key.Letter.E -> 0x08; Key.Letter.F -> 0x09
                    Key.Letter.G -> 0x0A; Key.Letter.H -> 0x0B; Key.Letter.I -> 0x0C
                    Key.Letter.J -> 0x0D; Key.Letter.K -> 0x0E; Key.Letter.L -> 0x0F
                    Key.Letter.M -> 0x10; Key.Letter.N -> 0x11; Key.Letter.O -> 0x12
                    Key.Letter.P -> 0x13; Key.Letter.Q -> 0x14; Key.Letter.R -> 0x15
                    Key.Letter.S -> 0x16; Key.Letter.T -> 0x17; Key.Letter.U -> 0x18
                    Key.Letter.V -> 0x19; Key.Letter.W -> 0x1A; Key.Letter.X -> 0x1B
                    Key.Letter.Y -> 0x1C; Key.Letter.Z -> 0x1D
                }

                is Key.Number -> when (key) {
                    Key.Number.Key1 -> 0x1E; Key.Number.Key2 -> 0x1F; Key.Number.Key3 -> 0x20
                    Key.Number.Key4 -> 0x21; Key.Number.Key5 -> 0x22; Key.Number.Key6 -> 0x23
                    Key.Number.Key7 -> 0x24; Key.Number.Key8 -> 0x25; Key.Number.Key9 -> 0x26
                    Key.Number.Key0 -> 0x27
                }

                is Key.Symbol -> when (key) {
                    Key.Symbol.Minus -> 0x2D; Key.Symbol.Equal -> 0x2E
                    Key.Symbol.LeftBrace -> 0x2F; Key.Symbol.RightBrace -> 0x30
                    Key.Symbol.Backslash -> 0x31; Key.Symbol.Semicolon -> 0x33
                    Key.Symbol.Apostrophe -> 0x34; Key.Symbol.Grave -> 0x35
                    Key.Symbol.Comma -> 0x36; Key.Symbol.Dot -> 0x37
                    Key.Symbol.Slash -> 0x38
                }

                is Key.Function -> when (key) {
                    Key.Function.Enter -> 0x28; Key.Function.Escape -> 0x29
                    Key.Function.Backspace -> 0x2A; Key.Function.Tab -> 0x2B
                    Key.Function.Space -> 0x2C; Key.Function.CapsLock -> 0x39
                    Key.Function.Up -> 0x52; Key.Function.Down -> 0x51
                    Key.Function.Left -> 0x50; Key.Function.Right -> 0x4F
                    Key.Function.Delete -> 0x4C; Key.Function.Home -> 0x4A
                    Key.Function.End -> 0x4D
                }
            }
            report[index + 2] = code.toByte()
        }
        report
    }


fun TouchpadState.toReport(): ByteArray =
    ByteArray(4).let { report ->
        report[0] = run {
            var buttons = 0
            if (leftButton) buttons = buttons or (1 shl 0)
            if (rightButton) buttons = buttons or (1 shl 1)
            if (middleButton) buttons = buttons or (1 shl 2)
            buttons.toByte()
        }
        report[1] = deltaX.coerceIn(-127, 127).toByte()
        report[2] = deltaY.coerceIn(-127, 127).toByte()
        report[3] = wheel.coerceIn(-127, 127).toByte()

        report
    }


fun ControllerState.toReport() =
    ByteArray(14).let { report ->
        leftStickX.to16bit().let {
            report[0] = (it and 0xFF).toByte()
            report[1] = ((it shr 8) and 0xFF).toByte()
        }
        leftStickY.to16bit().let {
            report[2] = (it and 0xFF).toByte()
            report[3] = ((it shr 8) and 0xFF).toByte()
        }
        rightStickX.to16bit().let {
            report[4] = (it and 0xFF).toByte()
            report[5] = ((it shr 8) and 0xFF).toByte()
        }
        rightStickY.to16bit().let {
            report[6] = (it and 0xFF).toByte()
            report[7] = ((it shr 8) and 0xFF).toByte()
        }

        //R2 and L2 have to be sent both as axis and buttons
        report[8] = r2Axis.to8bit().toByte()
        report[9] = l2Axis.to8bit().toByte()

        report[10] = run {
            var buttons = 0
            if (a) buttons = buttons or (1 shl 0)
            if (b) buttons = buttons or (1 shl 1)
            if (x) buttons = buttons or (1 shl 3)
            if (y) buttons = buttons or (1 shl 4)
            if (l1) buttons = buttons or (1 shl 6)
            if (r1) buttons = buttons or (1 shl 7)
            buttons.toByte()
        }

        report[11] = run {
            var buttons = 0
            if (l2Button) buttons = buttons or (1 shl 0)
            if (r2Button) buttons = buttons or (1 shl 1)
            if (select) buttons = buttons or (1 shl 2)
            if (start) buttons = buttons or (1 shl 3)
            if (guide) buttons = buttons or (1 shl 4)
            if (l3) buttons = buttons or (1 shl 5)
            if (r3) buttons = buttons or (1 shl 6)
            buttons.toByte()
        }

        report[12] = run {
            val up = dpadY < -0.5f
            val down = dpadY > 0.5f
            val left = dpadX < -0.5f
            val right = dpadX > 0.5f

            val hat = when {
                up && !left && !right -> 0    // North (0)
                up && right -> 1              // North-East (1)
                right && !up && !down -> 2    // East (2)
                down && right -> 3            // South-East (3)
                down && !left && !right -> 4  // South (4)
                down && left -> 5             // South-West (5)
                left && !up && !down -> 6     // West (6)
                up && left -> 7               // North-West (7)
                else -> 15                    // Center (Values 8-15 are "Null/Released")
            }
            (hat and 0x0F).toByte()
        }

        //Extra Padding byte
        report[13] = 0x00.toByte()
        report
    }

private fun Float.to16bit(): Int =
    (((coerceIn(-1f, 1f) + 1f) / 2f) * 65535).toInt()

private fun Float.to8bit(): Int =
    (coerceIn(0f, 1f) * 255).toInt()
