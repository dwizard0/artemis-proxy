package com.ussveritas.artemis.proxy.model;

public record TorpedoCounts(
    Integer homing,
    Integer nuke,
    Integer mine,
    Integer emp,
    Integer pshock,
    Integer beacon,
    Integer probe,
    Integer tag
) {
    public static TorpedoCounts createEmpty() {
        return new TorpedoCounts(null, null, null, null, null, null, null, null);
    }
}
