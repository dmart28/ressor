package xyz.ressor.source.s3;

import com.amazonaws.services.s3.model.S3ObjectId;
import xyz.ressor.source.ResourceId;

public class S3ResourceId implements ResourceId {
    private final S3ObjectId objectId;

    public S3ResourceId(String bucket, String key) {
        this(bucket, key, null);
    }

    public S3ResourceId(String bucket, String key, String version) {
        this.objectId = new S3ObjectId(bucket, key, version);
    }

    public S3ObjectId getObjectId() {
        return objectId;
    }

    @Override
    public String describe() {
        return objectId.toString();
    }

    @Override
    public Class<?> sourceType() {
        return S3Source.class;
    }
}
