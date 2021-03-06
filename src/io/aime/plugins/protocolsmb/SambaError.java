package io.aime.plugins.protocolsmb;

/**
 * Thrown for File error codes.
 *
 * @author Nutch.org
 * @author K-Zen
 */
public class SambaError extends SambaException {

    private int code;

    public int getCode(int code) {
        return code;
    }

    public SambaError(int code) {
        super("Samba file Error: " + code);
        this.code = code;
    }
}
