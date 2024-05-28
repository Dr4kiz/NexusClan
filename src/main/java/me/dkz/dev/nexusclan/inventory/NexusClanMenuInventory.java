package me.dkz.dev.nexusclan.inventory;

import com.henryfabio.minecraft.inventoryapi.editor.InventoryEditor;
import com.henryfabio.minecraft.inventoryapi.inventory.impl.simple.SimpleInventory;
import com.henryfabio.minecraft.inventoryapi.item.InventoryItem;
import com.henryfabio.minecraft.inventoryapi.viewer.Viewer;
import me.dkz.dev.nexusclan.Main;
import me.dkz.dev.nexusclan.manager.ClanInvitesManager;
import me.dkz.dev.nexusclan.nexus.NexusClan;
import me.dkz.dev.nexusclan.nexus.NexusMember;
import me.dkz.dev.nexusclan.utils.ItemStackUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class NexusClanMenuInventory extends SimpleInventory {

    private final NexusClan nexusClan;
    private final NexusMember nexusMember;

    private final Main plugin = Main.getInstance();
    private final ClanInvitesManager clanInvitesManager = plugin.getClanInvitesManager();

    public NexusClanMenuInventory(NexusClan clan, NexusMember member) {
        super("nexus.clan.menu_inventory", "Menu do Clan", 9 * 3);
        this.nexusClan = clan;
        this.nexusMember = member;
        configuration(inventoryConfiguration -> {
            inventoryConfiguration.secondUpdate(1);
        });

    }

    @Override
    protected void configureInventory(Viewer viewer, InventoryEditor editor) {
        ItemStack about = ItemStackUtils.builder(Material.BOOK_AND_QUILL).setDisplayName("§eInformações do Clan")
                .setLore(
                        "",
                        " §8["+nexusClan.getTag()+ "] §7"+nexusClan.getName(),
                        "",
                        " §eMembros: §7"+nexusClan.getMembers().size()+" ("+nexusClan.getMembers().stream().filter(member -> member.getPlayer().isOnline()).count()+" online)",
                        " §eEnergia Total: §b"+nexusClan.getMembers().stream().mapToDouble(NexusMember::getEnergy).sum(),
                        "",
                        " §cNexus não definido",
                        "",
                        " "+ String.join("", nexusClan.getDescription()),
                        ""

                ).build();

        ItemStack aboutMember = ItemStackUtils.builder(Material.BOOK).setDisplayName("§eSuas Informações")
                        .setLore(
                                "",
                                " §eNick: §7"+viewer.getName(),
                                "",
                                " §eCargo: §7"+nexusMember.getTag().getDisplayName(),
                                " §eEnergia: §b"+nexusMember.getEnergy(),
                                " §eK/D: §c"+(nexusMember.getKD()),
                                "",
                                "§8§oInformações do membro do clan"
                        ).build();

        ItemStack clanMembers = ItemStackUtils.builder(Material.PAPER).setDisplayName("§eMembros do Clan")
                        .setLore(
                                "",
                                " §7Botão §lESQUERDO§7: §aLista de membros",
                                " §7Botão §lDIREITO§7: §aConvidar jogadores",
                                "",
                                "§8§oObtenha informações dos membros"
                        ).build();

        ItemStack nexus = ItemStackUtils.builder(Material.BEACON).setDisplayName("§eNexus do Clan").setLore(
                nexusClan.getNexusCrystal() == null ? "§cNexus do clan não definido" : "§eClique para configurar o nexus"
        ).build();

        editor.setItem(11, InventoryItem.of(about));
        editor.setItem(13, InventoryItem.of(aboutMember));
        editor.setItem(14, InventoryItem.of(clanMembers).callback(ClickType.LEFT, event ->{
            NexusClanMembers
                    nexusClanMembers = new NexusClanMembers(this, this.nexusClan);
            nexusClanMembers.openInventory(event.getPlayer());
        }).callback(ClickType.RIGHT, event -> {

            Player player = event.getPlayer();
            if(nexusMember.getTag().getHierarchy() < 3) {
                clanInvitesManager.startInvite(player);
                player.closeInventory();
            }else{
                player.sendMessage("§cApenas membros podem convidar jogadores.");
            }
        }));
        editor.setItem(15, InventoryItem.of(nexus));
    }
}
