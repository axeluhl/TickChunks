package uhl.mc.tickchunks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.PlayerChunk;
import org.bukkit.PlayerChunkMap;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class TickChunksPlugin extends JavaPlugin {
    private ChunksToKeepTicking chunksToKeepTicking;
    private boolean debug;

    public TickChunksPlugin() {
        chunksToKeepTicking = new ChunksToKeepTicking();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final boolean result;
        if (args.length == 1) {
            if (sender instanceof Player) {
                final Player player = (Player) sender;
                final org.bukkit.World world = player.getWorld();
                final Chunk chunk = player.getLocation().getChunk();
                int x = chunk.getX();
                int z = chunk.getZ();
                final PlayerChunkMap playerChunkMap = world.getPlayerChunkMap();
                final PlayerChunk playerChunk = playerChunkMap.getPlayerChunk(x, z);
                if (args[0].toLowerCase().startsWith("k")) {
                    result = true;
                    if (playerChunk.isKeptTickingEvenIfEmpty()) {
                        sender.sendMessage("Chunk (" + x + "," + z + ") is already being kept in PlayerChunkMap.");
                    } else {
                        keepChunkTicking(world.getName(), playerChunk);
                        playerChunk.keepTickingEvenIfEmpty();
                        sender.sendMessage("Chunk (" + x + "," + z
                                + ") is now being kept in PlayerChunkMap even when no player has it in sight.");
                        final Plugin keepChunksPlugin = Bukkit.getServer().getPluginManager()
                                .getPlugin("KeepChunks");
                        if (keepChunksPlugin != null) {
                            if (debug) {
                                getLogger()
                                        .info("Found KeepChunks plugin. Requesting chunk [" +
                                                playerChunk.getChunk().getX() + "," + playerChunk.getChunk().getZ() + "] to keep loaded");
                            }
                            String commandLine = "kc kc " + x + " " + z + " " + world.getName();
                            final boolean keepChunksResult = Bukkit.getServer().dispatchCommand(sender, commandLine);
                            if (debug) {
                                getLogger().info(
                                        "KeepChunks command \"" + commandLine + "\" returned " + keepChunksResult);
                            }
                        }
                    }
                } else if (args[0].toLowerCase().startsWith("r")) {
                    result = true;
                    if (!playerChunk.isKeptTickingEvenIfEmpty()) {
                        sender.sendMessage("Chunk (" + x + "," + z + ") was not being kept in PlayerChunkMap.");
                    } else {
                        releaseChunk(world.getName(), playerChunk);
                        sender.sendMessage("Chunk (" + x + "," + z
                                + ") is now being released from PlayerChunkMap when no player has it in sight.");
                        if (Bukkit.getServer().getPluginManager().getPlugin("KeepChunks") != null) {
                            sender.sendMessage(
                                    "Consider releasing the chunk also from the KeepChunks plugin using command /kc rc "
                                            + x + " " + z + " " + world.getName());
                        }
                    }
                } else {
                    sender.sendMessage("Sub-Command " + args[0] + " not understood.");
                    result = false;
                }
            } else {
                result = false;
            }
        } else {
            result = false;
        }
        return result;
    }

    private void keepChunkTicking(String worldName, PlayerChunk playerChunk) {
        playerChunk.keepTickingEvenIfEmpty();
        chunksToKeepTicking.addChunk(worldName, playerChunk.getChunk().getX(), playerChunk.getChunk().getZ());
        updateDataFile();
    }

    private void releaseChunk(String worldName, PlayerChunk playerChunk) {
        playerChunk.releaseIfEmpty();
        chunksToKeepTicking.removeChunk(worldName, playerChunk.getChunk().getX(), playerChunk.getChunk().getZ());
        updateDataFile();
    }

    private void updateDataFile() {
        try {
            final FileWriter dataFileWriter = new FileWriter(getDataFile());
            dataFileWriter.write(chunksToKeepTicking.getData().saveToString());
            dataFileWriter.close();
        } catch (IOException e) {
            getLogger().severe("Couldn't write to data file " + getDataFile());
        }
    }

    @Override
    public void onEnable() {
        if (debug) {
            getLogger().info("onEnable");
        }
        for (final Entry<String, Set<ChunkCoordinate>> chunkCoordinatesForWorldDimension : chunksToKeepTicking.getTickingChunksPerWorldName()) {
            final World worldServer = Bukkit.getServer().getWorld(chunkCoordinatesForWorldDimension.getKey().toString());
            if (worldServer != null) {
                for (final ChunkCoordinate coord : chunkCoordinatesForWorldDimension.getValue()) {
                    if (debug) {
                        getLogger().info("keeping chunk " + coord + " in world '"+worldServer.getName()+"' ticking");
                    }
                    worldServer.getPlayerChunkMap().keepPlayerChunkTicking(coord.getX(), coord.getZ());
                }
            } else {
                getLogger().warning("Couldn't keep chunks for world "+chunkCoordinatesForWorldDimension.getKey().toString()+
                        " ticking because world was not found");
            }
        }
    }

    @Override
    public void onLoad() {
        saveDefaultConfig();
        debug = getConfig().getBoolean("general.debug");
        if (debug) {
            getLogger().info("loading data...");
        }
        final File dataFile = getDataFile();
        try {
            if (dataFile.exists()) {
                final YamlConfiguration data = YamlConfiguration.loadConfiguration(new FileReader(dataFile));
                chunksToKeepTicking.load(data);
            }
        } catch (FileNotFoundException e) {
            getLogger().warning(
                    "The data file " + dataFile + " just existed but still cannot be found. Not loading any data.");
        }
    }

    private File getDataFile() {
        return new File(getDataFolder(), "data.yml");
    }
}
