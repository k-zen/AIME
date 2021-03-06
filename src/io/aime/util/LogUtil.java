package io.aime.util;

// IO
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

// Lang
import java.lang.reflect.Method;

// Log4j
import org.apache.log4j.Logger;

/**
 * Clase encargada de realizar los logeos necesarios, ya sean a bases de datos,
 * archivos, correo, u otros.
 *
 * @author K-Zen
 */
public class LogUtil {

    private final static Logger LOG = Logger.getLogger(LogUtil.class.getName());
    private static Method TRACE = null;
    private static Method DEBUG = null;
    private static Method INFO = null;
    private static Method WARN = null;
    private static Method ERROR = null;
    private static Method FATAL = null;

    static {
        try {
            TRACE = Logger.class.getMethod("trace", new Class[]{Object.class});
            DEBUG = Logger.class.getMethod("debug", new Class[]{Object.class});
            INFO = Logger.class.getMethod("info", new Class[]{Object.class});
            WARN = Logger.class.getMethod("warn", new Class[]{Object.class});
            ERROR = Logger.class.getMethod("error", new Class[]{Object.class});
            FATAL = Logger.class.getMethod("fatal", new Class[]{Object.class});
        }
        catch (Exception e) {
            LOG.error("No se ha podido inicializar los metodos de logeo. Error -> " + e.toString(), e);
        }
    }

    public static PrintStream getTraceStream(final Logger logger) {
        return LogUtil.getLogStream(logger, TRACE);
    }

    public static PrintStream getDebugStream(final Logger logger) {
        return LogUtil.getLogStream(logger, DEBUG);
    }

    public static PrintStream getInfoStream(final Logger logger) {
        return LogUtil.getLogStream(logger, INFO);
    }

    public static PrintStream getWarnStream(final Logger logger) {
        return LogUtil.getLogStream(logger, WARN);
    }

    public static PrintStream getErrorStream(final Logger logger) {
        return LogUtil.getLogStream(logger, ERROR);
    }

    public static PrintStream getFatalStream(final Logger logger) {
        return LogUtil.getLogStream(logger, FATAL);
    }

    /**
     * Retorna un flujo que cuando se escribe en el, añade lineas al archivo de
     * Log correspondiente. Ej. Si agrego un flujo de Debug entonces los datos
     * que se escriben iran al log de que corresponda pero bajo DEBUG.
     */
    private static PrintStream getLogStream(final Logger logger, final Method method) {
        return new PrintStream(new ByteArrayOutputStream() {
            private int scan = 0;

            private boolean hasNewline() {
                for (; scan < count; scan++) {
                    if (buf[scan] == '\n') {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void flush() throws IOException {
                if (!hasNewline()) {
                    return;
                }
                try {
                    method.invoke(logger, new Object[]{toString().trim()});
                }
                catch (Exception e) {
                    LOG.fatal("No es posible logear con metodo [" + method + "]. Error -> " + e.toString(), e);
                }
                this.reset();
                scan = 0;
            }
        }, true);
    }
}
