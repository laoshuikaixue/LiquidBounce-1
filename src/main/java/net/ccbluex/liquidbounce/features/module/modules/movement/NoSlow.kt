/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.LiquidBounce.moduleManager
import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.SlowDownEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.item.*
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

@ModuleInfo(name = "NoSlow", description = "Cancels slowness effects caused by Soul Sand and using items.",
        category = ModuleCategory.MOVEMENT)
class NoSlow : Module() {

    // Highly customizable values

    private val blockForwardMultiplier = FloatValue("BlockForwardMultiplier", 1f, 0.2F, 1f)
    private val blockStrafeMultiplier = FloatValue("BlockStrafeMultiplier", 1f, 0.2F, 1f)

    private val consumeForwardMultiplier = FloatValue("ConsumeForwardMultiplier", 1f, 0.2F, 1f)
    private val consumeStrafeMultiplier = FloatValue("ConsumeStrafeMultiplier", 1f, 0.2F, 1f)

    private val bowForwardMultiplier = FloatValue("BowForwardMultiplier", 1f, 0.2F, 1f)
    private val bowStrafeMultiplier = FloatValue("BowStrafeMultiplier", 1f, 0.2F, 1f)

    // NCP mode
    private val packet = BoolValue("Packet", true)

    // Blocks
    val soulsandValue = BoolValue("Soulsand", true)
    val liquidPushValue = BoolValue("LiquidPush", true)

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val thePlayer = mc.thePlayer ?: return
        val heldItem = thePlayer.heldItem ?: return

        if (heldItem.item !is ItemSword || !isMoving)
            return

        val aura = moduleManager[KillAura::class.java] as KillAura
        if (!thePlayer.isBlocking && !aura.blockStatus)
            return

        if (this.packet.get()) {
            when (event.eventState) {
                EventState.PRE -> {
                    val digging = C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos(0, 0, 0), EnumFacing.DOWN)
                    mc.netHandler.addToSendQueue(digging)
                }
                EventState.POST -> {
                    val blockPlace = C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, mc.thePlayer.inventory.getCurrentItem(), 0f, 0f, 0f)
                    mc.netHandler.addToSendQueue(blockPlace)
                }
            }
        }
    }

    @EventTarget
    fun onSlowDown(event: SlowDownEvent) {
        val heldItem = mc.thePlayer.heldItem?.item

        event.forward = getMultiplier(heldItem, true)
        event.strafe = getMultiplier(heldItem, false)
    }

    private fun getMultiplier(item: Item?, isForward: Boolean): Float {
        return when (item) {
            is ItemFood, is ItemPotion, is ItemBucketMilk ->
                if (isForward) this.consumeForwardMultiplier.get() else this.consumeStrafeMultiplier.get()

            is ItemSword ->
                if (isForward) this.blockForwardMultiplier.get() else this.blockStrafeMultiplier.get()

            is ItemBow ->
                if (isForward) this.bowForwardMultiplier.get() else this.bowStrafeMultiplier.get()

            else -> 0.2F
        }
    }

}