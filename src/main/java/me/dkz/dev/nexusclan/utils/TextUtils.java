package me.dkz.dev.nexusclan.utils;

import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;

public class TextUtils {

    public static void sendActionText(Player player, String message) {
        PacketPlayOutChat packet = new PacketPlayOutChat(new ChatComponentText(message), (byte) 2);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
    public static void sendActionText(Player player, String message, Sound sound) {
        PacketPlayOutChat packet = new PacketPlayOutChat(new ChatComponentText(message), (byte) 2);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        player.playSound(player.getLocation(), sound, 1f, 1);
    }

    public static void sendTitle(Player p, String msg, String subtitle, int fadeIn, int stayTime, int fadeOut) {

        PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, IChatBaseComponent.ChatSerializer.a(ChatColor.translateAlternateColorCodes('&', "{\"text\": \"" + msg + "\"}")), fadeIn, stayTime, fadeOut);
        sendSubtitle(p, subtitle, fadeIn, stayTime, fadeOut);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(title);

    }

    private static void sendSubtitle(Player p, String msg, int fadeIn, int stayTime, int fadeOut) {

        PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, IChatBaseComponent.ChatSerializer.a(ChatColor.translateAlternateColorCodes('&', "{\"text\": \"" + msg + "\"}")), fadeIn, stayTime, fadeOut);

        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(title);

    }
}
