package net.axay.levelborder.vanilla;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.axay.levelborder.common.BorderMode;
import net.axay.levelborder.common.LevelBorderHandler;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec2;

import java.util.Locale;
import java.util.function.Supplier;

public class VanillaLevelBorderCommand {
    private static final SimpleCommandExceptionType ERROR_SAME_CENTER = new SimpleCommandExceptionType(Component.translatable("commands.worldborder.center.failed"));
    private static final SimpleCommandExceptionType ERROR_TOO_BIG = new SimpleCommandExceptionType(Component.translatable("commands.worldborder.set.failed.big", new Object[]{5.9999968E7}));


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                Supplier<LevelBorderHandler<ServerPlayer, ?, ?>> levelBorderHandlerSupplier) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("levelborder")
            .requires(source -> source.hasPermission(2))
            .executes(context -> {
                context.getSource().sendSuccess(
                    Component.literal("You executed the base command of level-border. If you want to change some settings, continue with the subcommands."),
                    true
                );
                return 1;
            })
            .then(Commands.literal("mode")
                .then(Commands.argument("mode", StringArgumentType.string())
                    .suggests((context, builder) -> {
                        for (BorderMode value : BorderMode.values()) {
                            builder.suggest(value.name());
                        }
                        return builder.buildFuture();
                    })
                    .executes(context -> {
                        levelBorderHandlerSupplier.get().setMode(
                            BorderMode.valueOf(context.getArgument("mode", String.class))
                        );
                        return 1;
                    })
                )
            ).then(Commands.literal("center").then(Commands.argument("pos", Vec2Argument.vec2()).executes((commandContext) -> {
                    return setCenter((CommandSourceStack) commandContext.getSource(), Vec2Argument.getVec2(commandContext, "pos"), levelBorderHandlerSupplier);
            })));
        dispatcher.register(command);
    }


    private static int setCenter(CommandSourceStack commandSourceStack, Vec2 vec2, Supplier<LevelBorderHandler<ServerPlayer, ?, ?>> levelBorderHandlerSupplier) throws CommandSyntaxException {
        WorldBorder worldBorder = commandSourceStack.getServer().overworld().getWorldBorder();
        if (worldBorder.getCenterX() == (double)vec2.x && worldBorder.getCenterZ() == (double)vec2.y) {
            throw ERROR_SAME_CENTER.create();

        } else if (!((double)Math.abs(vec2.x) > 2.9999984E7) && !((double)Math.abs(vec2.y) > 2.9999984E7)) {

            var player = commandSourceStack.getPlayer();

            LevelBorderHandler<ServerPlayer, ?, ?> borderHandler = levelBorderHandlerSupplier.get();
            borderHandler.setCenter(player, (double)vec2.x, (double)vec2.y);

            commandSourceStack.sendSuccess(Component.translatable("commands.worldborder.center.success", new Object[]{String.format(Locale.ROOT, "%.2f", vec2.x), String.format(Locale.ROOT, "%.2f", vec2.y)}), true);
            return 0;

        } else {
            throw ERROR_TOO_BIG.create();
        }
    }
}
