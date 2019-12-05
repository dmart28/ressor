package xyz.ressor.commons.watch.fs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createFile;
import static java.util.Arrays.asList;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.IOUtils.writeLines;
import static org.awaitility.Awaitility.await;

public class FileSystemWatchServiceTest {

    @Test
    public void test(@TempDir Path tempDir1) throws Exception {
        Path tempDir2 = Files.createTempDirectory("");
        Path file1 = createFile(tempDir1.resolve("myfile.txt"));
        Path file2 = createFile(tempDir2.resolve("myfile.txt"));
        FileSystemWatchService ws = new FileSystemWatchService();

        try {
            ws.init();

            AtomicBoolean firstJobAlerted = new AtomicBoolean();
            AtomicBoolean secondJobAlerted = new AtomicBoolean();
            ws.registerJob(file1, p -> {
                if (p != null && p.equals(file1)) {
                    firstJobAlerted.set(true);
                }
            });
            ws.registerJob(file2, p -> {
                if (p != null && p.equals(file2)) {
                    secondJobAlerted.set(true);
                }
            });

            writeLinesToFile(file1);
            writeLinesToFile(file2);

            await().atMost(30, TimeUnit.SECONDS).until(() -> firstJobAlerted.get() && secondJobAlerted.get());
        } finally {
            deleteDirectory(file2.getParent().toFile());
            ws.destroy();
        }
    }

    private void writeLinesToFile(Path f) throws IOException {
        try (FileOutputStream os = new FileOutputStream(f.toFile())) {
            writeLines(asList("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt",
                    "labore et dolore magna aliqua. Ut enim ad minim veniam, quis",
                    "nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure",
                    " dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat",
                    " cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."), "\n", os, UTF_8);
        }
    }

}
