package dev.liqw.borderful.mixin;

import com.mojang.datafixers.util.Either;
import dev.liqw.borderful.Borderful;
import dev.liqw.borderful.config.BorderfulConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.waypoints.ClientWaypointManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.waypoints.TrackedWaypoint;
import net.minecraft.world.waypoints.Waypoint;
import java.util.Optional;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Mixin(ClientWaypointManager.class)
public abstract class ClientWaypointManagerMixin {
    @Shadow @Final private Map<Either<UUID, String>, TrackedWaypoint> waypoints;

    @Inject(method = "forEachWaypoint", at = @At("HEAD"), cancellable = true)
    private void populateWaypointsMap(Entity fromEntity, Consumer<TrackedWaypoint> consumer, CallbackInfo ci) {
        if (!(fromEntity.level() instanceof ClientLevel level)) return;

        BorderfulConfig config = Borderful.getConfig();

        for (BorderfulConfig.CustomWaypoint customWaypoint : Borderful.getCurrentServerWaypoints(net.minecraft.client.Minecraft.getInstance())) {
            if (!customWaypoint.enabled) continue;

            consumer.accept(TrackedWaypoint.setPosition(
                    customWaypoint.getRuntimeId(),
                    customWaypointIcon(customWaypoint),
                    new BlockPos(customWaypoint.x, customWaypoint.y, customWaypoint.z)
            ));
        }

        if (config.forceWaypoints) {
            for (Player player : level.players()) {
                if (player == fromEntity || player.isInvisible()) continue;

                consumer.accept(
                        TrackedWaypoint.setPosition(player.getUUID(), Waypoint.Icon.NULL, player.blockPosition())
                );
            }
        }

        waypoints.values().forEach(waypoint -> {
            if (!waypoint.id().left()
                    .map(uuid -> config.forceWaypoints && level.getPlayerByUUID(uuid) == null)
                    .orElse(false)) {
                consumer.accept(waypoint);
            }
        });

        ci.cancel();
    }

    private static Waypoint.Icon customWaypointIcon(BorderfulConfig.CustomWaypoint customWaypoint) {
        Waypoint.Icon icon = new Waypoint.Icon();
        icon.color = Optional.of(0xFF000000 | customWaypoint.color);
        return icon;
    }

    @Inject(method = "hasWaypoints", at = @At("HEAD"), cancellable = true)
    private void forceHasWaypoints(CallbackInfoReturnable<Boolean> cir) {
        BorderfulConfig config = Borderful.getConfig();
        if (config.forceWaypoints || Borderful.getCurrentServerWaypoints(net.minecraft.client.Minecraft.getInstance()).stream().anyMatch(waypoint -> waypoint.enabled)) {
            cir.setReturnValue(true);
        }
    }
}