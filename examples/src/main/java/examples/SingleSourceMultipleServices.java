package examples;

import xyz.ressor.Ressor;
import xyz.ressor.source.http.Http;
import xyz.ressor.source.http.HttpSource;

import java.util.function.Function;

public class SingleSourceMultipleServices {

    public static void main(String[] args) {
        var ressor = Ressor.create();
        var httpSource = Http.builder()
                .socketTimeoutMs(12000)
                .connectTimeoutMs(12000)
                .receiveBufferSize(1024 * 1024)
                .keepAlive(true)
                .pool(100, 60 * 1000)
                .build();

        var serviceOne = createService(ressor, httpSource, "serviceOne.txt");
        var serviceTwo = createService(ressor, httpSource, "serviceTwo.txt");

        System.out.println(serviceOne);
        System.out.println(serviceTwo);

        ressor.shutdown();
    }

    private static CharSequence createService(Ressor ressor, HttpSource httpSource, String fileName) {
        return ressor.service(CharSequence.class)
                .source(httpSource)
                .resource(Http.url("http://ressor-examples.s3-website.us-east-2.amazonaws.com/" + fileName))
                .string()
                .factory(Function.identity())
                .build();
    }

}
