package io.github.bozzf.monsterlegion.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.bozzf.monsterlegion.entity.LichKingEntity;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import java.util.List;

public final class LichCommands {
    public static void initialize() {
        var names = List.of("idle_float", "fly_move", "summon", "command", "attack_magic", "hurt", "animationdeath");
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) ->
                dispatcher.register(CommandManager.literal("monsterlegion").requires(s -> s.hasPermissionLevel(2))
                        .then(CommandManager.literal("animate")
                                .then(CommandManager.argument("targets", EntityArgumentType.entities())
                                        .then(CommandManager.argument("animation", StringArgumentType.word())
                                                .suggests((context, builder) -> {
                                                    names.forEach(builder::suggest);
                                                    return builder.buildFuture();
                                                }).executes(context -> {
                                                    String animation = StringArgumentType.getString(context, "animation");
                                                    if (!names.contains(animation)) {
                                                        context.getSource().sendError(Text.literal("Animação desconhecida: " + animation));
                                                        return 0;
                                                    }
                                                    int count = 0;
                                                    for (var entity : EntityArgumentType.getEntities(context, "targets")) {
                                                        if (entity instanceof LichKingEntity lich) {
                                                            lich.previewAnimation(animation);
                                                            count++;
                                                        }
                                                    }
                                                    final int affected = count;
                                                    context.getSource().sendFeedback(() -> Text.literal("Prévia em " + affected + " Rei(s) Lich"), false);
                                                    return count;
                                                }))))));
    }
    private LichCommands() { }
}
