package me.dkz.dev.nexusclan.nexus.command;

import me.dkz.dev.nexusclan.Main;
import me.dkz.dev.nexusclan.manager.ChunkManager;
import me.dkz.dev.nexusclan.nexus.NexusClan;
import me.dkz.dev.nexusclan.manager.NexusManager;
import me.dkz.dev.nexusclan.nexus.NexusMember;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class NexusCommands implements CommandExecutor {

    private Main plugin = Main.getInstance();
    private NexusManager nexusManager = plugin.getNexusManager();
    private ChunkManager chunkManager = plugin.getChunkManager();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser executado por um jogador.");
            return false;
        }

        Player player = (Player) sender;
        Optional<NexusClan> clan = nexusManager.getClan(player);

        if (clan.isEmpty()) {
            player.sendMessage("§cVocê não esta em um clan.");
            return false;
        }

        NexusClan nexusClan = clan.get();
        NexusMember member = nexusClan.getMember(player).get();

        if (member.getTag().getHierarchy() > 1) {
            player.sendMessage("§cVocê não tem permissão para isso.");
            return false;
        }


        if (nexusClan.getNexusCrystal() == null) {
                 if (chunkManager.forceClaim(player, nexusClan)) {
                    nexusClan.spawnNexusClan(player.getLocation());
                }

        } else {
            player.sendMessage("§cSeu clan já definiu o nexus.");
        }

        return false;
    }

}
