package io.aime.plugins.protocolfile;

/**
 * Thrown for File error codes.
 *
 * @author Nutch.org
 * @author K-Zen
 */
public class FileError extends FileException {

    private int code;

    public int getCode(int code) {
        return code;
    }

    public FileError(int code) {
        super("File Error: " + code);
        this.code = code;
    }
}
