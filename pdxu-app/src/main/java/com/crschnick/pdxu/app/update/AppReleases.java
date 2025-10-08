package com.crschnick.pdxu.app.update;

import com.crschnick.pdxu.app.core.AppProperties;
import com.crschnick.pdxu.app.issue.ErrorEvent;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;

import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.RateLimitHandler;
import org.kohsuke.github.authorization.AuthorizationProvider;

import java.io.IOException;
import java.util.Optional;

public class AppReleases {

    private static final String GITHUB_REPOSITORY = "crschnick/pdx_unlimiter";

    private static GHRepository repository;

    @SuppressWarnings("deprecation")
    private static GHRepository getRepository() throws IOException {
        if (repository != null) {
            return repository;
        }

        var github = new GitHubBuilder()
                .withRateLimitHandler(RateLimitHandler.FAIL)
                .withAuthorizationProvider(AuthorizationProvider.ANONYMOUS)
                .build();
        repository = github.getRepository(GITHUB_REPOSITORY);
        return repository;
    }

    private static Optional<GHRelease> getMarkedLatestRelease() throws IOException {
        try {
            var repo = getRepository();
            return Optional.ofNullable(repo.getLatestRelease());
        } catch (IOException e) {
            throw ErrorEventFactory.expected(e);
        }
    }


    private static Optional<GHRelease> getTopReleaseIncludingPreRelease() throws IOException {
        var repo = getRepository();
        return Optional.ofNullable(repo.listReleases().iterator().next());
    }

    public static Optional<GHRelease> getLatestSuitableRelease() throws IOException {
        try {
            return getTopReleaseIncludingPreRelease();
        } catch (IOException e) {
            throw ErrorEventFactory.expected(e);
        }
    }

}
