package org.aestericgames.breeding.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.flock.FlockMembership;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.corecomponents.movement.BodyMotionWander;
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
        NPCEntity entityItem = commandBuffer.getComponent(ref, NPCEntity.getComponentType());
        TransformComponent entityTransform = commandBuffer.getComponent(ref, TransformComponent.getComponentType());

        // Section below is wrapped in a try...catch due to issues with executing a Tick while an entity dies
        // until I can figure out how to cleanly (and correctly) handle mid-actions when the ref dies, this is to
        // prevent killed breedables from crashing the server
        try {
            if (entityItem != null && breedComponent != null) {
                if (HusbandryAlmanac.IsBreedableEntity(entityItem.getNPCTypeId())) {
                    BreedableEntity bEntity = HusbandryAlmanac.GetBreedableEntity(entityItem.getNPCTypeId());

                    if (bEntity == null)
                        throw new RuntimeException("Unexpected Breedable Entity that was not registered.");

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
                    }
                    else if (flockSizeUnderLimit(store, ref)) {
                        if(breedComponent.getHeadingToPartner()) {
                            // Get partners position and move towards it
                            Ref<EntityStore> partnerRef = breedComponent.getPartnerReference();

                            // Partner may have died
                            if (partnerRef == null)
                                return;

                            TransformComponent partnerTransform = commandBuffer.getComponent(partnerRef, TransformComponent.getComponentType());
                            double distanceFromPartner = this.calculateDistance(partnerTransform.getPosition(), entityTransform.getPosition());

                            if (distanceFromPartner > breedDistance)
                                return;
                        }

                        breedComponent.decrementBreedableCooldown();

                        if (breedComponent.canBreed()) {
                            handleGetPartner(entityItem, entityTransform, breedComponent, commandBuffer);
                            handleTryBreed(store, ref, breedComponent.getPartnerReference());
                        }
                    }
                }
            }
        }
        catch (Exception eX){
            // TODO: Possibly log error
            // This is a safe catch because entities dying (by player or NPC) can cause parts of this to break, despite checks
            // Will remove this try...catch if a safer way can be found to ensure components for checks
        }
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(this.breedableComponentType);
    }

    private void handleGetPartner(
            NPCEntity entityItem,
            TransformComponent entityTransform,
            BreedableComponent breedComponent,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        World world = entityItem.getWorld();

        // Get Entity Looking for Partner
        String npcTypeId = entityItem.getNPCTypeId();
        Ref<EntityStore> entityRef = entityItem.getReference();

        Store<EntityStore> entityStore = world.getEntityStore().getStore();
        Ref<EntityStore> potentialPartnerBreedable = null;
        UUIDComponent entityUuidComponent = entityStore.getComponent(entityItem.getReference(), UUIDComponent.getComponentType());
        UUID selfUuid = entityUuidComponent.getUuid();

        ArrayList<Ref<EntityStore>> speciesInFlockRange = getSpeciciesInFlockCheckRange(entityTransform, entityStore, npcTypeId, breedComponent);

        // Determine who the closest valid entity to breed with is
        double currentClosestDistance = 999999;
        for(Ref<EntityStore> partnerRef : speciesInFlockRange) {
            BreedableComponent potentialPartnerBreedComp = entityStore.getComponent(partnerRef, breedableComponentType);

            // Potential partners need to be a breedable entity of the same species
            if(potentialPartnerBreedComp == null)
                continue;

            TransformComponent potentialPartnerTransform = entityStore.getComponent(partnerRef, TransformComponent.getComponentType());
            NPCEntity partnerNpcEntity = entityStore.getComponent(partnerRef, NPCEntity.getComponentType());
            UUIDComponent npcUuid = entityStore.getComponent(partnerRef, UUIDComponent.getComponentType());

            // Do not try to match with myself when breeding
            if(potentialPartnerTransform == null || partnerNpcEntity == null || npcUuid.equals(selfUuid))
                continue;

            if(potentialPartnerBreedable == null)
                potentialPartnerBreedable = partnerRef;
            else{
                TransformComponent checkTransform = entityStore.getComponent(potentialPartnerBreedable, TransformComponent.getComponentType());

                double distanceTest = calculateDistance(checkTransform.getPosition(), potentialPartnerTransform.getPosition());

                if(distanceTest < currentClosestDistance) {
                    currentClosestDistance = distanceTest;
                    potentialPartnerBreedable = partnerRef;
                }
            }
        }

        // Found the closest partner.
        // Now assign each other as partners
        if (potentialPartnerBreedable != null) {
            BreedableComponent partnerBreedableComp = entityStore.getComponent(potentialPartnerBreedable, breedableComponentType);

            // Set breeding partners, only set one as pregnant
            breedComponent.setPartnerReference(potentialPartnerBreedable);
            partnerBreedableComp.setPartnerReference(entityRef);
        }
    }

    private boolean flockSizeUnderLimit
    (
            Store<EntityStore> eStore,
            Ref<EntityStore> checkingEntity
    )
    {
        TransformComponent entityTf = eStore.getComponent(checkingEntity, TransformComponent.getComponentType());

        if(entityTf == null)
            return false;

        NPCEntity checkingNpcEntity = eStore.getComponent(checkingEntity, NPCEntity.getComponentType());
        BreedableComponent breedComponent = eStore.getComponent(checkingEntity, breedableComponentType);

        int currentFlockSize = 0;
        ArrayList<Ref<EntityStore>> speciesInRange = getSpeciciesInFlockCheckRange(entityTf, eStore, checkingNpcEntity.getNPCTypeId(),breedComponent);

        if (!speciesInRange.isEmpty()) {
            // Check for any entities that match the breedable or it's children, for flock size checking
            for (Ref<EntityStore> entityRef : speciesInRange) {
                NPCEntity npcEntity = eStore.getComponent(entityRef, NPCEntity.getComponentType());
                BreedableComponent eRefBreedComponent = eStore.getComponent(entityRef, breedableComponentType);
                String npcTypeId = checkingNpcEntity.getNPCTypeId();

                if (npcEntity != null) {
                    String checkingNpcTypeId = npcEntity.getNPCTypeId();

                    // If entity type matches the breedable's child or current type, add as flock size
                    if (
                            checkingNpcTypeId.equals(npcTypeId)
                                    || checkingNpcTypeId.equals(breedComponent.getChildTypeId())
                    ) {
                        currentFlockSize++;

                        // Count any unborn child entities as part of the flock
                        if (
                                checkingNpcTypeId.equals(npcTypeId)
                                && eRefBreedComponent.getIsPregnant()
                        ) {
                            currentFlockSize++;
                        }
                    }
                }
            }
        }

//        LOGGER.atInfo().log("BreedingSystem - FlockSizeUnderLimit Function - Flock Size: " + currentFlockSize);

        // Flock size is too big currently, allowing breeding
        if (currentFlockSize >= breedComponent.getMaxFlockSize()) {
            return false;
        }

        return true;
    }

    private void handleTryBreed(
            Store<EntityStore> entityStore,
            Ref<EntityStore> mainEntityRef,
            Ref<EntityStore> partnerEntityRef
    ) {
        BreedableComponent breedComponent = entityStore.getComponent(mainEntityRef, breedableComponentType);
        TransformComponent entityTransform = entityStore.getComponent(mainEntityRef, TransformComponent.getComponentType());

        if(breedComponent == null || entityTransform == null)
            return;

        // Determine if partner is in range. If not, start heading towards each other
        TransformComponent potentialPartnerTransform = entityStore.getComponent(partnerEntityRef, TransformComponent.getComponentType());
        double distanceFromBreedable = this.calculateDistance(potentialPartnerTransform.getPosition(), entityTransform.getPosition());

        // If we are not in range for breeding, set heading towards each other.
        // If we are in range, perform breeding.
        if (distanceFromBreedable >= this.breedDistance) {
            BreedableComponent partnerBreedableComp = entityStore.getComponent(partnerEntityRef, breedableComponentType);

            breedComponent.setHeadingToPartner(true);
            partnerBreedableComp.setHeadingToPartner(true);
        } else {
            BreedableComponent partnerBreedableComp = entityStore.getComponent(partnerEntityRef, breedableComponentType);

            // If the partner is NOT pregnant, make them pregnant.
            // If partner is pregnant, mark as having bred
            if(!partnerBreedableComp.getIsPregnant() && !breedComponent.getIsPregnant()) {
                breedComponent.getPregnant();
                partnerBreedableComp.doBreeding();
            }
            else if(partnerBreedableComp.getIsPregnant()) {
                // Partner is pregnant, just need to breed with them
                breedComponent.doBreeding();
            }

            NPCEntity bunnyEntityTest = entityStore.getComponent(partnerEntityRef, NPCEntity.getComponentType());
            if(bunnyEntityTest != null && bunnyEntityTest.getNPCTypeId().equals("Rabbit")) {
                LOGGER.atInfo().log("BreedingSystem - Handle Breeding - A rabbit has gotten pregnant!.");
            }
        }
    }

    private ArrayList<Ref<EntityStore>> getSpeciciesInFlockCheckRange(
            TransformComponent checkingEntityTransform,
            Store<EntityStore> eStore,
            String npcTypeId,
            BreedableComponent breedComponent
    ){
        List<Ref<EntityStore>> entitiesInRange = TargetUtil.getAllEntitiesInSphere(checkingEntityTransform.getPosition(), flockCheckRange, eStore);
        ArrayList<Ref<EntityStore>> result = new ArrayList<Ref<EntityStore>>();

        for (Ref<EntityStore> entityRef : entitiesInRange) {
            NPCEntity npcEntity = eStore.getComponent(entityRef, NPCEntity.getComponentType());

            if(npcEntity == null)
                continue;

            String checkingNpcTypeId = npcEntity.getNPCTypeId();

            if (checkingNpcTypeId.equals(npcTypeId) || checkingNpcTypeId.equals(breedComponent.getChildTypeId())
            ) {
                result.add(entityRef);
            }
        }

        return result;
    }

    private double calculateDistance(Vector3d pos1, Vector3d pos2) {
        double dx = pos2.getX() - pos1.getX();
        double dy = pos2.getY() - pos1.getY();
        double dz = pos2.getZ() - pos1.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}