package com.ussveritas.artemis.proxy.model;

public record ShipPosition(
    Float x,
    Float y,
    Float z,
    Float bearing,
    Float velocity,
    Float pitch,
    Float roll,
    Integer warpSpeed,
    Float impulseSpeed
) {
    public static ShipPosition createEmpty() {
        return new ShipPosition(null, null, null, null, null, null, null, null, null);
    }
}
