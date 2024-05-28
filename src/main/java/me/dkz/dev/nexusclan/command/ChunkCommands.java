package me.dkz.dev.nexusclan.command;

import me.dkz.dev.nexusclan.Main;
import me.dkz.dev.nexusclan.manager.ChunkManager;
import me.dkz.dev.nexusclan.manager.ChunkMap;
import me.dkz.dev.nexusclan.nexus.NexusClan;
import me.dkz.dev.nexusclan.manager.NexusManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class ChunkCommands implements CommandExecutor {

    private final Main plugin = Main.getInstance();
    private final NexusManager nexusManager = plugin.getNexusManager();
    private final ChunkManager chunkManager = plugin.getChunkManager();
    private ChunkMap chunkMap = plugin.getChunkMap();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser executado por um jogador.");
            return false;
        }

        Player player = (Player) sender;
        Optional<NexusClan> clan = nexusManager.getClan(player);


        chunkMap.sendMapToPlayer(player, clan);


        return true;
    }


}
