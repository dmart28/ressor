package examples;

import xyz.ressor.Ressor;
import xyz.ressor.source.http.Http;

import static java.util.function.Function.identity;
import static xyz.ressor.translator.Translators.string;

public class DynamicStringExample {

    public static void main(String[] args) {
        var ressor = Ressor.create();

        var string = ressor.service(CharSequence.class)
                .source(Http.source())
                .resource(Http.url("http://ressor-examples.s3-website.us-east-2.amazonaws.com/fruit.json"))
                .translator(string())
                .factory(identity())
                .build();

        System.out.println(string);

        ressor.shutdown();
    }

}
