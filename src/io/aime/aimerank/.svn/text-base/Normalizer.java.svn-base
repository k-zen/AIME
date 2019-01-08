package io.aime.aimerank;

/**
 * Handles the normalization of numbers to a number between 0 and 1.
 * <pre>
 * i.e.
 * normalize(2037.968) -> 0.9211259
 * </pre>
 *
 * @author Andreas P. Koenzen <akc@apkc.net>
 * @version 0.2
 */
public final class Normalizer
{

    /**
     * Normalizes any number between 0 and 1.
     * <pre>
     * µ = 0.85
     * Ω(∆) = 1 - 3√ ( ∆ / ( ∆^2 + µ ) )
     * </pre>
     *
     * @param number The number to normalize.
     *
     * @return A number between 0 and 1.
     */
    public static double normalize(long number)
    {
        double omega = 0.0d;
        double mu = 0.85d;
        long delta = number;

        if (number > 0) {
            omega = (1 - Math.cbrt(delta / (Math.pow(delta, 2) + mu)));
        }

        // Control momentaneo que no sea infinito.
        if (Double.isNaN(omega)) {
            return 0.0d;
        }
        else {
            return omega;
        }
    }

    /**
     * Normalizes any number between 0 and 1.
     * <pre>
     * µ = 0.85
     * Ω(∆) = 1 - 3√ ( ∆ / ( ∆^2 + µ ) )
     * </pre>
     *
     * @param number The number to normalize.
     *
     * @return A number between 0 and 1.
     */
    public static double normalize(float number)
    {
        double omega = 0.0d;
        double mu = 0.85d;
        float delta = number;

        if (number > 0) {
            omega = (1 - Math.cbrt(delta / (Math.pow(delta, 2) + mu)));
        }

        // Control momentaneo que no sea infinito.
        if (Double.isNaN(omega)) {
            return 0.0d;
        }
        else {
            return omega;
        }
    }

    /**
     * Normalizes any number between 0 and 1.
     * <pre>
     * µ = 0.85
     * Ω(∆) = 1 - 3√ ( ∆ / ( ∆^2 + µ ) )
     * </pre>
     *
     * @param number The number to normalize.
     *
     * @return A number between 0 and 1.
     */
    public static double normalize(double number)
    {
        double omega = 0.0d;
        double mu = 0.85d;
        double delta = number;

        if (number > 0) {
            omega = (1 - Math.cbrt(delta / (Math.pow(delta, 2) + mu)));
        }

        // Control momentaneo que no sea infinito.
        if (Double.isNaN(omega)) {
            return 0.0d;
        }
        else {
            return omega;
        }
    }
}
