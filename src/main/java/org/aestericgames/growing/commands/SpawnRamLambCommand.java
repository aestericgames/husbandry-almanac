package org.aestericgames.growing.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.Pair;

import javax.annotation.Nonnull;

public class SpawnRamLambCommand extends AbstractPlayerCommand {
    public SpawnRamLambCommand(){
        super("spawn-ram-lamb","This spawns a cow calf at the players location.");
    }

    @Override
    protected void execute(
            @Nonnull CommandContext commandContext,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());
        var position = player.getTransformComponent().getPosition();

        world.execute(() -> {
            NPCPlugin npcPlugin = NPCPlugin.get();
            int roleIndex = npcPlugin.getIndex("Ram_Lamb");
            ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset("Ram_Lamb");
            Model model = Model.createScaledModel(modelAsset, 1.0f);

            Pair<Ref<EntityStore>, NPCEntity> npcPair = npcPlugin.spawnEntity(
                    store,
                    roleIndex,
                    position,
                    new Vector3f(0,0,0),
                    model,
                    null
            );
        });
    }
}
