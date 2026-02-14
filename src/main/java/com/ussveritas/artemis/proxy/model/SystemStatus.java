package com.ussveritas.artemis.proxy.model;

public record SystemStatus(
    Float power,
    Float heat,
    Integer coolant,
    Float damage
) {
    public static SystemStatus createEmpty() {
        return new SystemStatus(null, null, null, null);
    }
}
