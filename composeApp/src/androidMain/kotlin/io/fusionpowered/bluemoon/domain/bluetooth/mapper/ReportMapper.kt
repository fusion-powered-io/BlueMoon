package io.fusionpowered.bluemoon.domain.bluetooth.mapper

import io.fusionpowered.bluemoon.domain.controller.model.ControllerState

fun ControllerState.toReport() =
    ByteArray(14).let { reportData ->
        leftStickX.to16bit().let {
            reportData[0] = (it and 0xFF).toByte()
            reportData[1] = ((it shr 8) and 0xFF).toByte()
        }
        leftStickY.to16bit().let {
            reportData[2] = (it and 0xFF).toByte()
            reportData[3] = ((it shr 8) and 0xFF).toByte()
        }
        rightStickX.to16bit().let {
            reportData[4] = (it and 0xFF).toByte()
            reportData[5] = ((it shr 8) and 0xFF).toByte()
        }
        rightStickY.to16bit().let {
            reportData[6] = (it and 0xFF).toByte()
            reportData[7] = ((it shr 8) and 0xFF).toByte()
        }

        //R2 and L2 have to be sent both as axis and buttons
        reportData[8] = r2Axis.to8bit().toByte()
        reportData[9] = l2Axis.to8bit().toByte()

        reportData[10] = run {
            var buttons = 0
            if (a) buttons = buttons or (1 shl 0)
            if (b) buttons = buttons or (1 shl 1)
            if (x) buttons = buttons or (1 shl 3)
            if (y) buttons = buttons or (1 shl 4)
            if (l1) buttons = buttons or (1 shl 6)
            if (r1) buttons = buttons or (1 shl 7)
            buttons.toByte()
        }

        reportData[11] = run {
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

        reportData[12] = run {
            val up = dpadY < -0.5f
            val down = dpadY > 0.5f
            val left = dpadX < -0.5f
            val right = dpadX > 0.5f
            val hat = when {
                up && !left && !right -> 1    // North
                up && right -> 2              // North-East
                right && !up && !down -> 3    // East
                down && right -> 4            // South-East
                down && !left && !right -> 5  // South
                down && left -> 6             // South-West
                left && !up && !down -> 7     // West
                up && left -> 8               // North-West
                else -> 0                     // Center / Released
            }
            (hat and 0x0F).toByte()
        }

        //Extra Padding byte
        reportData[13] = 0x00.toByte()
        reportData
    }


private fun Float.to16bit(): Int =
    (((coerceIn(-1f, 1f) + 1f) / 2f) * 65535).toInt()

private fun Float.to8bit(): Int =
    (coerceIn(0f, 1f) * 255).toInt()