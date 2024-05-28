package me.dkz.dev.nexusclan;

import com.henryfabio.minecraft.inventoryapi.manager.InventoryManager;
import lombok.Getter;
import me.dkz.dev.nexusclan.clan.command.ClanCommands;
import me.dkz.dev.nexusclan.clan.command.ClanCommandsComplete;
import me.dkz.dev.nexusclan.manager.ChunkManager;
import me.dkz.dev.nexusclan.clan.ClanCreator;
import me.dkz.dev.nexusclan.manager.ChunkMap;
import me.dkz.dev.nexusclan.manager.ClanInvitesManager;
import me.dkz.dev.nexusclan.command.*;
import me.dkz.dev.nexusclan.database.SQLite;
import me.dkz.dev.nexusclan.listener.NexusClanCreationListener;
import me.dkz.dev.nexusclan.listener.NexusListener;
import me.dkz.dev.nexusclan.manager.NexusManager;
import me.dkz.dev.nexusclan.nexus.*;
import me.dkz.dev.nexusclan.nexus.command.ClaimCommands;
import me.dkz.dev.nexusclan.nexus.command.NexusCommands;
import me.dkz.dev.nexusclan.utils.CustomEntities;
import me.dkz.dev.nexusclan.utils.TextUtils;
import net.minecraft.server.v1_8_R3.EntityEnderCrystal;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.logging.Level;

@Getter
public class Main extends JavaPlugin implements Listener {

    private final long tick = System.currentTimeMillis();

    private NexusManager nexusManager;
    private ClanCreator clanCreator;
    private ClanInvitesManager clanInvitesManager;
    private ChunkManager chunkManager;
    private ChunkMap chunkMap;

    private SQLite sqLite;

    public static Main getInstance() {
        return Main.getPlugin(Main.class);
    }


    @Override
    public void onLoad() {


        CustomEntities.registerEntity("customCrystal", 200, EntityEnderCrystal.class, NexusCrystal.class);
    }

    @Override
    public void onEnable() {

        InventoryManager.enable(this);


        saveDefaultConfig();
        chunkManager = new ChunkManager();

        sqLite = new SQLite();
        sqLite.createDatabase();
        nexusManager = new NexusManager();


        nexusManager.loadClans();


        clanCreator = new ClanCreator(this, nexusManager);
        clanInvitesManager = new ClanInvitesManager();

        chunkMap = new ChunkMap(chunkManager, nexusManager);

        getCommand("nexus").setExecutor(new NexusCommands());
        getCommand("clan").setExecutor(new ClanCommands());
        getCommand("clan").setTabCompleter(new ClanCommandsComplete());
        getCommand("nexusclear").setExecutor(this);
        getCommand("chunks").setExecutor(new ChunkCommands());
        getCommand("claimar").setExecutor(new ClaimCommands());

        getLogger().log(Level.INFO, "Plugin iniciado em {0} ms", System.currentTimeMillis() - tick);
        getServer().getPluginManager().registerEvents(new NexusClanCreationListener(), this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new NexusListener(), this);


    }


    @Override
    public void onDisable() {
        nexusManager.getClans().forEach(clan -> {
            int i = sqLite.saveClan(clan);
            clan.getMembers().forEach(member -> {
                sqLite.saveMember(member, i);
            });
            if (clan.getNexusCrystal() != null) {
                sqLite.saveNexus(clan.getNexusCrystal(), i);
                clan.getNexusCrystal().unload();
            }
            Set<Chunk> clanChunks = chunkManager.getClanChunks(clan);
            sqLite.saveChunks(clanChunks, clan.getName());
        });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player player = (Player) sender;

        if (label.equalsIgnoreCase("nexusclear")) {
            Bukkit.getServer().getWorlds().forEach(world -> world.getEntities()
                    .stream().filter(entity -> entity.getType().equals(EntityType.ENDER_CRYSTAL))
                    .forEach(Entity::remove));


            TextUtils.sendTitle(player, "§eLimpeza concluida", "§cEnderCrystals removidos do mundo", 20, 100, 20);
        }


        return super.onCommand(sender, command, label, args);
    }

    @EventHandler
    void onPlayerLeave(PlayerQuitEvent e) {
        if (clanCreator.getClanCreationQueue().contains(e.getPlayer())) {
            clanCreator.getClanCreationQueue().remove(e.getPlayer());
            clanCreator.getClanCreationSteps().remove(e.getPlayer());
        }
    }
}
