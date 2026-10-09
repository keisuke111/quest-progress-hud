package io.github.keisuke111.questprogresshud;

import java.util.Locale;

/** Empty ID means the unchanged all-quests scope; chapter IDs are stable unsigned hex IDs. */
final class QuestScope {
    static boolean valid(Object value) {
        return value instanceof String id && (id.isEmpty()
                || id.matches("[0-9a-fA-F]{16}") && !id.equals("0000000000000000"));
    }

    static String normalize(String id) {
        return valid(id) ? id.toUpperCase(Locale.ROOT) : "";
    }

    static long id(String selectedId) {
        return selectedId.isEmpty() ? 0 : Long.parseUnsignedLong(selectedId, 16);
    }

    static boolean includes(long selectedId, long chapterId) {
        return selectedId == 0 || selectedId == chapterId;
    }
}
