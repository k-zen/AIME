package io.aime.protocol;

// Net
import java.net.URL;

/**
 * @author Andrzej Bialecki &lt;ab@getopt.org&gt;
 */
public class EmptyRobotRules implements RobotRules {

    public static final RobotRules RULES = new EmptyRobotRules();

    @Override
    public long getCrawlDelay() {
        return -1;
    }

    @Override
    public long getExpireTime() {
        return -1;
    }

    @Override
    public boolean isAllowed(URL url) {
        return true;
    }
}
