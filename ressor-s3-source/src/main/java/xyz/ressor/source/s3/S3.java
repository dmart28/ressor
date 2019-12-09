package xyz.ressor.source.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3ObjectId;

public class S3 {

    public static S3SourceBuilder builder(String bucket, String key) {
        return new S3SourceBuilder(bucket, key);
    }

    public static S3Source source(String bucket, String key) {
        return source(AmazonS3ClientBuilder.defaultClient(), bucket, key);
    }

    public static S3Source source(String bucket, String key, String versionId) {
        return source(AmazonS3ClientBuilder.defaultClient(), bucket, key, versionId);
    }

    public static S3Source source(AmazonS3 client, String bucket, String key) {
        return new S3Source(client, new S3ObjectId(bucket, key));
    }

    public static S3Source source(AmazonS3 client, String bucket, String key, String versionId) {
        return new S3Source(client, new S3ObjectId(bucket, key, versionId));
    }

}
