package io.github.keisuke111.questprogresshud;

import java.util.List;

public final class QuestScopeTest {
    private record Quest(long chapter, boolean complete) {}

    public static void main(String[] args) {
        String a = "0123456789ABCDEF", b = "FFFFFFFFFFFFFFFF";
        expect(QuestScope.valid("") && QuestScope.id("") == 0, "Default all-quests selection");
        expect(QuestScope.normalize(a.toLowerCase(java.util.Locale.ROOT)).equals(a), "Stable case-insensitive ID");
        expect(QuestScope.id(b) == -1L, "Unsigned 64-bit IDs supported");
        for (Object invalid : new Object[]{1, "none", "not-an-id", "0000000000000000", "FFFFFFFFFFFFFFFFF"}) {
            expect(!QuestScope.valid(invalid), "Reject malformed IDs and reserved zero ID");
        }
        List<Quest> quests = List.of(new Quest(QuestScope.id(a), true), new Quest(QuestScope.id(a), false),
                new Quest(QuestScope.id(b), true), new Quest(QuestScope.id(b), true));
        expect(count("", quests).equals(new ProgressTracker.Snapshot(3, 4)), "All quests keeps every registered quest");
        expect(count(a, quests).equals(new ProgressTracker.Snapshot(1, 2)), "One scope for numerator and denominator");
        expect(count(b, quests).fraction() == 1.0, "Other chapter complete");
        expect(count("0000000000000001", quests).equals(new ProgressTracker.Snapshot(0, 0)), "Empty chapter gives zero percent");
        // Titles and display order never participate in membership.
        expect(count(a, quests.reversed()).equals(count(a, quests)), "Reordering/duplicate titles cannot retarget saved IDs");
        ProgressTracker tracker = new ProgressTracker();
        Object session = new Object();
        tracker.update(0, session, session, session, session, true, () -> count("", quests));
        tracker.clear(); // Production clears the sampling cache when the configured scope changes.
        expect(tracker.update(1, session, session, session, session, true, () -> count(a, quests))
                .equals(new ProgressTracker.Snapshot(1, 2)), "Selection refreshes immediately, not after the old interval");
        System.out.println("Chapter scope checks passed: default/all, stable unsigned IDs, identical membership, empty/full chapters and immediate invalidation.");
    }

    private static ProgressTracker.Snapshot count(String selected, List<Quest> quests) {
        ProgressTracker.Counter counter = new ProgressTracker.Counter();
        for (Quest quest : quests) {
            if (QuestScope.includes(QuestScope.id(selected), quest.chapter())) counter.accept(quest.complete());
        }
        return counter.snapshot();
    }
    private static void expect(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
