package me.dkz.dev.nexusclan.nexus;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Objects;

@Data
@SuperBuilder
public class NexusMember {
    private OfflinePlayer player;
    private double energy;
    private NexusClan clan;
    private NexusTag tag;
    private final int kills = 0;
    private final int deaths = 0;





    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NexusMember that = (NexusMember) o;
        return Objects.equals(player, that.player);
    }


    public NexusMember(NexusTag tag, OfflinePlayer player) {
        this.tag = tag;
        this.player = player;
    }

    @Override
    public String toString() {
        return player.getName() + " | " + tag.getDisplayName();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(player);
    }

    public double getKD() {
        try {
            return (double) kills / deaths;
        } catch (ArithmeticException e) {
            return 0;
        }
    }
}
