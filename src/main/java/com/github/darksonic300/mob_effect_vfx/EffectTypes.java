package com.github.darksonic300.mob_effect_vfx;

public enum EffectTypes {
    VERTICAL("vertical"),
    STATIONARY("stationary"),
    ROUND("round"),
    EMPTY("");

    private String id;

    EffectTypes(String string) {
        this.id = string;
    }

    public String getId() {
        return id;
    }
}
