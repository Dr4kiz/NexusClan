package me.dkz.dev.nexusclan.listener;

import me.dkz.dev.nexusclan.Main;
import me.dkz.dev.nexusclan.clan.ClanCreator;
import me.dkz.dev.nexusclan.clan.ClanInvites;
import me.dkz.dev.nexusclan.manager.ClanInvitesManager;
import me.dkz.dev.nexusclan.nexus.NexusClan;
import me.dkz.dev.nexusclan.manager.NexusManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.time.LocalDateTime;
import java.util.Optional;

public class NexusClanCreationListener implements Listener {
    private Main plugin = Main.getInstance();
    private NexusManager nexusManager = plugin.getNexusManager();
    private ClanCreator clanCreator = plugin.getClanCreator();
    private ClanInvitesManager clanInvitesManager = plugin.getClanInvitesManager();
    @EventHandler
    private void onPlayerChatMessage(AsyncPlayerChatEvent e) {

        Player player = e.getPlayer();

        if (clanCreator.getClanCreationQueue().contains(player)) {
            String message = e.getMessage();
            clanCreator.processClanCreation(player, message);
            e.setCancelled(true);
        }else if(clanInvitesManager.getClanInvitesQueue().contains(e.getPlayer())){

            if(e.getMessage().equalsIgnoreCase("cancelar")) {
                clanInvitesManager.stopInvite(player);
            }else {
                Optional<NexusClan> clan = nexusManager.getClan(e.getPlayer());
                Player invited = plugin.getServer().getPlayer(e.getMessage());

                if(invited != null){
                    ClanInvites invite = ClanInvites.builder().clan(clan.get()).invited(invited).inviter(clan.get().getMember(player).get()).invited_at(LocalDateTime.now()).build();
                    clanInvitesManager.invite(invited, invite);
                }else{
                    player.sendMessage("§cO jogador \""+e.getMessage()+"\" não foi encontrado.");
                }
            }



            e.setCancelled(true);
        }
    }

}
