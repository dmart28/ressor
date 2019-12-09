package xyz.ressor.source;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.ressor.commons.watch.fs.FileSystemWatchService;
import xyz.ressor.source.fs.FileSystemResourceId;
import xyz.ressor.source.fs.FileSystemSource;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.copy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class FileSystemSourceTest {

    @Test
    public void testResourceId() {
        var id = new FileSystemResourceId("classpath:fs/user_data.json");
        assertThat(id.isClasspath()).isTrue();
        assertThat(id.getResourcePath()).isNull();
        assertThat(id.getRawResourcePath()).isEqualTo("fs/user_data.json");

        id = new FileSystemResourceId("/tmp/data.txt");
        assertThat(id.isClasspath()).isFalse();
        assertThat(id.getResourcePath()).isEqualTo(Path.of("/tmp/data.txt"));
        assertThat(id.getRawResourcePath()).isEqualTo("/tmp/data.txt");
    }

    @Test
    public void testClasspathSourceUsage() throws Exception {
        final var source = new FileSystemSource();
        assertThat(source.isListenable()).isEqualTo(false);

        var id = new FileSystemResourceId("classpath:fs/user_data.json");
        var resource = source.load(id);

        assertThat(resource).isNotNull();
        assertThat((long) resource.getVersion().val()).isGreaterThan(0);
        assertThat(resource.getInputStream()).isNotNull();
        assertThat(resource.getResourceId()).isEqualTo(id);

        var originalURI = getClass().getClassLoader().getResource("fs/user_data.json");
        assertThat(originalURI).isNotNull();
        assertThat(IOUtils.toString(resource.getInputStream(), UTF_8))
                .isEqualTo(IOUtils.toString(originalURI, UTF_8));

        var conditionalResource = source.loadIfModified(id, resource.getVersion());

        assertThat(conditionalResource).isNull();

        assertThat(source.load(id)).isNotNull();
        assertThat(source.load(id)).isEqualTo(resource);
    }

    @Test
    public void testFileSourceUsage(@TempDir Path dir) throws Exception {
        var filePath = dir.resolve("user_data.json");
        copy(stream("fs/user_data.json"), filePath);

        final var source = new FileSystemSource();
        var id = new FileSystemResourceId(filePath);
        assertThat(source.isListenable()).isEqualTo(false);

        var resource = source.load(id);

        assertThat(resource).isNotNull();
        assertThat((long) resource.getVersion().val()).isGreaterThan(0);
        assertThat(resource.getInputStream()).isNotNull();
        assertThat(resource.getResourceId()).isEqualTo(id);

        assertThat(IOUtils.toString(resource.getInputStream(), UTF_8))
                .isEqualTo(IOUtils.toString(stream("fs/user_data.json"), UTF_8));

        assertThat(source.loadIfModified(id, resource.getVersion())).isNull();

        Thread.sleep(1000);
        copy(stream("fs/new_user_data.json"), filePath, StandardCopyOption.REPLACE_EXISTING);

        assertThat(source.loadIfModified(id, resource.getVersion())).isNotNull();

        var newResource = source.load(id);

        assertThat(newResource).isNotNull();
        assertThat(newResource).isNotEqualTo(resource);
        assertThat((long) newResource.getVersion().val()).isGreaterThan((long) resource.getVersion().val());
        assertThat(newResource.getInputStream()).isNotNull();
        assertThat(newResource.getResourceId()).isEqualTo(id);

        assertThat(IOUtils.toString(newResource.getInputStream(), UTF_8))
                .isEqualTo(IOUtils.toString(stream("fs/new_user_data.json"), UTF_8));

        assertThat(source.loadIfModified(id, newResource.getVersion())).isNull();
    }

    @Test
    public void testFileSourceWatchService(@TempDir Path dir,
                                           @Mock FileSystemWatchService watchService) throws Exception {
        var filePath = dir.resolve("user_data.json");
        copy(stream("fs/user_data.json"), filePath);

        final var source = new FileSystemSource(watchService);
        var id = new FileSystemResourceId(filePath);
        assertThat(source.isListenable()).isEqualTo(true);

        var subscription = source.subscribe(id, () -> {});
        source.subscribe(id, () -> {});

        verify(watchService, times(2)).registerJob(any(), any());

        subscription.unsubscribe();
    }

    private InputStream stream(String fileName) {
        return getClass().getClassLoader().getResourceAsStream(fileName);
    }

}
