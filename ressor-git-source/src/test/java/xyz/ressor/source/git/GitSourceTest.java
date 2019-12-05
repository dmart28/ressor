package xyz.ressor.source.git;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import xyz.ressor.source.Source;
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
        GitSource source = new GitSource(git, EMPTY_TRANSPORT_CONFIG, "data.txt", new GitRef("master"), false);

        assertThat(toString(source)).isEqualTo("master data");

        source = new GitSource(git, EMPTY_TRANSPORT_CONFIG, "data.txt", new GitRef("develop"), false);
        assertThat(toString(source)).isEqualTo("develop data");

        source = new GitSource(git, EMPTY_TRANSPORT_CONFIG, "nodata.txt", new GitRef("develop"), false);
        assertThat(source.load()).isNull();

        assertThrows(IllegalArgumentException.class, () -> new GitSource(git, EMPTY_TRANSPORT_CONFIG, "data.txt", new GitRef("nobranch"), false));
    }

    @Test
    public void testPredefinedRepository() throws Exception {
        Source source = local().filePath("data.txt").build();
        assertThat(source).isNotNull();
        assertThat(toString(source)).isEqualTo("One");

        source = local().refValue("master", TAG).filePath("data.txt").build();
        assertThat(toString(source)).isEqualTo("Three");

        source = local().refValue("develop").filePath("data.txt").build();
        assertThat(source.load()).isNull();

        source = local().refValue("develop").filePath("nodata.txt").build();
        assertThat(source.load()).isNotNull();

        source = local().refValue("develop", TAG).filePath("data.txt").build();
        assertThat(toString(source)).isEqualTo("Four");

        source = local().refValue("refs/tags/develop").filePath("data.txt").build();
        assertThat(toString(source)).isEqualTo("Four");

        source = local().refValue("refs/heads/master").filePath("data.txt").build();
        assertThat(toString(source)).isEqualTo("One");

        source = local().refValue("refs/tags/master").filePath("data.txt").build();
        assertThat(toString(source)).isEqualTo("Three");

        source = local().refValue("954a68210f524228ed29a85e7b8574dd1577bf40").filePath("data.txt").build();
        assertThat(toString(source)).isEqualTo("Five");

        source = local().refValue("tag-2").filePath("data.txt").build();
        assertThat(toString(source)).isEqualTo("Two");

        source = local().refValue("refs/tags/tag-2").filePath("data.txt").build();
        assertThat(toString(source, 1570108694000L)).isEqualTo("Two");
        assertThat(source.loadIfModified(new LastModified(1570108695000L))).isNull();

        assertThrows(IllegalArgumentException.class, () -> local().refValue("tag-3").filePath("data.txt").build());
    }

    private LocalRepositoryBuilder local() throws URISyntaxException {
        String repositoryDirectory = classpath("repository/test/HEAD").getParent().toFile().getAbsolutePath();
        return GitRepository.local().repositoryDirectory(repositoryDirectory);
    }

    private String toString(Source source) throws IOException {
        return IOUtils.toString(source.load().getInputStream(), UTF_8);
    }

    private String toString(Source source, long ifModifiedSince) throws IOException {
        return IOUtils.toString(source.loadIfModified(new LastModified(ifModifiedSince)).getInputStream(), UTF_8);
    }

    private Path classpath(String name) throws URISyntaxException {
        return FileSystems.getDefault().provider().getPath(getClass().getClassLoader().getResource(name).toURI());
    }

}
