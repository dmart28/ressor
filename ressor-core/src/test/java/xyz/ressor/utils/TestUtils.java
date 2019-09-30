package xyz.ressor.utils;

import xyz.ressor.source.LoadedResource;
import xyz.ressor.source.fs.FileSystemSource;

public class TestUtils {

    public static RuntimeException illegalConstructor() {
        throw new RuntimeException("Illegal constructor");
    }

    public static LoadedResource load(String path) {
        return new FileSystemSource(path).load();
    }

}
