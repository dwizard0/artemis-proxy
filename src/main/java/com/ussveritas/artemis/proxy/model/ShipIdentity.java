package com.ussveritas.artemis.proxy.model;

public record ShipIdentity(
    String name,
    String shipType,
    Integer side,
    Integer shipNumber
) {
    public static ShipIdentity createEmpty() {
        return new ShipIdentity(null, null, null, null);
    }
}
