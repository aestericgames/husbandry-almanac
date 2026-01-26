package org.aestericgames.growing.models.dto;

import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;

public class GrowDto {
    private int roleIndex;
    private ModelAsset modelAsset;
    private Model model;

    public GrowDto(
            int roleIndex,
            ModelAsset modelAsset,
            Model model
    ) {
        this.roleIndex = roleIndex;
        this.modelAsset = modelAsset;
        this.model = model;
    }

    // Getters
    public int getRoleIndex() {
        return roleIndex;
    }

    public ModelAsset getModelAsset(){
        return modelAsset;
    }

    public Model getModel(){
        return model;
    }
}
