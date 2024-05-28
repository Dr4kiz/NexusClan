package me.dkz.dev.nexusclan.manager;

import me.dkz.dev.nexusclan.nexus.NexusClan;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ChunkMap {

    private final ChunkManager chunkManager;
    private final NexusManager nexusManager;
    public ChunkMap(ChunkManager chunkManager, NexusManager nexusManager) {
        this.chunkManager = chunkManager;
        this.nexusManager = nexusManager;
    }

    public void sendMapToPlayer(Player player, Optional<NexusClan> clan) {

        TextComponent[][] mapMatrix = generateMap(player, clan.orElse(null));

        player.spigot().sendMessage(new TextComponent("\n §eVisualização de Chunks \n"));

        for (int x = 0; x < mapMatrix.length; x++) {
            TextComponent[] row = mapMatrix[x];
            TextComponent rowComponent = new TextComponent("");

            TextComponent leadingSpaces = new TextComponent("  ");
            rowComponent.addExtra(leadingSpaces);

            for (int z = 0; z < row.length; z++) {
                TextComponent component = row[z];
                rowComponent.addExtra(component);
            }

            player.spigot().sendMessage(rowComponent);
        }
        TextComponent additionalComponent = new TextComponent("\n §b▉ Você §a▉ Seu Clan §c▉ Clan Inimigo\n");
        player.spigot().sendMessage(additionalComponent);
    }

    private TextComponent[][] generateMap(Player player, NexusClan nexusClan) {
        Location location = player.getLocation();
        int playerChunkX = location.getChunk().getX();
        int playerChunkZ = location.getChunk().getZ();
        int radius = 9;

        TextComponent[][] textComponents = new TextComponent[2 * radius + 1][2 * radius + 1];

        for (int z = -radius; z <= radius; z++) {
            for (int x = -radius; x <= radius; x++) {
                Chunk chunk = location.getWorld().getChunkAt(playerChunkX + x, playerChunkZ + z);
                TextComponent chunkComponent = getChunkComponent(player, chunk, nexusClan);
                textComponents[z + radius][x + radius] = chunkComponent;
            }
        }
        return textComponents;
    }

    private TextComponent getChunkComponent(Player player, Chunk chunk, NexusClan nexusClan) {
        String color = getChunkColor(player, chunk, nexusClan);
        TextComponent chunkComponent = new TextComponent(color + "▉");

        String hoverText;
        String description;

        if (player.getLocation().getChunk().equals(chunk)) {
            hoverText = "§eVocê";
            description = "§eSua posição";
        } else if (nexusClan != null) {
            Set<Chunk> clanChunks = chunkManager.getClanChunks(nexusClan);
            if (clanChunks.contains(chunk)) {
                hoverText = nexusClan.getFormmatedName();
                description = String.join(" ", nexusClan.getDescription());
            } else {
                Pair<String, String> clanInfo = checkOtherClansForChunk(chunk);
                hoverText = clanInfo.getLeft();
                description = clanInfo.getRight();
            }
        } else {
            Pair<String, String> clanInfo = checkOtherClansForChunk(chunk);
            hoverText = clanInfo.getLeft();
            description = clanInfo.getRight();
        }

        chunkComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).append("\n" + description).create()));
        chunkComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/claimar " + chunk.getX() + " " + chunk.getZ()));

        return chunkComponent;
    }

    private Pair<String, String> checkOtherClansForChunk(Chunk chunk) {
        Map<String, Set<Chunk>> claimedChunks = chunkManager.getClaimedChunks();
        for (String clan : claimedChunks.keySet()) {
            if (claimedChunks.get(clan).contains(chunk)) {
                Optional<NexusClan> otherClan = nexusManager.getClanByName(clan);
                if (otherClan.isPresent()) {
                    String hoverText = otherClan.get().getFormmatedName();
                    String description = String.join(" ", otherClan.get().getDescription());
                    return Pair.of(hoverText, description);
                }
            }
        }
        return Pair.of("§7Chunk não claimada", "§8Sem dono");
    }


    private String getChunkColor(Player player, Chunk chunk, NexusClan nexusClan) {


        if (player.getLocation().getChunk().equals(chunk)) return "§b";

        if (nexusClan == null) {
            Set<NexusClan> clans = nexusManager.getClans();
            for (NexusClan clan : clans) {
                Set<Chunk> clanChunks = chunkManager.getClanChunks(clan);
                if(clanChunks.contains(chunk)) return "§c";
            }

        } else {
            Set<Chunk> clanChunks = chunkManager.getClanChunks(nexusClan);
            if (clanChunks.contains(chunk)) return "§a";

            Set<NexusClan> clans = nexusManager.getClans();
            for (NexusClan clan : clans) {
                Set<Chunk> clansChunks = chunkManager.getClanChunks(clan);
                if(clansChunks.contains(chunk)) return "§c";
            }

        }

        return "§7";
    }
}
