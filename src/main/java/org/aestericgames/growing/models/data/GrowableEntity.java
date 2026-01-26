package org.aestericgames.growing.models.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class GrowableEntity {
    public static final BuilderCodec<GrowableEntity> CODEC =
            BuilderCodec.builder(GrowableEntity.class, GrowableEntity::new)
                    .append(new KeyedCodec<>("EntityIndexId", Codec.STRING),
                            (config, entityIndexId, info) -> config.entityIndexId = entityIndexId, // Setter
                            (config, info) -> config.entityIndexId) // Getter
                    .add()
                    .append(new KeyedCodec<>("GrowIntoRoleIndex", Codec.STRING),
                            (config, growIntoRoleIndex, info) -> config.growIntoRoleIndex = growIntoRoleIndex, // Setter
                            (config, info) -> config.growIntoRoleIndex) // Getter
                    .add()
                    .append(new KeyedCodec<>("GrowIntoModelAsset",Codec.STRING),
                            (config, growIntoModelAsset, info) -> config.growIntoModelAsset = growIntoModelAsset, // Setter
                            (config, info) -> config.growIntoModelAsset) // Getter
                    .add()
                    .append(new KeyedCodec<>("DefaultTicksToGrow",Codec.INTEGER),
                            (config, ticksToGrow, info) -> config.defaultTicksToGrow = ticksToGrow, // Setter
                            (config, info) -> config.defaultTicksToGrow) // Getter
                    .add()
                    .build();

    private String entityIndexId;
    private String growIntoRoleIndex;
    private String growIntoModelAsset;
    private int defaultTicksToGrow;

    public String getEntityIndexId(){
        return entityIndexId;
    }

    public String getGrowIntoRoleIndex(){
        return growIntoRoleIndex;
    }

    public String getGrowIntoModelAsset(){
        return growIntoModelAsset;
    }

    public int getDefaultTicksToGrowToGrow(){ return defaultTicksToGrow; }

    public GrowableEntity(){

    };

    public GrowableEntity(
        String entityIndexId,
        String growIntoRoleIndex,
        String growIntoModelAsset,
        int defaultTicksToGrow
    ){
        this.entityIndexId = entityIndexId;
        this.growIntoRoleIndex = growIntoRoleIndex;
        this.growIntoModelAsset = growIntoModelAsset;
        this.defaultTicksToGrow = defaultTicksToGrow;
    }
}
