package xyz.ressor.source.s3;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3ObjectId;

public class S3SourceBuilder {
    private final String bucket;
    private final String key;
    private AWSCredentialsProvider credentialsProvider;
    private ClientConfiguration clientConfiguration;
    private Regions region;
    private String versionId;

    public S3SourceBuilder(String bucket, String key) {
        this.bucket = bucket;
        this.key = key;
    }

    public S3SourceBuilder credentialsProvider(AWSCredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return this;
    }

    public S3SourceBuilder credentials(AWSCredentials credentials) {
        this.credentialsProvider = new AWSStaticCredentialsProvider(credentials);
        return this;
    }

    public S3SourceBuilder credentials(String accessKey, String secretKey) {
        this.credentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
        return this;
    }

    public S3SourceBuilder region(Regions region) {
        this.region = region;
        return this;
    }

    public S3SourceBuilder region(String region) {
        this.region = Regions.fromName(region.toLowerCase());
        return this;
    }

    public S3SourceBuilder clientConfiguration(ClientConfiguration clientConfiguration) {
        this.clientConfiguration = clientConfiguration;
        return this;
    }

    public S3SourceBuilder versionId(String versionId) {
        this.versionId = versionId;
        return this;
    }

    public S3Source build() {
        var builder = AmazonS3ClientBuilder.standard();
        var objectId = new S3ObjectId(bucket, key, versionId);
        if (credentialsProvider != null) {
            builder.withCredentials(credentialsProvider);
        }
        if (clientConfiguration != null) {
            builder.withClientConfiguration(clientConfiguration);
        }
        if (region != null) {
            builder.withRegion(region);
        }
        return new S3Source(builder.build(), objectId);
    }

}
