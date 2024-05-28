package me.dkz.dev.nexusclan.manager;

import lombok.Data;
import me.dkz.dev.nexusclan.Main;
import me.dkz.dev.nexusclan.nexus.NexusClan;
import me.dkz.dev.nexusclan.utils.TextUtils;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.util.*;

@Data
public class ChunkManager {

    private final Main plugin = Main.getInstance();
    private final NexusManager nexusManager = plugin.getNexusManager();
    private final Map<String, Set<Chunk>> claimedChunks = new HashMap<>();

    private static final String NO_NEXUS_MESSAGE = "§cSeu clan não possuí um nexus.";
    private static final String CHUNK_ALREADY_CLAIMED_MESSAGE = "§cEssa chunk já está claimada.";

    public boolean claimChunk(Player player, NexusClan nexusClan, Chunk chunk) {
        if (nexusClan.getNexusCrystal() == null) {
            TextUtils.sendActionText(player, NO_NEXUS_MESSAGE);
            return false;
        }

        if (!hasClaimed(chunk)) {
            if(canClaim(nexusClan, chunk)){
                addChunks(nexusClan, chunk);
                return true;
            }else{
                TextUtils.sendActionText(player,"§cVocê não pode claimar essa chunk.");
                return false;
            }
        } else {
            TextUtils.sendActionText(player,CHUNK_ALREADY_CLAIMED_MESSAGE);
            return false;
        }
    }

    public boolean canClaim(NexusClan clan, Chunk chunk) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                Chunk adjacentChunk = chunk.getWorld().getChunkAt(chunk.getX() + dx, chunk.getZ() + dz);
                if (getClanChunks(clan).contains(adjacentChunk)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasClaimed(Chunk chunk) {
        return claimedChunks.values().stream().anyMatch(chunks -> chunks.contains(chunk));
    }

    private void addChunks(NexusClan clan, Chunk... chunks) {
        claimedChunks.computeIfAbsent(clan.getName(), k -> new HashSet<>()).addAll(Arrays.asList(chunks));
    }

    public Set<Chunk> getClanChunks(NexusClan clan) {
        return claimedChunks.getOrDefault(clan.getName(), Collections.emptySet());
    }

    public boolean forceClaim(Player player, NexusClan nexusClan){

        Chunk chunk = player.getLocation().getChunk();
        if (!hasClaimed(chunk)) {
            addChunks(nexusClan, chunk);
            return true;
        } else {
            TextUtils.sendActionText(player, "§cVocê não pode colocar o nexus aqui.");
            return false;
        }


    }

    public void addClaim(NexusClan nexusClan, Set<Chunk> chunks) {
        claimedChunks.put(nexusClan.getName(), chunks);
    }

    public void deleteChunks(NexusClan nexusClan) {
        Iterator<Map.Entry<String, Set<Chunk>>> iterator = claimedChunks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String , Set<Chunk>> entry = iterator.next();
            if (entry.getKey().equalsIgnoreCase(nexusClan.getName())) {
                iterator.remove();
                break;
            }
        }
    }
}
