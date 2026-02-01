package org.aestericgames.breeding.systems;

import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.group.EntityGroup;
import com.hypixel.hytale.server.core.universe.world.events.ChunkPreLoadProcessEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.Flock;
import com.hypixel.hytale.server.flock.FlockMembership;
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
            NPCEntity entityItem = store.getComponent(ref, NPCEntity.getComponentType());

            if (entityItem != null && HusbandryAlmanac.IsBreedableEntity(entityItem.getNPCTypeId())) {
                // Add breedable component if missing
                BreedableComponent checkForBreedable = store.getComponent(ref, HusbandryAlmanac.get().getBreedableComponentType());

                if(checkForBreedable == null) {
                    BreedableEntity bEntity = HusbandryAlmanac.GetBreedableEntity(entityItem.getNPCTypeId());

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
        if(removeReason == RemoveReason.REMOVE) {
            NPCEntity removingEntity = store.getComponent(ref, NPCEntity.getComponentType());

            if(removingEntity != null) {
                if(removingEntity.getNPCTypeId().equals("Chicken")) {
                    LOGGER.atInfo().log("OnBreedSpawn - OnBreedSpawn - OnEntityRemove - Removing Chicken");
                }
                else if(removingEntity.getNPCTypeId().equals("Chicken_Chick")) {
                    LOGGER.atInfo().log("OnBreedSpawn - OnBreedSpawn - OnEntityRemove - Removing Chicken_Chick");
                }
            }

            BreedableComponent breedComp = store.getComponent(ref, HusbandryAlmanac.get().getBreedableComponentType());

            if(breedComp != null) {
                breedComp.setPartnerReference(null);
            }
        }
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
