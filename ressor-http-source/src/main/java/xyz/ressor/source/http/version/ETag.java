package xyz.ressor.source.http.version;

import org.jetbrains.annotations.Nullable;
import xyz.ressor.source.SourceVersion;

import java.util.Objects;

public class ETag implements SourceVersion {
    private final String eTag;

    public ETag(String eTag) {
        this.eTag = eTag;
    }

    @Override
    public <V> @Nullable V val() {
        return (V) eTag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ETag that = (ETag) o;
        return Objects.equals(eTag, that.eTag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eTag);
    }
}
