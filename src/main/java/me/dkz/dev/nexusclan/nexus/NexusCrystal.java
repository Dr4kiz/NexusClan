package me.dkz.dev.nexusclan.nexus;

import lombok.Getter;
import me.dkz.dev.nexusclan.Main;
import me.dkz.dev.nexusclan.manager.NexusManager;
import me.dkz.dev.nexusclan.utils.TextUtils;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

@Getter
public class NexusCrystal extends EntityEnderCrystal {

    private NexusClan clan;
    private  final World world;
    private Chunk chunk;

    private double maxHealth = 1000;
    private double health = 1000;

    private NexusManager nexusManager = Main.getInstance().getNexusManager();

    public NexusCrystal(World world, NexusClan clan) {
        super(world);
        this.world = world;
        this.clan = clan;
        this.setCustomName(clan.getName());
    }



    public NexusCrystal(World world) {
        super(world);
        this.world = world;
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {

        if(!(damagesource.getEntity() instanceof EntityPlayer)){
            return false;
        }

        EntityPlayer entity = (EntityPlayer) damagesource.getEntity();
        Player player = entity.getBukkitEntity().getPlayer();

        if(clan.getMember(player).isPresent()){
            //player.sendMessage("§cVocê não pode interagir com o nexus");
            return false;
        }
        this.health -= f;
        if(this.health <= 0) {
            nexusManager.destroyClan(this.clan);
            return false;
        }
        TextUtils.sendActionText(player, String.format("§cVida: %.2f", health));
        return false;
    }

    @Override
    public void d(EntityHuman entityhuman) {
        if(clan == null) this.clan = nexusManager.getCrystal(this).get();
    }


    public void destroy() {
        if (chunk != null) {
            chunk.unload(true);
        }
        world.removeEntity(this);
        world.getWorld().strikeLightningEffect(this.bukkitEntity.getLocation());
    }

    public void unload(){
        world.removeEntity(this);
    }

    public void spawn(Location location) {
        int x = location.getBlockX(), y = location.getBlockY(), z = location.getBlockZ();
        setPosition(x, y, z);
        world.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }




    public boolean isSameCrystal(Location location){
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        return this.locX == x && this.locY == y && this.locZ == z;
    }


    public void load() {
        Location location = new Location(world.getWorld(), locX, locY, locZ);
        boolean hasCrystal = world.getWorld().getNearbyEntities(location, 60, 20, 60).stream().anyMatch(entity -> entity.getType().equals(EntityType.ENDER_CRYSTAL));
        if(!hasCrystal){
            world.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
        }
    }
}
