package io.aime.exceptions;

/**
 * Throws when a method is still running and we are trying to get the final
 * execution time.
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @version 0.2
 */
public class MethodStillRunningException extends Exception
{

    public MethodStillRunningException()
    {
        super();
    }

    public MethodStillRunningException(String message)
    {
        super(message);
    }

    public MethodStillRunningException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public MethodStillRunningException(Throwable cause)
    {
        super(cause);
    }
}
