package com.ussveritas.artemis.proxy.model;

import java.util.List;

public record WeaponStatus(
    List<TubeData> tubes,
    Integer tubeCount,
    TorpedoCounts torpedoCounts,
    BeamFrequency beamFreq,
    Boolean autoBeams
) {
    public static WeaponStatus createEmpty() {
        return new WeaponStatus(List.of(), null, TorpedoCounts.createEmpty(), BeamFrequency.createEmpty(), null);
    }
}
