package io.aime.exceptions;

/**
 * This exception gets thrown when a given object of AIME is not properly configured
 * or initialized through the use of annotations.
 *
 * <p>
 * Example: When a method of an object is configured as NonOptional but the method
 * hasn't been called for this given object, then the method that checks this object
 * before passing it along for use, will raise this exception.</p>
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @version 0.2
 */
public class ObjectConfigurationException extends Exception
{

    public ObjectConfigurationException()
    {
        super();
    }

    public ObjectConfigurationException(String message)
    {
        super(message);
    }

    public ObjectConfigurationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ObjectConfigurationException(Throwable cause)
    {
        super(cause);
    }
}
