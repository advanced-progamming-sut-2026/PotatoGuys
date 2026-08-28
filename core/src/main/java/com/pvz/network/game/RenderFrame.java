package com.pvz.network.game;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import com.pvz.models.engine.FrameConfig;

/**
 * The complete "picture" of one host frame, sent to the guest: a compact binary
 * message consisting of a small scalar header (seq / game-over flag / sun /
 * I,Zombie brains) followed by every {@link FrameConfig} the host is going to
 * draw, in draw order.
 *
 * <p>Transport note: this whole message is serialized to one byte[] by
 * {@link #write} and base64-encoded by the caller so it can ride the existing
 * JSON match relay (which only accepts a String payload). The guest decodes the
 * base64 back to bytes and calls {@link #read} — no per-entity JSON anywhere on
 * the hot path.
 */
public class RenderFrame {

    public int seq;
    public boolean gameOver;
    public int currentSun;
    /** I,Zombie-only; -1 for non-IZombie seasons. */
    public int brainCount = -1;
    /** I,Zombie-only; null elsewhere. */
    public boolean[] brainsEaten;
    public float plantSurvivalSecondsRemaining;
    public List<FrameConfig> frames;

    /** Serializes this frame into a single compact byte array. */
    public byte[] write() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(128 + frames.size() * 96);
            DataOutputStream out = new DataOutputStream(baos);
            out.writeInt(seq);
            out.writeByte(gameOver ? 1 : 0);
            out.writeInt(currentSun);
            out.writeFloat(plantSurvivalSecondsRemaining);
            out.writeInt(brainCount);
            if (brainCount >= 0) {
                for (int i = 0; i < brainCount; i++) {
                    out.writeByte((brainsEaten != null && i < brainsEaten.length && brainsEaten[i]) ? 1 : 0);
                }
            }
            // Frames inline right after the header.
            byte[] frameBytes = FrameConfigCodec.serialize(frames);
            out.write(frameBytes);
            out.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write RenderFrame", e);
        }
    }

    /** Deserializes a byte array produced by {@link #write}. */
    public static RenderFrame read(byte[] data) {
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
            RenderFrame rf = new RenderFrame();
            rf.seq = in.readInt();
            rf.gameOver = in.readByte() != 0;
            rf.currentSun = in.readInt();
            rf.plantSurvivalSecondsRemaining = in.readFloat();
            int brainCount = in.readInt();
            if (brainCount >= 0) {
                rf.brainCount = brainCount;
                rf.brainsEaten = new boolean[brainCount];
                for (int i = 0; i < brainCount; i++) {
                    rf.brainsEaten[i] = in.readByte() != 0;
                }
            }
            int frameBytesLen = in.available();
            byte[] frameBytes = new byte[frameBytesLen];
            in.readFully(frameBytes);
            rf.frames = FrameConfigCodec.deserialize(frameBytes);
            return rf;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read RenderFrame", e);
        }
    }
}
