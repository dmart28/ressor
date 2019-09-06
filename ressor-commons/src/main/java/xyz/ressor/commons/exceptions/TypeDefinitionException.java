package xyz.ressor.commons.exceptions;

import static java.lang.String.format;

public class TypeDefinitionException extends RuntimeException {

    public TypeDefinitionException(Class<?> type, String message) {
        super(format("Type %s problem: %s", type, message));
    }

}
