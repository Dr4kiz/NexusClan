package me.dkz.dev.nexusclan.inventory;

import com.henryfabio.minecraft.inventoryapi.editor.InventoryEditor;
import com.henryfabio.minecraft.inventoryapi.inventory.CustomInventory;
import com.henryfabio.minecraft.inventoryapi.inventory.impl.paged.PagedInventory;
import com.henryfabio.minecraft.inventoryapi.item.InventoryItem;
import com.henryfabio.minecraft.inventoryapi.item.supplier.InventoryItemSupplier;
import com.henryfabio.minecraft.inventoryapi.viewer.Viewer;
import com.henryfabio.minecraft.inventoryapi.viewer.impl.paged.PagedViewer;
import me.dkz.dev.nexusclan.manager.ClanInvitesManager;
import me.dkz.dev.nexusclan.utils.ItemSkullUtils;
import me.dkz.dev.nexusclan.utils.ItemStackUtils;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.List;

public class NexusClanInvitesInventory extends PagedInventory {
    private final ClanInvitesManager clanInvitesManager;
    private final CustomInventory lastInventory;
    private final ItemStack backArrow;


    public NexusClanInvitesInventory(CustomInventory lastInventory, ClanInvitesManager clanInvitesManager) {
        super("nexus.clan.invites_inventory", "Convites de Clans", 9 * 4);
        this.lastInventory = lastInventory;
        this.clanInvitesManager = clanInvitesManager;
        this.backArrow = ItemStackUtils.builder(ItemSkullUtils.getCustomSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY1MmUyYjkzNmNhODAyNmJkMjg2NTFkN2M5ZjI4MTlkMmU5MjM2OTc3MzRkMThkZmRiMTM1NTBmOGZkYWQ1ZiJ9fX0="))
                .setDisplayName("§cVoltar").build();
    }

    @Override
    protected void configureInventory(Viewer viewer, InventoryEditor editor) {
        editor.setItem(27, InventoryItem.of(backArrow).defaultCallback(event ->{
            if(lastInventory != null){
                lastInventory.openInventory(event.getPlayer());
            }else{
                event.getPlayer().closeInventory();
            }
        }));
    }

    @Override
    protected List<InventoryItemSupplier> createPageItems(PagedViewer pagedViewer) {
        LinkedList<InventoryItemSupplier> itemSuppliers = new LinkedList<>();

        clanInvitesManager.getInvites(pagedViewer.getPlayer()).forEach(clanInvites -> {
            ItemStack invite = ItemStackUtils.builder(Material.PAPER).setDisplayName(clanInvites.getClan().getFormmatedName())
                    .setLore(
                            "",
                            "§eConvidado por: " + clanInvites.getInviter().getPlayer().getName(),
                            "§eConvidado em: " + clanInvites.getFormattedTime(),
                            "",
                            "§7Botão §lESQUERDO: §aAceitar",
                            "§7Botão §lDIREITO: §CRecusar"
                    ).build();
            itemSuppliers.add(() -> {
                return InventoryItem.of(invite).callback(ClickType.LEFT, event ->{
                    clanInvitesManager.accept(clanInvites);
                    event.getPlayer().closeInventory();
                }).callback(ClickType.RIGHT, event ->{
                    clanInvitesManager.decline(clanInvites);
                    event.getPlayer().sendMessage("§cVocê recusou o convite do clan.");
                    event.getPlayer().closeInventory();

                });

            });
        });

        return itemSuppliers;
    }
}
