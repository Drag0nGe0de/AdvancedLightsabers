package com.drag0nge0de.lightsabers.hilt;

import java.util.HashMap;
import java.util.Map;

public final class HiltStatsTable {

    private static final float[] EMPTY_FLOATS = new float[0];

    private static final Map<String, HiltStats> STATS = new HashMap<>();

    private static final Map<String, Integer> DEFAULT_FOCUSING = Map.of(
            "fulcrum", 1,
            "knighted", 2,
            "mandalorian", 12,
            "rebel", 1);

    public static int defaultFocusing(String hilt) {
        return DEFAULT_FOCUSING.getOrDefault(legacyModelKey(hilt), 0);
    }

    public static HiltStats stats(String hilt) {
        String id = hilt == null ? Hilt.DEFAULT.getId() : Hilt.byName(hilt).getId();
        HiltStats s = STATS.get(legacyModelKey(id));
        return s != null ? s : STATS.get(legacyModelKey(Hilt.DEFAULT.getId()));
    }

    public static String legacyModelKey(String id) {
        return "grafx".equals(id) ? "graflex" : id;
    }

    static {
        STATS.put("droideka", new HiltStats(false, 16758272, 5F, 9.6F, 29.4F, 6.5F, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, null));
        STATS.put("fulcrum", new HiltStats(false, 16777215, 19F, 10F, 30F, 7F, new float[]{11.7F,2F,9.8F,3F,9F}, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, null));
        STATS.put("fury", new HiltStats(false, 11337901, 19F, 5.6F, 16F, 8.3F, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, null));
        STATS.put("graflex", new HiltStats(false, 255, 16F, 8.8F, 16F, 1F, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, null));
        STATS.put("imperial", new HiltStats(false, 16711680, 7.6F, 3.7F, 15.8F, 6.5F, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, null));
        STATS.put("juggernaut", new HiltStats(false, 16711680, 14.7F, 12.4F, 16F, 7F, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, null));
        STATS.put("knighted", new HiltStats(false, 16711680, 12.6F, 8.4F, 20F, 13.3F, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, new float[]{0F,.083F,.23F}));
        STATS.put("mandalorian", new HiltStats(false, 16777215, 12.55F, 2.87F, 26F, 7.45F, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, null));
        STATS.put("mauler", new HiltStats(true, 16711680, 18F, 12F, 21.6F, .25F, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, null));
        STATS.put("mechanical", new HiltStats(false, 16711680, 16F, 8.8F, 16F, 2.5F, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, null));
        STATS.put("prodigal_son", new HiltStats(false, 65280, 31F, 8.4F, 13.3F, 2F, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, null));
        STATS.put("rebel", new HiltStats(false, 27647, 12.9F, 7F, 20F, 6F, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, null));
        STATS.put("reborn", new HiltStats(false, 11337901, 14.86F, 10F, 19F, 6F, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, null));
        STATS.put("redeemer", new HiltStats(false, 255, 30F, 8F, 12.3F, 1F, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, null));
        STATS.put("vaid_ancient", new HiltStats(false, 11337901, 18.5F, 9.2F, 22.37F, 6.6F, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, null));
        STATS.put("vaid_modern", new HiltStats(false, 11337901, 18.5F, 9.2F, 22.37F, 6.6F, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, EMPTY_FLOATS, null));
    }

    private HiltStatsTable() {
    }
}
