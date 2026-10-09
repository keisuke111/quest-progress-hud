package io.github.keisuke111.questprogresshud;

/** Reject held-key repeats and discard input received outside gameplay. */
final class HudToggleInput {
    private boolean held;
    private boolean waitForRelease;

    boolean update(boolean down, boolean clicked, boolean allowed) {
        if (!allowed) {
            waitForRelease = true;
            held = down;
            return false;
        }
        if (waitForRelease) {
            held = down;
            if (!down && !clicked) waitForRelease = false;
            return false;
        }
        boolean toggle = allowed && clicked && !held;
        held = down;
        return toggle;
    }
}
