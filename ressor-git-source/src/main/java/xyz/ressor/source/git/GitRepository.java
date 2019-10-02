package xyz.ressor.source.git;

import xyz.ressor.source.git.builder.LocalRepositoryBuilder;
import xyz.ressor.source.git.builder.RemoteRepositoryBuilder;

public class GitRepository {

    public static LocalRepositoryBuilder local() {
        return new LocalRepositoryBuilder();
    }

    public static RemoteRepositoryBuilder remote() {
        return new RemoteRepositoryBuilder();
    }

}
