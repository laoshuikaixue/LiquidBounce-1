package net.ccbluex.liquidbounce.features.module.modules.misc.debugRecorder.modes

import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.event.repeatable
import net.ccbluex.liquidbounce.features.module.modules.misc.debugRecorder.ModuleDebugRecorder
import net.ccbluex.liquidbounce.utils.aiming.RotationManager
import net.ccbluex.liquidbounce.utils.combat.shouldBeAttacked
import net.ccbluex.liquidbounce.utils.entity.box
import net.ccbluex.liquidbounce.utils.entity.eyes
import net.ccbluex.liquidbounce.utils.entity.lastRotation
import net.ccbluex.liquidbounce.utils.entity.rotation

object AimDebugRecorder : ModuleDebugRecorder.DebugRecorderMode("Aim") {

    val repeatable = repeatable {
        val turnSpeed = RotationManager.rotationDifference(player.rotation, player.lastRotation)

        recordPacket(JsonObject().apply {
            addProperty("TurnSpeed", turnSpeed)

            world.entities.filter {
                it.shouldBeAttacked() && it.distanceTo(player) < 10.0f
            }.minByOrNull {
                it.distanceTo(player)
            }?.let {
                addProperty("Distance", player.distanceTo(it))
                val rotation = RotationManager.makeRotation(it.box.center, player.eyes)
                addProperty("Difference", RotationManager.rotationDifference(player.rotation, rotation))
            }
        })
    }

}
