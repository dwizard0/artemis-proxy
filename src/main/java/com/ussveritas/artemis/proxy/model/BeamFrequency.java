package com.ussveritas.artemis.proxy.model;

public record BeamFrequency(Integer frequency) {
    public static BeamFrequency createEmpty() {
        return new BeamFrequency(null);
    }
}
