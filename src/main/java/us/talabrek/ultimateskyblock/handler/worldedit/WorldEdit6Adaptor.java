package us.talabrek.ultimateskyblock.handler.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.registry.WorldData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.handler.AsyncWorldEditHandler;
import us.talabrek.ultimateskyblock.player.PlayerPerk;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The World Edit 6.0 specific adaptations
 */
public class WorldEdit6Adaptor implements WorldEditAdaptor {
    private static final Logger log = Logger.getLogger(WorldEdit6Adaptor.class.getName());
    private WorldEditPlugin worldEditPlugin;

    public WorldEdit6Adaptor() {
    }

    @Override
    public void init(WorldEditPlugin worldEditPlugin) {
        this.worldEditPlugin = worldEditPlugin;
    }

    @Override
    public void loadIslandSchematic(File file, Location origin, PlayerPerk playerPerk) {
        log.finer("Trying to load schematic " + file);
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            BukkitWorld bukkitWorld = new BukkitWorld(origin.getWorld());
            ClipboardReader reader = ClipboardFormat.SCHEMATIC.getReader(in);

            WorldData worldData = bukkitWorld.getWorldData();
            Clipboard clipboard = reader.read(worldData);
            ClipboardHolder holder = new ClipboardHolder(clipboard, worldData);

            Player player = Bukkit.getPlayer(playerPerk.getPlayerInfo().getUniqueId());
            int maxBlocks = (255 * Settings.island_protectionRange * Settings.island_protectionRange);
            final EditSession editSession = AsyncWorldEditHandler.createEditSession(bukkitWorld, maxBlocks);
            editSession.enableQueue();
            editSession.setFastMode(true);
            Vector to = new Vector(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
            final Operation operation = holder
                    .createPaste(editSession, worldData)
                    .to(to)
                    .ignoreAirBlocks(false)
                    .build();
            Operations.completeBlindly(operation);
            AsyncWorldEditHandler.registerCompletion(player);
            editSession.flushQueue();
        } catch (IOException e) {
            uSkyBlock.log(Level.WARNING, "Unable to load schematic " + file, e);
        }
    }

}