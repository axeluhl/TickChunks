# TickChunks
SpigotMC / Minecraft plugin that keeps ticking chunks

When all players move away further than the server viewing distance from a chunk then mobs in that chunk
including villagers will stop moving, and crops will stop growing. These activities depend on what is called
"random ticks" which are sent only to chunks within viewing distance of at least one player.

This plug-in allows you to mark chunks for which you would like those "random ticks" to continue being sent,
despite no player being near. For this, the plug-in introduces the "/tickchunks" (or /tc for short)
command. Used with the sub-command "keep" or "k" for short the command will mark the chunk the player
is currently in such that this chunk will keep receiving random ticks. Using with the sub-command "release"
or "r" for short, the player's current chunk will be released from this mechanism.

When used in conjunction with the "KeepChunks" plug-in (see https://www.spigotmc.org/resources/keepchunks.23307/)
this plug-in will automatically request that chunks marked with /tickchunks are also kept loaded using
the KeepChunks plug-in.

Technical note: this plug-in depends on a Spigot feature that is yet to be released with the standard Spigot
version. See https://hub.spigotmc.org/jira/browse/SPIGOT-3747 which describes the feature request, and
https://hub.spigotmc.org/stash/projects/SPIGOT/repos/spigot/pull-requests/78/overview which describes
the pull request that contains the Bukkit/CraftBukkit extensions required by this plug-in. Clone
https://donaldduck70@hub.spigotmc.org/stash/scm/~donaldduck70/spigot.git if you have access to the Spigot Hub,
or https://github.com/axeluhl/SPIGOT-3747.git for everyone else and checkout SPIGOT-3747,
download BuildTools.jar and run them. See also https://www.spigotmc.org/wiki/buildtools/.

```
    mkdir <my-spigot-dir>
    cd <my-spigot-dir>
    git clone https://github.com/axeluhl/SPIGOT-3747.git Spigot --branch SPIGOT-3747
    wget "https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar"
    java -jar BuildTools.jar --dont-update
```

This will give a spigot-X.YY.Z.jar with the necessary patches in place in your ``<my-spigot-dir>`` directory. Have fun.