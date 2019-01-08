package io.aime.plugins.protocolsmb;

// AIME
import io.aime.protocol.ProtocolException;

public class SambaException extends ProtocolException {

    public SambaException() {
        super();
    }

    public SambaException(String message) {
        super(message);
    }

    public SambaException(String message, Throwable cause) {
        super(message, cause);
    }

    public SambaException(Throwable cause) {
        super(cause);
    }
}
