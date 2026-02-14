package com.ussveritas.artemis.proxy.model;

public record ReactorStatus(
    Float energy,
    Float energyMax,
    Integer coolantAvailable
) {
    public static ReactorStatus createEmpty() {
        return new ReactorStatus(null, null, null);
    }
}
