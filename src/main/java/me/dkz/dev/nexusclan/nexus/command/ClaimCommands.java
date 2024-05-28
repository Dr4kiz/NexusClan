package me.dkz.dev.nexusclan.nexus.command;

import me.dkz.dev.nexusclan.Main;
import me.dkz.dev.nexusclan.manager.ChunkMap;
import me.dkz.dev.nexusclan.manager.ChunkManager;
import me.dkz.dev.nexusclan.nexus.NexusClan;
import me.dkz.dev.nexusclan.manager.NexusManager;
import me.dkz.dev.nexusclan.utils.TextUtils;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class ClaimCommands implements CommandExecutor {
    private Main plugin = Main.getInstance();
    private NexusManager nexusManager = plugin.getNexusManager();
    private ChunkManager chunkManager = plugin.getChunkManager();
    private ChunkMap chunkMap = plugin.getChunkMap();
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser executado por um jogador.");
            return false;
        }

        Player player = (Player) sender;
        Optional<NexusClan> clan = nexusManager.getClan(player);

        if(clan.isEmpty()){
            return false;
        }


        try {
            int chunkX = Integer.parseInt(args[0]);
            int chunkZ = Integer.parseInt(args[1]);

            Chunk chunk = player.getLocation().getWorld().getChunkAt(chunkX, chunkZ);
            if(chunkManager.claimChunk(player, clan.get(), chunk)) {
                chunkMap.sendMapToPlayer(player, clan);
            }

        }catch (NumberFormatException e){

        }


        return false;
    }

}
