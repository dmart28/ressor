package xyz.ressor.source.git;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static xyz.ressor.source.git.GitSource.EMPTY_TRANSPORT_CONFIG;

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
        var source = new GitSource(git, EMPTY_TRANSPORT_CONFIG, "data.txt", "master", false);

        var resource = source.load();
        assertThat(IOUtils.toString(resource.getInputStream(), UTF_8)).isEqualTo("master data");

        source = new GitSource(git, EMPTY_TRANSPORT_CONFIG, "data.txt", "develop", false);
        resource = source.load();
        assertThat(IOUtils.toString(resource.getInputStream(), UTF_8)).isEqualTo("develop data");

        source = new GitSource(git, EMPTY_TRANSPORT_CONFIG, "nodata.txt", "develop", false);
        resource = source.load();
        assertThat(resource).isNull();

        assertThrows(IllegalArgumentException.class, () -> new GitSource(git, EMPTY_TRANSPORT_CONFIG, "data.txt", "nobranch", false));
    }

    private Path classpath(String name) throws URISyntaxException {
        return Path.of(getClass().getClassLoader().getResource(name).toURI());
    }

}
