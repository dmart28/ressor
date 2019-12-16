package quickstart;

import xyz.ressor.Ressor;

import java.util.function.Function;

import static helper.ExampleHelper.constantStringSource;
import static helper.ExampleHelper.stringValue;

public class ServiceInjectionExample {

    public static void main(String[] args) {
        var ressor = Ressor.create();

        var stringOne = ressor.service(CharSequence.class)
                .source(constantStringSource())
                .resource(stringValue("one"))
                .string()
                .factory(Function.identity())
                .build();

        var stringTwo = ressor.service(CharSequence.class)
                .source(constantStringSource())
                .resource(stringValue("two"))
                .string()
                .factory((String v) -> v + " " + stringOne)
                .build();

        System.out.println(stringTwo);

        ressor.shutdown();
    }

}
