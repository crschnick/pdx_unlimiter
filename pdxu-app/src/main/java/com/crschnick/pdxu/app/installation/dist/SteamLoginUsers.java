package com.crschnick.pdxu.app.installation.dist;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.function.LongConsumer;
import java.util.regex.Pattern;

final class SteamLoginUsers {

    private static final Pattern USER_ID = Pattern.compile("^\\s*\"([0-9]+)\"\\s*$");
    private static final Pattern MOST_RECENT =
            Pattern.compile("^\\s*\"MostRecent\"\\s+\"1\"\\s*$", Pattern.CASE_INSENSITIVE);

    private SteamLoginUsers() {}

    static OptionalLong determineUserId(String contents, LongConsumer mostRecentUserSelected) {
        List<User> users = new ArrayList<>();
        long currentUserId = 0;
        boolean currentUserMostRecent = false;

        for (var line : contents.lines().toList()) {
            var userMatcher = USER_ID.matcher(line);
            if (userMatcher.matches()) {
                if (currentUserId != 0) {
                    users.add(new User(currentUserId, currentUserMostRecent));
                }

                try {
                    currentUserId = Long.parseLong(userMatcher.group(1));
                } catch (NumberFormatException ignored) {
                    currentUserId = 0;
                }
                currentUserMostRecent = false;
            } else if (currentUserId != 0 && MOST_RECENT.matcher(line).matches()) {
                currentUserMostRecent = true;
            }
        }

        if (currentUserId != 0) {
            users.add(new User(currentUserId, currentUserMostRecent));
        }

        if (users.size() == 1) {
            return OptionalLong.of(users.getFirst().id());
        }

        var mostRecent = users.stream().filter(User::mostRecent).toList();
        if (mostRecent.size() != 1) {
            return OptionalLong.empty();
        }

        long userId = mostRecent.getFirst().id();
        mostRecentUserSelected.accept(userId);
        return OptionalLong.of(userId);
    }

    private record User(long id, boolean mostRecent) {}
}
