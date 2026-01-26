package org.aestericgames.growing.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.aestericgames.growing.models.data.GrowableEntity;

import javax.annotation.Nullable;

import static com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil.getLogger;

public class GrowingComponent implements Component<EntityStore> {
    private int ticksToGrow;
    private int remainingGrowingTicks;

    public GrowingComponent() {
//        this(24000);
        this(1800); // 1 minute
    }

    public GrowingComponent(GrowingComponent component){
        this.ticksToGrow = component.getRemainingGrowingTicks();
        this.remainingGrowingTicks = component.getRemainingGrowingTicks();
    }

    public GrowingComponent(GrowableEntity gEntity){
        this(gEntity.getDefaultTicksToGrowToGrow());
    }

    public GrowingComponent(int ticksToGrow) {
        this.ticksToGrow = ticksToGrow;
        this.remainingGrowingTicks = ticksToGrow;

//        getLogger().info("GrowingComponent ticksToGrow constructor called.");
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
//        getLogger().info("GrowingComponent ticksToGrow clone called.");
        return new GrowingComponent(this);
    }

    // Getters
    public int getRemainingGrowingTicks(){
        return this.remainingGrowingTicks;
    }

    // Setters

    public void decrementGrowTime(){
//        getLogger().info("GrowingComponent decrementGrowTime called.");
        this.remainingGrowingTicks--;
    }

    public boolean isGrownUp(){
        return this.remainingGrowingTicks <= 0;
    }
}
