package me.dkz.dev.nexusclan.manager;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.dkz.dev.nexusclan.Main;
import me.dkz.dev.nexusclan.clan.ClanInvites;
import me.dkz.dev.nexusclan.nexus.NexusClan;
import me.dkz.dev.nexusclan.nexus.NexusMember;
import me.dkz.dev.nexusclan.nexus.NexusTag;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public class ClanInvitesManager {

    private final Set<ClanInvites> clanInvites = new HashSet<>();
    private final Set<Player> clanInvitesQueue = new HashSet<>();

    private static final String INVITE_MESSAGE = "§aVocê foi convidado para o clan %s§a, verifique seus convites.";
    private static final String ALREADY_INVITED_MESSAGE = "§cEsse jogador já foi convidado ao seu clan, aguarde a resposta.";
    private static final String YOURSELF_INVITED_MESSAGE = "§cVocê não pode se convidar para o clan.";
    private static final String ALREADY_IN_CLAN_MESSAGE = "§cEsse jogador é membro do  seu clan.";
    private static final String START_INVITE_MESSAGE = "§eQual o nick do jogador que deseja convidar?";
    private static final String CANCEL_MESSAGE = "§7Caso queira cancelar, digite \"§ccancelar§7\".";
    private static final String INVITED_MESSAGE = "§eO jogador %s foi convidado ao clan, contiune convidando ou digite §ccancelar§e para sair.";

    public List<ClanInvites> getInvites(OfflinePlayer player) {
        return clanInvites.stream().filter(invite -> invite.getInvited().getUniqueId().equals(player.getUniqueId())).collect(Collectors.toList());
    }

    public boolean hasInvited(Player player, NexusClan nexusClan) {
        return clanInvites.stream().anyMatch(invite -> invite.getClan().equals(nexusClan));
    }

    public boolean alreadyInClan(Player player, NexusClan nexusClan) {
        return nexusClan.getMember(player).isPresent();
    }

    public void invite(Player player, ClanInvites invite) {
        Player inviter = invite.getInviter().getPlayer().getPlayer();
        if (inviter.getUniqueId().equals(player.getUniqueId())) {
            inviter.sendMessage(YOURSELF_INVITED_MESSAGE);
        } else if (alreadyInClan(player, invite.getClan())) {
            inviter.sendMessage(ALREADY_IN_CLAN_MESSAGE);
        } else {
            if (!hasInvited(player, invite.getClan())) {
                clanInvites.add(invite);
                sendInviteMessage(player, String.format(INVITE_MESSAGE, invite.getClan().getFormmatedName()));
                inviter.sendMessage(String.format(INVITED_MESSAGE, player.getDisplayName()));
            } else {
                sendInviteMessage(invite.getInviter().getPlayer().getPlayer(), ALREADY_INVITED_MESSAGE);
            }
        }
    }


    public void startInvite(Player player) {
        clanInvitesQueue.add(player);
        player.sendMessage("");
        player.sendMessage(START_INVITE_MESSAGE);
        player.sendMessage(CANCEL_MESSAGE);
        player.sendMessage("");
    }

    private void sendInviteMessage(Player player, String message) {
        if (player != null) {
            player.sendMessage(message);
        }
    }


    public void stopInvite(Player player) {
        clanInvitesQueue.remove(player);
        player.sendMessage("§cVocê cancelou o convite de clan.");
    }

    private Main plugin = Main.getInstance();
    private NexusManager nexusManager = plugin.getNexusManager();

    public void accept(ClanInvites clanInvites) {


        if (nexusManager.getClan(clanInvites.getInvited()).isEmpty()) {

            NexusClan clan = clanInvites.getClan();
            clan.joinMember(new NexusMember(NexusTag.RECRUIT, clanInvites.getInvited()));
            removeInvite(clanInvites);
        } else {
            clanInvites.getInvited().getPlayer().sendMessage("§cVocê não pode aceitar convites estando em um clan.");
        }
    }


    public void decline(ClanInvites clanInvites) {
        removeInvite(clanInvites);
    }


    private void removeInvite(ClanInvites clanInvites) {
        Iterator<ClanInvites> iterator = this.clanInvites.iterator();
        while (iterator.hasNext()) {
            ClanInvites invite = iterator.next();
            if (invite.equals(clanInvites)) {
                iterator.remove();
                break;
            }
        }
    }
}
