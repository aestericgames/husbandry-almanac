package org.aestericgames.growing.models.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class WorldGrowingData {
    public static final BuilderCodec<WorldGrowingData> CODEC =
            BuilderCodec.builder(WorldGrowingData.class, WorldGrowingData::new)
            .append(new KeyedCodec<>("World", Codec.STRING),
                    (config, world, info) -> config.world = world, // Setter
                    (config, info) -> config.world) // Getter
            .add()
            .append(new KeyedCodec<>("Uid",Codec.STRING),
                    (config, uId, info) -> config.uId = uId, // Setter
                    (config, info) -> config.uId) // Getter
            .add()
            .append(
                    new KeyedCodec<>("TicksToGrow",Codec.INTEGER),
                    (config, ticks, info) -> config.ticksToGrow = ticks, // Setter
                    (config, info) -> config.ticksToGrow) // Getter
            .add()
            .build();

    private String world = "Test";
    private String uId = "";
    private int ticksToGrow = -1;

    public WorldGrowingData() {
    }

    public WorldGrowingData(String uId, int ticksToGrow){
        this.uId = uId;
        this.ticksToGrow = ticksToGrow;
    }

    // Getters
    public String getUid() {
        return this.uId;
    }

    public int getTicksToGrow() {
        return this.ticksToGrow;
    }

    public String getWorld() {
        return this.world;
    }
}
