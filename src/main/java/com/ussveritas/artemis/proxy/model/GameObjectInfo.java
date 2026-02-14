package com.ussveritas.artemis.proxy.model;

public record GameObjectInfo(
    Integer id,
    String name,
    String type,
    Integer side,
    Float x,
    Float y,
    Float z,
    Float shields,
    Float shieldsMax,
    Float bearing,
    Float velocity
) {
    public static GameObjectInfo createEmpty() {
        return new GameObjectInfo(null, null, null, null, null, null, null, null, null, null, null);
    }
}
