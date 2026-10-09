package io.github.keisuke111.questprogresshud;

public final class HudEditorStateTest {
    public static void main(String[] args) {
        HudLayout.Row row = HudLayout.compactRow(36, 56, 28, 9, true);
        for (boolean right : new boolean[]{false, true}) {
            for (boolean bottom : new boolean[]{false, true}) {
                for (double scale : new double[]{0.5, 1.0, 1.75, 3.0}) {
                    HudEditorState.Settings original = new HudEditorState.Settings(right, bottom, 12, 17, scale, 43);
                    HudEditorState state = new HudEditorState(original);
                    HudLayout.Placement p = state.placement(854, 480, row);
                    expect(!state.beginDrag(-1, -1, 854, 480, row), "Click outside ignored");
                    expect(state.beginDrag(p.x() + 3, p.y() + 4, 854, 480, row), "Grab inside HUD");
                    state.dragTo(223, 164, 854, 480, row);
                    HudLayout.Placement moved = state.placement(854, 480, row);
                    near(moved.x(), 220);
                    near(moved.y(), 160);
                    expect(state.draft().right() == right && state.draft().bottom() == bottom, "Corner preserved after drag");
                    for (int mouse : new int[]{-10000, 10000}) {
                        state.dragTo(mouse, mouse, 854, 480, row);
                        bounds(state.placement(854, 480, row), 854, 480, row);
                    }
                    state.endDrag();
                    HudEditorState.Settings stopped = state.draft();
                    state.dragTo(0, 0, 854, 480, row);
                    expect(state.draft().equals(stopped), "Release stops drag");
                    state.setScale(2.5);
                    state.setOpacity(80);
                    for (int[] screen : new int[][]{{320, 180}, {64, 32}, {1, 1}, {854, 480}}) {
                        bounds(state.placement(screen[0], screen[1], row), screen[0], screen[1], row);
                    }
                    expect(state.original().equals(original), "Cancel source unchanged by editing");
                    state.reset();
                    expect(state.draft().equals(new HudEditorState.Settings(false, false, 0, 0, 1.0, 72)), "Reset previews existing defaults");
                    expect(state.original().equals(original), "Reset can still be cancelled");
                }
            }
        }
        HudEditorState state = new HudEditorState(new HudEditorState.Settings(false, false, 0, 0, 1, 72));
        state.setScale(Double.NaN);
        expect(state.draft().scale() == 1, "NaN rejected");
        state.setScale(99);
        state.setOpacity(999);
        expect(state.draft().scale() == 3 && state.draft().opacity() == 100, "Maximum values bounded");
        state.setScale(-99);
        state.setOpacity(-99);
        expect(state.draft().scale() == 0.5 && state.draft().opacity() == 0, "Minimum values bounded");
        System.out.println("HUD editor checks passed: all corner/scale drags, screen bounds, resize, reset and isolated cancellation state.");
    }

    private static void bounds(HudLayout.Placement p, int width, int height, HudLayout.Row row) {
        expect(p.x() >= 0 && p.y() >= 0 && p.x() + row.width() * p.scale() <= width + 1e-6
                && p.y() + row.height() * p.scale() <= height + 1e-6, "HUD remains on screen");
    }
    private static void near(double actual, double expected) { expect(Math.abs(actual - expected) <= 0.5, "Rounded GUI-pixel offset matches drag"); }
    private static void expect(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
