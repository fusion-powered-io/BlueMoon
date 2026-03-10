package io.fusionpowered.bluemoon.domain.bluetooth.model


data class BluetoothDevice(
    val name: String,
    val mac: String,
    val majorClass: MajorClass,
) {

    enum class MajorClass {
        COMPUTER,
        PHONE,
        NETWORKING,
        AUDIO_VIDEO,
        PERIPHERAL,
        IMAGING,
        WEARABLE,
        TOY,
        HEALTH,
        UNCATEGORIZED,
    }

}
