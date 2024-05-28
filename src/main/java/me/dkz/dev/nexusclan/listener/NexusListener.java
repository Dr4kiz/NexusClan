package me.dkz.dev.nexusclan.listener;

import me.dkz.dev.nexusclan.Main;
import me.dkz.dev.nexusclan.manager.NexusManager;
import me.dkz.dev.nexusclan.nexus.NexusClan;
import me.dkz.dev.nexusclan.utils.TextUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.*;

public class NexusListener implements Listener {

    private Main plugin = Main.getInstance();
    private NexusManager nexusManager = plugin.getNexusManager();


    @EventHandler
    void onNexusDamageEvent(EntityDamageByEntityEvent e){

    }

    private Set<Player> players = new HashSet<>();


    @EventHandler
    void onChunkChange(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        Location from = e.getFrom();
        Location to = e.getTo();

        if (from.getChunk().equals(to.getChunk())) {
            return;
        }

        String title = "§aArea Livre";
        String subtitle = "§eCuidado por onde anda";

        Optional<NexusClan> fromByChunk = nexusManager.getClanByChunk(from.getChunk());
        Optional<NexusClan> toByChunk = nexusManager.getClanByChunk(to.getChunk());

        if(fromByChunk.isPresent()){
            if(toByChunk.isPresent()){
                if(fromByChunk.get().getName().equalsIgnoreCase(toByChunk.get().getName())){
                    return;
                }
                title = toByChunk.get().getFormmatedName();
                subtitle = String.join(" ", toByChunk.get().getDescription());
            }
        } else if(toByChunk.isPresent()){
            title = toByChunk.get().getFormmatedName();
            subtitle = String.join(" ", toByChunk.get().getDescription());
        }

        if(fromByChunk.isEmpty() && toByChunk.isEmpty() && players.contains(player)) return;
        players.add(player);
        TextUtils.sendTitle(player, title, subtitle, 20, 60, 20);
    }
     
}
