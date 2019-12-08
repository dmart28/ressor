package xyz.ressor.source.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.ressor.source.s3.version.S3Version;
import xyz.ressor.source.s3.version.VersionType;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class S3SourceTest {
    @Mock
    private AmazonS3 client;

    @Test
    public void testSourceDefaultRequestBehavior() {
        var objectId = new S3ObjectId("bucket", "key");
        var source = new S3Source(client, objectId);

        var result = source.load();
        assertThat(result).isNull();
        verify(client).getObject(new GetObjectRequest(objectId));
        reset(client);

        var version = new S3Version("123", VersionType.ETAG);
        result = source.loadIfModified(version);
        assertThat(result).isNull();
        var request = new GetObjectRequest(objectId);
        request.setNonmatchingETagConstraints(Collections.singletonList(version.val()));
        verify(client).getObject(request);
        reset(client);

        version = new S3Version(new Date(), VersionType.LAST_MODIFIED);
        result = source.loadIfModified(version);
        assertThat(result).isNull();
        request = new GetObjectRequest(objectId);
        request.setModifiedSinceConstraint(version.val());
        verify(client).getObject(request);
        reset(client);
    }

    @Test
    public void testSourceDefaultResponseBehavior() {
        var objectId = new S3ObjectId("bucket", "key");
        var source = new S3Source(client, objectId);

        var stream = mock(S3ObjectInputStream.class);
        var response = mock(S3Object.class);
        var metadata = mock(ObjectMetadata.class);

        when(client.getObject(any())).thenReturn(response);
        when(response.getObjectMetadata()).thenReturn(metadata);
        when(response.getObjectMetadata().getETag()).thenReturn("123");
        when(response.getObjectContent()).thenReturn(stream);

        var resource = source.load();

        assertThat(resource).isNotNull();
        assertThat(resource.getVersion()).isEqualTo(new S3Version("123", VersionType.ETAG));
        assertThat(resource.getInputStream()).isEqualTo(stream);

        verify(client).getObject(any());

        reset(metadata);
        var date = new Date();
        when(metadata.getLastModified()).thenReturn(date);
        when(metadata.getETag()).thenReturn(null);

        resource = source.load();

        assertThat(resource).isNotNull();
        assertThat(resource.getVersion()).isEqualTo(new S3Version(date, VersionType.LAST_MODIFIED));
        assertThat(resource.getInputStream()).isEqualTo(stream);
    }

}
