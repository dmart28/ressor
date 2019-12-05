package xyz.ressor.source;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.ressor.commons.watch.fs.FileSystemWatchService;
import xyz.ressor.source.fs.FileSystemSource;

import java.io.InputStream;
import java.net.URL;
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
    public void testClasspathSourceUsage() throws Exception {
        FileSystemSource source = new FileSystemSource("classpath:fs/user_data.json");
        assertThat(source.isClasspath()).isEqualTo(true);
        assertThat(source.isListenable()).isEqualTo(false);

        LoadedResource resource = source.load();

        assertThat(resource).isNotNull();
        assertThat((long) resource.getVersion().val()).isGreaterThan(0);
        assertThat(resource.getInputStream()).isNotNull();
        assertThat(resource.getResourceId()).isEqualTo("classpath:fs/user_data.json");

        URL originalURI = getClass().getClassLoader().getResource("fs/user_data.json");
        assertThat(originalURI).isNotNull();
        assertThat(IOUtils.toString(resource.getInputStream(), UTF_8))
                .isEqualTo(IOUtils.toString(originalURI, UTF_8));

        LoadedResource conditionalResource = source.loadIfModified(resource.getVersion());

        assertThat(conditionalResource).isNull();

        assertThat(source.load()).isNotNull();
        assertThat(source.load()).isEqualTo(resource);
    }

    @Test
    public void testFileSourceUsage(@TempDir Path dir) throws Exception {
        Path filePath = dir.resolve("user_data.json");
        copy(stream("fs/user_data.json"), filePath);

        FileSystemSource source = new FileSystemSource(filePath);
        assertThat(source.isClasspath()).isEqualTo(false);
        assertThat(source.isListenable()).isEqualTo(false);

        LoadedResource resource = source.load();

        assertThat(resource).isNotNull();
        assertThat((long) resource.getVersion().val()).isGreaterThan(0);
        assertThat(resource.getInputStream()).isNotNull();
        assertThat(resource.getResourceId()).isEqualTo(filePath.toString());

        assertThat(IOUtils.toString(resource.getInputStream(), UTF_8))
                .isEqualTo(IOUtils.toString(stream("fs/user_data.json"), UTF_8));

        assertThat(source.loadIfModified(resource.getVersion())).isNull();

        Thread.sleep(1000);
        copy(stream("fs/new_user_data.json"), filePath, StandardCopyOption.REPLACE_EXISTING);

        assertThat(source.loadIfModified(resource.getVersion())).isNotNull();

        LoadedResource newResource = source.load();

        assertThat(newResource).isNotNull();
        assertThat(newResource).isNotEqualTo(resource);
        assertThat((long) newResource.getVersion().val()).isGreaterThan((long) resource.getVersion().val());
        assertThat(newResource.getInputStream()).isNotNull();
        assertThat(newResource.getResourceId()).isEqualTo(filePath.toString());

        assertThat(IOUtils.toString(newResource.getInputStream(), UTF_8))
                .isEqualTo(IOUtils.toString(stream("fs/new_user_data.json"), UTF_8));

        assertThat(source.loadIfModified(newResource.getVersion())).isNull();
    }

    @Test
    public void testFileSourceWatchService(@TempDir Path dir,
                                           @Mock FileSystemWatchService watchService) throws Exception {
        Path filePath = dir.resolve("user_data.json");
        copy(stream("fs/user_data.json"), filePath);

        FileSystemSource source = new FileSystemSource(filePath, watchService);
        assertThat(source.isClasspath()).isEqualTo(false);
        assertThat(source.isListenable()).isEqualTo(true);

        Subscription subscription = source.subscribe(() -> {});
        source.subscribe(() -> {});

        verify(watchService, times(2)).registerJob(any(), any());

        subscription.unsubscribe();
    }

    private InputStream stream(String fileName) {
        return getClass().getClassLoader().getResourceAsStream(fileName);
    }

}
