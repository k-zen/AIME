package io.aime.plugin;

public class PluginRuntimeException extends Exception {

    public PluginRuntimeException(Throwable cause) {
        super(cause);
    }

    public PluginRuntimeException(String message) {
        super(message);
    }
}
