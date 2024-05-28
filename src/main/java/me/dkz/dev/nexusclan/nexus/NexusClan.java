package me.dkz.dev.nexusclan.nexus;

import lombok.Data;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;

import java.util.*;

@Data
public class NexusClan {

    private String name;
    private String tag;
    private NexusMember owner;
    private List<String> description = List.of("§7§oDescrição indefinida");

    private NexusCrystal nexusCrystal;

    private Set<NexusMember> members = new HashSet<>();


    public void create(){
        owner.getPlayer().getPlayer().sendMessage("§aO clan §e["+tag+"] "+name+"§a foi criado com sucesso.");
        this.members.add(owner);
    }


    public void spawnNexusClan(Location location){
        World world  = ((CraftWorld)location.getWorld()).getHandle();
        this.nexusCrystal = new NexusCrystal(world,this);
        this.nexusCrystal.spawn(location);
    }

    public Optional<NexusMember> getMember(OfflinePlayer player){
        return members.stream().filter(nexusMember -> nexusMember.getPlayer().getUniqueId().equals(player.getUniqueId())).findFirst();
    }

    public String getFormmatedName() {
        return "§8["+this.tag+"] §7" + this.name;
    }


    public void broadcastMessage(String message){
        members.forEach(member -> {
            Player player = member.getPlayer().getPlayer();
            player.sendMessage(message);
        });
    }

    @Override
    public String toString(){
        return getFormmatedName();
    }

    public void joinMember(NexusMember nexusMember) {
        this.members.add(nexusMember);
        broadcastMessage("§a[+] §7O jogador "+nexusMember.getPlayer().getName()+" entrou no clan.");
    }

    public void removeMember(NexusMember nexusMember){
        this.members.remove(nexusMember);
        broadcastMessage("§c[-] §7O jogador "+nexusMember.getPlayer().getName()+" saiu no clan.");
    }

    public void undo() {
        broadcastMessage("§cO clan §f" + getFormmatedName()+ "§c foi desfeito.");
        this.members.clear();
        if(nexusCrystal != null) nexusCrystal.destroy();
    }

    public void destroy() {
        Bukkit.broadcastMessage("§cO clan " + getFormmatedName()+ "§c foi destruido.");
        this.members.clear();
        if(nexusCrystal != null) nexusCrystal.destroy();

    }


    public void kickMember(NexusMember member) {
        Player target = member.getPlayer().getPlayer();
        this.members.remove(member);
        broadcastMessage("§c[-] "+target.getDisplayName()+" foi expulso do clan");
        target.sendMessage("§cVocê foi expulso do clan "+getFormmatedName()+"§c.");

    }
}
