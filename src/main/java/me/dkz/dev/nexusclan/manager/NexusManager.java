package me.dkz.dev.nexusclan.manager;

import lombok.Getter;
import me.dkz.dev.nexusclan.Main;
import me.dkz.dev.nexusclan.database.SQLite;
import me.dkz.dev.nexusclan.nexus.NexusClan;
import me.dkz.dev.nexusclan.nexus.NexusCrystal;
import me.dkz.dev.nexusclan.nexus.NexusMember;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;

import java.util.*;

@Getter
public class NexusManager {




    private Set<NexusClan> clans = new HashSet<>();
    private Main plugin = Main.getInstance();
    private SQLite sqLite = plugin.getSqLite();
    private ChunkManager chunkManager = plugin.getChunkManager();





    public Optional<NexusClan> getClan(OfflinePlayer player) {
        return clans.stream()
                .filter(clan -> clan.getMembers().stream()
                        .anyMatch(member -> member.getPlayer().getUniqueId().equals(player.getUniqueId())))
                .findFirst();
    }

    public Optional<NexusClan> getClanByTag(String tag) {
        return clans.stream()
                .filter(clan -> clan.getTag().equals(tag)).findFirst();
    }

    public Optional<NexusClan> getClanByName(String name) {
        return clans.stream()
                .filter(clan -> clan.getName().equals(name)).findFirst();
    }

    public void addClan(NexusClan clan){
        this.clans.add(clan);
    }


    public void loadClans(){
        List<NexusClan> clanList = sqLite.getClans();
        clanList.forEach(clan ->{
            if(clan.getNexusCrystal() != null) clan.getNexusCrystal().load();
        });
        this.clans.addAll(clanList);
    }



    public void leaveClan(NexusMember nexusMember) {
        NexusClan clan = nexusMember.getClan();
        nexusMember.getPlayer().getPlayer().sendMessage("§cVocê saiu do clan.");
        clan.removeMember(nexusMember);
        sqLite.deleteMember(nexusMember);
    }
    public void kickMember(NexusMember nexusMember){
        sqLite.deleteMember(nexusMember);
        nexusMember.getClan().kickMember(nexusMember);
    }

    public void undoClan(NexusClan nexusClan) {
        nexusClan.undo();
        removeClan(nexusClan);
    }

    public void destroyClan(NexusClan nexusClan) {
        removeClan(nexusClan);
        nexusClan.destroy();
    }

    public void removeClan(NexusClan nexusClan){
        Iterator<NexusClan> iterator = this.clans.iterator();
        while (iterator.hasNext()) {
            NexusClan clan = iterator.next();
            if (clan.equals(nexusClan)) {
                iterator.remove();
                break;
            }
        }
        chunkManager.deleteChunks(nexusClan);
        sqLite.deleteClan(nexusClan);
    }

    public Optional<NexusClan> getCrystal(NexusCrystal entity) {
        Optional<NexusClan> first = clans.stream().filter(clan -> clan.getNexusCrystal().isSameCrystal(entity.getBukkitEntity().getLocation())).findFirst();
        return first;

    }

    public Optional<NexusClan> getClanByChunk(Chunk chunk) {
        return chunkManager.getClaimedChunks().entrySet().stream()
                .filter(entry -> entry.getValue().contains(chunk))
                .map(entry -> getClanByName(entry.getKey()))
                .findFirst()
                .orElse(Optional.empty());
    }
}
