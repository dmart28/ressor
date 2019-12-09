package xyz.ressor.source.fs;

import xyz.ressor.source.ResourceId;

import java.nio.file.Path;

public class FileSystemResourceId implements ResourceId {
    private static final String CLASSPATH_PREFIX = "classpath:";
    private final String rawResourcePath;
    private final Path resourcePath;
    private final boolean isClasspath;

    public FileSystemResourceId(Path resourcePath) {
        this(resourcePath.toString());
    }

    public FileSystemResourceId(String resourcePath) {
        this.isClasspath = isClasspath(resourcePath);
        this.resourcePath = isClasspath ? null : Path.of(resourcePath);
        this.rawResourcePath = resourcePath.replaceFirst(CLASSPATH_PREFIX, "");
    }

    public String getRawResourcePath() {
        return rawResourcePath;
    }

    public Path getResourcePath() {
        return resourcePath;
    }

    public boolean isClasspath() {
        return isClasspath;
    }

    private static boolean isClasspath(String resourcePath) {
        return resourcePath.startsWith(CLASSPATH_PREFIX);
    }

    @Override
    public String describe() {
        return rawResourcePath;
    }

    @Override
    public Class<?> sourceType() {
        return FileSystemSource.class;
    }
}
