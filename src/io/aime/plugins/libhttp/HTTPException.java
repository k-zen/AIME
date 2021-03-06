package io.aime.plugins.libhttp;

// AIME
import io.aime.protocol.ProtocolException;

public class HTTPException extends ProtocolException {

    public HTTPException() {
        super();
    }

    public HTTPException(String message) {
        super(message);
    }

    public HTTPException(String message, Throwable cause) {
        super(message, cause);
    }

    public HTTPException(Throwable cause) {
        super(cause);
    }
}
