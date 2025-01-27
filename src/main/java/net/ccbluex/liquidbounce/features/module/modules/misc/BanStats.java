package net.ccbluex.liquidbounce.features.module.modules.misc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification;
import net.ccbluex.liquidbounce.utils.misc.HttpUtils;
import net.ccbluex.liquidbounce.utils.timer.MSTimer;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.IntegerValue;

@ModuleInfo(name = "BanStats", spacedName = "Ban Stats", category = ModuleCategory.MISC)
public class BanStats extends Module {

    // no u
    private static final String API_PUNISHMENT = aB("68747470733a2f2f6170692e706c616e636b652e696f2f6879706978656c2f76312f70756e6973686d656e745374617473");
    public static int WATCHDOG_BAN_LAST_MIN = 0;
    public static int LAST_TOTAL_STAFF = -1;
    public static int STAFF_BAN_LAST_MIN = 0;
    public final BoolValue alertValue = new BoolValue("Alert", true);
    public final BoolValue serverCheckValue = new BoolValue("ServerCheck", true);
    public final IntegerValue alertTimeValue = new IntegerValue("Alert-Time", 10, 1, 50, " seconds");
    private String checkTag = "Idle...";

    public BanStats() {
        (new Thread("Hypixel-BanChecker") {
            public void run() {
                MSTimer checkTimer = new MSTimer();
                while (true) {
                    if (checkTimer.hasTimePassed(60000L)) {
                        try {
                            String apiContent = HttpUtils.get(API_PUNISHMENT);
                            final JsonObject jsonObject = new JsonParser().parse(apiContent).getAsJsonObject();
                            if (jsonObject.get("success").getAsBoolean() && jsonObject.has("record")) {
                                JsonObject objectAPI = jsonObject.get("record").getAsJsonObject();
                                WATCHDOG_BAN_LAST_MIN = objectAPI.get("watchdog_lastMinute").getAsInt();
                                int staffBanTotal = objectAPI.get("staff_total").getAsInt();

                                if (staffBanTotal < LAST_TOTAL_STAFF)
                                    staffBanTotal = LAST_TOTAL_STAFF;

                                if (LAST_TOTAL_STAFF == -1)
                                    LAST_TOTAL_STAFF = staffBanTotal;
                                else {
                                    STAFF_BAN_LAST_MIN = staffBanTotal - LAST_TOTAL_STAFF;
                                    LAST_TOTAL_STAFF = staffBanTotal;
                                }

                                checkTag = STAFF_BAN_LAST_MIN + "";

                                if (LiquidBounce.moduleManager.getModule(BanStats.class).getState() && alertValue.get() && mc.thePlayer != null && (!serverCheckValue.get() || isOnHypixel()))
                                    if (STAFF_BAN_LAST_MIN > 0)
                                        LiquidBounce.hud.addNotification(new Notification("Staffs banned " + STAFF_BAN_LAST_MIN + " players in the last minute!", STAFF_BAN_LAST_MIN > 3 ? Notification.Type.ERROR : Notification.Type.WARNING, alertTimeValue.get() * 1000L));
                                    else
                                        LiquidBounce.hud.addNotification(new Notification("Staffs didn't ban any player in the last minute.", Notification.Type.SUCCESS, alertTimeValue.get() * 1000L));

                                // watchdog ban doesnt matter, open an issue if you want to add it.
                            }
                        } catch (Exception e) {
                            e.printStackTrace();

                            if (LiquidBounce.moduleManager.getModule(BanStats.class).getState() && alertValue.get() && mc.thePlayer != null && (!serverCheckValue.get() || isOnHypixel()))
                                LiquidBounce.hud.addNotification(new Notification("An error has occurred.", Notification.Type.ERROR, 1000L));
                        }
                        checkTimer.reset();
                    }
                }
            }
        }).start();
    }

    public static String aB(String str) { // :trole:
        String result = "";
        char[] charArray = str.toCharArray();
        for (int i = 0; i < charArray.length; i = i + 2) {
            String st = "" + charArray[i] + "" + charArray[i + 1];
            char ch = (char) Integer.parseInt(st, 16);
            result = result + ch;
        }
        return result;
    }

    public boolean isOnHypixel() {
        return !mc.isIntegratedServerRunning() && mc.getCurrentServerData().serverIP.contains("hypixel.net");
    }

    @Override
    public String getTag() {
        return checkTag;
    }

}