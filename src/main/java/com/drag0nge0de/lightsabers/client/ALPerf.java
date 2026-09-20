package com.drag0nge0de.lightsabers.client;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ALPerf {

    public static final boolean ENABLED = Boolean.getBoolean("al.perf");

    private static final Map<String, long[]> SECTIONS = new LinkedHashMap<>();
    private static final int WINDOW = Integer.getInteger("al.perf.frames", 60);
    private static long sectionStart;
    private static String current;
    private static long frameStart;
    private static long frameAccum;
    private static int frames;
    private static String gui;

    private ALPerf() {
    }

    public static void frameBegin(String name) {
        if (!ENABLED) {
            return;
        }
        long now = System.nanoTime();
        if (frameStart > 0) {
            frameAccum += now - frameStart;
            frames++;
            if (frames >= WINDOW) {
                report(name);
            }
        }
        frameStart = now;
        current = null;
        sectionStart = now;
        gui = name;
    }

    public static void section(String name) {
        if (!ENABLED) {
            return;
        }
        long now = System.nanoTime();
        if (current != null) {
            SECTIONS.computeIfAbsent(current, k -> new long[2])[0] += now - sectionStart;
            SECTIONS.get(current)[1]++;
        }
        current = name;
        sectionStart = now;
    }

    public static void frameEnd() {
        if (!ENABLED) {
            return;
        }
        section("$end");
    }

    private static void report(String name) {
        StringBuilder sb = new StringBuilder("[AL-PERF] ").append(name).append(": avg frame ")
                .append(String.format("%.2fms (%.1f fps)", frameAccum / (double) WINDOW / 1e6,
                        1e9 * (double) WINDOW / Math.max(1, frameAccum)));
        for (Map.Entry<String, long[]> e : SECTIONS.entrySet()) {
            sb.append(String.format(" | %s %.2fms", e.getKey(), e.getValue()[0] / Math.max(1, e.getValue()[1]) / 1e6));
        }
        System.out.println(sb);
        SECTIONS.clear();
        frameAccum = 0;
        frames = 0;
    }
}
