package io.fusionpowered.bluemoon.domain.controller

interface ControllerInputHandler {

    fun handle(keyCode: String): Unit

}