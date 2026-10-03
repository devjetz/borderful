package dev.liqw.borderful.keybind;

import dev.liqw.borderful.Borderful;
import dev.liqw.borderful.config.BorderfulConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import dev.liqw.borderful.screen.WaypointCreateScreen;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;
import java.util.HashSet;
import java.util.Set;

import com.mojang.blaze3d.platform.InputConstants;

//? fabric {
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//? if <26.1 {
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
//? } else {
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
//? }
//? }

//? neoforge {
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
//? }

public final class BorderfulKeybind {
    private static final KeyMapping ADD_WAYPOINT = new KeyMapping(
            "key.borderful.quick_add_waypoint",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            //? if <1.21.9 {
            KeyMapping.CATEGORY_MISC
            //? } else {
            /*KeyMapping.Category.MISC
            *///? }
    );
    private static final KeyMapping OPEN_WAYPOINT_MENU = new KeyMapping(
            "key.borderful.open_waypoint_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            //? if <1.21.9 {
            KeyMapping.CATEGORY_MISC
            //? } else {
            /*KeyMapping.Category.MISC
            *///? }
    );

    private BorderfulKeybind() {}

    public static void registerFabric() {
        //? fabric {
        //? if <26.1 {
        KeyBindingHelper.registerKeyBinding(ADD_WAYPOINT);
        KeyBindingHelper.registerKeyBinding(OPEN_WAYPOINT_MENU);
        //? }
        //? if >=26.1 {
        /*KeyMappingHelper.registerKeyMapping(ADD_WAYPOINT);
        KeyMappingHelper.registerKeyMapping(OPEN_WAYPOINT_MENU);
        *///? }
        ClientTickEvents.END_CLIENT_TICK.register(BorderfulKeybind::onClientTick);
        //? }
    }

    //? neoforge {
    public static void registerNeoForge(RegisterKeyMappingsEvent event) {
        event.register(ADD_WAYPOINT);
        event.register(OPEN_WAYPOINT_MENU);
    }

    public static void onNeoForgeClientTick(ClientTickEvent.Post event) {
        onClientTick(Minecraft.getInstance());
    }
    //? }

    private static void onClientTick(Minecraft client) {
        boolean quickAdd = ADD_WAYPOINT.consumeClick();
        boolean openMenu = OPEN_WAYPOINT_MENU.consumeClick();
        if (!quickAdd && !openMenu) return;
        Player player = client.player;
        if (player == null) return;

        if (openMenu) {
            //? if <26.2 {
            client.setScreen(new WaypointCreateScreen(null));
            //? } else {
            /*client.setScreenAndShow(new WaypointCreateScreen(null));
            *///? }
            return;
        }

        if (!quickAdd) return;
        BorderfulConfig config = Borderful.getConfig();
        String serverId = Borderful.getCurrentServerId(client);
        String name = nextWaypointName(Borderful.getCurrentServerWaypoints(client));
        BlockPos position = player.blockPosition();
        BorderfulConfig.CustomWaypoint waypoint = new BorderfulConfig.CustomWaypoint(name, position);
        waypoint.serverId = serverId;
        config.customWaypoints.add(waypoint);
        Borderful.saveConfig();
        //? if <26.2 {
        client.gui.setOverlayMessage(
                Component.translatable("message.borderful.waypoint_added", name, position.getX(), position.getY(), position.getZ()),
                false
        );
        //? } else {
        /*client.gui.hud.setOverlayMessage(
                Component.translatable("message.borderful.waypoint_added", name, position.getX(), position.getY(), position.getZ()),
                false
        );
        *///? }
    }

    private static String nextWaypointName(Iterable<BorderfulConfig.CustomWaypoint> waypoints) {
        Set<String> existingNames = new HashSet<>();
        for (BorderfulConfig.CustomWaypoint waypoint : waypoints) {
            existingNames.add(waypoint.name.toLowerCase(Locale.ROOT));
        }

        int index = 1;
        while (existingNames.contains("waypoint " + index)) index++;
        return "Waypoint " + index;
    }
}
