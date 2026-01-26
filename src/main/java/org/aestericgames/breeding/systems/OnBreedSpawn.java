package org.aestericgames.breeding.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.events.ChunkPreLoadProcessEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.aestericgames.HusbandryAlmanac;
import org.aestericgames.breeding.components.BreedableComponent;
import org.aestericgames.breeding.models.data.BreedableEntity;
import org.aestericgames.growing.components.GrowingComponent;
import org.aestericgames.growing.models.data.GrowableEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.logging.Logger;

import static com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil.getLogger;

public class OnBreedSpawn extends RefSystem<EntityStore> {
    public static final HytaleLogger LOGGER = HytaleLogger.get("HusbandryAlmanac");

    public OnBreedSpawn(){
    }

    @Override
    public void onEntityAdded(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull AddReason addReason,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        var entityItem = store.getComponent(ref, NPCEntity.getComponentType());

        if(entityItem != null && HusbandryAlmanac.IsBreedableEntity(entityItem.getNPCTypeId())) {
            var testHasComponent = store.getComponent(ref, HusbandryAlmanac.get().getBreedableComponentType());
            BreedableEntity bEntity = HusbandryAlmanac.GetBreedableEntity(entityItem.getNPCTypeId());

            if(testHasComponent == null) {
                BreedableComponent breedComp = new BreedableComponent(bEntity);
                commandBuffer.addComponent(ref, HusbandryAlmanac.get().getBreedableComponentType(), breedComp);
            }
        }
    }

    @Override
    public void onEntityRemove(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull RemoveReason removeReason,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {

        // TODO: Commented out to prevent chicken coop from resetting growth time
//        var entityItem = store.getComponent(ref, NPCEntity.getComponentType());
//        var testComp = store.getComponent(ref, HusbandryAlmanac.get().getBreedableComponentType());
//
//
//        if(entityItem != null && HusbandryAlmanac.IsBreedableEntity(entityItem.getNPCTypeId()) && testComp != null) {
//            commandBuffer.removeComponent(ref, HusbandryAlmanac.get().getBreedableComponentType());
//        }
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return NPCEntity.getComponentType();
    }

    public void handleOnChunkUnload(ChunkPreLoadProcessEvent event) {
        getLogger().info("OnBreedSpawn - handleOnChunkUnload called!");
    }
}
