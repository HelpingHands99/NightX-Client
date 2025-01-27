package net.ccbluex.liquidbounce.file.configs;

import com.google.gson.*;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.special.AntiForge;
import net.ccbluex.liquidbounce.features.special.BungeeCordSpoof;
import net.ccbluex.liquidbounce.features.special.MacroManager;
import net.ccbluex.liquidbounce.file.FileConfig;
import net.ccbluex.liquidbounce.file.FileManager;
import net.ccbluex.liquidbounce.ui.client.GuiBackground;
import net.ccbluex.liquidbounce.ui.client.altmanager.menus.altgenerator.GuiTheAltening;
import net.ccbluex.liquidbounce.utils.EntityUtils;
import net.ccbluex.liquidbounce.value.Value;

import java.io.*;
import java.util.Iterator;
import java.util.Map;

public class ValuesConfig extends FileConfig {

    /**
     * Constructor of config
     *
     * @param file of config
     */
    public ValuesConfig(final File file) {
        super(file);
    }

    /**
     * Load config from file
     *
     * @throws IOException
     */
    @Override
    protected void loadConfig() throws IOException {
        final JsonElement jsonElement = new JsonParser().parse(new BufferedReader(new FileReader(getFile())));

        if (jsonElement instanceof JsonNull)
            return;

        final JsonObject jsonObject = (JsonObject) jsonElement;

        final Iterator<Map.Entry<String, JsonElement>> iterator = jsonObject.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, JsonElement> entry = iterator.next();

            if (entry.getKey().equalsIgnoreCase("CommandPrefix")) {
                LiquidBounce.commandManager.setPrefix(entry.getValue().getAsCharacter());
            } else if (entry.getKey().equalsIgnoreCase("ShowRichPresence")) {
                LiquidBounce.clientRichPresence.setShowRichPresenceValue(entry.getValue().getAsBoolean());
            } else if (entry.getKey().equalsIgnoreCase("targets")) {
                JsonObject jsonValue = (JsonObject) entry.getValue();

                if (jsonValue.has("TargetPlayer"))
                    EntityUtils.targetPlayer = jsonValue.get("TargetPlayer").getAsBoolean();
                if (jsonValue.has("TargetMobs"))
                    EntityUtils.targetMobs = jsonValue.get("TargetMobs").getAsBoolean();
                if (jsonValue.has("TargetAnimals"))
                    EntityUtils.targetAnimals = jsonValue.get("TargetAnimals").getAsBoolean();
                if (jsonValue.has("TargetInvisible"))
                    EntityUtils.targetInvisible = jsonValue.get("TargetInvisible").getAsBoolean();
                if (jsonValue.has("TargetDead"))
                    EntityUtils.targetDead = jsonValue.get("TargetDead").getAsBoolean();
            } else if (entry.getKey().equalsIgnoreCase("macros")) {
                JsonArray jsonValue = entry.getValue().getAsJsonArray();
                for (final JsonElement macroElement : jsonValue) {
                    JsonObject macroObject = macroElement.getAsJsonObject();
                    JsonElement keyValue = macroObject.get("key");
                    JsonElement commandValue = macroObject.get("command");

                    MacroManager.INSTANCE.addMacro(keyValue.getAsInt(), commandValue.getAsString());
                }
            } else if (entry.getKey().equalsIgnoreCase("features")) {
                JsonObject jsonValue = (JsonObject) entry.getValue();

                if (jsonValue.has("VanillaSpoof"))
                    AntiForge.enabled = jsonValue.get("VanillaSpoof").getAsBoolean();
                if (jsonValue.has("FMLSpoof"))
                    AntiForge.blockFML = jsonValue.get("FMLSpoof").getAsBoolean();
                if (jsonValue.has("ProxySpoof"))
                    AntiForge.blockProxyPacket = jsonValue.get("ProxySpoof").getAsBoolean();
                if (jsonValue.has("PayloadsSpoof"))
                    AntiForge.blockPayloadPackets = jsonValue.get("PayloadsSpoof").getAsBoolean();
                if (jsonValue.has("BungeeExploit"))
                    BungeeCordSpoof.enabled = jsonValue.get("BungeeExploit").getAsBoolean();
            } else if (entry.getKey().equalsIgnoreCase("thealtening")) {
                JsonObject jsonValue = (JsonObject) entry.getValue();

                if (jsonValue.has("API-Key"))
                    GuiTheAltening.Companion.setApiKey(jsonValue.get("API-Key").getAsString());
            } else if (entry.getKey().equalsIgnoreCase("Background")) {
                JsonObject jsonValue = (JsonObject) entry.getValue();

                if (jsonValue.has("Enabled"))
                    GuiBackground.Companion.setEnabled(jsonValue.get("Enabled").getAsBoolean());

                if (jsonValue.has("Particles"))
                    GuiBackground.Companion.setParticles(jsonValue.get("Particles").getAsBoolean());
            } else {
                final Module module = LiquidBounce.moduleManager.getModule(entry.getKey());

                if (module != null) {
                    final JsonObject jsonModule = (JsonObject) entry.getValue();

                    for (final Value moduleValue : module.getValues()) {
                        final JsonElement element = jsonModule.get(moduleValue.getName());

                        if (element != null) moduleValue.fromJson(element);
                    }
                }
            }
        }
    }

    /**
     * Save config to file
     *
     * @throws IOException
     */
    @Override
    protected void saveConfig() throws IOException {
        final JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("CommandPrefix", LiquidBounce.commandManager.getPrefix());
        jsonObject.addProperty("ShowRichPresence", LiquidBounce.clientRichPresence.getShowRichPresenceValue());

        final JsonObject jsonTargets = new JsonObject();
        jsonTargets.addProperty("TargetPlayer", EntityUtils.targetPlayer);
        jsonTargets.addProperty("TargetMobs", EntityUtils.targetMobs);
        jsonTargets.addProperty("TargetAnimals", EntityUtils.targetAnimals);
        jsonTargets.addProperty("TargetInvisible", EntityUtils.targetInvisible);
        jsonTargets.addProperty("TargetDead", EntityUtils.targetDead);
        jsonObject.add("targets", jsonTargets);

        final JsonArray jsonMacros = new JsonArray();
        MacroManager.INSTANCE.getMacroMapping().forEach((k, v) -> {
            final JsonObject jsonMacro = new JsonObject();
            jsonMacro.addProperty("key", k);
            jsonMacro.addProperty("command", v);
            jsonMacros.add(jsonMacro);
        });
        jsonObject.add("macros", jsonMacros);

        final JsonObject jsonFeatures = new JsonObject();
        jsonFeatures.addProperty("VanillaSpoof", AntiForge.enabled);
        jsonFeatures.addProperty("FMLSpoof", AntiForge.blockFML);
        jsonFeatures.addProperty("ProxySpoof", AntiForge.blockProxyPacket);
        jsonFeatures.addProperty("PayloadsSpoof", AntiForge.blockPayloadPackets);
        jsonFeatures.addProperty("BungeeExploit", BungeeCordSpoof.enabled);
        jsonObject.add("features", jsonFeatures);

        final JsonObject theAlteningObject = new JsonObject();
        theAlteningObject.addProperty("API-Key", GuiTheAltening.Companion.getApiKey());
        jsonObject.add("thealtening", theAlteningObject);

        final JsonObject backgroundObject = new JsonObject();
        backgroundObject.addProperty("Enabled", GuiBackground.Companion.getEnabled());
        backgroundObject.addProperty("Particles", GuiBackground.Companion.getParticles());
        jsonObject.add("Background", backgroundObject);

        LiquidBounce.moduleManager.getModules().stream().filter(module -> !module.getValues().isEmpty()).forEach(module -> {
            final JsonObject jsonModule = new JsonObject();
            module.getValues().forEach(value -> jsonModule.add(value.getName(), value.toJson()));
            jsonObject.add(module.getName(), jsonModule);
        });

        final PrintWriter printWriter = new PrintWriter(new FileWriter(getFile()));
        printWriter.println(FileManager.PRETTY_GSON.toJson(jsonObject));
        printWriter.close();
    }
}
