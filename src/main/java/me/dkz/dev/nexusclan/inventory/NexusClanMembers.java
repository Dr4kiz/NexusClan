package me.dkz.dev.nexusclan.inventory;

import com.henryfabio.minecraft.inventoryapi.editor.InventoryEditor;
import com.henryfabio.minecraft.inventoryapi.event.impl.CustomInventoryClickEvent;
import com.henryfabio.minecraft.inventoryapi.inventory.CustomInventory;
import com.henryfabio.minecraft.inventoryapi.inventory.impl.paged.PagedInventory;
import com.henryfabio.minecraft.inventoryapi.item.InventoryItem;
import com.henryfabio.minecraft.inventoryapi.item.supplier.InventoryItemSupplier;
import com.henryfabio.minecraft.inventoryapi.viewer.Viewer;
import com.henryfabio.minecraft.inventoryapi.viewer.impl.paged.PagedViewer;
import me.dkz.dev.nexusclan.Main;
import me.dkz.dev.nexusclan.nexus.NexusClan;
import me.dkz.dev.nexusclan.manager.NexusManager;
import me.dkz.dev.nexusclan.nexus.NexusMember;
import me.dkz.dev.nexusclan.nexus.NexusTag;
import me.dkz.dev.nexusclan.utils.ItemSkullUtils;
import me.dkz.dev.nexusclan.utils.ItemStackUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class NexusClanMembers extends PagedInventory {
    private final NexusClan nexusClan;
    private final CustomInventory lastInventory;

    private final ItemStack backArrow;

    private final Main plugin = Main.getInstance();
    private final NexusManager nexusManager = plugin.getNexusManager();

    public NexusClanMembers(CustomInventory lastInventory, NexusClan clan) {
        super("nexus.clan.members_inventory", "Membros do Clan", 9 * 5);
        this.nexusClan = clan;
        this.lastInventory = lastInventory;
        this.backArrow = ItemStackUtils.builder(ItemSkullUtils.getCustomSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY1MmUyYjkzNmNhODAyNmJkMjg2NTFkN2M5ZjI4MTlkMmU5MjM2OTc3MzRkMThkZmRiMTM1NTBmOGZkYWQ1ZiJ9fX0="))
                .setDisplayName("§cVoltar").build();

        configuration(inventoryConfiguration -> {
            inventoryConfiguration.secondUpdate(1);
        });

    }


    @Override
    protected void configureInventory(Viewer viewer, InventoryEditor editor) {
        editor.setItem(36, InventoryItem.of(backArrow).defaultCallback(event -> {
            lastInventory.openInventory(event.getPlayer());
        }));
    }

    @Override
    protected List<InventoryItemSupplier> createPageItems(PagedViewer pagedViewer) {
        LinkedList<InventoryItemSupplier> pageItems = new LinkedList<>();

        Player viewer = pagedViewer.getPlayer();
        NexusMember viewerMember = nexusClan.getMember(viewer).get();


        nexusClan.getMembers().stream().sorted(Comparator.comparingInt(value -> value.getTag().getHierarchy())).forEach(member -> {
                    Player player = member.getPlayer().getPlayer();
                    ItemStack skull = ItemSkullUtils.getPlayerSkull(player.getDisplayName());
                    ItemStack playerSkull = ItemStackUtils.builder(skull).setDisplayName(player == pagedViewer.getPlayer() ? "§a" + player.getDisplayName() + " (Você)" : "§7" + player.getDisplayName())
                            .setLore(getLore(viewerMember, member)).build();
                    pageItems.add(() -> {
                        if(viewerMember.getTag().getHierarchy() <= 1){
                            return InventoryItem.of(playerSkull).callback(ClickType.LEFT, event -> {
                                promoteMember(viewerMember, member, event);
                            }).callback(ClickType.RIGHT, event ->{
                                kickMember(viewerMember, member, event);
                            });
                        }else{
                            return InventoryItem.of(playerSkull);
                        }
                    });
                });

        return pageItems;
    }



    public List<String> getLore(NexusMember viewerMember, NexusMember member){
        Player player = member.getPlayer().getPlayer();
        if(viewerMember.getTag().getHierarchy() <= 1){
            return Arrays.asList(
                    "",
                    " §eCargo: §7" + member.getTag().getDisplayName(),
                    " §eStatus: " + (player.isOnline() ? "§aOnline" : "§cOffline"),
                    " §eEnergia: §b" + member.getEnergy(),
                    " §eK/D: §c"+member.getKD(),
                    "",
                    "§7Botão §lESQUERDO: §7Promover",
                    "§7Botão §lDIREITO: §7Expulsar",
                    "");
        }else{
            return Arrays.asList(
                    "",
                    " §eCargo: §7" + member.getTag().getDisplayName(),
                    " §eStatus: " + (player.isOnline() ? "§aOnline" : "§cOffline"),
                    " §eEnergia: §b" + member.getEnergy(),
                    " §eK/D: §c"+member.getKD(),
                    "");
        }
    }


    private void kickMember(NexusMember viewerMember, NexusMember member, CustomInventoryClickEvent event) {
        Player target = member.getPlayer().getPlayer();
        Player viewer = viewerMember.getPlayer().getPlayer();

        if (viewer.equals(target)) {
            viewer.sendMessage("§cVocê não pode expulsar a si mesmo.");
            return;
        }

        NexusTag viewerTag = viewerMember.getTag();
        NexusTag targetTag = member.getTag();

        if (targetTag.getHierarchy() - 1 == viewerTag.getHierarchy()) {
            viewer.sendMessage("§cVocê não pode expulsar alguém do mesmo cargo que você.");
            return;
        }

        if (targetTag.getHierarchy() - 1 < viewerTag.getHierarchy()) {
            viewer.sendMessage("§cVocê só pode expulsar alguém diretamente abaixo de você.");
            return;
        }


        if(nexusClan.getMember(target).isPresent()){
            nexusManager.kickMember(member);
        }

    }

    private void promoteMember(NexusMember viewerMember, NexusMember member, CustomInventoryClickEvent event) {
        Player target = member.getPlayer().getPlayer();
        Player viewer = viewerMember.getPlayer().getPlayer();

        if (viewer.equals(target)) {
            viewer.sendMessage("§cVocê não pode promover a si mesmo.");
            return;
        }


        NexusTag viewerTag = viewerMember.getTag();
        NexusTag targetTag = member.getTag();


        if (targetTag.getHierarchy() - 1 == viewerTag.getHierarchy()) {
            viewer.sendMessage("§cVocê não pode promover alguém ao mesmo cargo que você.");
            return;
        }

        if (targetTag.getHierarchy() - 1 < viewerTag.getHierarchy()) {
            viewer.sendMessage("§cVocê só pode promover alguém diretamente abaixo de você.");
            return;
        }

        NexusTag newTag = NexusTag.values()[targetTag.getHierarchy() - 1];
        member.setTag(newTag);

        viewer.sendMessage("§eVocê promoveu " + target.getDisplayName() + " para " + newTag.getDisplayName() + ".");
        target.sendMessage("§aVocê foi promovido para " + newTag.getDisplayName() + " por " + viewer.getDisplayName() + ".");
    }


}
