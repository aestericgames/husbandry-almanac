package org.aestericgames.growing.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.aestericgames.HusbandryAlmanac;
import org.aestericgames.growing.components.GrowingComponent;
import org.aestericgames.growing.models.data.GrowableEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil.getLogger;

public class OnGrowingSpawn extends RefSystem<EntityStore> {
    public OnGrowingSpawn(){
    }

    @Override
    public void onEntityAdded(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull AddReason addReason,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        GrowingComponent testGrowComp = store.getComponent(ref, HusbandryAlmanac.get().getGrowingComponentType());

        if(testGrowComp == null){
            NPCEntity entityItem = store.getComponent(ref, NPCEntity.getComponentType());

            if(entityItem != null && HusbandryAlmanac.get().IsGrowableEntity(entityItem.getNPCTypeId())) {
                GrowableEntity gEntity = HusbandryAlmanac.get().GetGrowableEntity(entityItem.getNPCTypeId());

                if (gEntity != null) {
                    GrowingComponent growComp = new GrowingComponent(gEntity);
                    commandBuffer.addComponent(ref, HusbandryAlmanac.get().getGrowingComponentType(), growComp);
                } else {
                    GrowingComponent growComp = new GrowingComponent();
                    commandBuffer.addComponent(ref, HusbandryAlmanac.get().getGrowingComponentType(), growComp);
                }
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
        // TODO: Commented out to try and prevent chicken coop from resetting
//        getLogger().info("OnGrowingSpawn - onEntityRemove called!");

//        var entityItem = store.getComponent(ref, NPCEntity.getComponentType());
//
//        if(entityItem != null && isValidGrower(entityItem.getNPCTypeId())){
////            getLogger().info("OnGrowingSpawn onEntityRemove - Entity type = . " + entityItem.getNPCTypeId());
//
//            commandBuffer.removeComponent(ref, HusbandryAlmanac.get().getGrowingComponentType());
//        }
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return NPCEntity.getComponentType();
    }

//    public void handleOnChunkUnload(ChunkPreLoadProcessEvent event) {
////        getLogger().info("GrowingSystem - handleOnChunkUnload called!");
//    }

//    private boolean isValidGrower(String entityTypeId){
////        getLogger().info("GrowingSystem - isValidGrower called!");
////        getLogger().info("GrowingSystem - EntityTypeId is: " + entityTypeId);
//        boolean isValidGrower = HusbandryAlmanac.get().IsGrowableEntity(entityTypeId);
//
////        getLogger().info("GrowingSystem - IsValidGrower: " + isValidGrower);
//
//        return isValidGrower;
//    }
}
