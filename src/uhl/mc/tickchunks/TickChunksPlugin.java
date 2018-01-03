package uhl.mc.tickchunks;

import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.plugin.java.JavaPlugin;

import net.minecraft.server.PlayerChunk;
import net.minecraft.server.PlayerChunkMap;
import net.minecraft.server.World;
import net.minecraft.server.WorldServer;

public class TickChunksPlugin extends JavaPlugin {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
        	sender.sendMessage("Usage: /tc [ k[eep] | r[elease] ]");
        } else {
	        if (sender instanceof CraftPlayer) {
	        	final CraftPlayer player = (CraftPlayer) sender;
	        	final World world = player.getHandle().world;
	        	if (world instanceof WorldServer) { // what else...?
	        		final WorldServer worldServer = (WorldServer) world;
	        		final PlayerChunkMap playerChunkMap = worldServer.getPlayerChunkMap();
	        		final Chunk chunk = player.getLocation().getChunk();
					final PlayerChunk playerChunk = playerChunkMap.getChunk(chunk.getX(), chunk.getZ());
					if (args[0].toLowerCase().startsWith("k")) {
						if (playerChunk.isKeptLoadedEvenIfEmpty()) {
							sender.sendMessage("Chunk ("+chunk.getX()+","+chunk.getZ()+") is already being kept in PlayerChunkMap.");
						} else {
							playerChunk.keepLoadedEvenIfEmpty();
							sender.sendMessage("Chunk ("+chunk.getX()+","+chunk.getZ()+") is now being kept in PlayerChunkMap even when no player has it in sight.");
						}
					} else if (args[0].toLowerCase().startsWith("r")) {
						if (!playerChunk.isKeptLoadedEvenIfEmpty()) {
							sender.sendMessage("Chunk ("+chunk.getX()+","+chunk.getZ()+") was not being kept in PlayerChunkMap.");
						} else {
							playerChunk.releaseIfEmpty();
							sender.sendMessage("Chunk ("+chunk.getX()+","+chunk.getZ()+") is now being released from PlayerChunkMap when no player has it in sight.");
						}
					} else {
						sender.sendMessage("Sub-Command "+args[0]+" not understood.");
					}
	        	}
	        }
        }
        return true;
	}

	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		super.onDisable();
	}

	@Override
	public void onEnable() {
		// TODO Auto-generated method stub
		super.onEnable();
	}

	@Override
	public void onLoad() {
		// TODO Auto-generated method stub
		super.onLoad();
	}

	@Override
	public void saveConfig() {
		// TODO Auto-generated method stub
		super.saveConfig();
	}

	@Override
	public void saveDefaultConfig() {
		// TODO Auto-generated method stub
		super.saveDefaultConfig();
	}

}
