package org.aestericgames.growing.systems;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.Pair;
import org.aestericgames.HusbandryAlmanac;
import org.aestericgames.growing.components.GrowingComponent;
import org.aestericgames.growing.models.data.GrowableEntity;
import org.aestericgames.growing.models.data.WorldGrowingData;
import org.aestericgames.growing.models.dto.GrowDto;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;

public class GrowingSystem extends EntityTickingSystem<EntityStore>{
    public static final KeyedCodec<WorldGrowingData> GROWING_DATA = new KeyedCodec<>("GrowingData", WorldGrowingData.CODEC);

    private final ComponentType<EntityStore, GrowingComponent> growingComponentType;
    public static final HytaleLogger LOGGER = HytaleLogger.get("HusbandryAlmanac");

    private HashMap<String, GrowingComponent> data = new HashMap<>();

    public GrowingSystem(ComponentType<EntityStore, GrowingComponent> growingComponentType){
//        LOGGER.atInfo().log("GrowingSystem constructor - Called.");

        if(growingComponentType != null)
            LOGGER.atInfo().log("GrowingSystem constructor - GrowingComponentType not null.");

        this.growingComponentType = growingComponentType;
    }

    @Override
    public void tick(
            float v,
            int i,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        GrowingComponent growComponent = archetypeChunk.getComponent(i, growingComponentType);
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(i);
        var entityItem = store.getComponent(ref, NPCEntity.getComponentType());

        if(entityItem != null && !entityItem.isDespawning()) {
            String entityId = entityItem.getUuid().toString();

            if (!data.containsKey(entityId)) {
                data.put(entityId, growComponent);
            }

            growComponent.decrementGrowTime();

            if (growComponent.isGrownUp()) {
                LOGGER.atInfo().log("GrowingSystem tick - Entity is grown up!");

                commandBuffer.removeComponent(ref, growingComponentType);

                var position = entityItem.getOldPosition();

                World world = entityItem.getWorld();
                Store<EntityStore> entityStore = world.getEntityStore().getStore();

                String entityTypeId = entityItem.getNPCTypeId();
                NPCPlugin npcPlugin = NPCPlugin.get();

//                commandBuffer.removeComponent(ref, growingComponentType);

                world.execute(() -> {
                    GrowDto growInfo = getGrowDto(entityTypeId, npcPlugin);

                    Pair<Ref<EntityStore>, NPCEntity> npcPair = npcPlugin.spawnEntity(
                            store,
                            growInfo.getRoleIndex(),
                            position,
                            new Vector3f(0, 0, 0),
                            growInfo.getModel(),
                            null
                    );

                    entityStore.removeEntity(ref, RemoveReason.REMOVE);
                });
            } else {
                // TODO: This section would need to simulate persisting the change
//                Path file = Constants.UNIVERSE_PATH.resolve("HusbandryAlmanac").resolve("GrowingData.json");
//                BsonDocument doc = new BsonDocument("GrowingEntites",data);
            }
        }
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(this.growingComponentType);
    }

    private GrowDto getGrowDto(String entityIndexId, NPCPlugin npcPlugin){
        int roleIndex = -1;
        ModelAsset modelAsset = null;
        Model model = null;

        GrowableEntity entity = HusbandryAlmanac.get().GetGrowableEntity(entityIndexId);

        roleIndex = npcPlugin.getIndex(entity.getGrowIntoRoleIndex());
        modelAsset = ModelAsset.getAssetMap().getAsset(entity.getGrowIntoModelAsset());
        model = Model.createScaledModel(modelAsset, 1.0f);

        return new GrowDto(roleIndex, modelAsset, model);
    }
}
