package main.jlox;

import java.io.Serial;

public class RuntimeError extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    final Token token;

    RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}
