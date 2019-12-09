package xyz.ressor.source.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ressor.source.LoadedResource;
import xyz.ressor.source.NonListenableSource;
import xyz.ressor.source.SourceVersion;
import xyz.ressor.source.s3.version.S3Version;
import xyz.ressor.source.s3.version.VersionType;

import java.util.Collections;
import java.util.Date;

import static com.amazonaws.util.StringUtils.isNullOrEmpty;
import static java.util.Objects.requireNonNull;
import static xyz.ressor.source.SourceVersion.EMPTY;

public class S3Source implements NonListenableSource {
    private static final Logger log = LoggerFactory.getLogger(S3Source.class);
    private final AmazonS3 client;
    private final S3ObjectId objectId;
    private final String resourceId;

    public S3Source(AmazonS3 client, S3ObjectId objectId) {
        this.client = requireNonNull(client, "Amazon S3 client is required");
        this.objectId = requireNonNull(objectId, "Amazon S3 object id is required");
        this.resourceId = objectId.toString();
    }

    @Override
    public LoadedResource loadIfModified(SourceVersion version) {
        GetObjectRequest request = new GetObjectRequest(objectId);
        if (!version.isEmpty()) {
            VersionType type = ((S3Version) version).getType();
            if (type == VersionType.ETAG) {
                String val = version.val();
                request.setNonmatchingETagConstraints(Collections.singletonList(val));
            } else if (type == VersionType.LAST_MODIFIED) {
                Date val = version.val();
                request.setModifiedSinceConstraint(val);
            }
        }
        S3Object response = client.getObject(request);
        if (response != null) {
            SourceVersion resultVersion;
            String eTag = response.getObjectMetadata().getETag();
            if (isNullOrEmpty(eTag)) {
                Date lastModified = response.getObjectMetadata().getLastModified();
                if (lastModified != null) {
                    log.debug("Loaded Last-Modified version for {}: {}", objectId, eTag);
                    resultVersion = new S3Version(lastModified, VersionType.LAST_MODIFIED);
                } else {
                    log.debug("No resource version was found for {}", objectId);
                    resultVersion = EMPTY;
                }
            } else {
                log.debug("Loaded ETag version for {}: {}", objectId, eTag);
                resultVersion = new S3Version(eTag, VersionType.ETAG);
            }
            return new LoadedResource(response.getObjectContent(), resultVersion, resourceId);
        } else {
            return null;
        }
    }

    @Override
    public String describe() {
        return "S3: [" + resourceId + "]";
    }
}
