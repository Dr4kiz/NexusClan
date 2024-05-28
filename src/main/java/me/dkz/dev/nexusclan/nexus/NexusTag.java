package me.dkz.dev.nexusclan.nexus;

public enum NexusTag {
    LEADER("Lider", 0),
    COLEADER("Co-Lider", 1),
    MEMBER("Membro", 2),
    RECRUIT("Recruta", 3);

    private final String displayName;
    private final int hierarchy;

    NexusTag(String displayName, int hierarchy) {
        this.displayName = displayName;
        this.hierarchy = hierarchy;
    }
    public String getDisplayName() {
        return displayName;
    }
    public int getHierarchy() {
        return hierarchy;
    }
}
