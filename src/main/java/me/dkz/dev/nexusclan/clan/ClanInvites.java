package me.dkz.dev.nexusclan.clan;

import lombok.Builder;
import lombok.Getter;
import me.dkz.dev.nexusclan.nexus.NexusClan;
import me.dkz.dev.nexusclan.nexus.NexusMember;
import org.bukkit.OfflinePlayer;

import javax.swing.text.DateFormatter;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;


@Builder
@Getter
public class ClanInvites {

    private NexusClan clan;
    private NexusMember inviter;
    private OfflinePlayer invited;

    private LocalDateTime invited_at;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClanInvites that = (ClanInvites) o;
        return Objects.equals(invited.getUniqueId(), that.invited.getUniqueId()) &&
                Objects.equals(clan.getName(), that.clan.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(clan);
    }

    public String getFormattedTime(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss");
        return invited_at.format(formatter);
    }
}
