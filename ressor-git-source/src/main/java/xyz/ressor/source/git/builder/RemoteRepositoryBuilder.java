package xyz.ressor.source.git.builder;

import org.eclipse.jgit.api.Git;
import xyz.ressor.commons.utils.Exceptions;
import xyz.ressor.source.git.GitSource;

import java.io.File;
import java.nio.file.Files;

public class RemoteRepositoryBuilder extends RepositoryBuilderBase<RemoteRepositoryBuilder> {
    protected String repositoryURI;
    protected boolean bare = true;

    public RemoteRepositoryBuilder repositoryURI(String repositoryURI) {
        this.repositoryURI = repositoryURI;
        return this;
    }

    public RemoteRepositoryBuilder bare(boolean bare) {
        this.bare = bare;
        return this;
    }

    public GitSource build() {
        if (repositoryURI == null) {
            throw new IllegalArgumentException("Repository URI can't be empty");
        }
        try {
            if (repositoryDirectory == null) {
                repositoryDirectory = Files.createTempDirectory("git_source").toFile().getAbsolutePath();
            }
            var transportConfig = createTransportConfig();
            return new GitSource(Git.cloneRepository()
                    .setURI(repositoryURI)
                    .setBare(bare)
                    .setDirectory(new File(repositoryDirectory))
                    .setTransportConfigCallback(transportConfig).call(), transportConfig, asyncPull);
        } catch (Throwable t) {
            throw Exceptions.wrap(t);
        }
    }

}
