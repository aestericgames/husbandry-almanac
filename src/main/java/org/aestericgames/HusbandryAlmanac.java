package org.aestericgames;

import com.google.gson.*;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.command.system.CommandRegistry;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.aestericgames.breeding.components.BreedableComponent;
import org.aestericgames.breeding.models.data.BreedableEntity;
import org.aestericgames.breeding.systems.BreedingSystem;
import org.aestericgames.breeding.systems.OnBreedSpawn;
import org.aestericgames.growing.models.data.GrowableEntity;
import org.aestericgames.growing.commands.*;
import org.aestericgames.growing.components.GrowingComponent;
import org.aestericgames.growing.systems.GrowingSystem;
import org.aestericgames.growing.systems.OnGrowingSpawn;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class HusbandryAlmanac extends JavaPlugin {
    private static HusbandryAlmanac instance;
    public static final HytaleLogger LOGGER = HytaleLogger.get("HusbandryAlmanac");
    private ComponentType<EntityStore, GrowingComponent> growingComponent;
    private ComponentType<EntityStore, BreedableComponent> breedableComponent;
    private HashMap<String, GrowableEntity> GrowableEntities = new HashMap<String, GrowableEntity>();
    private HashMap<String, BreedableEntity> BreedableEntities = new HashMap<String, BreedableEntity>();

    public HusbandryAlmanac(@NonNullDecl JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();
        instance = this;

        // Load configurations
        try {
            LoadGrowableEntities();
            LoadBreedableEntities();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        AddModelsToAssetMap();

        LOGGER.atInfo().log("HusbandryAlmanac - GrowableEntities loaded!");
        LOGGER.atInfo().log("HusbandryAlmanac - Number of GrowableEntities: " + GrowableEntities.size());

        RegisterEvents();
        RegisterCommands();
        RegisterGrowingSystem();
        RegisterBreedingSystem();
    }

    public static HusbandryAlmanac get(){
        return instance;
    }

    public ComponentType<EntityStore, GrowingComponent> getGrowingComponentType(){
        return this.growingComponent;
    }
    public ComponentType<EntityStore, BreedableComponent> getBreedableComponentType(){
        return this.breedableComponent;
    }

    public static Boolean IsGrowableEntity(String entityIndexId){
        return instance.GrowableEntities.containsKey(entityIndexId);
    }

    public static Boolean IsBreedableEntity(String entityTypeId){
        return instance.BreedableEntities.containsKey(entityTypeId);
    }

    public static GrowableEntity GetGrowableEntity(String entityIndexId){
        return instance.GrowableEntities.get(entityIndexId);
    }

    public static BreedableEntity GetBreedableEntity(String entityTypeId){
        return instance.BreedableEntities.get(entityTypeId);
    }

    private void LoadGrowableEntities() throws IOException {
        Gson gson = new Gson();

        LOGGER.atInfo().log("Attempting to load growable entities");
        try(var inputStream = getClass().getClassLoader().getResourceAsStream("HusbandryAlmanac/Growing/GrowableEntities.json")) {
            if(inputStream == null){
                LOGGER.atInfo().log("HusbandryAlmanac - InputStream for resource null.");
            }
            else {
                LOGGER.atInfo().log("HusbandryAlmanac - InputStream for resource loaded!");
            }

            byte[] bytes = inputStream.readAllBytes();
            String jsonString = new String(bytes, StandardCharsets.UTF_8);

            JsonObject obj = JsonParser.parseString(jsonString).getAsJsonObject();
            LOGGER.atInfo().log("HusbandryAlmanac - JSON parsed successfully!");

            JsonArray array = obj.getAsJsonArray("GrowableEntities");
            for(JsonElement element : array){
                GrowableEntity gEntity = parseGrowableEntity(element);
                GrowableEntities.put(gEntity.getEntityIndexId(),gEntity);
            }
        }
    }

    private void LoadBreedableEntities() throws IOException {
        Gson gson = new Gson();

        LOGGER.atInfo().log("Attempting to load breedable entities");
        try(var inputStream = getClass().getClassLoader().getResourceAsStream("HusbandryAlmanac/Breeding/BreedableEntities.json")) {
            if(inputStream == null){
                LOGGER.atInfo().log("HusbandryAlmanac - InputStream for Breedable Entities null.");
            }
            else {
                LOGGER.atInfo().log("HusbandryAlmanac - InputStream for Breedable Entities loaded!");
            }

            byte[] bytes = inputStream.readAllBytes();
            String jsonString = new String(bytes, StandardCharsets.UTF_8);

            JsonObject obj = JsonParser.parseString(jsonString).getAsJsonObject();
            LOGGER.atInfo().log("HusbandryAlmanac - JSON parsed successfully!");

            JsonArray array = obj.getAsJsonArray("BreedableEntities");
            for(JsonElement element : array){
                BreedableEntity bEntity = parseBreedableEntity(element);
                BreedableEntities.put(bEntity.getEntityTypeId(),bEntity);
            }
        }
    }

    private GrowableEntity parseGrowableEntity(JsonElement ele){
        JsonObject jObj = ele.getAsJsonObject();

        String entityIndexId = jObj.get("EntityIndexId").getAsString();
        String roleIndex = jObj.get("GrowIntoRoleIndexId").getAsString();
        String modelAsset = jObj.get("GrowIntoModelAsset").getAsString();
        int defaultTicksToGrow = jObj.get("DefaultTicksToGrow").getAsInt();

        return new GrowableEntity(
                entityIndexId,
                roleIndex,
                modelAsset,
                defaultTicksToGrow
        );
    }

    private BreedableEntity parseBreedableEntity(JsonElement ele){
        JsonObject jObj = ele.getAsJsonObject();

        String entityTypeId = jObj.get("EntityTypeId").getAsString();
        String childEntityTypeId = jObj.get("ChildEntityTypeId").getAsString();
        String childModelAsset = jObj.get("ChildModelAsset").getAsString();
        int baseBreedingCooldown = jObj.get("BaseBreedingCooldown").getAsInt();
        int baseBirthingTime = jObj.get("BaseBirthingTime").getAsInt();
        int baseDomesticationTime = jObj.get("BaseDomesticationTime").getAsInt();
        int maxFlockSize = jObj.get("MaxFlockSize").getAsInt();
        String pregnantModelName = jObj.get("PregnantModelName").getAsString();

        return new BreedableEntity(
                entityTypeId,
                childEntityTypeId,
                childModelAsset,
                baseBreedingCooldown,
                baseBirthingTime,
                baseDomesticationTime,
                maxFlockSize,
                pregnantModelName
        );
    }

    private void RegisterEvents() {
        // Example of registering to the EntityRemoveEvent event
//        this.getEventRegistry().registerGlobal(EntityRemoveEvent.class, event -> {
//            LOGGER.atInfo().log("HusbandryAlmanac - EntityRemoveEvent called. Event: " + event);
//        });
    }

    private void RegisterCommands(){
        CommandRegistry cmdReg = this.getCommandRegistry();

        cmdReg.registerCommand(new SpawnSheepLambCommand());
        cmdReg.registerCommand(new SpawnChickCommand());
        cmdReg.registerCommand(new SpawnPigPigletCommand());
        cmdReg.registerCommand(new SpawnCowCalfCommand());
        cmdReg.registerCommand(new SpawnHorseFoalCommand());
        cmdReg.registerCommand(new SpawnGoatKidCommand());
        cmdReg.registerCommand(new SpawnMouflonLambCommand());
        cmdReg.registerCommand(new SpawnRamLambCommand());
        cmdReg.registerCommand(new SpawnSkrillChickCommand());
        cmdReg.registerCommand(new SpawnDesertChickenChickCommand());
        cmdReg.registerCommand(new SpawnDesertChickenCommand());
        cmdReg.registerCommand(new SpawnWildPigletCommand());
        cmdReg.registerCommand(new SpawnWildPigCommand());
        cmdReg.registerCommand(new SpawnBunnyCommand());
        cmdReg.registerCommand(new SpawnPregnantChickenCommand());
        cmdReg.registerCommand(new SpawnPregnantCowCommand());
    }

    private void RegisterGrowingSystem(){
        // Register the Component(s) of the Growing System
        this.growingComponent = this.getEntityStoreRegistry()
                .registerComponent(GrowingComponent.class, GrowingComponent::new);

        // Register the System(s) of the Growing System
        this.getEntityStoreRegistry()
                .registerSystem(new GrowingSystem(this.growingComponent));

        this.getEntityStoreRegistry().registerSystem(new OnGrowingSpawn());
    }

    private void RegisterBreedingSystem() {
        this.breedableComponent = this.getEntityStoreRegistry().registerComponent(BreedableComponent.class, BreedableComponent::new);

        this.getEntityStoreRegistry().registerSystem(new BreedingSystem(this.breedableComponent));
        this.getEntityStoreRegistry().registerSystem(new OnBreedSpawn());
    }

//    private void AddModelsToAssetMap(){
////        DefaultAssetMap<String, ModelAsset> defaultModelAssetMap = ModelAsset.getAssetMap();
////        Map<String, ModelAsset> modelAssetMap = defaultModelAssetMap.getAssetMap();
////        modelAssetMap.put("Pregnant_Chicken",new ModelAsset());
//    }
}