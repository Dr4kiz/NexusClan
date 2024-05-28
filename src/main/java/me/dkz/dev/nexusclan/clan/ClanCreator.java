package me.dkz.dev.nexusclan.clan;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.dkz.dev.nexusclan.Main;
import me.dkz.dev.nexusclan.nexus.NexusClan;
import me.dkz.dev.nexusclan.manager.NexusManager;
import me.dkz.dev.nexusclan.nexus.NexusMember;
import me.dkz.dev.nexusclan.nexus.NexusTag;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Getter
public class ClanCreator {

    private final Main plugin;
    private final NexusManager nexusManager;

    private final Set<Player> clanCreationQueue = new HashSet<>();
    private final HashMap<Player, NexusCreationManager> clanCreationSteps = new HashMap<>();

    private static final String CANCEL_MESSAGE = "§7Caso queira cancelar, digite \"§ccancelar§7\".";
    private static final String INVALID_OPTION_MESSAGE = "§cOpção inválida, utilize \"§4cancelar§c\" §cpara sair da criação.";



    public void createClan(Player owner) {
        clanCreationQueue.add(owner);
        clanCreationSteps.put(owner, new NexusCreationManager());
        sendChatMessage("§eQual será a tag do seu clan?", owner);
    }

    public void sendChatMessage(String message, Player player) {
        player.sendMessage("");
        player.sendMessage(message);
        player.sendMessage(CANCEL_MESSAGE);
        player.sendMessage("");
    }

    public void processClanCreation(Player player, String data) {
        if (data.equalsIgnoreCase("cancelar")) {
            player.sendMessage("§cVocê cancelou a criação do clan.");
            cancelClanCreation(player);
            return;
        }

        NexusCreationManager nexusCreationManager = clanCreationSteps.get(player);
        NexusClan nexusClan = nexusCreationManager.getClan();

        switch (nexusCreationManager.getStep()) {
            case TAG:
                handleTagStep(player, data, nexusClan, nexusCreationManager);
                break;
            case NAME:
                handleNameStep(player, data, nexusClan, nexusCreationManager);
                break;
            case CONFIRM:
                handleConfirmStep(player, data, nexusClan);
                break;
            default:
                player.sendMessage("§cOps, digite §lCANCELAR §cpara recomeçar a criação do seu clan.");
                break;
        }
    }

    private void handleTagStep(Player player, String data, NexusClan nexusClan, NexusCreationManager manager) {
        if (!isValidTag(data)) {
            player.sendMessage("§cA tag do clan deve conter 3 caracteres.");
            return;
        }

        if(nexusManager.getClanByTag(data).isPresent()){
            player.sendMessage("§cUm clan com essa tag já foi criado, utilize outra.");
            return;
        }

        nexusClan.setTag(data.toUpperCase());
        manager.setStep(Step.NAME);
        clanCreationSteps.put(player, manager);
        sendChatMessage("§eQual será o nome do seu clan?", player);
    }

    private void handleNameStep(Player player, String data, NexusClan nexusClan, NexusCreationManager manager) {
        if (!isValidName(data)) {
            player.sendMessage("§cO nome do clan deve conter entre 4 e 16 caracteres.");
            return;
        }

        if(nexusManager.getClanByName(data).isPresent()){
            player.sendMessage("§cUm clan com esse nome já foi criado, utilize outro.");
            return;
        }

        nexusClan.setName(data);
        NexusMember nexusMember = NexusMember.builder().player(player).tag(NexusTag.LEADER).build();
        nexusClan.setOwner(nexusMember);
        sendChatMessage("§eVocê realmente deseja criar o clan §f[" + nexusClan.getTag() + "] " + nexusClan.getName() + "§e?", player);
        player.sendMessage("§7Responda com \"§asim§7\" §7ou \"§cnão§7\".");
        manager.setStep(Step.CONFIRM);
        clanCreationSteps.put(player, manager);
    }

    private void handleConfirmStep(Player player, String data, NexusClan nexusClan) {
        if (data.equalsIgnoreCase("sim") || data.equalsIgnoreCase("s")) {
            nexusClan.create();
            nexusManager.addClan(nexusClan);
            cancelClanCreation(player);
        } else if (data.equalsIgnoreCase("não") || data.equalsIgnoreCase("nao") || data.equalsIgnoreCase("n")) {
            player.sendMessage("§cVocê cancelou a criação do clan.");
            cancelClanCreation(player);
        } else {
            player.sendMessage(INVALID_OPTION_MESSAGE);
        }
    }

    private void cancelClanCreation(Player player) {
        clanCreationSteps.remove(player);
        clanCreationQueue.remove(player);
    }

    private boolean isValidTag(String tag) {
        return tag.length() == 3;
    }

    private boolean isValidName(String name) {
        return name.length() >= 4 && name.length() <= 16;
    }

    @Data
    class NexusCreationManager {
        private Step step = Step.TAG;
        private NexusClan clan = new NexusClan();
    }

    private enum Step {
        TAG,
        NAME,
        CONFIRM
    }
}
