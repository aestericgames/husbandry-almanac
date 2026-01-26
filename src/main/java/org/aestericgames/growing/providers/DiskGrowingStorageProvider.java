package org.aestericgames.growing.providers;

import com.hypixel.hytale.server.core.Constants;
import org.aestericgames.growing.storage.DiskGrowingStorage;

import java.nio.file.Path;

public class DiskGrowingStorageProvider {
    private Path path = Constants.UNIVERSE_PATH.resolve("husbandryalmanac/growing");
    private static DiskGrowingStorage storage;

    public DiskGrowingStorageProvider(){
        // TODO: Initialize if needed
    }

    // Getters
    public Path getPath(){
        return this.path;
    }

    public DiskGrowingStorage getGrowingStorage(){
        if(storage == null)
            storage = new DiskGrowingStorage(path);

        return storage;
    }
}
