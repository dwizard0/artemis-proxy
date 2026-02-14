package com.ussveritas.artemis.proxy.model;

public record TacticalStatus(
    Integer mainScreenView,
    Integer targetId,
    Integer scanningId,
    Float scanLevel
) {
    public static TacticalStatus createEmpty() {
        return new TacticalStatus(null, null, null, null);
    }
}
