package xyz.ressor.source.http;

public class Http {

    public static HttpSourceBuilder source(String resourceURI) {
        return new HttpSourceBuilder(resourceURI);
    }

}
