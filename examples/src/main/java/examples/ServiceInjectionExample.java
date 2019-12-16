package examples;

import xyz.ressor.Ressor;

import java.util.function.Function;

import static helper.ExampleHelper.constantStringSource;
import static helper.ExampleHelper.stringValue;

public class ServiceInjectionExample {

    public static void main(String[] args) {
        Ressor ressor = Ressor.create();

        CharSequence stringOne = ressor.service(CharSequence.class)
                .source(constantStringSource())
                .resource(stringValue("one"))
                .string()
                .factory(Function.identity())
                .build();

        CharSequence stringTwo = ressor.service(CharSequence.class)
                .source(constantStringSource())
                .resource(stringValue("two"))
                .string()
                .factory((String v) -> v + " " + stringOne)
                .build();

        System.out.println(stringTwo);

        ressor.shutdown();
    }

}
