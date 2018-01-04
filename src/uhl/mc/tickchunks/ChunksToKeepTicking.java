package uhl.mc.tickchunks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import net.minecraft.server.ChunkCoordIntPair;

/**
 * Manages the collection of chunk coordinates that are to be kept ticking. Such an object can be constructed from a
 * {@link YamlConfiguration} object if such object was produced by an instance of this class.
 */
public class ChunksToKeepTicking {
    private final Map<Integer, Set<ChunkCoordIntPair>> coordinatesOfChunksToKeepTickingPerWorldDimension;
    private static final String DATA_KEY = "world-dimensions";
    private static final String X_KEY = "x";
    private static final String Z_KEY = "z";

    public ChunksToKeepTicking() {
        coordinatesOfChunksToKeepTickingPerWorldDimension = Collections.synchronizedMap(new HashMap<>());
    }

    void addChunk(int worldDimension, int x, int z) {
        Set<ChunkCoordIntPair> set = coordinatesOfChunksToKeepTickingPerWorldDimension.get(worldDimension);
        if (set == null) {
            set = new HashSet<>();
            coordinatesOfChunksToKeepTickingPerWorldDimension.put(worldDimension, set);
        }
        set.add(new ChunkCoordIntPair(x, z));
    }

    void removeChunk(int worldDimension, int x, int z) {
        Set<ChunkCoordIntPair> set = coordinatesOfChunksToKeepTickingPerWorldDimension.get(worldDimension);
        if (set != null) {
            set.remove(new ChunkCoordIntPair(x, z));
            if (set.isEmpty()) {
                coordinatesOfChunksToKeepTickingPerWorldDimension.remove(worldDimension);
            }
        }
    }

    void load(YamlConfiguration data) {
        final ConfigurationSection coordinates = data.getConfigurationSection(DATA_KEY);
        for (final String dimensionNumberAsString : coordinates.getKeys(/* deep */ false)) {
            final Set<ChunkCoordIntPair> chunkCoordinatePairs = new HashSet<>();
            for (final Map<?, ?> coordinate : coordinates.getMapList(dimensionNumberAsString)) {
                final ChunkCoordIntPair c = new ChunkCoordIntPair(((Number) coordinate.get(X_KEY)).intValue(),
                        ((Number) coordinate.get(Z_KEY)).intValue());
                chunkCoordinatePairs.add(c);
            }
            coordinatesOfChunksToKeepTickingPerWorldDimension.put(Integer.valueOf(dimensionNumberAsString),
                    chunkCoordinatePairs);
        }
    }

    YamlConfiguration getData() {
        final YamlConfiguration result = new YamlConfiguration();
        final Map<String, List<Map<String, Integer>>> coordinatesAsMap = new HashMap<>();
        synchronized (coordinatesOfChunksToKeepTickingPerWorldDimension) {
            for (final Entry<Integer, Set<ChunkCoordIntPair>> c : coordinatesOfChunksToKeepTickingPerWorldDimension
                    .entrySet()) {
                final List<Map<String, Integer>> coordinatesList = new ArrayList<>();
                for (final ChunkCoordIntPair coord : c.getValue()) {
                    final Map<String, Integer> mapForCoordinate = new HashMap<>();
                    mapForCoordinate.put(X_KEY, coord.x);
                    mapForCoordinate.put(Z_KEY, coord.z);
                    coordinatesList.add(mapForCoordinate);
                }
                coordinatesAsMap.put(c.getKey().toString(), coordinatesList);
            }
        }
        result.createSection(DATA_KEY, coordinatesAsMap);
        return result;
    }

    public Iterable<Map.Entry<Integer, Set<ChunkCoordIntPair>>> getTickingChunksPerWorldDimension() {
        return Collections.unmodifiableMap(coordinatesOfChunksToKeepTickingPerWorldDimension).entrySet();
    }
}
