package org.aestericgames.breeding.components;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.aestericgames.breeding.models.data.BreedableEntity;

import javax.annotation.Nullable;

public class BreedableComponent implements Component<EntityStore> {
    private int breedingCooldownTicksRemaining;
    private int baseBreedingTicksCooldown;
    private int ticksUntilDomesticated;
    private int baseTicksUntilDomesticated;
    private boolean isBeingDomesticated;
    private int maxFlockSize;
    private String childTypeId;
    private int babyBirthTicksRemaining;
    private int baseBabyBirthTicks;
    private boolean isPregnant;
    private String partnerUuid;
    private boolean beingConsideredForPartner;

    public BreedableComponent(){
        baseBreedingTicksCooldown = 36000;
        breedingCooldownTicksRemaining = 36000;
        ticksUntilDomesticated = 36000;
        baseTicksUntilDomesticated = 36000;
        isBeingDomesticated = false;
        maxFlockSize = 10;
        babyBirthTicksRemaining = 36000;
        baseBabyBirthTicks = 36000;
        beingConsideredForPartner = false;
        isPregnant = false;
        partnerUuid = "";
    }

    public BreedableComponent(BreedableEntity bEntity){
        baseBreedingTicksCooldown = bEntity.getBaseBreedingCooldown();
        isBeingDomesticated = false;
        baseTicksUntilDomesticated = bEntity.getBaseDomesticationTime();
        maxFlockSize = bEntity.getMaxFlockSize();
        baseBabyBirthTicks = bEntity.getBaseBirthingTime();
        childTypeId = bEntity.getChildEntityTypeId();
        isPregnant = false;
        partnerUuid = "";
        beingConsideredForPartner = false;
    }

    public BreedableComponent(BreedableComponent comp){
        baseBreedingTicksCooldown = comp.baseBreedingTicksCooldown;
        breedingCooldownTicksRemaining = comp.breedingCooldownTicksRemaining;
        ticksUntilDomesticated = comp.ticksUntilDomesticated;
        isBeingDomesticated = comp.isBeingDomesticated;
        baseTicksUntilDomesticated = comp.baseTicksUntilDomesticated;
        maxFlockSize = comp.maxFlockSize;
        babyBirthTicksRemaining = comp.babyBirthTicksRemaining;
        baseBabyBirthTicks = comp.baseBabyBirthTicks;
        isPregnant = comp.isPregnant;
        partnerUuid = comp.partnerUuid;
        beingConsideredForPartner = comp.beingConsideredForPartner;
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        return new BreedableComponent(this);
    }

    public void setChildTypeId(String childTypeId) {
        this.childTypeId = childTypeId;
    }

    public String getChildTypeId(){
        return this.childTypeId;
    }

    public void setIsBeingConsidered(boolean beingConsidered){
        this.beingConsideredForPartner = beingConsidered;
    }

    public boolean getIsBeingConsidered(){
        return this.beingConsideredForPartner;
    }

    public void setBreedingPartner(String partnerUuid) {
        this.partnerUuid = partnerUuid;
    }

    public String getBreedingPartner() {
        return this.partnerUuid;
    }

    public void decrementBreedableCooldown(){
            this.breedingCooldownTicksRemaining--;
    }

    public void decrementBirthingTime(){
            this.babyBirthTicksRemaining--;
    }

    public void tryDecrementDomesticatedTime(){
        if(this.isBeingDomesticated) {
            this.ticksUntilDomesticated--;
        }
    }

    public boolean canBreed() {
        return !this.isPregnant && this.breedingCooldownTicksRemaining <= 0;
    }

    public boolean canBirth() {
        return this.babyBirthTicksRemaining <= 0;
    }

    public boolean getIsPregnant() {
        return this.isPregnant;
    }

    public int getMaxFlockSize() {
        return this.maxFlockSize;
    }

    public void doBirthChild() {
        this.isPregnant = false;
        this.breedingCooldownTicksRemaining = this.baseBreedingTicksCooldown;
        this.partnerUuid = "";
    }

    public void doBreeding() {
        this.isPregnant = false;
        this.breedingCooldownTicksRemaining = this.baseBreedingTicksCooldown;
        this.partnerUuid = "";
    }

    public void getPregnant() {
        this.isPregnant = true;
        this.babyBirthTicksRemaining = this.baseBabyBirthTicks;
    }
}