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
        // Compact row, loading state and widened/localized text across GUI-scale/resolution changes.
        for (int[] panel : new int[][]{{148, 26}, {116, 19}, {400, 26}}) {
            for (int[] screen : new int[][]{{1, 1}, {64, 32}, {320, 180}, {854, 480}, {3440, 1440}}) {
                for (boolean right : new boolean[]{false, true}) {
                    for (boolean bottom : new boolean[]{false, true}) {
                        for (double requested : new double[]{0.5, 1.0, 3.0}) {
                            for (int offset : new int[]{-10000, 0, 10000}) {
                                HudLayout.Placement p = HudLayout.place(screen[0], screen[1], panel[0], panel[1],
                                        requested, right, bottom, offset, offset);
                                if (!Double.isFinite(p.scale()) || p.scale() <= 0 || p.scale() > requested
                                        || p.x() < 0 || p.y() < 0 || p.x() + panel[0] * p.scale() > screen[0] + 1e-6
                                        || p.y() + panel[1] * p.scale() > screen[1] + 1e-6) {
                                    throw new AssertionError("Resized HUD outside screen: " + p);
                                }
                            }
                        }
                    }
                }
            }
        }
        for (boolean loaded : new boolean[]{false, true}) {
            for (int[] text : new int[][]{{36, 56, 28}, {36, 68, 34}, {150, 200, 70}, {0, 0, 0}}) {
                for (int lineHeight : new int[]{9, 18}) {
                    HudLayout.Row row = HudLayout.compactRow(text[0], text[1], text[2], lineHeight, loaded);
                    if (row.countX() < HudLayout.PADDING + text[0] + HudLayout.GAP
                            || row.countX() + text[1] > row.width() - HudLayout.PADDING
                            || (loaded && row.countX() + text[1] + HudLayout.GAP > row.percentageX())
                            || (loaded && row.percentageX() + text[2] > row.width() - HudLayout.PADDING)
                            || (loaded && row.barY() < HudLayout.PADDING + lineHeight + 3)
                            || (loaded && row.barY() + HudLayout.BAR_HEIGHT > row.height() - HudLayout.PADDING)
                            || row.height() >= 39 + (lineHeight - 9) * 2) {
                        throw new AssertionError("Compact text overlaps or clips: " + row);
                    }
                    for (boolean right : new boolean[]{false, true}) {
                        for (boolean bottom : new boolean[]{false, true}) {
                            HudLayout.Placement p = HudLayout.place(64, 32, row.width(), row.height(),
                                    3, right, bottom, 10000, -10000);
                            if (p.x() < 0 || p.y() < 0 || p.x() + row.width() * p.scale() > 64 + 1e-6
                                    || p.y() + row.height() * p.scale() > 32 + 1e-6) {
                                throw new AssertionError("Compact row outside small screen: " + p);
                            }
                        }
                    }
                }
            }
        }
        System.out.println("HUD layout checks passed: compact row without overlapping text, loading/long text, corners, offsets, scales, screen bounds and opacity.");
    }

    private static void check(HudLayout.Placement actual, double x, double y, double scale) {
        if (Math.abs(actual.x() - x) > 1e-6 || Math.abs(actual.y() - y) > 1e-6
                || Math.abs(actual.scale() - scale) > 1e-6) {
            throw new AssertionError("Unexpected placement: " + actual);
        }
    }
}
