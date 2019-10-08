package xyz.ressor.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import xyz.ressor.commons.utils.Exceptions;
import xyz.ressor.source.LoadedResource;
import xyz.ressor.source.SourceVersion;
import xyz.ressor.source.fs.FileSystemSource;

import java.io.IOException;

public class TestUtils {

    public static RuntimeException illegalConstructor() {
        throw new RuntimeException("Illegal constructor");
    }

    public static LoadedResource load(String path) {
        return load(path, false);
    }

    public static LoadedResource load(String path, boolean force) {
        var r = new FileSystemSource(path).load();
        return force ? new LoadedResource(r.getInputStream(), SourceVersion.EMPTY, r.getResourceId()) : r;
    }

    public static JsonNode json(String jsonValue) {
        try {
            return new ObjectMapper(new JsonFactory()).readTree(jsonValue);
        } catch (IOException e) {
            throw Exceptions.wrap(e);
        }
    }

}
