package xyz.ressor.source.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class S3 {

    public static S3SourceBuilder builder(String bucket, String key) {
        return new S3SourceBuilder();
    }

    public static S3Source source() {
        return source(AmazonS3ClientBuilder.defaultClient());
    }

    public static S3Source source(AmazonS3 client) {
        return new S3Source(client);
    }

    public static S3ResourceId id(String bucket, String key) {
        return new S3ResourceId(bucket, key);
    }

    public static S3ResourceId id(String bucket, String key, String versionId) {
        return new S3ResourceId(bucket, key, versionId);
    }

}
