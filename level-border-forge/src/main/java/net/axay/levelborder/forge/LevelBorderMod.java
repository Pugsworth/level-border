package net.axay.levelborder.forge;

import net.axay.levelborder.common.LevelBorderHandler;
import net.axay.levelborder.vanilla.ModLevelBorderHandler;
import net.axay.levelborder.vanilla.VanillaLevelBorderCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("levelborder")
public class LevelBorderMod {
    private LevelBorderHandler<ServerPlayer, WorldBorder, ?> levelBorderHandler;

    public LevelBorderMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        levelBorderHandler = new ModLevelBorderHandler(event.getServer());
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        VanillaLevelBorderCommand.register(event.getDispatcher(), () -> levelBorderHandler);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player instanceof ServerPlayer player) {
            levelBorderHandler.checkOutsideBorder(player);
        }
    }

    @SubscribeEvent
    public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            final var pos = levelBorderHandler.getRespawnPos();
            player.teleportTo(
                player.server.overworld(),
                pos.x(), pos.y(), pos.z(),
                0f, 0f
            );

            levelBorderHandler.initBorder(player);
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            levelBorderHandler.initBorder(player, player.getLevel().dimension() == Level.NETHER);
        }
    }

    @SubscribeEvent
    public void onPlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            levelBorderHandler.onLeave(player);
        }
    }

    @SubscribeEvent
    public void onChangeWorld(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            levelBorderHandler.initBorder(player, event.getTo() == Level.NETHER);
        }
    }

    @SubscribeEvent
    public void onChangeLevel(PlayerXpEvent.LevelChange event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            levelBorderHandler.onChangeLevel(player);
        }
    }

    @SubscribeEvent
    public void onChangePoints(PlayerXpEvent.XpChange event) {
        if (event.getPlayer() instanceof ServerPlayer) {
            levelBorderHandler.onChangeExperience();
        }
    }
}
