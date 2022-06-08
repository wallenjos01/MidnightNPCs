package org.wallentines.midnightnpcs.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.network.chat.Component;
import org.wallentines.midnightcore.fabric.util.CommandUtil;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightcore.api.item.InventoryGUI;
import org.wallentines.midnightcore.api.module.lang.CustomPlaceholderInline;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightnpcs.api.MidnightNPCsAPI;
import org.wallentines.midnightnpcs.api.npc.NPC;
import org.wallentines.midnightnpcs.api.npc.NPCAction;
import org.wallentines.midnightnpcs.api.npc.NPCActionType;
import org.wallentines.midnightnpcs.api.npc.NPCSelector;
import org.wallentines.midnightnpcs.api.trait.Trait;
import org.wallentines.midnightnpcs.api.trait.TraitType;
import org.wallentines.midnightnpcs.common.npc.NPCManager;
import org.wallentines.midnightnpcs.fabric.entity.NPCEntity;
import org.wallentines.midnightnpcs.fabric.npc.FabricNPC;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NPCCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        LiteralArgumentBuilder<CommandSourceStack> commands = Commands.literal("npc")
                .requires(Permissions.require("midnightnpcs.command.npc", 2))
                .then(Commands.literal("select")
                    .executes(context -> executeSelect(context, null))
                    .then(Commands.argument("id", UuidArgument.uuid())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(NPCManager.INSTANCE.getIds(), builder, UUID::toString, uid -> Component.literal(uid.toString())))
                        .executes(context -> executeSelect(context, context.getArgument("id", UUID.class)))
                    )
                )
                .then(Commands.literal("rename")
                    .then(Commands.argument("name", StringArgumentType.greedyString())
                        .executes(context -> executeRename(context, context.getArgument("name", String.class)))
                    )
                )
                .then(Commands.literal("create")
                    .then(Commands.argument("name", StringArgumentType.greedyString())
                        .executes(context -> executeCreate(context, context.getArgument("name", String.class)))
                    )
                )
                .then(Commands.literal("commands")
                    .then(Commands.literal("add")
                        .then(Commands.argument("type", ResourceLocationArgument.id())
                            .suggests((context, builder) -> {

                                List<String> ids = new ArrayList<>();
                                for(NPCActionType type : NPCActionType.ACTION_TYPE_REGISTRY) {
                                    ids.add(NPCActionType.ACTION_TYPE_REGISTRY.getId(type).toString());
                                }

                                return SharedSuggestionProvider.suggest(ids, builder);
                            })
                            .then(Commands.argument("command", StringArgumentType.greedyString())
                                .executes(context -> executeAddCommand(context, context.getArgument("type", ResourceLocation.class), context.getArgument("command", String.class)))
                            )
                        )
                    )
                    .then(Commands.literal("clear")
                        .executes(NPCCommand::executeClearCommands)
                    )
                    .then(Commands.literal("list")
                        .executes(NPCCommand::executeListCommands)
                    )
                )
                .then(Commands.literal("type")
                    .then(Commands.argument("type", ResourceLocationArgument.id())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(Registry.ENTITY_TYPE.keySet(), builder))
                        .executes(context -> executeType(context, context.getArgument("type", ResourceLocation.class)))
                    )
                )
                .then(Commands.literal("traits")
                    .then(Commands.literal("add")
                        .then(Commands.argument("trait", ResourceLocationArgument.id())
                            .suggests((context, builder) -> {

                                List<ResourceLocation> locs = new ArrayList<>();
                                for(TraitType traitType : TraitType.TRAIT_REGISTRY) {
                                    locs.add(ConversionUtil.toResourceLocation(TraitType.TRAIT_REGISTRY.getId(traitType)));
                                }

                                return SharedSuggestionProvider.suggestResource(locs, builder);
                            })
                            .executes(context -> executeAddTrait(context, context.getArgument("trait", ResourceLocation.class)))
                        )
                    )
                    .then(Commands.literal("remove")
                        .then(Commands.argument("trait", ResourceLocationArgument.id())
                            .suggests(NPCCommand::suggestNPCTraits)
                            .executes(context -> executeRemoveTrait(context, context.getArgument("trait", ResourceLocation.class)))
                        )
                    )
                    .then(Commands.literal("config")
                        .then(Commands.literal("set")
                            .then(Commands.argument("trait", ResourceLocationArgument.id())
                                .suggests(NPCCommand::suggestNPCTraits)
                                .then(Commands.argument("data", StringArgumentType.greedyString())
                                    .executes(context -> executeSetTraitConfig(context, context.getArgument("trait", ResourceLocation.class), context.getArgument("data", String.class)))
                                )
                            )
                        )
                        .then(Commands.literal("merge")
                            .then(Commands.argument("trait", ResourceLocationArgument.id())
                                .suggests(NPCCommand::suggestNPCTraits)
                                .then(Commands.argument("data", StringArgumentType.greedyString())
                                    .executes(context -> executeFillTraitConfig(context, context.getArgument("trait", ResourceLocation.class), context.getArgument("data", String.class)))
                                )
                            )
                        )
                    )
                )
                .then(Commands.literal("respawn")
                    .executes(NPCCommand::executeRespawn)
                )
                .then(Commands.literal("tp")
                    .executes(NPCCommand::executeTeleport)
                );

        dispatcher.register(commands);

    }

    private static int executeSelect(CommandContext<CommandSourceStack> stack, UUID uid) {

        Entity pl = stack.getSource().getEntity();
        NPC npc;

        if(!(pl instanceof NPCSelector)) return 0;

        if(uid == null) {

            AABB aabb = pl.getBoundingBox().move(pl.getForward().scale(2.0f)).inflate(0.3d);
            List<Entity> entities = pl.level.getEntities(pl, aabb, entity -> entity instanceof NPCEntity);

            if(entities.isEmpty()) {

                CommandUtil.sendCommandFailure(stack, MidnightNPCsAPI.getInstance().getLangProvider(), "command.error.no_entity");
                return 0;
            }

            npc = ((NPCEntity) entities.get(0)).getNPC();

        } else {

            npc = NPCManager.INSTANCE.getByUUID(uid);
        }

        ((NPCSelector) pl).setSelectedNPC(npc);
        CommandUtil.sendCommandSuccess(stack, MidnightNPCsAPI.getInstance().getLangProvider(), false, "command.select.result", npc);

        return 1;
    }

    private static int executeRename(CommandContext<CommandSourceStack> stack, String string) {

        NPC n;
        if((n = getSelectedNPC(stack)) == null) return 0;

        n.setDisplayName(MComponent.parse(string));
        CommandUtil.sendCommandSuccess(stack, MidnightNPCsAPI.getInstance().getLangProvider(), false, "command.rename.result", n);

        return 1;
    }

    private static int executeCreate(CommandContext<CommandSourceStack> stack, String string) {

        NPC n = new FabricNPC(getLocation(stack), new Identifier("minecraft", "player"), null, MComponent.parse(string));
        n.spawn();

        Entity ent = stack.getSource().getEntity();
        if(ent instanceof NPCSelector) {
            ((NPCSelector) ent).setSelectedNPC(n);
        }

        CommandUtil.sendCommandSuccess(stack, MidnightNPCsAPI.getInstance().getLangProvider(), false, "command.create.result", n);

        return 1;
    }

    private static int executeTeleport(CommandContext<CommandSourceStack> stack) {

        NPC n;
        if((n = getSelectedNPC(stack)) == null) return 0;
        n.teleport(getLocation(stack));

        CommandUtil.sendCommandSuccess(stack, MidnightNPCsAPI.getInstance().getLangProvider(), false, "command.teleport.result", n);


        return 1;
    }

    private static int executeAddCommand(CommandContext<CommandSourceStack> stack, ResourceLocation actionType, String command) {

        NPC n;
        if((n = getSelectedNPC(stack)) == null) return 0;

        NPCActionType type = NPCActionType.ACTION_TYPE_REGISTRY.get(ConversionUtil.toIdentifier(actionType));

        n.addCommand(InventoryGUI.ClickType.RIGHT, type, command, null);
        CommandUtil.sendCommandSuccess(stack, MidnightNPCsAPI.getInstance().getLangProvider(), false, "command.commands.add.result", n);

        return 1;
    }

    private static int executeClearCommands(CommandContext<CommandSourceStack> stack) {

        NPC n;
        if((n = getSelectedNPC(stack)) == null) return 0;

        n.clearCommands();
        CommandUtil.sendCommandSuccess(stack, MidnightNPCsAPI.getInstance().getLangProvider(), false, "command.commands.clear.result", n);

        return 1;
    }

    private static int executeListCommands(CommandContext<CommandSourceStack> stack) {

        NPC n;
        if((n = getSelectedNPC(stack)) == null) return 0;

        CommandUtil.sendCommandSuccess(stack, MidnightNPCsAPI.getInstance().getLangProvider(), false, "command.commands.list.header");

        for(NPCAction cmd : n.getCommands()) {

            CustomPlaceholderInline in = CustomPlaceholderInline.create("command", "(" + NPCActionType.ACTION_TYPE_REGISTRY.getId(cmd.getType()) + ") " + cmd.getValue());
            CommandUtil.sendCommandSuccess(stack, MidnightNPCsAPI.getInstance().getLangProvider(), false, "command.commands.list.entry", in);

        }

        return 1;
    }

    private static int executeType(CommandContext<CommandSourceStack> stack, ResourceLocation location) {

        NPC n = getSelectedNPC(stack);
        if(n == null) return 0;

        n.setEntityType(ConversionUtil.toIdentifier(location));
        CommandUtil.sendCommandSuccess(stack, MidnightNPCsAPI.getInstance().getLangProvider(), false, "command.type.result", n, CustomPlaceholderInline.create("type", location.toString()));

        return 1;
    }

    private static int executeAddTrait(CommandContext<CommandSourceStack> stack, ResourceLocation location) {

        NPC n = getSelectedNPC(stack);
        if(n == null) return 0;

        TraitType t = TraitType.TRAIT_REGISTRY.get(ConversionUtil.toIdentifier(location));
        if(t == null) {
            CommandUtil.sendCommandFailure(stack, MidnightNPCsAPI.getInstance().getLangProvider(), "command.error.invalid_trait");
            return 0;
        }

        n.addTrait(t);
        CommandUtil.sendCommandSuccess(stack, MidnightNPCsAPI.getInstance().getLangProvider(), false, "command.traits.add.result", n);

        return 1;
    }

    private static int executeRemoveTrait(CommandContext<CommandSourceStack> stack, ResourceLocation location) {

        NPC n = getSelectedNPC(stack);
        if(n == null) return 0;

        Identifier id = ConversionUtil.toIdentifier(location);
        TraitType type = TraitType.TRAIT_REGISTRY.get(id);

        if(type == null) {
            CommandUtil.sendCommandFailure(stack, MidnightNPCsAPI.getInstance().getLangProvider(), "command.error.invalid_trait");
            return 0;
        }

        if(!n.hasTrait(type)) {
            CommandUtil.sendCommandFailure(stack, MidnightNPCsAPI.getInstance().getLangProvider(), "command.error.no_trait");
            return 0;
        }

        n.removeTrait(type);
        CommandUtil.sendCommandSuccess(stack, MidnightNPCsAPI.getInstance().getLangProvider(), false, "command.traits.add.result", n);

        return 1;
    }

    private static int executeSetTraitConfig(CommandContext<CommandSourceStack> stack, ResourceLocation typeId, String config) {

        NPC n = getSelectedNPC(stack);
        if(n == null) return 0;

        Identifier id = ConversionUtil.toIdentifier(typeId);
        TraitType type = TraitType.TRAIT_REGISTRY.get(id);

        if(type == null) {
            CommandUtil.sendCommandFailure(stack, MidnightNPCsAPI.getInstance().getLangProvider(), "command.error.invalid_trait");
            return 0;
        }

        ConfigSection sec = getConfigSection(stack, n, config);
        if(sec == null) return 0;

        n.setTraitConfig(type, sec, false);
        CommandUtil.sendCommandSuccess(stack, MidnightNPCsAPI.getInstance().getLangProvider(), false, "command.traits.set.result", n);
        return 1;
    }

    private static int executeFillTraitConfig(CommandContext<CommandSourceStack> stack, ResourceLocation typeId, String config) {

        NPC n = getSelectedNPC(stack);
        if(n == null) return 0;

        Identifier id = ConversionUtil.toIdentifier(typeId);
        TraitType type = TraitType.TRAIT_REGISTRY.get(id);

        if(type == null) {
            CommandUtil.sendCommandFailure(stack, MidnightNPCsAPI.getInstance().getLangProvider(), "command.error.invalid_trait");
            return 0;
        }

        ConfigSection sec = getConfigSection(stack, n, config);
        if(sec == null) return 0;

        n.setTraitConfig(type, sec, true);
        CommandUtil.sendCommandSuccess(stack, MidnightNPCsAPI.getInstance().getLangProvider(), false, "command.traits.fill.result", n);
        return 1;
    }

    private static int executeRespawn(CommandContext<CommandSourceStack> stack) {

        NPC n = getSelectedNPC(stack);
        if(n == null) return 0;

        n.respawn();
        CommandUtil.sendCommandSuccess(stack, MidnightNPCsAPI.getInstance().getLangProvider(), false, "command.respawn.result", n);

        return 1;
    }

    private static Location getLocation(CommandContext<CommandSourceStack> stack) {

        Vec3 vec = stack.getSource().getPosition();
        Identifier world = ConversionUtil.toIdentifier(stack.getSource().getLevel().dimension().location());
        Vec2 rot = stack.getSource().getRotation();

        return new Location(world, new Vec3d(vec.x, vec.y, vec.z), rot.x, rot.y);
    }

    private static NPC getSelectedNPC(CommandContext<CommandSourceStack> stack) {
        return getSelectedNPC(stack, true);
    }

    private static NPC getSelectedNPC(CommandContext<CommandSourceStack> stack, boolean send) {

        Entity ent = stack.getSource().getEntity();
        if(!(ent instanceof NPCSelector)) return null;

        NPC n = ((NPCSelector) ent).getSelectedNPC();

        if (n == null && send) {
            CommandUtil.sendCommandFailure(stack, MidnightNPCsAPI.getInstance().getLangProvider(), "command.error.no_selected");
        }

        return n;
    }

    private static ConfigSection getConfigSection(CommandContext<CommandSourceStack> stack, NPC n, String config) {

        ConfigSection sec = JsonConfigProvider.INSTANCE.loadFromString(config);
        if(sec == null) {
            CommandUtil.sendCommandFailure(stack, MidnightNPCsAPI.getInstance().getLangProvider(), "command.error.invalid_data");
            return null;
        }

        return sec;
    }

    private static CompletableFuture<Suggestions> suggestNPCTraits(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {

        NPC n;
        if((n = getSelectedNPC(context, false)) == null) return null;

        List<ResourceLocation> locs = new ArrayList<>();
        for(Trait trait : n.getTraits()) {
            locs.add(ConversionUtil.toResourceLocation(trait.getId()));
        }

        return SharedSuggestionProvider.suggestResource(locs, builder);
    }

}
