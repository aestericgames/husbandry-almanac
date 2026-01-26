package org.aestericgames.growing.storage;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DiskGrowingStorage {
    public static final String FILE_EXTENSION = ".json";

    private final Path path;

    public DiskGrowingStorage(@Nonnull Path path){
        this.path = path;

        try {
            Files.createDirectory(path);
        }
        catch(IOException eX){
            throw new RuntimeException("Failed to create husbandry almanac directory.",eX);
        }
    }
}
