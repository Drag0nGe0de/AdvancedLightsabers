package com.drag0nge0de.lightsabers.client.render.model;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.model.Dilation;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

public final class TileModels {

    public record Box(float x, float y, float z, float w, float h, float d) {}

    public record Node(int u, int v, float px, float py, float pz, float rx, float ry,
            float rz, boolean mirror, int parent, Box[] boxes) {}

    public record Op(int node, float ax, float ay, float az, float rx, float ry, float rz,
            float sx, float sy, float sz) {}

    public record Part(int texW, int texH, Node[] nodes, Op[] ops) {}

    private static Node node(int u, int v, float px, float py, float pz, float rx, float ry,
            float rz, boolean mirror, int parent, Box[] boxes) {
        return new Node(u, v, px, py, pz, rx, ry, rz, mirror, parent, boxes);
    }

    private static Op op(int node, float ax, float ay, float az, float rx, float ry, float rz,
            float sx, float sy, float sz) {
        return new Op(node, ax, ay, az, rx, ry, rz, sx, sy, sz);
    }

    private static final Map<String, Part> DATA = new HashMap<>();

    public static com.drag0nge0de.lightsabers.hilt.HiltStats stats(String hilt) {
        return com.drag0nge0de.lightsabers.hilt.HiltStatsTable.stats(hilt);
    }

    public static Part get(String key) {
        return DATA.get(key);
    }

    public static boolean has(String key) {
        return DATA.containsKey(key);
    }

    static {
        init0();
        init1();
        init2();
        init3();
        init4();
        init5();
    }

    private static void init0() {
        DATA.put("LightsaberForge", new Part(128, 96, new Node[]{
        node(0, 33, 31F, -10F, 4F, 0F, 0F, 0F, false, 14, new Box[]{new Box(0F, 0F, 0F, 1F, 10F, 11F)}),
        node(0, 35, 0.6F, 1F, 5F, 0F, 0F, 0F, false, 36, new Box[]{new Box(0F, 0F, 0F, 1F, 1F, 4F)}),
        node(0, 72, 2F, 0F, 0F, 0F, -0.7853981633974483F, 0F, false, 8, new Box[]{new Box(0F, 0F, 0F, 2F, 1F, 1F)}),
        node(0, 62, -1F, 0F, 5F, 0F, 0F, 0F, false, 17, new Box[]{new Box(0F, 0F, 0F, 3F, 1F, 1F)}),
        node(0, 60, -0.5F, -11.5F, 0.5F, 0F, 0F, 0F, false, 14, new Box[]{new Box(0F, 0F, 0F, 1F, 1F, 15F)}),
        node(53, 61, -6F, 9F, 2F, 0F, -0.2617993877991494F, 0F, false, -1, new Box[]{new Box(0F, 0F, 0F, 7F, 3F, 3F)}),
        node(24, 19, -6F, 0F, 4F, 0F, 0F, 0F, false, 11, new Box[]{new Box(0F, 0F, 0F, 7F, 1F, 1F)}),
        node(0, 17, 10F, -10F, 0F, 0F, 0F, 0F, false, 14, new Box[]{new Box(0F, 0F, 0F, 2F, 10F, 4F)}),
        node(0, 41, 0F, 0F, 6F, 0F, -0.7853981633974483F, 0F, false, 20, new Box[]{new Box(0F, 0F, 0F, 2F, 1F, 1F)}),
        node(8, 12, 3F, 0F, 0F, 0F, 0F, 0F, false, 19, new Box[]{new Box(0F, 0F, 0F, 1F, 1F, 1F)}),
        node(0, 58, -0.5F, -11.5F, 15.5F, 0F, 0F, 0F, false, 14, new Box[]{new Box(0F, 0F, 0F, 33F, 1F, 1F)}),
        node(32, 64, 0.25F, 0F, 3.3F, 0F, -0.7504915783575618F, 0F, false, 1, new Box[]{new Box(0F, 0F, 0F, 1F, 1F, 4F)}),
        node(2, 12, 0F, 0F, -1F, 0F, 0F, 0F, false, 6, new Box[]{new Box(0F, 0F, 0F, 1F, 1F, 1F)}),
        node(0, 17, 0F, -10F, 0F, 0F, 0F, 0F, false, 14, new Box[]{new Box(0F, 0F, 0F, 2F, 10F, 4F)}),
        node(32, 0, -8F, 23F, -8F, 0F, 0F, 0F, false, -1, new Box[]{new Box(0F, 0F, 0F, 32F, 1F, 16F)}),
        node(0, 17, 20F, -10F, 0F, 0F, 0F, 0F, false, 14, new Box[]{new Box(0F, 0F, 0F, 2F, 10F, 4F)}),
        node(0, 0, 30F, -10F, 15F, 0F, 0F, 0F, false, 14, new Box[]{new Box(0F, 0F, 0F, 2F, 10F, 1F)}),
        node(0, 65, -4F, 11.3F, -3F, 0F, 0.6632251157578453F, 0F, false, -1, new Box[]{new Box(0F, 0F, 0F, 1F, 1F, 5F)}),
        node(15, 30, 1F, 9.7F, -1F, 0F, 0F, 0F, false, 40, new Box[]{new Box(0F, 0F, 0F, 2F, 10F, 2F)}),
        node(8, 12, 1.5F, -1F, 1F, 0F, 0F, 0F, false, 5, new Box[]{new Box(0F, 0F, 0F, 1F, 1F, 1F)}),
        node(41, 62, 22F, 11.3F, 2F, 0F, -1.0995574287564276F, 0F, false, -1, new Box[]{new Box(-1F, 0F, 0F, 1F, 1F, 7F)}),
        node(7, 0, 3F, 0.5F, -0.2F, 0F, 0F, 0F, false, 5, new Box[]{new Box(0F, 0F, 0F, 1F, 1F, 1F)}),
        node(32, 17, 0F, -11F, 0F, 0F, 0F, 0F, false, 14, new Box[]{new Box(0F, 0F, 0F, 32F, 1F, 16F)}),
        node(0, 60, 31.5F, -11.5F, 0.5F, 0F, 0F, 0F, false, 14, new Box[]{new Box(0F, 0F, 0F, 1F, 1F, 15F)}),
        node(15, 25, 0F, -1F, -1F, 0F, 0F, 0F, false, 40, new Box[]{new Box(0F, 0F, 0F, 14F, 1F, 2F)}),
        node(25, 37, -3F, -0.7F, 0F, 0F, 0F, 0F, false, 9, new Box[]{new Box(0F, 0F, 0F, 4F, 1F, 1F)}),
        node(19, 62, 0F, 11.3F, -3F, 0F, 1.0995574287564276F, 0F, false, -1, new Box[]{new Box(0F, 0F, 0F, 1F, 1F, 9F)}),
        node(0, 72, -2F, 0F, 0F, 0F, 0.7853981633974483F, 0F, true, 38, new Box[]{new Box(-2F, 0F, 0F, 2F, 1F, 1F)}),
        node(73, 58, 6F, 0F, 0F, 0F, 0F, 0.7853981633974483F, true, 30, new Box[]{new Box(0F, 0F, 0F, 3F, 2F, 2F)}),
        node(0, 17, 30F, -10F, 0F, 0F, 0F, 0F, false, 14, new Box[]{new Box(0F, 0F, 0F, 2F, 10F, 4F)}),
        node(73, 58, 5.5F, 10.5F, 0F, 0F, 0F, 0F, false, -1, new Box[]{new Box(0F, 0F, 0F, 6F, 2F, 2F)}),
        node(15, 25, 0F, 9F, -1F, 0F, 0F, 0F, false, 40, new Box[]{new Box(0F, 0F, 0F, 14F, 1F, 2F)}),
        node(0, 0, 0F, -10F, 15F, 0F, 0F, 0F, false, 14, new Box[]{new Box(0F, 0F, 0F, 2F, 10F, 1F)}),
        node(38, 34, 0.5F, -10.5F, 1F, -1.2217304763960306F, 0F, 0F, false, 14, new Box[]{new Box(0F, 0F, 0F, 31F, 1F, 11F)}),
        node(51, 46, 2F, -10F, 15F, 0F, 0F, 0F, false, 14, new Box[]{new Box(0F, 0F, 0F, 28F, 10F, 1F)}),
        node(0, 58, -0.5F, -11.5F, -0.5F, 0F, 0F, 0F, false, 14, new Box[]{new Box(0F, 0F, 0F, 33F, 1F, 1F)}),
        node(26, 47, 20F, 10.6F, -5F, 0F, -0.5585053606381855F, 0F, false, -1, new Box[]{new Box(0F, 0F, 0F, 3F, 2F, 5F)}),
        node(15, 11, 0F, 0F, -1F, 0F, 0F, 0F, false, 40, new Box[]{new Box(0F, 0F, 0F, 1F, 9F, 2F)}),
        node(0, 41, -1F, 0F, 6F, 0F, 0.7853981633974483F, 0F, true, 20, new Box[]{new Box(-2F, 0F, 0F, 2F, 1F, 1F)}),
        node(0, 33, 0F, -10F, 4F, 0F, 0F, 0F, false, 14, new Box[]{new Box(0F, 0F, 0F, 1F, 10F, 11F)}),
        node(14, 0, -3F, 2.2F, 7F, 0F, 0F, 0F, false, -1, new Box[]{new Box(1F, 0F, 0F, 12F, 9F, 1F)}),
        node(15, 11, 13F, 0F, -1F, 0F, 0F, 0F, false, 40, new Box[]{new Box(0F, 0F, 0F, 1F, 9F, 2F)}),
        node(73, 58, 0F, 0F, 0F, 0F, 0F, -0.7853981633974483F, false, 30, new Box[]{new Box(-3F, 0F, 0F, 3F, 2F, 2F)})
        }, new Op[]{
        op(5, 0F, 0F, 0F, 0F, 0F, 0F, 1F, 1F, 1F),
        op(14, 0F, 0F, 0F, 0F, 0F, 0F, 1F, 1F, 1F),
        op(17, -4F, 11.3F, -3.0000000000000004F, 0F, 0F, 0F, 0.7F, 0.7F, 0.7F),
        op(20, 22F, 11.3F, 2F, 0F, 0F, 0F, 0.5F, 0.5F, 0.5F),
        op(26, 0F, 11.3F, -3F, 0F, 0F, 0F, 0.5F, 0.5F, 0.5F),
        op(30, 0F, 0F, 0F, 0F, 0F, 0F, 1F, 1F, 1F),
        op(36, 19.999999999999996F, 10.6F, -4.999999999999999F, 0F, 0F, 0F, 0.7F, 0.7F, 0.7F),
        op(40, -3F, 2.2F, 7F, 0F, 0F, 0F, 0.5F, 0.5F, 0.5F)
        }));
    }

    private static void init1() {
        DATA.put("LightsaberStand", new Part(64, 32, new Node[]{
        node(10, 9, 0F, 0F, 0F, 0F, 0F, 0F, false, 2, new Box[]{new Box(0F, 0F, 0F, 15F, 3F, 1F)}),
        node(0, 0, 0.3F, 0.7F, 0F, 0F, 0F, -0.7853981633974483F, false, 3, new Box[]{new Box(0F, 0F, 0F, 1F, 1F, 1F)}),
        node(0, 9, -5.2F, 22.8F, -2.8F, 0F, 0F, 0F, false, -1, new Box[]{new Box(0F, 0F, 0F, 1F, 3F, 8F)}),
        node(0, 0, 2.5F, 22.3F, -0.5F, 0F, 0F, 0F, false, -1, new Box[]{new Box(0F, 0F, 0F, 1F, 1F, 1F)}),
        node(0, 0, -3.5F, 22.3F, -0.5F, 0F, 0F, 0F, false, -1, new Box[]{new Box(0F, 0F, 0F, 1F, 1F, 1F)}),
        node(10, 9, 0F, 0F, 7F, 0F, 0F, 0F, false, 2, new Box[]{new Box(0F, 0F, 0F, 15F, 3F, 1F)}),
        node(0, 0, 0F, 0F, 0F, 0F, 0F, 0.7853981633974483F, false, 4, new Box[]{new Box(0F, 0F, 0F, 1F, 1F, 1F)}),
        node(0, 0, -4.8F, 23F, -2.3F, 0F, 0F, 0F, false, -1, new Box[]{new Box(0F, 0F, 0F, 14F, 1F, 8F)}),
        node(0, 9, 14F, 0F, 0F, 0F, 0F, 0F, false, 2, new Box[]{new Box(0F, 0F, 0F, 1F, 3F, 8F)})
        }, new Op[]{
        op(2, -5.200000000000001F, 22.8F, -2.8F, 0F, 0F, 0F, 0.7F, 0.4F, 0.7F),
        op(3, 0F, 0F, 0F, 0F, 0F, 0F, 1F, 1F, 1F),
        op(4, 0F, 0F, 0F, 0F, 0F, 0F, 1F, 1F, 1F),
        op(7, -4.799999999999999F, 0F, -2.2999999999999994F, 0F, 0F, 0F, 0.7F, 1F, 0.6F)
        }));
    }

    private static void init2() {
        DATA.put("Crystal", new Part(64, 32, new Node[]{
        node(0, 0, -0.7999999999999998F, 24.3F, -0.8999999999999999F, 0.32551654794114643F, 0.3547259408961385F, -0.0699586487234274F, false, -1, new Box[]{new Box(-0.5F, -3F, -0.5F, 1F, 3F, 1F)}),
        node(0, 0, 0.5000000000000001F, 24F, -1.5F, 0.07720986750153881F, -0.8378148220521404F, 0.17911836686189572F, false, -1, new Box[]{new Box(-0.5F, -3F, -0.5F, 2F, 3F, 2F)}),
        node(0, 0, 0F, 0F, 0F, 0.5375647586253018F, 1.1136631997297022F, 0.8509015162038353F, false, -1, new Box[]{new Box(-0.5F, -4F, -1F, 1F, 4F, 2F)}),
        node(0, 0, 0F, 0F, 0F, -0.17193371309896593F, -0.030158260992957955F, -0.17193371309896593F, false, -1, new Box[]{new Box(-1F, -4F, -1F, 2F, 4F, 2F)}),
        node(0, 0, 0F, 0F, 0F, -0.5641743602931918F, -0.3381347255866406F, 0.11211168137329063F, false, -1, new Box[]{new Box(-0.5F, -3F, -0.5F, 1F, 3F, 1F)}),
        node(0, 0, 0F, 0F, 0F, 0.08679081139943337F, -0.009110381879359624F, 0.10432415531534107F, false, -1, new Box[]{new Box(-1F, -6F, -1F, 2F, 6F, 2F)})
        }, new Op[]{
        op(0, 0F, 0F, 0F, 0F, 0F, 0F, 1F, 1F, 1F),
        op(1, 0F, 0F, 0F, 0F, 0F, 0F, 1F, 1F, 1F),
        op(2, 0F, 0F, 0F, 0F, 0F, 0F, 1F, 1F, 1F),
        op(3, 0F, 0F, 0F, 0F, 0F, 0F, 1F, 1F, 1F),
        op(4, 0F, 0F, 0F, 0F, 0F, 0F, 1F, 1F, 1F),
        op(5, 0F, 0F, 0F, 0F, 0F, 0F, 1F, 1F, 1F)
        }));
    }

    private static void init3() {
        DATA.put("DisassemblyStation", new Part(128, 128, new Node[]{
        node(0, 0, 0F, 0F, 0F, 0F, 0F, 0F, false, -1, new Box[]{new Box(-16F, 10F, -8F, 32F, 14F, 16F)}),
        node(81, 10, 0F, 0F, 0F, 3.141592653589793F, -0.7853981633974484F, 3.141592653589793F, false, 13, new Box[]{new Box(-1.5F, -2F, 0.62F, 3F, 2F, 3F)}),
        node(81, 10, 0F, 0F, 0F, 3.141592653589793F, 0.7853981633974484F, 3.141592653589793F, false, 13, new Box[]{new Box(-1.5F, -2F, 0.62F, 3F, 2F, 3F)}),
        node(81, 0, -0.4F, 9.6F, 0F, 0F, 0F, -0.8726646259971648F, false, 10, new Box[]{new Box(0F, 0.6F, -0.01F, 2F, 6F, 2F)}),
        node(81, 10, 0F, 0F, 0F, 0F, 0.7853981633974483F, 0F, false, 13, new Box[]{new Box(-1.5F, -2F, 0.62F, 3F, 2F, 3F)}),
        node(81, 10, 0F, 0F, 0F, 3.141592653589793F, 1.2246467991473532e-16F, 3.141592653589793F, false, 13, new Box[]{new Box(-1.5F, -2F, 0.62F, 3F, 2F, 3F)}),
        node(81, 0, 4.5F, 6.6F, 0F, 0F, 0F, 1.5707963267948966F, false, 3, new Box[]{new Box(0F, 0.6F, -0.01F, 0F, 4F, 0F)}),
        node(81, 10, 0F, 0F, 0F, 0F, -0.7853981633974483F, 0F, false, 13, new Box[]{new Box(-1.5F, -2F, 0.62F, 3F, 2F, 3F)}),
        node(103, 11, -3.4F, 0.1F, -0.02F, 0F, 0F, -1.0995574287564276F, false, 11, new Box[]{new Box(0F, 0F, 0F, 2F, 5F, 2F)}),
        node(90, 0, 22F, -3F, 4F, 0F, 0F, 0F, false, -1, new Box[]{new Box(-19F, 10F, -8F, 12F, 3F, 7F)}),
        node(0, 0, -18F, -2F, 4F, -0.9075712110370513F, 0.13962634015954636F, 0F, false, -1, new Box[]{new Box(0F, 0F, 0F, 2F, 10F, 2F)}),
        node(94, 10, 1.84F, 5.61F, 0F, 0F, 0F, 2.356194490192345F, false, 6, new Box[]{new Box(0F, 0.6F, -0.01F, 2F, 3F, 2F)}),
        node(103, 11, 3.71F, 0.86F, -0.02F, 0F, 0F, 0.7330382858376184F, false, 21, new Box[]{new Box(0F, 0F, 0F, 2F, 4F, 2F)}),
        node(81, 10, -15F, -1.1F, 4F, 1.5707963267948966F, 1.5707963267948966F, 0F, false, -1, new Box[]{new Box(-1.5F, -2F, 0.62F, 3F, 2F, 3F)}),
        node(94, 26, -19F, 13.54F, -11.54F, 0.7853981633974483F, 0F, 0F, false, 9, new Box[]{new Box(0F, 0F, 0F, 12F, 3F, 5F)}),
        node(0, 30, 0F, -7F, 11F, 0F, 0F, 0F, false, -1, new Box[]{new Box(-16F, 2F, -8F, 32F, 15F, 5F)}),
        node(81, 10, 0F, 0F, 0F, 0F, 1.5707963267948966F, 0F, false, 13, new Box[]{new Box(-1.5F, -2F, 0.62F, 3F, 2F, 3F)}),
        node(0, 72, 0F, -2F, -13.47F, 1.0821041362364843F, 0F, 0F, false, 19, new Box[]{new Box(-15.99F, 10F, -8F, 32F, 8F, 4F)}),
        node(81, 10, 0F, 0F, 0F, 3.141592653589793F, -1.56049888420813F, 3.141592653589793F, false, 13, new Box[]{new Box(-1.5F, -2F, 0.62F, 3F, 2F, 3F)}),
        node(0, 50, 0F, -4F, -16F, 0.8726646259971648F, 0F, 0F, false, 15, new Box[]{new Box(-16.01F, 10F, -8F, 32F, 8F, 6F)}),
        node(0, 64, -16F, 6.22F, -6.52F, -0.8726646259971648F, 0F, 0F, false, 19, new Box[]{new Box(0F, 0F, 0F, 32F, 4F, 4F)}),
        node(94, 10, 1.84F, 5.61F, 0F, 0F, 0F, -2.356194490192345F, true, 6, new Box[]{new Box(3.2F, -0.8F, -0.01F, 2F, 3F, 2F)})
        }, new Op[]{
        op(0, 0F, 0F, 0F, 0F, 0F, 0F, 1F, 1F, 1F),
        op(9, 0F, 0F, 0F, 0F, 0F, 0F, 1F, 1F, 1F),
        op(10, 0F, 0F, 0F, 0F, 0F, 0F, 1F, 1F, 1F),
        op(13, 0F, -1.1F, 4F, 0F, 0F, 0F, 1F, 0.8F, 0.8F),
        op(15, 0F, 0F, 0F, 0F, 0F, 0F, 1F, 1F, 1F)
        }));
    }

    private static void init4() {
        DATA.put("SithCoffin", new Part(256, 128, new Node[]{
        node(107, 0, 0F, -2F, -13.5F, 0F, 0F, 0F, false, 4, new Box[]{new Box(-6F, -7F, -0.5F, 12F, 7F, 1F)}),
        node(107, 0, 0F, -2F, 13.5F, 3.141592653589793F, 1.2246467991473532e-16F, 3.141592653589793F, false, 4, new Box[]{new Box(-6F, -7F, -0.5F, 12F, 7F, 1F)}),
        node(64, 28, 1.5F, -2.3F, 0F, 0F, 0F, 0.6981317007977318F, false, 8, new Box[]{new Box(0F, 0F, -13F, 1F, 3F, 26F)}),
        node(0, 90, 0F, -2F, 0F, 0F, 0F, 0F, false, 4, new Box[]{new Box(-5.5F, -6F, -12F, 11F, 6F, 24F)}),
        node(110, 0, 0F, -3F, 8F, 0F, 0F, 0F, false, 9, new Box[]{new Box(-6F, -2F, -14F, 12F, 2F, 28F)}),
        node(172, 0, 3F, -1F, -13.98F, 0F, 0F, -1.5707963267948966F, true, 13, new Box[]{new Box(0F, -2F, 0F, 3F, 5F, 1F)}),
        node(58, 0, 0F, -5.5F, 0F, 0F, 0F, 0F, false, 0, new Box[]{new Box(-10F, -2F, -1F, 20F, 2F, 2F)}),
        node(172, 0, -3F, -1F, 13.98F, 0F, 0F, 1.5707963267948966F, false, 12, new Box[]{new Box(-3F, -2F, -1F, 3F, 5F, 1F)}),
        node(0, 60, -9F, 11.5F, 8F, 0F, 0F, 0F, false, -1, new Box[]{new Box(1.5F, -2.3F, -13F, 15F, 1F, 26F)}),
        node(160, 0, 0F, 24F, 0F, 0F, 0F, 0F, false, -1, new Box[]{new Box(-8F, -3F, -8F, 16F, 3F, 32F)}),
        node(0, 30, 9F, -1F, 1F, 0F, 0F, 0F, true, 6, new Box[]{new Box(-1F, -1F, 0F, 2F, 2F, 25F)}),
        node(0, 0, 9F, -1.3F, -12.5F, 0F, 0F, 0F, false, 8, new Box[]{new Box(-8F, 0F, -0.5F, 16F, 2F, 1F)}),
        node(183, 36, -6F, -2F, 0F, 0F, 0F, 1.0471975511965976F, false, 4, new Box[]{new Box(-8F, -1F, -14F, 8F, 1F, 28F)}),
        node(183, 36, 6F, -2F, 0F, 0F, 0F, -1.0471975511965976F, true, 4, new Box[]{new Box(0F, -1F, -14F, 8F, 1F, 28F)}),
        node(0, 0, 9F, -1.3F, 12.5F, 3.141592653589793F, 1.2246467991473532e-16F, 3.141592653589793F, false, 8, new Box[]{new Box(-8F, 0F, -0.5F, 16F, 2F, 1F)}),
        node(0, 30, -9F, -1F, 1F, 0F, 0F, 0F, false, 6, new Box[]{new Box(-1F, -1F, 0F, 2F, 2F, 25F)}),
        node(172, 0, -3F, -1F, -13.98F, 0F, 0F, 1.5707963267948966F, false, 12, new Box[]{new Box(-3F, -2F, 0F, 3F, 5F, 1F)}),
        node(64, 28, 16.5F, -2.3F, 0F, 0F, 0F, -0.6981317007977318F, true, 8, new Box[]{new Box(-1F, 0F, -13F, 1F, 3F, 26F)}),
        node(172, 0, 3F, -1F, 13.98F, 0F, 0F, -1.5707963267948966F, true, 13, new Box[]{new Box(0F, -2F, -1F, 3F, 5F, 1F)}),
        node(58, 0, 0F, 0F, 27F, 3.141592653589793F, 1.2246467991473532e-16F, 3.141592653589793F, false, 6, new Box[]{new Box(-10F, -2F, -1F, 20F, 2F, 2F)})
        }, new Op[]{
        op(8, 0F, 0F, 0F, 0F, 0F, 0F, 1F, 1F, 1F),
        op(9, 0F, 0F, 0F, 0F, 0F, 0F, 1F, 1F, 1F)
        }));
    }

    private static void init5() {
        DATA.put("SithStoneCoffin", new Part(128, 128, new Node[]{
        node(0, 0, 0F, 24F, 0F, 0F, 0F, 0F, false, -1, new Box[]{new Box(-8F, -3F, -8F, 16F, 3F, 16F)}),
        node(0, 20, 0F, -3F, 2F, 1.5707963267948966F, 0F, 0F, false, 0, new Box[]{new Box(-6.5F, -4.5F, 0F, 13F, 9F, 28F)})
        }, new Op[]{
        op(0, 0F, 0F, 0F, 0F, 0F, 0F, 1F, 1F, 1F)
        }));
    }

    private static final java.util.Map<String, ModelPart> CACHE = new HashMap<>();

    public static ModelPart cached(String key) {
        return CACHE.computeIfAbsent(key, k -> buildModel(DATA.get(k)));
    }

    public static ModelPart buildModel(Part part) {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData[] built = new ModelPartData[part.nodes().length];
        for (int i = 0; i < part.nodes().length; i++) {
            buildNode(root, built, part, i);
        }
        ModelPart model = TexturedModelData.of(data, part.texW(), part.texH()).createModel();

        for (int i = 0; i < part.ops().length; i++) {
            Op op = part.ops()[i];
            if (op.sx() == 1F && op.sy() == 1F && op.sz() == 1F) {
                continue;
            }
            ModelPart wrapper = model.getChild("w" + i).getChild("ws" + i);
            wrapper.xScale = op.sx();
            wrapper.yScale = op.sy();
            wrapper.zScale = op.sz();
        }
        return model;
    }

    public static boolean isIdentity(Op op) {
        return op.ax() == 0F && op.ay() == 0F && op.az() == 0F
            && op.rx() == 0F && op.ry() == 0F && op.rz() == 0F
            && op.sx() == 1F && op.sy() == 1F && op.sz() == 1F;
    }

    public static ModelPart opPart(ModelPart model, Part part, int i) {
        Op op = part.ops()[i];
        if (isIdentity(op)) {
            return model.getChild("n" + op.node());
        }
        return model.getChild("w" + i).getChild("ws" + i).getChild("wb" + i).getChild("n" + op.node());
    }

    public static void renderOpPart(MatrixStack matrices, ModelPart model, Part part, int i,
            VertexConsumer vc, int light, int overlay) {
        Op op = part.ops()[i];
        if (isIdentity(op)) {

            model.getChild("n" + op.node()).render(matrices, vc, light, overlay);
            return;
        }

        ModelPart subtree = model.getChild("w" + i).getChild("ws" + i)
                .getChild("wb" + i).getChild("n" + op.node());
        matrices.push();
        if (op.rx() != 0F || op.ry() != 0F || op.rz() != 0F) {
            matrices.multiply(new org.joml.Quaternionf().rotationZYX(op.rz(), op.ry(), op.rx()));
        }
        matrices.translate(op.ax() / 16F, op.ay() / 16F, op.az() / 16F);
        matrices.scale(op.sx(), op.sy(), op.sz());
        matrices.translate(-op.ax() / 16F, -op.ay() / 16F, -op.az() / 16F);
        subtree.render(matrices, vc, light, overlay);
        matrices.pop();
    }

    private static ModelPartData buildNode(ModelPartData root, ModelPartData[] built, Part part, int i) {
        if (built[i] != null) {
            return built[i];
        }
        Node n = part.nodes()[i];
        ModelPartBuilder builder = ModelPartBuilder.create().uv(n.u(), n.v());
        if (n.mirror()) {
            builder.mirrored();
        }
        for (Box b : n.boxes()) {
            builder.cuboid(b.x(), b.y(), b.z(), b.w(), b.h(), b.d(), Dilation.NONE);
        }
        ModelPartData parentData;
        int opIdx = wrapperOf(part, i);
        if (opIdx >= 0 && !isIdentity(part.ops()[opIdx])) {

            Op op = part.ops()[opIdx];
            ModelPartData w = root.addChild("w" + opIdx, ModelPartBuilder.create(),
                    ModelTransform.of(0F, 0F, 0F, op.rx(), op.ry(), op.rz()));
            ModelPartData ws = w.addChild("ws" + opIdx, ModelPartBuilder.create(),
                    ModelTransform.pivot(op.ax(), op.ay(), op.az()));
            parentData = ws.addChild("wb" + opIdx, ModelPartBuilder.create(),
                    ModelTransform.pivot(-op.ax(), -op.ay(), -op.az()));
        } else if (n.parent() < 0) {
            parentData = root;
        } else {
            parentData = buildNode(root, built, part, n.parent());
        }
        built[i] = parentData.addChild("n" + i, builder,
                ModelTransform.of(n.px(), n.py(), n.pz(), n.rx(), n.ry(), n.rz()));
        return built[i];
    }

    private static int wrapperOf(Part part, int i) {
        for (int k = 0; k < part.ops().length; k++) {
            if (part.ops()[k].node() == i) {
                return k;
            }
        }
        return -1;
    }
}
