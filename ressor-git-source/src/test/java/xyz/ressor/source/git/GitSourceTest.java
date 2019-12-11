package xyz.ressor.source.git;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import xyz.ressor.source.git.builder.LocalRepositoryBuilder;
import xyz.ressor.source.version.LastModified;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static xyz.ressor.source.git.GitRepository.path;
import static xyz.ressor.source.git.GitSource.EMPTY_TRANSPORT_CONFIG;
import static xyz.ressor.source.git.RefType.TAG;

public class GitSourceTest {
    private Git git;

    @BeforeEach
    private void setUp(@TempDir File tempDir) throws Exception {
        this.git = Git.init().setBare(false).setDirectory(tempDir).call();
        Files.copy(classpath("git/master-branch.txt"), tempDir.toPath().resolve("data.txt"));
        this.git.add().addFilepattern(".").call();
        this.git.commit().setMessage("add data").call();

        this.git.branchCreate().setName("develop").call();
        this.git.checkout().setName("develop").call();
        Files.copy(classpath("git/develop-branch.txt"), tempDir.toPath().resolve("data.txt"), StandardCopyOption.REPLACE_EXISTING);
        this.git.add().addFilepattern(".").call();
        this.git.commit().setMessage("add another data").call();
    }

    @Test
    public void test() throws Exception {
        final GitSource source = new GitSource(git, EMPTY_TRANSPORT_CONFIG, false);

        assertThat(toString(source, path("data.txt"))).isEqualTo("master data");
        assertThat(toString(source, path("data.txt", "develop"))).isEqualTo("develop data");
        assertThat(source.load(path("nodata.txt", "develop"))).isNull();

        assertThrows(IllegalArgumentException.class, () -> source.load(path("data.txt", "nobranch")));
    }

    @Test
    public void testPredefinedRepository() throws Exception {
        final GitSource source = local().build();
        assertThat(source).isNotNull();

        assertThat(toString(source, path("data.txt"))).isEqualTo("One");
        assertThat(toString(source, path("data.txt", "master", TAG))).isEqualTo("Three");
        assertThat(source.load(path("data.txt", "develop"))).isNull();
        assertThat(source.load(path("nodata.txt", "develop"))).isNotNull();
        assertThat(toString(source, path("data.txt", "develop", TAG))).isEqualTo("Four");
        assertThat(toString(source, path("data.txt", "refs/tags/develop"))).isEqualTo("Four");
        assertThat(toString(source, path("data.txt", "refs/heads/master"))).isEqualTo("One");
        assertThat(toString(source, path("data.txt", "refs/tags/master"))).isEqualTo("Three");
        assertThat(toString(source, path("data.txt", "954a68210f524228ed29a85e7b8574dd1577bf40"))).isEqualTo("Five");
        assertThat(toString(source, path("data.txt", "tag-2"))).isEqualTo("Two");
        assertThat(toString(source, path("data.txt", "refs/tags/tag-2"), 1570108694000L)).isEqualTo("Two");
        assertThat(source.loadIfModified(path("data.txt", "refs/tags/tag-2"), new LastModified(1570108695000L))).isNull();

        assertThrows(IllegalArgumentException.class, () -> source.load(path("data.txt", "tag-3")));
    }

    private LocalRepositoryBuilder local() throws URISyntaxException {
        String repositoryDirectory = classpath("repository/test/HEAD").getParent().toFile().getAbsolutePath();
        return GitRepository.local().repositoryDirectory(repositoryDirectory);
    }

    private String toString(GitSource source, GitResourceId id) throws IOException {
        return IOUtils.toString(source.load(id).getInputStream(), UTF_8);
    }

    private String toString(GitSource source, GitResourceId id, long ifModifiedSince) throws IOException {
        return IOUtils.toString(source.loadIfModified(id, new LastModified(ifModifiedSince)).getInputStream(), UTF_8);
    }

    private Path classpath(String name) throws URISyntaxException {
        return FileSystems.getDefault().provider().getPath(getClass().getClassLoader().getResource(name).toURI());
    }

}
