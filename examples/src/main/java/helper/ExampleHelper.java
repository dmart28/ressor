package helper;

import xyz.ressor.source.*;

import java.io.ByteArrayInputStream;

public class ExampleHelper {

    public static ConstantStringSource constantStringSource() {
        return new ConstantStringSource();
    }

    public static StringResourceId stringValue(String value) {
        return new StringResourceId(value);
    }

    public static class ConstantStringSource implements NonListenableSource<StringResourceId> {

        @Override
        public String id() {
            return "constant-string";
        }

        @Override
        public LoadedResource loadIfModified(StringResourceId resourceId, SourceVersion version) {
            return new LoadedResource(new ByteArrayInputStream(resourceId.getValue().getBytes()), version, resourceId);
        }

        @Override
        public String describe() {
            return null;
        }
    }

    public static class StringResourceId implements ResourceId {
        private final String value;

        public String getValue() {
            return value;
        }

        public StringResourceId(String value) {
            this.value = value;
        }

        @Override
        public Class<?> sourceType() {
            return ConstantStringSource.class;
        }
    }

}
