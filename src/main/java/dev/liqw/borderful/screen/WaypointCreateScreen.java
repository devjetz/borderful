package dev.liqw.borderful.screen;

import dev.liqw.borderful.Borderful;
import dev.liqw.borderful.config.BorderfulConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.regex.Pattern;

public final class WaypointCreateScreen extends Screen {
    private static final Pattern COLOR_PATTERN = Pattern.compile("#?[0-9a-fA-F]{6}");
    private static final PaletteColor[] PALETTE_COLORS = {
            new PaletteColor("Red", 0xE74C3C),
            new PaletteColor("Orange", 0xE67E22),
            new PaletteColor("Yellow", 0xF1C40F),
            new PaletteColor("Green", 0x2ECC71),
            new PaletteColor("Teal", 0x1ABC9C),
            new PaletteColor("Blue", 0x3498DB),
            new PaletteColor("Indigo", 0x3F51B5),
            new PaletteColor("Purple", 0x9B59B6),
            new PaletteColor("Pink", 0xE84393),
            new PaletteColor("White", 0xFFFFFF),
            new PaletteColor("Gray", 0x95A5A6),
            new PaletteColor("Dark Slate", 0x34495E)
    };

    private final Screen parent;
    private EditBox nameField;
    private EditBox xField;
    private EditBox yField;
    private EditBox zField;
    private EditBox colorField;
    private Button status;

    public WaypointCreateScreen(Screen parent) {
        super(Component.translatable("screen.borderful.create_waypoint"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int formWidth = 260;
        int left = width / 2 - formWidth / 2;
        int top = height / 2 - 100;

        Component heading = Component.translatable("screen.borderful.add_waypoint");
        addRenderableWidget(new StringWidget(left + (formWidth - font.width(heading)) / 2, top, font.width(heading), 20, heading, font));

        addLabel(left, top + 24, formWidth, "screen.borderful.waypoint_name");
        nameField = addRenderableWidget(new EditBox(font, left, top + 42, formWidth, 20, Component.translatable("screen.borderful.waypoint_name")));
        nameField.setHint(Component.translatable("screen.borderful.waypoint_name"));
        nameField.setMaxLength(64);

        int columnGap = 8;
        int columnWidth = (formWidth - columnGap * 2) / 3;
        int secondColumn = left + columnWidth + columnGap;
        int thirdColumn = secondColumn + columnWidth + columnGap;
        addLabel(left, top + 68, columnWidth, "screen.borderful.x");
        xField = addRenderableWidget(numberField(left, top + 86, columnWidth, "0"));
        addLabel(secondColumn, top + 68, columnWidth, "screen.borderful.y");
        yField = addRenderableWidget(numberField(secondColumn, top + 86, columnWidth, "0"));
        addLabel(thirdColumn, top + 68, columnWidth, "screen.borderful.z");
        zField = addRenderableWidget(numberField(thirdColumn, top + 86, columnWidth, "0"));
        updatePositionFields();

        addLabel(left, top + 112, formWidth, "screen.borderful.color");
        CycleButton.Builder<PaletteColor> paletteBuilder =
                //? if <1.21.11 {
                CycleButton.builder(PaletteColor::label);
                //? } else {
                /*CycleButton.builder(PaletteColor::label, PALETTE_COLORS[0]);
                *///? }
        addRenderableWidget(paletteBuilder.withValues(PALETTE_COLORS)
                .create(left, top + 130, 116, 20, Component.translatable("screen.borderful.palette"), (button, paletteColor) ->
                        setColor(paletteColor.color)));
        colorField = addRenderableWidget(new EditBox(font, left + 122, top + 130, 86, 20, Component.translatable("screen.borderful.color")));
        colorField.setMaxLength(7);
        colorField.setValue(String.format(Locale.ROOT, "#%06X", BorderfulConfig.CustomWaypoint.randomColor()));
        colorField.setTextColor(0xFFFFFFFF);
        addRenderableWidget(Button.builder(Component.translatable("screen.borderful.randomize"), button ->
                colorField.setValue(String.format(Locale.ROOT, "#%06X", BorderfulConfig.CustomWaypoint.randomColor()))
        ).bounds(left + 214, top + 130, formWidth - 214, 20).build());

        status = addRenderableWidget(Button.builder(Component.empty(), button -> {}).bounds(left, top + 158, formWidth, 20).build());
        status.active = false;

        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .bounds(left, top + 186, 84, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> saveWaypoint())
                .bounds(left + 92, top + 186, formWidth - 92, 20).build());
        setInitialFocus(nameField);
    }

    private void setColor(int color) {
        colorField.setValue(String.format(Locale.ROOT, "#%06X", color));
    }

    private void addLabel(int x, int y, int width, String translationKey) {
        Component label = Component.translatable(translationKey);
        addRenderableWidget(new StringWidget(x, y, width, 18, label, font));
    }

    private record PaletteColor(String name, int color) {
        private Component label() {
            return Component.literal(name).withStyle(style -> style.withColor(color));
        }
    }

    private EditBox numberField(int x, int y, int width, String initialValue) {
        EditBox field = new EditBox(font, x, y, width, 20, Component.empty());
        field.setMaxLength(12);
        field.setValue(initialValue);
        return field;
    }

    private void updatePositionFields() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        BlockPos position = minecraft.player.blockPosition();
        xField.setValue(Integer.toString(position.getX()));
        yField.setValue(Integer.toString(position.getY()));
        zField.setValue(Integer.toString(position.getZ()));
    }

    private void saveWaypoint() {
        String name = nameField.getValue().trim();
        if (name.isEmpty()) {
            showError("screen.borderful.error_name_required");
            setInitialFocus(nameField);
            return;
        }

        final int x;
        final int y;
        final int z;
        try {
            x = Integer.parseInt(xField.getValue().trim());
            y = Integer.parseInt(yField.getValue().trim());
            z = Integer.parseInt(zField.getValue().trim());
        } catch (NumberFormatException exception) {
            showError("screen.borderful.error_coordinates");
            return;
        }

        String colorValue = colorField.getValue().trim();
        if (!COLOR_PATTERN.matcher(colorValue).matches()) {
            showError("screen.borderful.error_color");
            setInitialFocus(colorField);
            return;
        }

        BorderfulConfig config = Borderful.getConfig();
        BorderfulConfig.CustomWaypoint waypoint = new BorderfulConfig.CustomWaypoint(name, new BlockPos(x, y, z));
        waypoint.serverId = Borderful.getCurrentServerId(Minecraft.getInstance());
        waypoint.color = Integer.parseInt(colorValue.replace("#", ""), 16);
        config.customWaypoints.add(waypoint);
        Borderful.saveConfig();
        onClose();
    }

    private void showError(String translationKey) {
        status.setMessage(Component.translatable(translationKey));
    }

    @Override
    public void onClose() {
        //? if <26.2 {
        Minecraft.getInstance().setScreen(parent);
        //? } else {
        /*Minecraft.getInstance().setScreenAndShow(parent);
        *///? }
    }
}
