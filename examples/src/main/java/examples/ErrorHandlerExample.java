package examples;

import xyz.ressor.Ressor;
import xyz.ressor.service.error.SimpleErrorHandler;
import xyz.ressor.source.http.Http;

import java.util.function.Function;

import static xyz.ressor.translator.Translators.string;

public class ErrorHandlerExample {

    public static void main(String[] args) {
        var ressor = Ressor.create();

        var serviceValue = ressor.service(CharSequence.class)
                .translator(string())
                .source(Http.builder().connectTimeoutMs(100).socketTimeoutMs(100).build())
                .errorHandler(new SimpleErrorHandler(t -> System.out.println(t.getMessage())))
                .resource(Http.url("http://never-existing-host.neh.xyz/noData.txt"))
                .factory(Function.identity())
                .initialInstance("stub")
                .build();

        System.out.println(serviceValue);

        ressor.shutdown();
    }

}
