package io.github.keisuke111.questprogresshud;

import java.util.function.Supplier;

/** Client-thread sampling state. Reference identities distinguish worlds, quest files and teams. */
final class ProgressTracker {
    private static final long INTERVAL_NANOS = 1_000_000_000L;
    record Snapshot(int completed, int total) {
        double fraction() { return total == 0 ? 0.0 : (double) completed / total; }
    }

    private Object connection, world, file, team, disconnectedFile;
    private long lastSample;
    private Snapshot snapshot;

    Snapshot update(long now, Object connection, Object world, Object file, Object team,
                    boolean ready, Supplier<Snapshot> count) {
        if (connection == null || world == null) {
            disconnect(file);
            return null;
        }
        if (!ready || file == null || team == null || file == disconnectedFile) {
            clear();
            return null;
        }
        if (snapshot == null || this.connection != connection || this.world != world
                || this.file != file || this.team != team || now - lastSample >= INTERVAL_NANOS) {
            snapshot = count.get();
            this.connection = connection;
            this.world = world;
            this.file = file;
            this.team = team;
            lastSample = now;
        }
        return snapshot;
    }

    void disconnect(Object currentFile) {
        // FTB can retain its old client file until the next server's quest sync arrives.
        if (currentFile != null) disconnectedFile = currentFile;
        else if (file != null) disconnectedFile = file;
        clear();
    }

    void clear() {
        connection = world = file = team = null;
        snapshot = null;
    }

    static final class Counter {
        private int completed, total;
        void accept(boolean complete) {
            total++;
            if (complete) completed++;
        }
        Snapshot snapshot() { return new Snapshot(completed, total); }
    }
}
