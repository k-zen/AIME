package io.aime.plugins.protocolhttpclient;

/**
 * Can be used to identify problems during creation of Authentication objects.
 *
 * <p>
 * In the future it may be used as a method of collecting authentication
 * failures during Http protocol transfer in order to present the user with
 * credentials required during a future fetch.
 * </p>
 *
 * @author Matt Tencati
 * @author K-Zen
 */
public class HTTPAuthenticationException extends Exception {

    /**
     * Constructs a new exception with null as its detail message.
     */
    public HTTPAuthenticationException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message. The detail message is saved for later
     *                retrieval by the {@link Throwable#getMessage()} method.
     */
    public HTTPAuthenticationException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified message and cause.
     *
     * @param message the detail message. The detail message is saved for later
     *                retrieval by the {@link Throwable#getMessage()} method.
     * @param cause   the cause (use {@link #getCause()} to retrieve the cause)
     */
    public HTTPAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause and detail message
     * from given clause if it is not null.
     *
     * @param cause the cause (use {@link #getCause()} to retrieve the cause)
     */
    public HTTPAuthenticationException(Throwable cause) {
        super(cause);
    }
}
