package main.jlox;

import java.io.Serial;

public class Return extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    final Object value;

    Return(Object value) {
        super(null, null, false, false);

        this.value = value;
    }
}
