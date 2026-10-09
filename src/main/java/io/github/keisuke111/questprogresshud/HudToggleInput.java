package io.github.keisuke111.questprogresshud;

/** Reject held-key repeats and discard input received outside gameplay. */
final class HudToggleInput {
    private boolean held;

    boolean update(boolean down, boolean clicked, boolean allowed) {
        boolean toggle = allowed && clicked && !held;
        held = down;
        return toggle;
    }
}
