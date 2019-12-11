package xyz.ressor.source.git;

import xyz.ressor.source.git.builder.LocalRepositoryBuilder;
import xyz.ressor.source.git.builder.RemoteRepositoryBuilder;

public class GitRepository {
    private static final String DEFAULT_BRANCH = "master";

    public static LocalRepositoryBuilder local() {
        return new LocalRepositoryBuilder();
    }

    public static RemoteRepositoryBuilder remote() {
        return new RemoteRepositoryBuilder();
    }

    public static GitResourceId path(String filePath) {
        return path(filePath, DEFAULT_BRANCH);
    }

    public static GitResourceId path(String filePath, String refName) {
        return path(filePath, refName, null);
    }

    public static GitResourceId path(String filePath, String refName, RefType refType) {
        if (filePath == null) {
            throw new IllegalArgumentException("No target file path is provided for this repository");
        }
        return refType != null ? new GitResourceId(filePath, refName, refType) : new GitResourceId(filePath, refName);
    }

}
