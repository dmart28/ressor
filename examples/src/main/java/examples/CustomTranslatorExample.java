package examples;

import xyz.ressor.Ressor;
import xyz.ressor.commons.utils.Exceptions;
import xyz.ressor.source.http.Http;
import xyz.ressor.translator.Translator;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.function.Function;

public class CustomTranslatorExample {

    public static void main(String[] args) {
        var ressor = Ressor.create();

        var user = ressor.service(User.class)
                .source(Http.source())
                .resource(Http.url("http://ressor-examples.s3-website.us-east-2.amazonaws.com/user.ser"))
                .translator(new JavaSerializerTranslator<>(User.class))
                .build();

        System.out.println(user);

        ressor.shutdown();
    }


    /**
     * Translates binary data to entity instances using Java standard serialization.
     * Alternatively, you can use {@link Translator#define(Function, Class, Class)} for translator creation.
     */
    public static class JavaSerializerTranslator<T> implements Translator<InputStream, T> {
        private final Class<T> type;

        public JavaSerializerTranslator(Class<T> type) {
            this.type = type;
        }

        @Override
        public T translate(InputStream resource) {
            try {
                var ois = new ObjectInputStream(resource);
                return (T) ois.readObject();
            } catch (Throwable t) {
                throw Exceptions.wrap(t);
            }
        }

        @Override
        public Class<InputStream> inputType() {
            return InputStream.class;
        }

        @Override
        public Class<T> outputType() {
            return type;
        }
    }

    public static class User implements Serializable {
        private static final long serialVersionUID = 3658395613658196L;
        public String userName;
        public String address;

        public User() {
        }

        public User(User other) {
            this.userName = other.userName;
            this.address = other.address;
        }

        @Override
        public String toString() {
            return "User{" +
                    "userName='" + userName + '\'' +
                    ", address='" + address + '\'' +
                    '}';
        }
    }

}
