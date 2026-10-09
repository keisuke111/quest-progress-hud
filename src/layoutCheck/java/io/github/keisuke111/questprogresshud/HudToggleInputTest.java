package io.github.keisuke111.questprogresshud;

public final class HudToggleInputTest {
    public static void main(String[] args) {
        HudToggleInput input = new HudToggleInput();
        expect(!input.update(false, false, true), "Unbound/idle input does nothing");
        expect(input.update(true, true, true), "First press toggles");
        for (int tick = 0; tick < 100; tick++) {
            expect(!input.update(true, true, true), "Held-key repeat does not toggle");
        }
        expect(!input.update(false, false, true), "Release does not toggle");
        expect(input.update(true, true, true), "Next press toggles again");
        input.update(false, false, true);
        expect(input.update(false, true, true), "Quick tap released between ticks still toggles");
        expect(!input.update(true, true, false), "Chat/menu/title/focus-loss input discarded");
        expect(!input.update(true, true, true), "Key held when closing a menu is not replayed");
        input.update(false, false, false);
        expect(!input.update(false, false, true), "Queued menu click is drained");
        expect(input.update(true, true, true), "Fresh gameplay press works");
        System.out.println("HUD toggle input checks passed: idle, press/hold/release, quick tap and menu/focus suppression.");
    }
    private static void expect(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
