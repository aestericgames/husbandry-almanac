package org.aestericgames.breeding.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.Pair;
import org.aestericgames.HusbandryAlmanac;
import org.aestericgames.breeding.components.BreedableComponent;
import org.aestericgames.breeding.models.data.BreedableEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BreedingSystem extends EntityTickingSystem<EntityStore> {
    private final ComponentType<EntityStore, BreedableComponent> breedableComponentType;
    private final int flockCheckRange = 64; // 64-x, 64-x, 64-z for check on flock size
    private final double breedDistance = 5; // How close entities have to be in order to breed
    public static final HytaleLogger LOGGER = HytaleLogger.get("HusbandryAlmanac");

    public BreedingSystem(ComponentType<EntityStore, BreedableComponent> breedableComponentType){
        this.breedableComponentType = breedableComponentType;
    }

    @Override
    public void tick(
            float v,
            int i,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        BreedableComponent breedComponent = archetypeChunk.getComponent(i, breedableComponentType);
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(i);
        var entityItem = store.getComponent(ref, NPCEntity.getComponentType());
        TransformComponent entityTransform = store.getComponent(ref, TransformComponent.getComponentType());

        if(entityItem != null && breedComponent != null) {
            // Only proceed if a valid breedable
            // TODO: Ensure that the type check is dynamic
            // TODO: Should be part of a "IsBreedable" call
            if(HusbandryAlmanac.IsBreedableEntity(entityItem.getNPCTypeId())) {
                BreedableEntity bEntity = HusbandryAlmanac.GetBreedableEntity(entityItem.getNPCTypeId());

                if(bEntity == null)
                    throw new RuntimeException("Unexpected Breedable Entity that was not registered.");

                // Entity is Pregnant, handle pregnant logic
                if (breedComponent.getIsPregnant()) {
                    breedComponent.decrementBirthingTime();

                    if (breedComponent.canBirth()) {
                        LOGGER.atInfo().log("BreedingSystem - Entity has birthed child!");
                        // Breed new child
                        World world = entityItem.getWorld();
                        var position = entityTransform.getPosition();
                        var newChickPos = new Vector3d(position.x, position.y, position.z);
                        var rotation = entityTransform.getRotation();

                        // TODO: Retrieve what child should be spawned dynamically
                        NPCPlugin npcPlugin = NPCPlugin.get();

                        int roleIndex = npcPlugin.getIndex(bEntity.getChildEntityTypeId());
                        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(bEntity.getChildModelAsset());
                        Model model = Model.createScaledModel(modelAsset, 1.0f);

                        world.execute(() -> {
                            Pair<Ref<EntityStore>, NPCEntity> npcPair = npcPlugin.spawnEntity(
                                    store,
                                    roleIndex,
                                    newChickPos,
                                    rotation,
                                    model,
                                    null
                            );

                            breedComponent.doBirthChild();
                        });
                    }
                } else {
                    breedComponent.decrementBreedableCooldown();

                    if (breedComponent.canBreed()) {
                        handleBreeding(entityItem, bEntity, entityTransform, breedComponent);
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(this.breedableComponentType);
    }

    private void handleBreeding(
            NPCEntity entityItem,
            BreedableEntity entity,
            TransformComponent entityTransform,
            BreedableComponent breedComponent
    ) {
        World world = entityItem.getWorld();
        Store<EntityStore> entityStore = world.getEntityStore().getStore();
        List<Ref<EntityStore>> entitiesInRange = TargetUtil.getAllEntitiesInSphere(entityItem.getOldPosition(), flockCheckRange, entityStore);
        String npcTypeId = entityItem.getNPCTypeId();
        Ref<EntityStore> potentialPartnerBreedable = null;
        UUID selfUuid = entityItem.getUuid();

        if (flockSizeSmallEnough(breedComponent, entityStore, entitiesInRange, npcTypeId)) {
            ArrayList<Ref<EntityStore>> validEntitiesForBreeding = new ArrayList<>();

            for (Ref<EntityStore> entityRef : entitiesInRange) {
                NPCEntity testEntity = entityStore.getComponent(entityRef, NPCEntity.getComponentType());

                // TODO: This will need to be grabbed dynamically
                if(testEntity == null
                        || (
                            !testEntity.getNPCTypeId().equals(entity.getChildEntityTypeId())
                                    && !testEntity.getNPCTypeId().equals(entity.getEntityTypeId()
                            )
                )) {
                    continue;
                }

                BreedableComponent partnerBreedable = entityStore.getComponent(entityRef, breedableComponentType);
                var potentialPartnerTransform = entityStore.getComponent(entityRef, TransformComponent.getComponentType());

                double distanceFromBreedable = this.calculateDistance(potentialPartnerTransform.getPosition(), entityTransform.getPosition());

                // Check to see if potential entity is within range and able to breed.
                // Add to list that will be filtered for closest one.
                if (partnerBreedable != null && distanceFromBreedable <= this.breedDistance) {
                    // Ensure that potential partner does not have a partner is can breed
                    if (partnerBreedable.canBreed() && partnerBreedable.getBreedingPartner().isEmpty()) {
                        validEntitiesForBreeding.add(entityRef);
                    }
                }
            }

            // Determine who the closest valid entity to breed with is
            double currentClosestDistance = 999999;
            for(Ref<EntityStore> partnerRef : validEntitiesForBreeding) {
                TransformComponent transform = entityStore.getComponent(partnerRef, TransformComponent.getComponentType());
                NPCEntity npcEntity = entityStore.getComponent(partnerRef, NPCEntity.getComponentType());

                // Do not try to match with myself when breeding
                if(transform == null || npcEntity == null || npcEntity.getUuid().equals(selfUuid))
                    continue;

                if(potentialPartnerBreedable == null)
                    potentialPartnerBreedable = partnerRef;
                else{
                    TransformComponent checkTransform = entityStore.getComponent(potentialPartnerBreedable, TransformComponent.getComponentType());

                    double distanceTest = calculateDistance(checkTransform.getPosition(), transform.getPosition());

                    if(distanceTest < currentClosestDistance) {
                        currentClosestDistance = distanceTest;
                        potentialPartnerBreedable = partnerRef;
                    }
                }
            }

            // Found a partner to breed with.
            if (potentialPartnerBreedable != null) {
                BreedableComponent partnerBreedableComp = entityStore.getComponent(potentialPartnerBreedable, breedableComponentType);
                NPCEntity breedPartnerEntity = entityStore.getComponent(potentialPartnerBreedable, NPCEntity.getComponentType());
                var partnerUuid = breedPartnerEntity.getUuid();

                // Set breeding partners, only set one as pregnant
                breedComponent.setBreedingPartner(breedPartnerEntity.getUuid().toString());
                partnerBreedableComp.setBreedingPartner(entityItem.getUuid().toString());

                // If the partner is NOT pregnant, make them pregnant.
                // If partner is pregnant, mark as having bred
                if(!partnerBreedableComp.getIsPregnant() && !breedComponent.getIsPregnant()) {
                    breedComponent.getPregnant();
                    partnerBreedableComp.doBreeding();
                }
                else {
                    // Partner is pregnant, just need to breed with them
                    breedComponent.doBreeding();
                }
            }
        }
    }

    private boolean flockSizeSmallEnough(
            BreedableComponent breedComponent,
            Store<EntityStore> entityStore,
            List<Ref<EntityStore>> entitiesInRange,
            String npcTypeId
    ){
        int currentSizeInFlock = 0;

        // If there was more than just the single entity, check to see if they match the same type or not
        if (entitiesInRange.size() >= 1) {
            // Check for any entities that match the breedable or it's children, for flock size checking
            for (Ref<EntityStore> entityRef : entitiesInRange) {
                NPCEntity npcEntity = entityStore.getComponent(entityRef, NPCEntity.getComponentType());

                if (npcEntity != null) {
                    String checkingNpcTypeId = npcEntity.getNPCTypeId();

                    // If entity type matches the breedable's child or current type, add as flock size
                    // TODO: Determine how to dynamically check for the Parent and the Child
                    if (checkingNpcTypeId.equals(npcTypeId) || checkingNpcTypeId.equals(breedComponent.getChildTypeId())) {
                        currentSizeInFlock++;
                    }
                }
            }
        }

        // Flock size is too big currently, allowing breeding
        if (currentSizeInFlock >= breedComponent.getMaxFlockSize()) {
            return false;
        }

        return true;
    }

    private double calculateDistance(Vector3d pos1, Vector3d pos2) {
        double dx = pos2.getX() - pos1.getX();
        double dy = pos2.getY() - pos1.getY();
        double dz = pos2.getZ() - pos1.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
