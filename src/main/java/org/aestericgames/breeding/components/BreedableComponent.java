package org.aestericgames.breeding.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.aestericgames.breeding.models.data.BreedableEntity;

import javax.annotation.Nullable;

public class BreedableComponent implements Component<EntityStore> {
    public static final BuilderCodec<BreedableComponent> CODEC = BuilderCodec.builder(BreedableComponent.class, BreedableComponent::new)
            .append(new KeyedCodec<>("BreedingCooldownTicksRemaining", Codec.INTEGER),
                    (config, breedingCooldownTicks, info) -> config.breedingCooldownTicksRemaining = breedingCooldownTicks, // SETTER
                    (config, info) -> config.breedingCooldownTicksRemaining)
            .add()
            .append(new KeyedCodec<>("BaseBreedingCooldown", Codec.INTEGER),
                    (config, baseBreedingTicksCooldown, info) -> config.baseBreedingTicksCooldown = baseBreedingTicksCooldown, // SETTER
                    (config, info) -> config.baseBreedingTicksCooldown)
            .add()
            .append(new KeyedCodec<>("TicksUntilDomesticated", Codec.INTEGER),
                    (config, ticksUntilDomesticated, info) -> config.ticksUntilDomesticated = ticksUntilDomesticated, // SETTER
                    (config, info) -> config.ticksUntilDomesticated)
            .add()
            .append(new KeyedCodec<>("BaseDomesticationTime", Codec.INTEGER),
                    (config, baseTicksUntilDomesticated, info) -> config.baseTicksUntilDomesticated = baseTicksUntilDomesticated, // SETTER
                    (config, info) -> config.baseTicksUntilDomesticated)
            .add()
            .append(new KeyedCodec<>("IsBeingDomesticated", Codec.BOOLEAN),
                    (config, isBeingDomesticated, info) -> config.isBeingDomesticated = isBeingDomesticated, // SETTER
                    (config, info) -> config.isBeingDomesticated)
            .add()
            .append(new KeyedCodec<>("MaxFlockSize", Codec.INTEGER),
                    (config, maxFlockSize, info) -> config.maxFlockSize = maxFlockSize, // SETTER
                    (config, info) -> config.maxFlockSize)
            .add()
            .append(new KeyedCodec<>("ChildEntityTypeId", Codec.STRING),
                    (config, childTypeId, info) -> config.childTypeId = childTypeId, // SETTER
                    (config, info) -> config.childTypeId)
            .add()
            .append(new KeyedCodec<>("BabyBirthTicksRemaining", Codec.INTEGER),
                    (config, babyBirthTicksRemaining, info) -> config.babyBirthTicksRemaining = babyBirthTicksRemaining, // SETTER
                    (config, info) -> config.babyBirthTicksRemaining)
            .add()
            .append(new KeyedCodec<>("BaseBirthingTime", Codec.INTEGER),
                    (config, baseBabyBirthTicks, info) -> config.baseBabyBirthTicks = baseBabyBirthTicks, // SETTER
                    (config, info) -> config.baseBabyBirthTicks)
            .add()
            .append(new KeyedCodec<>("IsPregnant", Codec.BOOLEAN),
                    (config, isPregnant, info) -> config.isPregnant = isPregnant, // SETTER
                    (config, info) -> config.isPregnant)
            .add()
            .append(new KeyedCodec<>("PartnerUuid", Codec.STRING),
                    (config, partnerUuid, info) -> config.partnerUuid = partnerUuid, // SETTER
                    (config, info) -> config.partnerUuid)
            .add()
            .append(new KeyedCodec<>("BeingConsideredForPartner", Codec.BOOLEAN),
                    (config, beingConsideredForPartner, info) -> config.beingConsideredForPartner = beingConsideredForPartner, // SETTER
                    (config, info) -> config.beingConsideredForPartner)
            .add()
            .append(new KeyedCodec<>("HeadingToParter", Codec.BOOLEAN),
                    (config, headingToPartner, info) -> config.headingToPartner = headingToPartner, // SETTER
                    (config, info) -> config.headingToPartner)
            .add()
            .build();

    public static final HytaleLogger LOGGER = HytaleLogger.get("HusbandryAlmanac");

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
    private Ref<EntityStore> partnerRef;
    private boolean headingToPartner;

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
        headingToPartner = false;
        this.partnerRef = null;
    }

    public BreedableComponent(BreedableEntity bEntity){
        baseBreedingTicksCooldown = bEntity.getBaseBreedingCooldown();
        baseBabyBirthTicks = bEntity.getBaseBirthingTime();
        baseTicksUntilDomesticated = bEntity.getBaseDomesticationTime();
        maxFlockSize = bEntity.getMaxFlockSize();
        childTypeId = bEntity.getChildEntityTypeId();
        beingConsideredForPartner = false;
        isPregnant = false;
        isBeingDomesticated = false;
        partnerUuid = "";
        partnerRef = null;

        // Set the base values so we don't breed immediatelly
        breedingCooldownTicksRemaining = bEntity.getBaseBreedingCooldown();
        babyBirthTicksRemaining = bEntity.getBaseBirthingTime();
        ticksUntilDomesticated = bEntity.getBaseDomesticationTime();
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
        partnerRef = comp.partnerRef;
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

    public Ref<EntityStore> getPartnerReference(){
        return this.partnerRef;
    }

    public void setPartnerReference(Ref<EntityStore> partnerRef) {
        this.partnerRef = partnerRef;
    }

    public boolean getHeadingToPartner() {
        return this.headingToPartner;
    }

    public void setHeadingToPartner(boolean headToPartner) {
        this.headingToPartner = headToPartner;
    }

    public void setIsBeingConsidered(boolean beingConsidered){
        this.beingConsideredForPartner = beingConsidered;
    }

    public boolean getIsBeingConsidered(){
        return this.beingConsideredForPartner;
    }

    public void setBreedingPartnerUuid(String partnerUuid) {
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
        this.partnerRef = null;
    }

    public void doBreeding() {
        this.isPregnant = false;
        this.headingToPartner = false;
        this.breedingCooldownTicksRemaining = this.baseBreedingTicksCooldown;
        this.partnerRef = null;
    }

    public void getPregnant() {
        this.isPregnant = true;
        this.headingToPartner = false;
        this.babyBirthTicksRemaining = this.baseBabyBirthTicks;
    }
}