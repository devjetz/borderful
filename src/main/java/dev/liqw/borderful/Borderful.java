package dev.liqw.borderful;

//~ !skip_replace

import dev.liqw.borderful.config.BorderfulConfig;
import dev.liqw.borderful.keybind.BorderfulKeybind;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.InteractionResult;

//? fabric
import net.fabricmc.api.ClientModInitializer;

//? neoforge {
/*//~ if <=1.21.10 'AutoConfigClient' -> 'AutoConfig'
import me.shedaniel.autoconfig.AutoConfigClient;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
*///? }

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

//? neoforge
//@Mod("borderful")
public class Borderful /*? fabric { */ implements ClientModInitializer /*? } */ {
    public static final String MOD_ID = "borderful";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private void initialize() {
        migrateConfig();
        ConfigHolder<BorderfulConfig> holder = AutoConfig.register(BorderfulConfig.class, GsonConfigSerializer::new);

        // temp fix, validatePostLoad isn't called when saving
        holder.registerSaveListener(((configHolder, config) -> {
            config.validatePostLoad();
            return InteractionResult.SUCCESS;
        }));
    }

    private void migrateConfig() {
        Path legacyConfig = Path.of("config", "locator-border.json");
        Path currentConfig = Path.of("config", MOD_ID + ".json");
        if (!Files.exists(legacyConfig) || Files.exists(currentConfig)) return;

        try {
            Files.copy(legacyConfig, currentConfig);
            LOGGER.info("Migrated configuration from {} to {}", legacyConfig, currentConfig);
        } catch (IOException exception) {
            LOGGER.warn("Failed to migrate configuration from {} to {}", legacyConfig, currentConfig, exception);
        }
    }

    //? fabric {
    @Override
    public void onInitializeClient() {
        initialize();
        BorderfulKeybind.registerFabric();
    }
    //? }

    //? neoforge {
    /*public Borderful() {
        initialize();
        ModLoadingContext.get().getActiveContainer().getEventBus().addListener(BorderfulKeybind::registerNeoForge);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(BorderfulKeybind::onNeoForgeClientTick);

        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () ->
                //~ if <=1.21.10 'AutoConfigClient' -> 'AutoConfig'
                (client, parent) -> AutoConfigClient.getConfigScreen(BorderfulConfig.class, parent).get()
        );
    }
    *///? }

    public static BorderfulConfig getConfig() {
        return AutoConfig.getConfigHolder(BorderfulConfig.class).getConfig();
    }

    public static void saveConfig() {
        AutoConfig.getConfigHolder(BorderfulConfig.class).save();
    }

    public static String getCurrentServerId(Minecraft minecraft) {
        ServerData serverData = minecraft.getCurrentServer();
        if (serverData != null) {
            return "server:" + serverData.ip.trim().toLowerCase(Locale.ROOT);
        }

        IntegratedServer integratedServer = minecraft.getSingleplayerServer();
        if (integratedServer == null) return null;

        Path worldPath = integratedServer.getWorldPath(LevelResource.ROOT).normalize();
        Path worldFolder = worldPath.getFileName();
        return "singleplayer:" + (worldFolder == null ? worldPath.toString() : worldFolder);
    }

    public static List<BorderfulConfig.CustomWaypoint> getCurrentServerWaypoints(Minecraft minecraft) {
        String serverId = getCurrentServerId(minecraft);
        if (serverId == null) return List.of();

        BorderfulConfig config = getConfig();
        boolean migratedLegacyWaypoints = false;
        for (BorderfulConfig.CustomWaypoint waypoint : config.customWaypoints) {
            if (waypoint.serverId == null || waypoint.serverId.isBlank()) {
                waypoint.serverId = serverId;
                migratedLegacyWaypoints = true;
            }
        }
        if (migratedLegacyWaypoints) saveConfig();

        return config.customWaypoints.stream()
                .filter(waypoint -> Objects.equals(waypoint.serverId, serverId))
                .toList();
    }
}