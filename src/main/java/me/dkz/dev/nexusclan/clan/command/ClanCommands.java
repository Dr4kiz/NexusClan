package me.dkz.dev.nexusclan.clan.command;

import me.dkz.dev.nexusclan.Main;
import me.dkz.dev.nexusclan.manager.ChunkManager;
import me.dkz.dev.nexusclan.clan.ClanCreator;
import me.dkz.dev.nexusclan.manager.ClanInvitesManager;
import me.dkz.dev.nexusclan.inventory.NexusClanInventory;
import me.dkz.dev.nexusclan.inventory.NexusClanInvitesInventory;
import me.dkz.dev.nexusclan.inventory.NexusClanMenuInventory;
import me.dkz.dev.nexusclan.nexus.NexusClan;
import me.dkz.dev.nexusclan.manager.NexusManager;
import me.dkz.dev.nexusclan.nexus.NexusMember;
import me.dkz.dev.nexusclan.nexus.NexusTag;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class ClanCommands implements CommandExecutor {

    private Main plugin = Main.getInstance();
    private NexusManager nexusManager = plugin.getNexusManager();
    private ClanCreator clanCreator = plugin.getClanCreator();
    private ClanInvitesManager clanInvitesManager = plugin.getClanInvitesManager();
    private ChunkManager chunkManager = plugin.getChunkManager();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser executado por um jogador.");
            return false;
        }

        Player player = (Player) sender;
        Optional<NexusClan> clan = nexusManager.getClan(player);

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("criar")) {
                if(clan.isPresent()){
                    player.sendMessage("§cPara criar um clan saia do seu atual.");
                    return false;
                }
                clanCreator.createClan(player);
            } else
             if (args[0].equalsIgnoreCase("convites")) {
                NexusClanInvitesInventory nexusClanInvitesInventory = new NexusClanInvitesInventory(null, clanInvitesManager);
                nexusClanInvitesInventory.openInventory(player);
            }else {
                 if(!clan.isPresent()){
                     player.sendMessage("§cVocê não esta em um clan.");
                 }else{
                     NexusMember nexusMember = clan.get().getMember(player).get();
                     NexusClan nexusClan = clan.get();
                     if (args[0].equalsIgnoreCase("convidar")) {
                         if(nexusMember.getTag().getHierarchy() < 3) {
                             clanInvitesManager.startInvite(player);
                         }else{
                             player.sendMessage("§cApenas membros podem convidar jogadores.");
                         }

                     } else if(args[0].equalsIgnoreCase("sair")){
                         if(nexusMember.getTag().equals(NexusTag.LEADER)){
                             player.sendMessage("§cVocê não pode sair do seu clan, desfaça-o para isso.");
                             return false;
                         }

                         nexusManager.leaveClan(nexusMember);

                     }else if (args[0].equalsIgnoreCase("desfazer")) {
                         if(!nexusMember.getTag().equals(NexusTag.LEADER)){
                             player.sendMessage("§cVocê não pode desfazer o seu clan.");
                             return false;
                         }
                         nexusManager.undoClan(clan.get());
                     }else if(args[0].equalsIgnoreCase("claimar")){
                         Chunk chunk = player.getLocation().getChunk();
                         chunkManager.claimChunk(player, nexusClan, chunk);

                     }else {
                         player.sendMessage("§cOps! argumentos inválidos.");
                     }
                 }
             }

        } else {
            clan.ifPresentOrElse(nexusClan -> {
                NexusMember member = nexusClan.getMember(player).get();
                NexusClanMenuInventory nexusClanMenuInventory = new NexusClanMenuInventory(clan.get(), member);
                nexusClanMenuInventory.openInventory(player);
            }, () -> {
                NexusClanInventory clanInventory = new NexusClanInventory(clanCreator, clanInvitesManager);
                clanInventory.openInventory(player);
            });
        }

        return false;
    }
}
