package com.ussveritas.artemis.proxy.model;

public record ShieldStatus(
    Float fore,
    Float foreMax,
    Float aft,
    Float aftMax,
    Float port,
    Float portMax,
    Float starboard,
    Float starboardMax
) {
    public static ShieldStatus createEmpty() {
        return new ShieldStatus(null, null, null, null, null, null, null, null);
    }
}
