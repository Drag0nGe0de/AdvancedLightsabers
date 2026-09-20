package com.drag0nge0de.lightsabers.hilt;

public record HiltStats(boolean doubleSaber, int defaultColor, float emitterH,
        float switchH, float bodyH, float pommelH, float[] bodyGl, float[] switchGl,
        float[] emitterGl, float[] pommelGl, float[] crossguard) {

    public float totalHeight() {
        return emitterH + switchH + bodyH + pommelH;
    }

    public float heightOf(String part) {
        return switch (part) {
            case "emitter" -> emitterH;
            case "switch_section" -> switchH;
            case "body" -> bodyH;
            default -> pommelH;
        };
    }
}
