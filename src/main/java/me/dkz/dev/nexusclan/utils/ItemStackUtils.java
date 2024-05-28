package me.dkz.dev.nexusclan.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;

public class ItemStackUtils {

    private ItemStack itemStack;
    private ItemMeta meta;

    private ItemStackUtils(Material material) {
        this.itemStack = new ItemStack(material);
        this.meta = itemStack.getItemMeta();
    }

    private ItemStackUtils(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.meta = itemStack.getItemMeta();
    }



    public static ItemStackUtils builder(Material material) {
        return new ItemStackUtils(material);
    }


    public static ItemStackUtils builder(ItemStack itemStack) {
        return new ItemStackUtils(itemStack);
    }


    public ItemStackUtils setDisplayName(String displayName) {
        meta.setDisplayName(displayName);
        return this;
    }

    public ItemStackUtils setAmount(int amount){
        itemStack.setAmount(amount);
        return this;
    }

    public ItemStackUtils setLore(String... lore) {
        meta.setLore(List.of(lore));
        return this;
    }
    public ItemStackUtils setLore(List<String> lore) {
        meta.setLore(lore);
        return this;
    }


    public ItemStack build() {
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
