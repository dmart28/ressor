package xyz.ressor.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Nullable;
import xyz.ressor.RessorBuilder;
import xyz.ressor.commons.utils.Exceptions;
import xyz.ressor.service.RessorService;
import xyz.ressor.service.error.ErrorHandler;
import xyz.ressor.source.*;
import xyz.ressor.source.fs.FileSystemResourceId;
import xyz.ressor.source.fs.FileSystemSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class TestUtils {

    public static <T> RessorService<T> ressorService(Object service) {
        return (RessorService<T>) service;
    }

    public static LoadedResource string(String value) {
        return new LoadedResource(new ByteArrayInputStream(value.getBytes()), SourceVersion.EMPTY, null);
    }

    public static LoadedResource stringVersioned(String value) {
        return new LoadedResource(new ByteArrayInputStream(value.getBytes()), new SourceVersion() {
            @Override
            public <V> V val() {
                return (V) value;
            }
        }, null);
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

    public static <T, D> RessorBuilder<T, D> stringBuilderSource(StringBuilder sb, RessorBuilder<T, D> builder) {
        var source = stringBuilderSource(sb);
        var resource = matching(source);

        return builder.source(source).resource(resource);
    }

    public static Source stringBuilderSource(StringBuilder sb) {
        return new NonListenableSource() {
            @Override
            public String id() {
                return null;
            }

            @Override
            public LoadedResource loadIfModified(ResourceId resourceId, SourceVersion version) {
                return stringVersioned(sb.toString());
            }

            @Override
            public String describe() {
                return "";
            }
        };
    }

    public static <T, D> RessorBuilder<T, D> stubSource(RessorBuilder<T, D> builder) {
        var source = stubSource();
        var resource = matching(source);

        return builder.source(source).resource(resource);
    }

    public static Source stubSource() {
        return new NonListenableSource() {
            @Override
            public String id() {
                return null;
            }

            @Override
            public LoadedResource loadIfModified(ResourceId resourceId, SourceVersion version) {
                return any();
            }

            @Override
            public String describe() {
                return "";
            }
        };
    }

    public static ResourceId matching(Source source) {
        return source::getClass;
    }

    public static LoadedResource any() {
        return stringVersioned("");
    }

}
