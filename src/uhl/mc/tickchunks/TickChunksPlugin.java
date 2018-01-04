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
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.minecraft.server.ChunkCoordIntPair;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerChunk;
import net.minecraft.server.PlayerChunkMap;
import net.minecraft.server.World;
import net.minecraft.server.WorldServer;

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
            if (sender instanceof CraftPlayer) {
                final CraftPlayer player = (CraftPlayer) sender;
                final World world = player.getHandle().world;
                if (world instanceof WorldServer) { // what else...?
                    final Chunk chunk = player.getLocation().getChunk();
                    int x = chunk.getX();
                    int z = chunk.getZ();
                    final WorldServer worldServer = (WorldServer) world;
                    final PlayerChunkMap playerChunkMap = worldServer.getPlayerChunkMap();
                    final PlayerChunk playerChunk = playerChunkMap.getChunk(x, z);
                    if (args[0].toLowerCase().startsWith("k")) {
                        result = true;
                        if (playerChunk.isKeptTickingEvenIfEmpty()) {
                            sender.sendMessage("Chunk (" + x + "," + z + ") is already being kept in PlayerChunkMap.");
                        } else {
                            keepChunkTicking(((WorldServer) world).dimension, playerChunk);
                            playerChunk.keepTickingEvenIfEmpty();
                            sender.sendMessage("Chunk (" + x + "," + z
                                    + ") is now being kept in PlayerChunkMap even when no player has it in sight.");
                            final Plugin keepChunksPlugin = Bukkit.getServer().getPluginManager()
                                    .getPlugin("KeepChunks");
                            if (keepChunksPlugin != null) {
                                if (debug) {
                                    getLogger()
                                            .info("Found KeepChunks plugin. Requesting chunk [" + playerChunk.chunk.locX
                                                    + "," + playerChunk.chunk.locZ + "] to keep loaded");
                                }
                                String commandLine = "kc kc " + x + " " + z + " "
                                        + ((WorldServer) world).getWorldData().getName();
                                final boolean keepChunksResult = Bukkit.getServer().dispatchCommand(sender,
                                        commandLine);
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
                            releaseChunk(((WorldServer) world).dimension, playerChunk);
                            sender.sendMessage("Chunk (" + x + "," + z
                                    + ") is now being released from PlayerChunkMap when no player has it in sight.");
                            if (Bukkit.getServer().getPluginManager().getPlugin("KeepChunks") != null) {
                                sender.sendMessage(
                                        "Consider releasing the chunk also from the KeepChunks plugin using command /kc rc "
                                                + x + " " + z + " " + ((WorldServer) world).getWorldData().getName());
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
        } else {
            result = false;
        }
        return result;
    }

    private void keepChunkTicking(int worldDimension, PlayerChunk playerChunk) {
        playerChunk.keepTickingEvenIfEmpty();
        chunksToKeepTicking.addChunk(worldDimension, playerChunk.chunk.locX, playerChunk.chunk.locZ);
        updateDataFile();
    }

    private void releaseChunk(int worldDimension, PlayerChunk playerChunk) {
        playerChunk.releaseIfEmpty();
        chunksToKeepTicking.removeChunk(worldDimension, playerChunk.chunk.locX, playerChunk.chunk.locZ);
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
        for (final Entry<Integer, Set<ChunkCoordIntPair>> chunkCoordinatesForWorldDimension : chunksToKeepTicking
                .getTickingChunksPerWorldDimension()) {
            @SuppressWarnings("deprecation") // how else can we obtain the server without using a deprecated method?
            final WorldServer worldServer = MinecraftServer.getServer()
                    .getWorldServer(chunkCoordinatesForWorldDimension.getKey());
            for (final ChunkCoordIntPair coord : chunkCoordinatesForWorldDimension.getValue()) {
                if (debug) {
                    getLogger().info("keeping chunk " + coord + " ticking");
                }
                worldServer.getPlayerChunkMap().keepPlayerChunkTicking(coord.x, coord.z);
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
