package com.pvz.controller.game.npc;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import com.pvz.PvZ2;
import com.pvz.controller.AudioManager;

import pvz.skin.PvzSkin;

import java.util.List;

/**
 * Overlay shown before a level's objectives: plays an NPC (PAM) walk-on, types
 * out its dialogue lines in a comic speech bubble, then walks it off and runs
 * {@code onFinished}.
 *
 * <p>Fully data-driven by {@link NpcDialogueSequence}. Port of group-51's
 * {@code NpcDialogueOverlay}, adapted to this project (no speech-bubble image
 * asset, so the bubble is built from a rounded nine-patch).
 *
 * <p>State machine: ENTERING -&gt; TYPING -&gt; WAITING_FOR_ENTER (-&gt; next
 * line or) -&gt; LEAVING -&gt; FINISHED. Tap anywhere or press Enter to advance.
 *
 * <p>Once finished the overlay removes itself and calls {@code onFinished}.
 */
public final class NpcDialogueOverlay extends Table {
    private static final float TYPE_SECONDS_PER_CHARACTER = 0.035f;
    private static final float ENTER_FALLBACK_DURATION = 1.4f;
    private static final float LEAVE_FALLBACK_DURATION = 1.4f;
    private static final float NPC_SCALE = 0.70f;
    private static final float BACKGROUND_DIM_ALPHA = 0.52f;
    private static final float BUBBLE_BOTTOM_MARGIN = 300f;
    private static final float BUBBLE_LEFT_MARGIN = 550f;

    private static final String TYPETICK_PATH = "audio/music/typing.mp3";

    private enum State {
        ENTERING,
        TYPING,
        WAITING_FOR_ENTER,
        LEAVING,
        FINISHED
    }

    private final NpcDialogueSequence sequence;
    private final Runnable onFinished;

    private final NpcSpriteActor npcActor;
    private final Image bubbleBackground;
    private final Label dialogueLabel;
    private final Label continueLabel;

    private State state = State.ENTERING;
    private int lineIndex = -1;
    private int visibleCharacters;
    private float phaseTime;
    private float typingTime;
    private float phaseDuration;
    private boolean finishCallbackRun;

    public NpcDialogueOverlay(NpcDialogueSequence sequence, Runnable onFinished) {
        this.sequence = sequence;
        this.onFinished = onFinished;

        setFillParent(true);
        setTouchable(Touchable.enabled);
        setBackground(PvzSkin.get().newDrawable("white_pixel", new Color(0f, 0f, 0f, BACKGROUND_DIM_ALPHA)));

        bubbleBackground = new Image(roundedBubbleDrawable());
        bubbleBackground.setTouchable(Touchable.disabled);
        bubbleBackground.setVisible(false);

        dialogueLabel = createDialogueLabel();
        continueLabel = createContinueLabel();

        npcActor = new NpcSpriteActor(sequence.pamPath());
        npcActor.setScale(NPC_SCALE);
        npcActor.setTouchable(Touchable.disabled);

        addActor(bubbleBackground);
        addActor(dialogueLabel);
        addActor(continueLabel);
        addActor(npcActor);

        PvZ2.pamPlayer.loadSync(sequence.pamPath());
        installInputListener();
        beginEnterAnimation();
    }

    // ---------------------------------------------------------------- bubble

    private static NinePatchDrawable roundedBubbleDrawable() {
        return roundedPatch(56, 26);
    }

    private static NinePatchDrawable roundedPatch(int size, int radius) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.setColor(0.98f, 0.94f, 0.84f, 1f);
        pm.fillCircle(radius, radius, radius);
        pm.fillCircle(size - radius - 1, radius, radius);
        pm.fillCircle(radius, size - radius - 1, radius);
        pm.fillCircle(size - radius - 1, size - radius - 1, radius);
        pm.fillRectangle(radius, 0, size - 2 * radius, size);
        pm.fillRectangle(0, radius, size, size - 2 * radius);
        Texture texture = new Texture(pm);
        pm.dispose();
        return new NinePatchDrawable(new NinePatch(texture, radius, radius, radius, radius));
    }

    private Label createDialogueLabel() {
        Label.LabelStyle style = new Label.LabelStyle(
                PvzSkin.get().getFont("FBUSV8C5EI_2"),
                Color.valueOf("2A1A09"));
        Label label = new Label("", style);
        label.setAlignment(Align.topLeft);
        label.setWrap(true);
        label.setTouchable(Touchable.disabled);
        label.setVisible(false);
        return label;
    }

    private Label createContinueLabel() {
        Label.LabelStyle style = PvzSkin.get().get("medium", Label.LabelStyle.class);
        Label label = new Label("tap or ENTER to continue", style);
        label.setColor(Color.valueOf("6F3E1C"));
        label.setAlignment(Align.center);
        label.setFontScale(0.72f);
        label.setTouchable(Touchable.disabled);
        label.setVisible(false);
        return label;
    }

    // ---------------------------------------------------------------- input

    private void installInputListener() {
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                handleAdvanceInput();
                return true;
            }

            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode != Input.Keys.ENTER && keycode != Input.Keys.SPACE) {
                    return false;
                }
                handleAdvanceInput();
                return true;
            }
        });
    }

    // ---------------------------------------------------------------- state

    private void beginEnterAnimation() {
        state = State.ENTERING;
        phaseTime = 0f;
        phaseDuration = clipDuration(sequence.enterClip(), ENTER_FALLBACK_DURATION);
        npcActor.play(sequence.enterClip(), false);
    }

    private void beginLine(int nextLineIndex) {
        lineIndex = nextLineIndex;
        visibleCharacters = 0;
        typingTime = 0f;
        state = State.TYPING;

        dialogueLabel.setText("");
        dialogueLabel.setVisible(true);
        bubbleBackground.setVisible(true);
        continueLabel.setVisible(false);

        String talkClip = sequence.talkClips().get(lineIndex % sequence.talkClips().size());
        npcActor.play(talkClip, true);

        playTypetick();
    }

    private void finishTyping() {
        String line = sequence.lines().get(lineIndex);
        dialogueLabel.setText(line);
        visibleCharacters = line.length();
        state = State.WAITING_FOR_ENTER;
        continueLabel.setVisible(true);
        npcActor.play(sequence.idleClip(), true);
    }

    private void handleAdvanceInput() {
        AudioManager.getInstance().stopSfxMusic(TYPETICK_PATH);
        if (state == State.TYPING) {
            finishTyping();
            return;
        }
        if (state == State.WAITING_FOR_ENTER) {
            advanceDialogue();
        }
    }

    private void advanceDialogue() {
        if (state != State.WAITING_FOR_ENTER) {
            return;
        }
        int nextLine = lineIndex + 1;
        if (nextLine < sequence.lines().size()) {
            beginLine(nextLine);
            return;
        }
        beginLeaveAnimation();
    }

    private void beginLeaveAnimation() {
        state = State.LEAVING;
        phaseTime = 0f;
        phaseDuration = clipDuration(sequence.leaveClip(), LEAVE_FALLBACK_DURATION);

        bubbleBackground.setVisible(false);
        dialogueLabel.setVisible(false);
        continueLabel.setVisible(false);

        npcActor.play(sequence.leaveClip(), false);
    }

    // ---------------------------------------------------------------- update

    @Override
    public void act(float delta) {
        super.act(delta);
        switch (state) {
            case ENTERING -> updateEntering(delta);
            case TYPING -> updateTyping(delta);
            case LEAVING -> updateLeaving(delta);
            case WAITING_FOR_ENTER, FINISHED -> {
            }
        }
    }

    private void updateEntering(float delta) {
        phaseTime += Math.max(0f, delta);
        if (phaseTime >= phaseDuration) {
            beginLine(0);
        }
    }

    private void updateTyping(float delta) {
        String line = sequence.lines().get(lineIndex);
        typingTime += Math.max(0f, delta);

        int characters = Math.min(
                line.length(),
                (int) (typingTime / TYPE_SECONDS_PER_CHARACTER));

        if (characters != visibleCharacters) {
            visibleCharacters = characters;
            dialogueLabel.setText(line.substring(0, visibleCharacters));
        }

        if (visibleCharacters >= line.length()) {
            finishTyping();
        }
    }

    private void updateLeaving(float delta) {
        phaseTime += Math.max(0f, delta);
        if (phaseTime < phaseDuration) {
            return;
        }
        state = State.FINISHED;
        remove();
        runFinishCallback();
    }

    private void runFinishCallback() {
        if (finishCallbackRun) {
            return;
        }
        finishCallbackRun = true;
        if (onFinished != null) {
            onFinished.run();
        }
    }

    private float clipDuration(String clip, float fallback) {
        try {
            return Math.max(0.05f, PvZ2.pamPlayer.clipDurationSeconds(sequence.pamPath(), clip));
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    // ---------------------------------------------------------------- sound

    /** Plays the typewriter sound once when a dialogue line begins. Respects the
     *  user's SFX volume/mute via {@link AudioManager}. */
    private void playTypetick() {
        AudioManager.getInstance().playSfxMusic(TYPETICK_PATH);
    }

    // ---------------------------------------------------------------- layout

    @Override
    public void layout() {
        super.layout();
        layoutBubble();
        layoutNpc();
    }

    private void layoutBubble() {
        float bubbleWidth = 620f;
        float bubbleHeight = 210f;
        float bubbleX = BUBBLE_LEFT_MARGIN;
        float bubbleY = BUBBLE_BOTTOM_MARGIN;

        bubbleBackground.setBounds(bubbleX, bubbleY, bubbleWidth, bubbleHeight);

        float textPadX = 46f;
        float textTopPad = 40f;
        float textBottomPad = 44f;
        dialogueLabel.setBounds(
                bubbleX + textPadX,
                bubbleY + textBottomPad,
                bubbleWidth - 2f * textPadX,
                bubbleHeight - textTopPad - textBottomPad);

        continueLabel.setBounds(
                bubbleX + 40f,
                bubbleY + 16f,
                bubbleWidth - 80f,
                26f);
    }

    private void layoutNpc() {
        float targetX = getWidth() * 0.30f;
        float targetY = getHeight() * 0.40f;
        npcActor.setPosition(targetX, targetY);
    }

    /**
     * A scene2d {@link Actor} that renders a single PAM clip via
     * {@link PvZ2#pamPlayer}, with a runtime-switchable clip (enter -&gt; talk
     * -&gt; idle -&gt; leave) and optional looping.
     */
    private static final class NpcSpriteActor extends Actor {
        private final String pamPath;
        private String clip;
        private boolean loop;
        private float stateTime;

        NpcSpriteActor(String pamPath) {
            this.pamPath = pamPath;
        }

        void play(String clip, boolean loop) {
            this.clip = clip;
            this.loop = loop;
            this.stateTime = 0f;
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += Math.max(0f, delta);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (clip == null) {
                return;
            }
            float cx = getX() + getWidth() / 2f;
            float cy = getY() + getHeight() / 2f;
            float sx = getScaleX();
            float sy = getScaleY();
            PvZ2.pamPlayer.draw(batch, pamPath, clip, stateTime, cx, cy, sx, sy, loop);
        }
    }
}
