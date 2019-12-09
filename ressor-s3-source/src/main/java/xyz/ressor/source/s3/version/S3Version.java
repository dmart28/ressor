package xyz.ressor.source.s3.version;

import org.jetbrains.annotations.Nullable;
import xyz.ressor.source.SourceVersion;

import java.util.Objects;

public class S3Version implements SourceVersion {
    private final Object val;
    private final VersionType type;

    public S3Version(Object val, VersionType type) {
        this.val = val;
        this.type = type;
    }

    public VersionType getType() {
        return type;
    }

    @Override
    public <V> @Nullable V val() {
        return (V) val;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        S3Version s3Version = (S3Version) o;
        return Objects.equals(val, s3Version.val) &&
                type == s3Version.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(val, type);
    }
}
