package org.aestericgames.breeding.models.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class BreedableEntity {
    public static final BuilderCodec<BreedableEntity> CODEC =
            BuilderCodec.builder(BreedableEntity.class, BreedableEntity::new)
                    .append(new KeyedCodec<>("EntityTypeId", Codec.STRING),
                            (config, entityTypeId, info) -> config.entityTypeId = entityTypeId, // Setter
                            (config, info) -> config.entityTypeId) // Getter
                    .add()
                    .append(new KeyedCodec<>("ChildEntityTypeId", Codec.STRING),
                            (config, childEntityTypeId, info) -> config.childEntityTypeId = childEntityTypeId, // Setter
                            (config, info) -> config.childEntityTypeId) // Getter
                    .add()
                    .append(new KeyedCodec<>("ChildModelAsset", Codec.STRING),
                            (config, childModelAsset, info) -> config.childModelAsset = childModelAsset, // Setter
                            (config, info) -> config.childModelAsset) // Getter
                    .add()
                    .append(new KeyedCodec<>("BaseBreedingCooldown",Codec.INTEGER),
                            (config, baseBreedingCooldown, info) -> config.baseBreedingCooldown = baseBreedingCooldown, // Setter
                            (config, info) -> config.baseBreedingCooldown) // Getter
                    .add()
                    .append(new KeyedCodec<>("BaseBirthingTime",Codec.INTEGER),
                            (config, baseBirthingTime, info) -> config.baseBirthingTime = baseBirthingTime, // Setter
                            (config, info) -> config.baseBirthingTime) // Getter
                    .add()
                    .append(new KeyedCodec<>("BaseDomesticationTime",Codec.INTEGER),
                            (config, baseDomesitcationTime, info) -> config.baseDomesitcationTime = baseDomesitcationTime, // Setter
                            (config, info) -> config.baseDomesitcationTime) // Getter
                    .add()
                    .append(new KeyedCodec<>("MaxFlockSize",Codec.INTEGER),
                            (config, maxFlockSize, info) -> config.maxFlockSize = maxFlockSize, // Setter
                            (config, info) -> config.maxFlockSize) // Getter
                    .add()
                    .build();

    private String entityTypeId;
    private String childEntityTypeId;
    private String childModelAsset;
    private int baseBreedingCooldown;
    private int baseBirthingTime;
    private int baseDomesitcationTime;
    private int maxFlockSize;

    public BreedableEntity(){

    }

    public String getEntityTypeId() {
        return this.entityTypeId;
    }

    public String getChildEntityTypeId() {
        return this.childEntityTypeId;
    }

    public String getChildModelAsset() {
        return this.childModelAsset;
    }

    public int getBaseBreedingCooldown() {
        return this.baseBreedingCooldown;
    }

    public int getBaseBirthingTime() {
        return this.baseBirthingTime;
    }

    public int getBaseDomesticationTime() {
        return this.baseDomesitcationTime;
    }

    public int getMaxFlockSize() {
        return this.maxFlockSize;
    }

    public BreedableEntity(
            String entityTypeId,
            String childEntityTypeId,
            String childModelAsset,
            int baseBreedingCooldown,
            int baseBirthingTime,
            int baseDomesticationTime,
            int maxFlockSize
    ) {
        this.entityTypeId = entityTypeId;
        this.childEntityTypeId = childEntityTypeId;
        this.childModelAsset = childModelAsset;
        this.baseBreedingCooldown = baseBreedingCooldown;
        this.baseBirthingTime = baseBirthingTime;
        this.baseDomesitcationTime = baseDomesticationTime;
        this.maxFlockSize = maxFlockSize;
    }
}
