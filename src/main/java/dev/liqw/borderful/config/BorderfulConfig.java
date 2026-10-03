package dev.liqw.borderful.config;

import dev.liqw.borderful.Borderful;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.ThreadLocalRandom;

@Config(name = Borderful.MOD_ID)
public class BorderfulConfig implements ConfigData {
    @ConfigEntry.Gui.Excluded
    public transient Map<String, PlayerSpecificConfig.Override> overrideCache = new HashMap<>();

    @ConfigEntry.Gui.Tooltip
    public boolean enabled = true;

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public Waypoint waypoint = new Waypoint();

    @ConfigEntry.Category("custom_waypoints")
    public List<CustomWaypoint> customWaypoints = new ArrayList<>();

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("overrides")
    public List<PlayerSpecificConfig> overrides = new ArrayList<>();

    @ConfigEntry.Category("miscellaneous")
    @ConfigEntry.Gui.CollapsibleObject
    public CardinalDirections compass = new CardinalDirections();

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("miscellaneous")
    public boolean forceWaypoints = false;

    @ConfigEntry.Category("miscellaneous")
    public boolean animations = true;

    @ConfigEntry.Gui.Excluded
    public transient Map<UUID, CustomWaypoint> customWaypointCache = new HashMap<>();

    @Override
    public void validatePostLoad() {
        if (overrides == null) overrides = new ArrayList<>();
        if (customWaypoints == null) customWaypoints = new ArrayList<>();

        overrides.removeIf(entry -> entry.name == null || entry.name.isBlank());
        overrideCache = overrides.stream()
                .collect(Collectors.toMap(e -> e.name.toLowerCase(), e -> e.override));

        customWaypoints.removeIf(Objects::isNull);
        Set<UUID> waypointIds = new HashSet<>();
        for (CustomWaypoint waypoint : customWaypoints) {
            if (waypoint.id == null || !waypointIds.add(waypoint.id)) {
                do {
                    waypoint.id = UUID.randomUUID();
                } while (!waypointIds.add(waypoint.id));
            }
            if (waypoint.name == null || waypoint.name.isBlank()) waypoint.name = "Waypoint";
        }
        customWaypointCache = customWaypoints.stream()
                .collect(Collectors.toMap(CustomWaypoint::getRuntimeId, waypoint -> waypoint));
    }

    public static class CustomWaypoint {
        @ConfigEntry.Gui.Excluded
        public UUID id = UUID.randomUUID();

        public String name = "Waypoint";
        public int x;
        public int y;
        public int z;

        @ConfigEntry.ColorPicker
        @ConfigEntry.Gui.Tooltip
        public int color = randomColor();

        @ConfigEntry.Gui.Tooltip
        public String serverId;

        @ConfigEntry.Gui.Tooltip
        public boolean enabled = true;

        public CustomWaypoint() {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft != null) {
                serverId = Borderful.getCurrentServerId(minecraft);
                if (minecraft.player != null) setPosition(minecraft.player.blockPosition());
            }
        }

        public CustomWaypoint(String name, BlockPos position) {
            this();
            this.name = name;
            setPosition(position);
        }

        private void setPosition(BlockPos position) {
            x = position.getX();
            y = position.getY();
            z = position.getZ();
        }

        public static int randomColor() {
            float hue = ThreadLocalRandom.current().nextFloat() * 6.0f;
            float secondary = 1.0f - Math.abs(hue % 2.0f - 1.0f);
            float red = 0.0f;
            float green = 0.0f;
            float blue = 0.0f;

            if (hue < 1.0f) {
                red = 1.0f;
                green = secondary;
            } else if (hue < 2.0f) {
                red = secondary;
                green = 1.0f;
            } else if (hue < 3.0f) {
                green = 1.0f;
                blue = secondary;
            } else if (hue < 4.0f) {
                green = secondary;
                blue = 1.0f;
            } else if (hue < 5.0f) {
                red = secondary;
                blue = 1.0f;
            } else {
                red = 1.0f;
                blue = secondary;
            }

            int r = 0x50 + Math.round(red * 0xAF);
            int g = 0x50 + Math.round(green * 0xAF);
            int b = 0x50 + Math.round(blue * 0xAF);
            return (r << 16) | (g << 8) | b;
        }

        public UUID getRuntimeId() {
            return id;
        }
    }

    public static class Waypoint {
        public enum Color {
            Waypoint, Team,
        }

        @ConfigEntry.Gui.Tooltip
        public int inset = 4;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public Color color = Color.Waypoint;

        @ConfigEntry.Gui.Tooltip
        public boolean arrows = false;

        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public PlayerFace playerFace = new PlayerFace();

        @ConfigEntry.Gui.CollapsibleObject
        public FocusedWaypoint focus = new FocusedWaypoint();

        public static class PlayerFace {
            @ConfigEntry.Gui.Tooltip
            public boolean enabled = false;

            @ConfigEntry.Gui.Tooltip
            public boolean distanceScale = true;

            @ConfigEntry.Gui.CollapsibleObject
            public Outline outline = new Outline();

            public static class Outline {
                public enum Style {
                    Border, Shadow, None,
                }

                public enum Color {
                    Waypoint, Team, Black,
                }

                @ConfigEntry.Gui.Tooltip
                @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
                public Style style = Style.Border;

                @ConfigEntry.Gui.Tooltip
                @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
                public Color color = Color.Black;
            }
        }

        public static class FocusedWaypoint {
            public enum Trigger {
                Hover, Focal, PlayerList, None;

                public String toString() {
                    return this.name().replaceAll("([a-z])([A-Z])", "$1 $2");
                }
            }

            @ConfigEntry.Gui.Tooltip
            @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
            public Trigger trigger = Trigger.Hover;

            @ConfigEntry.Gui.Tooltip
            public float scale = 1.2f;

            @ConfigEntry.Gui.Tooltip
            public int inset = 2;

            @ConfigEntry.Gui.CollapsibleObject
            public FocusLabels labels = new FocusLabels();

            public static class FocusLabels {
                @ConfigEntry.Gui.Tooltip
                public boolean name = true;

                @ConfigEntry.Gui.Tooltip
                public boolean distance = false;
            }
        }
    }

    public static class PlayerSpecificConfig {
        public String name;

        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Override override = new Override();

        public static class Override {
            @ConfigEntry.Gui.Tooltip
            @ConfigEntry.ColorPicker
            public int color = 0xFFFFFF;

            @ConfigEntry.Gui.Tooltip
            public boolean alwaysFocused = false;

            @ConfigEntry.Gui.Tooltip
            public boolean hide = false;
        }
    }

    public static class CardinalDirections {
        @ConfigEntry.Gui.Tooltip
        public boolean enabled = false;

        @ConfigEntry.Gui.Tooltip
        public boolean intercardinal = false;
    }
}