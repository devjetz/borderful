package dev.liqw.borderful;

//~ !skip_replace

import dev.liqw.borderful.config.BorderfulConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
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
    }
    //? }

    //? neoforge {
    /*public Borderful() {
        initialize();

        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () ->
                //~ if <=1.21.10 'AutoConfigClient' -> 'AutoConfig'
                (client, parent) -> AutoConfigClient.getConfigScreen(BorderfulConfig.class, parent).get()
        );
    }
    *///? }

    public static BorderfulConfig getConfig() {
        return AutoConfig.getConfigHolder(BorderfulConfig.class).getConfig();
    }
}