package me.dkz.dev.nexusclan.inventory;

import com.henryfabio.minecraft.inventoryapi.editor.InventoryEditor;
import com.henryfabio.minecraft.inventoryapi.inventory.impl.simple.SimpleInventory;
import com.henryfabio.minecraft.inventoryapi.item.InventoryItem;
import com.henryfabio.minecraft.inventoryapi.viewer.Viewer;
import me.dkz.dev.nexusclan.clan.ClanCreator;
import me.dkz.dev.nexusclan.clan.ClanInvites;
import me.dkz.dev.nexusclan.manager.ClanInvitesManager;
import me.dkz.dev.nexusclan.utils.ItemStackUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class NexusClanInventory extends SimpleInventory {

    private final ClanCreator clanCreator;
    private final ClanInvitesManager clanInvitesManager;



    public NexusClanInventory(ClanCreator clanCreator, ClanInvitesManager clanInvitesManager) {
        super("nexus.clan.inventory", "Menu do Clan", 9 * 3);
        this.clanCreator = clanCreator;
        this.clanInvitesManager = clanInvitesManager;
    }

    @Override
    protected void configureInventory(Viewer viewer, InventoryEditor editor) {

        Player player = viewer.getPlayer();

        ItemStack creation = ItemStackUtils.builder(Material.BANNER).setDisplayName("§eCriar um clan").setLore(
                "§fDigite \"/clan criar\" ou clique aqui",
                "§fe inicie a criação do seu novo clan."
        ).build();

        List<ClanInvites> clanInvites = clanInvitesManager.getInvites(player);


        ItemStack invites = ItemStackUtils.builder(Material.PAPER).setDisplayName("§eConvites de clan").setLore(
                !clanInvites.isEmpty() ? "§aVocê possuí §e"+clanInvites.size() +" §aconvites." : "§cVocê não possuí convites."
        ).setAmount(clanInvites.size()).build();

        editor.setItem(11, InventoryItem.of(creation).defaultCallback(event ->{
            clanCreator.createClan(event.getPlayer());
            event.getPlayer().closeInventory();
        }));
        editor.setItem(13, InventoryItem.of(invites).defaultCallback(event ->{
           NexusClanInvitesInventory nexusClanInvitesInventory = new NexusClanInvitesInventory(this, clanInvitesManager);
           nexusClanInvitesInventory.openInventory(player);
        }));

        super.configureInventory(viewer, editor);
    }
}
