package xyz.ressor.source.version;

import xyz.ressor.source.SourceVersion;

import java.util.Objects;

public class LastModified implements SourceVersion {
    private final Long lastModifiedMillis;

    public LastModified(long lastModifiedMillis) {
        this.lastModifiedMillis = lastModifiedMillis;
    }

    @Override
    public <V> V val() {
        return (V) lastModifiedMillis;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LastModified that = (LastModified) o;
        return Objects.equals(lastModifiedMillis, that.lastModifiedMillis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastModifiedMillis);
    }
}
