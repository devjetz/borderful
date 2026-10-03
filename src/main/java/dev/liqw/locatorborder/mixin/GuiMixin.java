package dev.liqw.locatorborder.mixin;

import dev.liqw.locatorborder.LocatorBorder;
import dev.liqw.locatorborder.config.LocatorBorderConfig;
import dev.liqw.locatorborder.util.CardinalDirections;
import dev.liqw.locatorborder.util.ScreenBounds;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >26.1 {
import net.minecraft.client.gui.Hud;
//? }
import net.minecraft.client.gui.contextualbar.LocatorBar;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if <26.2 {
/*import net.minecraft.client.gui.Gui;
*///? }

//? if >26.1 {
@Mixin(Hud.class)
//? }
//? if <26.2 {
/*@Mixin(Gui.class)
*///? }
public abstract class GuiMixin {
    @Shadow @Final private Minecraft minecraft;
    @Unique private LocatorBar renderer;

    //? if <26.2 {
    /*
    @ModifyVariable(method = "nextContextualInfoState", at = @At("STORE"), ordinal = 0)
    public boolean forceLocatorStateOff(boolean original) {
        if (LocatorBorder.getConfig().enabled) return false;
        return original;
    }

    //? fabric {
    //~ if <26 'extractHotbarAndDecorations' -> 'renderHotbarAndDecorations'
    @Inject(method = "extractHotbarAndDecorations", at = @At("TAIL"))
    //? } else
    //@Inject(method = "renderContextualInfoBar", at = @At("HEAD"), cancellable = true)
    public void renderLocatorBorder(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci) {
        LocatorBorderConfig config = LocatorBorder.getConfig();

        if (config.enabled && this.minecraft.player != null && this.minecraft.player.connection.getWaypointManager().hasWaypoints()) {
            if (this.renderer == null) {
                this.renderer = new LocatorBar(this.minecraft);
            }

            //~ if <26 'renderer.extractRenderState' -> 'renderer.render'
            this.renderer.extractRenderState(graphics, delta);
        }
    }

    //~ if <26 'extractRenderState' -> 'render'
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    public void extractCardinalDirections(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci) {
        renderCardinalDirections(graphics);
    }
    *///? }

    //? if >26.1 {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    public void extractLocatorBorder(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci) {
        LocatorBorderConfig config = LocatorBorder.getConfig();

        if (config.enabled && this.minecraft.player != null && this.minecraft.player.connection.getWaypointManager().hasWaypoints()) {
            if (this.renderer == null) {
                this.renderer = new LocatorBar(this.minecraft);
            }

            this.renderer.extractRenderState(graphics, delta);
        }

        renderCardinalDirections(graphics);
    }
    //? }

    @Unique
    private void renderCardinalDirections(GuiGraphicsExtractor graphics) {
        LocatorBorderConfig config = LocatorBorder.getConfig();

        if (!config.enabled || !config.compass.enabled) return;
        //? if <26.2 {
        /*if (this.minecraft.options.hideGui) return;
        *///? }

        Entity cameraEntity = this.minecraft.getCameraEntity();
        if (cameraEntity == null) return;

        float yaw = cameraEntity.getYRot();

        for (CardinalDirections.Direction point : CardinalDirections.DIRECTIONS) {
            if (point.isIntercardinal() && !config.compass.intercardinal) continue;

            ScreenBounds bounds = new ScreenBounds(this.minecraft, graphics, config);
            Font font = this.minecraft.font;

            bounds.project(point.angle() - yaw, (g, state) -> {
                //~ if <26 'centeredText' -> 'drawCenteredString'
                g.centeredText(font, point.label(), 0, -font.lineHeight / 2, state.setAlpha(point.getColor()));
            });
        }
    }
}
