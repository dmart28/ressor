package examples;

import xyz.ressor.Ressor;
import xyz.ressor.source.http.Http;

import java.util.function.Function;

import static xyz.ressor.translator.Translators.json;

public class HttpSourceExample {

    public static void main(String[] args) {
        var ressor = Ressor.create();

        var fruit = ressor.service(Fruit.class)
                .source(Http.builder().connectTimeoutMs(5000).socketTimeoutMs(5000).build())
                .resource(Http.url("http://ressor-examples.s3-website.us-east-2.amazonaws.com/fruit.json"))
                .translator(json(Fruit.class))
                .factory(Function.identity())
                .build();

        System.out.println(fruit);

        ressor.shutdown();
    }

    public static class Fruit {
        public String fruit;
        public String size;
        public String color;

        @Override
        public String toString() {
            return "Fruit{" +
                    "fruit='" + fruit + '\'' +
                    ", size='" + size + '\'' +
                    ", color='" + color + '\'' +
                    '}';
        }
    }

}
