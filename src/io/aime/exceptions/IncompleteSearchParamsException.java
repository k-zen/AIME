package io.aime.exceptions;

/**
 * Throws when search parameters are incomplete to perform a search.
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @version 0.2
 */
public class IncompleteSearchParamsException extends Exception
{

    public IncompleteSearchParamsException()
    {
        super();
    }

    public IncompleteSearchParamsException(String message)
    {
        super(message);
    }

    public IncompleteSearchParamsException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public IncompleteSearchParamsException(Throwable cause)
    {
        super(cause);
    }
}
