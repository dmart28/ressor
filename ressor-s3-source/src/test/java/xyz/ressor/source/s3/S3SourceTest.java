package xyz.ressor.source.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.ressor.source.LoadedResource;
import xyz.ressor.source.s3.version.S3Version;
import xyz.ressor.source.s3.version.VersionType;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class S3SourceTest {
    private static final S3ResourceId resourceId = new S3ResourceId("bucket", "key");
    @Mock
    private AmazonS3 client;

    @Test
    public void testSourceDefaultRequestBehavior() {
        S3Source source = new S3Source(client);

        LoadedResource result = source.load(resourceId);
        assertThat(result).isNull();
        verify(client).getObject(new GetObjectRequest(resourceId.getObjectId()));
        reset(client);

        S3Version version = new S3Version("123", VersionType.ETAG);
        result = source.loadIfModified(resourceId, version);
        assertThat(result).isNull();
        GetObjectRequest request = new GetObjectRequest(resourceId.getObjectId());
        request.setNonmatchingETagConstraints(Collections.singletonList(version.val()));
        verify(client).getObject(request);
        reset(client);

        version = new S3Version(new Date(), VersionType.LAST_MODIFIED);
        result = source.loadIfModified(resourceId, version);
        assertThat(result).isNull();
        request = new GetObjectRequest(resourceId.getObjectId());
        request.setModifiedSinceConstraint(version.val());
        verify(client).getObject(request);
        reset(client);
    }

    @Test
    public void testSourceDefaultResponseBehavior() {
        S3ObjectId objectId = new S3ObjectId("bucket", "key");
        S3Source source = new S3Source(client);

        S3ObjectInputStream stream = mock(S3ObjectInputStream.class);
        S3Object response = mock(S3Object.class);
        ObjectMetadata metadata = mock(ObjectMetadata.class);

        when(client.getObject(any())).thenReturn(response);
        when(response.getObjectMetadata()).thenReturn(metadata);
        when(response.getObjectMetadata().getETag()).thenReturn("123");
        when(response.getObjectContent()).thenReturn(stream);

        LoadedResource resource = source.load(resourceId);

        assertThat(resource).isNotNull();
        assertThat(resource.getVersion()).isEqualTo(new S3Version("123", VersionType.ETAG));
        assertThat(resource.getInputStream()).isEqualTo(stream);

        verify(client).getObject(any());

        reset(metadata);
        Date date = new Date();
        when(metadata.getLastModified()).thenReturn(date);
        when(metadata.getETag()).thenReturn(null);

        resource = source.load(resourceId);

        assertThat(resource).isNotNull();
        assertThat(resource.getVersion()).isEqualTo(new S3Version(date, VersionType.LAST_MODIFIED));
        assertThat(resource.getInputStream()).isEqualTo(stream);
    }
}