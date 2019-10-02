package xyz.ressor.source.git.builder;

import org.eclipse.jgit.api.Git;
import xyz.ressor.commons.utils.Exceptions;
import xyz.ressor.source.git.GitSource;

import java.io.File;

public class LocalRepositoryBuilder extends RepositoryBuilderBase<LocalRepositoryBuilder> {

    public GitSource build() {
        if (repositoryDirectory == null) {
            throw new IllegalArgumentException("Git repository directory can't be empty");
        }
        if (filePath == null) {
            throw new IllegalArgumentException("No target file path is provided for this repository");
        }
        try {
            return new GitSource(Git.open(new File(repositoryDirectory)), createTransportConfig(),
                    filePath, branch, asyncPull);
        } catch (Throwable t) {
            throw Exceptions.wrap(t);
        }
    }

}
