package io.github.keisuke111.questprogresshud;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public final class ProgressTrackerTest {
    private static int samples;
    private static ProgressTracker.Snapshot value = new ProgressTracker.Snapshot(508, 4790);
    private static final Supplier<ProgressTracker.Snapshot> COUNT = () -> { samples++; return value; };

    public static void main(String[] args) throws Exception {
        ProgressTracker t = new ProgressTracker();
        Object connection = new Object(), world = new Object(), file = new Object(), team = new Object();
        expect(t.update(0, connection, world, file, null, false, COUNT) == null && samples == 0, "No team sync");
        expect(t.update(1, connection, world, file, team, false, COUNT) == null && samples == 0, "Placeholder team");
        expect(t.update(2, connection, world, file, team, true, COUNT).equals(value) && samples == 1, "Initial sync");
        value = new ProgressTracker.Snapshot(509, 4790);
        for (int tick = 1; tick < 20; tick++) {
            expect(t.update(2 + tick * 50_000_000L, connection, world, file, team, true, COUNT).completed() == 508,
                    "Stable session refresh is throttled");
        }
        expect(samples == 1, "No per-frame/per-tick full scan");
        expect(t.update(1_000_000_002L, connection, world, file, team, true, COUNT).completed() == 509
                && samples == 2, "Refresh at one second");
        expect(t.update(1_000_000_003L, connection, world, file, team, false, COUNT) == null, "Sync loss clears old counts immediately");
        expect(t.update(1_000_000_004L, connection, world, file, team, true, COUNT) != null && samples == 3, "Sync return refreshes immediately");
        team = new Object();
        value = new ProgressTracker.Snapshot(0, 4790);
        expect(t.update(1_000_000_005L, connection, world, file, team, true, COUNT).completed() == 0 && samples == 4, "Team replacement");
        world = new Object();
        t.update(1_000_000_006L, connection, world, file, team, true, COUNT);
        expect(samples == 5, "Dimension/world replacement");
        t.disconnect(file);
        connection = new Object(); world = new Object();
        expect(t.update(1_000_000_007L, connection, world, file, team, true, COUNT) == null && samples == 5,
                "Old FTB file cannot appear on another server");
        t.clear(); // Disabled HUD must not discard the disconnect fence.
        expect(t.update(1_000_000_008L, connection, world, file, team, true, COUNT) == null, "Fence survives hiding");
        file = new Object();
        expect(t.update(1_000_000_009L, connection, world, file, null, false, COUNT) == null, "New file still needs team sync");
        expect(t.update(1_000_000_010L, connection, world, file, team, true, COUNT) != null && samples == 6, "New server sync");
        t.clear();
        expect(t.update(1_000_000_011L, connection, world, file, team, true, COUNT) != null && samples == 7, "HUD re-enable refresh");
        value = new ProgressTracker.Snapshot(0, 0);
        expect(t.update(2_000_000_011L, connection, world, file, team, true, COUNT).fraction() == 0.0, "Empty quest file");
        value = new ProgressTracker.Snapshot(4790, 4790);
        expect(t.update(3_000_000_011L, connection, world, file, team, true, COUNT).fraction() == 1.0, "100 percent");
        value = new ProgressTracker.Snapshot(1, 4791);
        expect(t.update(4_000_000_011L, connection, world, file, team, true, COUNT).equals(value), "Reset and definition edits are rescanned");
        expect(t.update(4_000_000_012L, null, null, file, team, true, COUNT) == null, "Disconnected tick clears display");
        expect(t.update(4_000_000_013L, new Object(), new Object(), file, team, true, COUNT) == null, "Tick fallback fences old file");

        // Subtraction remains valid when the monotonic clock wraps.
        t = new ProgressTracker(); file = new Object();
        t.update(Long.MAX_VALUE - 500_000_000L, connection, world, file, team, true, COUNT);
        int before = samples;
        t.update(Long.MIN_VALUE + 499_999_999L, connection, world, file, team, true, COUNT);
        expect(samples == before + 1, "Monotonic clock wrap");
        checkCounts(4790, 508);
        checkCounts(4736, 123);
        checkCounts(0, 0);
        checkCounts(20000, 20000);
        System.out.println("Progress checks passed: one-second cadence, sync loss, placeholder, world/team/file changes, disconnect, hide/re-enable, resets, empty/full counts and clock wrap.");
        if (args.length == 2) {
            audit(Path.of(args[0]), 4790);
            audit(Path.of(args[1]), 4736);
        }
    }

    private static void checkCounts(int total, int completed) {
        ProgressTracker.Counter c = new ProgressTracker.Counter();
        for (int i = 0; i < total; i++) c.accept(i < completed);
        expect(c.snapshot().equals(new ProgressTracker.Snapshot(completed, total)), "Every registered quest counted once");
    }

    private static void audit(Path path, int expected) throws Exception {
        List<String> lines = Files.readAllLines(path);
        long[] ids = lines.stream().mapToLong(s -> Long.parseUnsignedLong(s, 16)).toArray();
        Set<Long> completed = new HashSet<>();
        for (int i = 0; i < ids.length; i += 10) completed.add(ids[i]);
        expect(ids.length == expected && Arrays.stream(ids).distinct().count() == expected, "Audited IDs are unique");
        expect(scan(ids, completed).equals(new ProgressTracker.Snapshot(completed.size(), expected)), "Real pack ID traversal");
        for (int i = 0; i < 500; i++) scan(ids, completed);
        long[] times = new long[2000];
        int checksum = 0;
        for (int i = 0; i < times.length; i++) {
            long start = System.nanoTime();
            checksum += scan(ids, completed).completed();
            times[i] = System.nanoTime() - start;
        }
        Arrays.sort(times);
        System.out.printf(java.util.Locale.ROOT,
                "%s: %d IDs, 2000 scans after 500 warmups, median %.3f ms, p95 %.3f ms, max %.3f ms; checksum %d%n",
                path.getFileName(), ids.length, times[1000] / 1e6, times[1900] / 1e6, times[1999] / 1e6, checksum);
    }

    private static ProgressTracker.Snapshot scan(long[] ids, Set<Long> completed) {
        ProgressTracker.Counter counter = new ProgressTracker.Counter();
        for (long id : ids) counter.accept(completed.contains(id));
        return counter.snapshot();
    }

    private static void expect(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
