package com.ussveritas.artemis.proxy.model;

public record ShipStatus(
    Float hull,
    Float hullMax,
    Boolean shieldsUp,
    Boolean redAlert,
    Boolean docked
) {
    public static ShipStatus createEmpty() {
        return new ShipStatus(null, null, null, null, null);
    }
}
