package com.ussveritas.artemis.proxy.model;

public record ShipSnapshot(
    ShipIdentity identity,
    ShipPosition position,
    ShipStatus status,
    ShieldStatus shields,
    WeaponStatus weapons,
    ShipSystems systems,
    TacticalStatus tactical
) {
    public static ShipSnapshot createEmpty() {
        return new ShipSnapshot(
            ShipIdentity.createEmpty(),
            ShipPosition.createEmpty(),
            ShipStatus.createEmpty(),
            ShieldStatus.createEmpty(),
            WeaponStatus.createEmpty(),
            ShipSystems.createEmpty(),
            TacticalStatus.createEmpty()
        );
    }
}
