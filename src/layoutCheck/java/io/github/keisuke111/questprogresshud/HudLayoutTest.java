package io.github.keisuke111.questprogresshud;

public final class HudLayoutTest {
    public static void main(String[] args) {
        check(HudLayout.place(320, 180, 128, 42, 1, false, false, 0, 0), 8, 8, 1);
        check(HudLayout.place(320, 180, 128, 42, 1, true, false, 0, 0), 184, 8, 1);
        check(HudLayout.place(320, 180, 128, 42, 1, false, true, 0, 0), 8, 130, 1);
        check(HudLayout.place(320, 180, 128, 42, 1, true, true, 0, 0), 184, 130, 1);
        check(HudLayout.place(320, 180, 128, 42, 1, true, true, 12, 5), 172, 125, 1);
        check(HudLayout.place(320, 180, 128, 42, 0.5, true, true, 0, 0), 248, 151, 0.5);
        check(HudLayout.place(64, 32, 128, 42, 3, true, true, 0, 0), 0, 3, 0.5);
        check(HudLayout.place(320, 180, 128, 42, 1, false, false, -10000, -10000), 0, 0, 1);
        check(HudLayout.place(320, 180, 128, 42, 1, false, false, 10000, 10000), 192, 138, 1);

        for (boolean right : new boolean[]{false, true}) {
            for (boolean bottom : new boolean[]{false, true}) {
                for (double scale : new double[]{0.5, 1, 1.5, 3}) {
                    for (int[] screen : new int[][]{{320, 180}, {854, 480}, {64, 32}}) {
                        for (int offset : new int[]{-10000, 0, 12, 10000}) {
                            HudLayout.Placement p = HudLayout.place(screen[0], screen[1], 200, 42,
                                    scale, right, bottom, offset, offset);
                            if (p.x() < 0 || p.y() < 0 || p.x() + 200 * p.scale() > screen[0] + 1e-6
                                    || p.y() + 42 * p.scale() > screen[1] + 1e-6) {
                                throw new AssertionError("HUD outside screen: " + p);
                            }
                        }
                    }
                }
            }
        }
        if (HudLayout.backgroundColor(0) != 0x0014171A
                || HudLayout.backgroundColor(72) != 0xB814171A
                || HudLayout.backgroundColor(100) != 0xFF14171A) {
            throw new AssertionError("Incorrect background opacity");
        }
        System.out.println("HUD layout checks passed: corners, offsets, scales, screen bounds and opacity.");
    }

    private static void check(HudLayout.Placement actual, double x, double y, double scale) {
        if (Math.abs(actual.x() - x) > 1e-6 || Math.abs(actual.y() - y) > 1e-6
                || Math.abs(actual.scale() - scale) > 1e-6) {
            throw new AssertionError("Unexpected placement: " + actual);
        }
    }
}
