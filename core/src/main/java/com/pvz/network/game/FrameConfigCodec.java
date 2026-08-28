package com.pvz.network.game;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.math.Vector2;
import com.pvz.models.engine.FrameConfig;

/**
 * Compact binary serializer for a rendered frame — the exact list of {@link FrameConfig}s
 * the host is about to draw, in draw order. The guest deserializes this and renders the
 * frames directly (it never reconstructs entities or re-runs the FSM).
 *
 * <p>This is the network-heavy payload that used to be a JSON blob of entities. It's
 * written as lean bytes instead: length-prefixed UTF-8 strings, raw floats/ints, and
 * a single byte per boolean. The result is base64-encoded only at the very last hop
 * because the existing match relay tunnels a String payload (see
 * {@link com.pvz.network.MatchDTOs.MatchMessageEnvelope}); the inner content is pure
 * bytes, never per-field JSON.
 */
public final class FrameConfigCodec {

    private FrameConfigCodec() {
    }

    /** Serializes a list of frames to a compact byte array. */
    public static byte[] serialize(List<FrameConfig> frames) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(64 + frames.size() * 96);
            DataOutputStream out = new DataOutputStream(baos);
            out.writeInt(frames.size());
            for (FrameConfig fc : frames) {
                writeFrame(out, fc);
            }
            out.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to serialize frames", e);
        }
    }

    /** Deserializes a byte array produced by {@link #serialize}. */
    public static List<FrameConfig> deserialize(byte[] data) {
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
            int count = in.readInt();
            List<FrameConfig> frames = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                frames.add(readFrame(in));
            }
            return frames;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to deserialize frames", e);
        }
    }

    private static void writeFrame(DataOutputStream out, FrameConfig fc) throws IOException {
        if (fc == null) {
            out.writeByte(1);
            return;
        }
        out.writeByte(0);
        writeString(out, fc.pamPath);
        writeString(out, fc.label);
        out.writeFloat(fc.stateTime);
        out.writeFloat(fc.position.x);
        out.writeFloat(fc.position.y);
        out.writeFloat(fc.scale.x);
        out.writeFloat(fc.scale.y);
        out.writeByte(fc.looping ? 1 : 0);
        out.writeByte((int) (fc.r * 255f) & 0xFF);
        out.writeByte((int) (fc.g * 255f) & 0xFF);
        out.writeByte((int) (fc.b * 255f) & 0xFF);
        out.writeByte((int) (fc.a * 255f) & 0xFF);
        if (fc.partsVisibility == null) {
            out.writeInt(-1);
        } else {
            out.writeInt(fc.partsVisibility.size());
            for (Map.Entry<String, Boolean> e : fc.partsVisibility.entrySet()) {
                writeString(out, e.getKey());
                out.writeByte(e.getValue() ? 1 : 0);
            }
        }
    }

    private static FrameConfig readFrame(DataInputStream in) throws IOException {
        if (in.readByte() != 0)
            return null;
        String pamPath = readString(in);
        String label = readString(in);
        float stateTime = in.readFloat();
        float px = in.readFloat();
        float py = in.readFloat();
        float sx = in.readFloat();
        float sy = in.readFloat();
        boolean looping = in.readByte() != 0;
        int r = in.readUnsignedByte();
        int g = in.readUnsignedByte();
        int b = in.readUnsignedByte();
        int a = in.readUnsignedByte();

        int visCount = in.readInt();
        Map<String, Boolean> vis = null;
        if (visCount >= 0) {
            vis = new HashMap<>(visCount);
            for (int i = 0; i < visCount; i++) {
                vis.put(readString(in), in.readByte() != 0);
            }
        }

        FrameConfig fc = new FrameConfig(pamPath, label, stateTime,
                new Vector2(px, py), new Vector2(sx, sy), vis, looping);
        fc.setColor(r / 255f, g / 255f, b / 255f, a / 255f);
        return fc;
    }

    private static void writeString(DataOutputStream out, String s) throws IOException {
        if (s == null) {
            out.writeInt(-1);
            return;
        }
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    private static String readString(DataInputStream in) throws IOException {
        int len = in.readInt();
        if (len < 0)
            return null;
        byte[] bytes = new byte[len];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
