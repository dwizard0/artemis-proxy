package com.ussveritas.artemis.proxy.model;

public record ShipSystems(
    ReactorStatus reactor,
    SystemStatus beams,
    SystemStatus torpedoes,
    SystemStatus sensors,
    SystemStatus maneuvering,
    SystemStatus impulse,
    SystemStatus warp,
    SystemStatus foreShields,
    SystemStatus aftShields,
    SystemStatus portShields,
    SystemStatus starboardShields
) {
    public static ShipSystems createEmpty() {
        return new ShipSystems(
            ReactorStatus.createEmpty(),
            SystemStatus.createEmpty(),
            SystemStatus.createEmpty(),
            SystemStatus.createEmpty(),
            SystemStatus.createEmpty(),
            SystemStatus.createEmpty(),
            SystemStatus.createEmpty(),
            SystemStatus.createEmpty(),
            SystemStatus.createEmpty(),
            SystemStatus.createEmpty(),
            SystemStatus.createEmpty()
        );
    }
}
