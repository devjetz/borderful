package dev.liqw.borderful.mixin;

import dev.liqw.borderful.Borderful;
import dev.liqw.borderful.config.BorderfulConfig;
import dev.liqw.borderful.util.ScreenBounds;
import dev.liqw.borderful.util.WaypointIcon;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.contextualbar.LocatorBar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >1.21.7 {
import net.minecraft.world.waypoints.PartialTickSupplier;
//? }

@Mixin(LocatorBar.class)
public abstract class LocatorMixin {
    @Shadow @Final private Minecraft minecraft;

    //~ if <26 'extractRenderState' -> 'render'
    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    public void onRender(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci) {
        BorderfulConfig config = Borderful.getConfig();

        if (!config.enabled) return;

        ci.cancel();

        Entity cameraEntity = this.minecraft.getCameraEntity();
        if (cameraEntity == null || this.minecraft.player == null) return;

        Level level = cameraEntity.level();
        Camera camera = this.minecraft.gameRenderer.mainCamera();

        //? if >1.21.7 {
        boolean isFrozen = level.tickRateManager().isEntityFrozen(cameraEntity);
        PartialTickSupplier tickSupplier = entity -> delta.getGameTimeDeltaPartialTick(!isFrozen);
        //? }

        this.minecraft.player.connection.getWaypointManager().forEachWaypoint(cameraEntity, waypoint -> {
            if (waypoint.id().left().filter(cameraEntity.getUUID()::equals).isPresent()) return;

            ScreenBounds bounds = new ScreenBounds(this.minecraft, graphics, config, waypoint);
            WaypointIcon icon = new WaypointIcon(this.minecraft, config);
            float angle = (float) waypoint.yawAngleToCamera(level, camera /*? if >1.21.7 {*/ , tickSupplier /*? }*/);
            int size = icon.getBaseSize(waypoint, cameraEntity);

            bounds.project(angle, size, size, (g, state) -> {
                icon.render(g, state, cameraEntity, waypoint /*? if >1.21.7 {*/ , tickSupplier /*? }*/);
            });
        });
    }

    //~ if <26 'extractBackground' -> 'renderBackground'
    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    public void onRenderBackground(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci) {
        if (Borderful.getConfig().enabled) ci.cancel();
    }
}