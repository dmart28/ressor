package xyz.ressor.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Nullable;
import xyz.ressor.commons.utils.Exceptions;
import xyz.ressor.service.error.ErrorHandler;
import xyz.ressor.source.LoadedResource;
import xyz.ressor.source.SourceVersion;
import xyz.ressor.source.fs.FileSystemResourceId;
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
        try {
            return new FileSystemSource().load(new FileSystemResourceId(path));
        } catch (Throwable t) {
            if (force) {
                throw t;
            } else {
                return null;
            }
        }
    }

    public static LoadedResource throwingResource() {
        return new LoadedResource(new ThrowingInputStream(), SourceVersion.EMPTY, null);
    }

    public static JsonNode json(String jsonValue) {
        try {
            return new ObjectMapper(new JsonFactory()).readTree(jsonValue);
        } catch (IOException e) {
            throw Exceptions.wrap(e);
        }
    }

    public static ErrorHandler simpleErrorHandler(Runnable failedReload, Runnable translateFailed) {
        return new ErrorHandler() {
            @Override
            public void onSourceFailed(Throwable exception, @Nullable Object service) {
                failedReload.run();
            }

            @Override
            public void onTranslateFailed(Throwable exception, LoadedResource resource, @Nullable Object service) {
                translateFailed.run();
            }
        };
    }

}
