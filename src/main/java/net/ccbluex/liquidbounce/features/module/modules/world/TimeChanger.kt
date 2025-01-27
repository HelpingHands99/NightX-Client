package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.server.S03PacketTimeUpdate

@ModuleInfo(name = "TimeChanger", spacedName = "Time Changer", category = ModuleCategory.WORLD)
class TimeChanger : Module() {
    val timeModeValue = ListValue("Time", arrayOf("Static", "Cycle"), "Static")
    val cycleSpeedValue = IntegerValue("CycleSpeed", 30, -30, 100, { timeModeValue.get().equals("cycle", true) })
    val staticTimeValue = IntegerValue("StaticTime", 18000, 0, 24000, { timeModeValue.get().equals("static", true) })
    val weatherModeValue = ListValue("Weather", arrayOf("Clear", "Rain", "NoModification"), "Clear")
    val rainStrengthValue = FloatValue("RainStrength", 1F, 0.01F, 1F, { weatherModeValue.get().equals("rain", true) })

    private var timeCycle = 0L

    override fun onEnable() {
        timeCycle = 0L
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.packet is S03PacketTimeUpdate)
            event.cancelEvent()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (timeModeValue.get().equals("static", true))
            mc.theWorld.worldTime = staticTimeValue.get().toLong()
        else {
            mc.theWorld.worldTime = timeCycle
            timeCycle += (cycleSpeedValue.get() * 10).toLong()

            if (timeCycle > 24000L) timeCycle = 0L
            if (timeCycle < 0L) timeCycle = 24000L
        }

        if (!weatherModeValue.get().equals("nomodification", true))
            mc.theWorld.setRainStrength(
                if (weatherModeValue.get().equals("clear", true)) 0F else rainStrengthValue.get()
            )
    }
}