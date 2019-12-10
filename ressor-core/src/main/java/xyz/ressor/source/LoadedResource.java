package xyz.ressor.source;

import java.io.InputStream;
import java.util.Objects;

public class LoadedResource {
    /**
     * The InputStream of the read resource data
     */
    private final InputStream inputStream;
    /**
     * The current version stamp of the given entity value
     * For the file it's read from the FS metadata, in HTTP from Last-Modified/ETag headers, etc.
     */
    private final SourceVersion version;
    /**
     * The unique string that would uniquely identify the resource in the best way.
     * FS path in case of file, URL in case of HTTP resource, etc.
     */
    private final ResourceId resourceId;

    public LoadedResource(InputStream inputStream, SourceVersion version, ResourceId resourceId) {
        this.inputStream = inputStream;
        this.version = version;
        this.resourceId = resourceId;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public SourceVersion getVersion() {
        return version;
    }

    public ResourceId getResourceId() {
        return resourceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoadedResource that = (LoadedResource) o;
        return  Objects.equals(version, that.version) &&
                Objects.equals(resourceId, that.resourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, resourceId);
    }
}
