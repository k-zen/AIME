package io.aime.util;

/**
 * This class builds up all kinds of HTML formatted messages for the view.
 *
 * @author K-Zen
 */
public class HtmlMessageBuilder
{

    /**
     * Return the default CSS configuration for the app.
     *
     * @return A String containing the CSS.
     */
    public static String mainCSS()
    {
        StringBuilder css = new StringBuilder();
        css.append("<style type=\"text/css\">");
        css.append("html {margin:0px;padding:0px;}");
        css.append("body {background-color:transparent;margin:0px;padding:0px;}");
        css.append("div {margin:0px;padding:0px;}");
        css.append("h1, h2, h3, h4, h5, h6 {margin:2px 0px;padding:0px;text-decoration:underline;}");
        css.append("p {margin:0.4em 0 0.5em 0;}");
        css.append("a {color:orange;}");
        css.append("ul {margin:0px 8px;padding:0px;vertical-align:top;display:block;}");
        css.append("li {list-style-type:none;margin:0px;padding:0px;}");
        css.append("table {border-collapse:collapse;margin:0px;padding:0px;width:100%;}");
        css.append("th {border:0;margin:2px;padding:2px;}");
        css.append("td {border:0;margin:2px;padding:2px;}");
        css.append("#container {margin:0px;padding:0px;}");
        css.append(".subtitle {font-weight:bold;}");
        css.append(".domain {margin-left:4px;}");
        css.append(".title {color:#222222;text-decoration:none;}");
        css.append("</style>");

        return css.toString();
    }

    /**
     * Builds a one paragraph HTML formatted message.
     *
     * @param msg The message in plain text.
     *
     * @return The message in HTML format.
     */
    private static String buildOnlyOneParagraphMsg(String msg)
    {
        StringBuilder text = new StringBuilder();
        text.append("<html>");
        text.append("<head>");
        text.append(HtmlMessageBuilder.mainCSS());
        text.append("</head>");
        text.append("<body>");
        text.append("<p>").append(msg).append("</p>");
        text.append("</body>");
        text.append("</html>");

        return text.toString();
    }

    /**
     * Builds a HTML formatted message.
     *
     * @param msg The message in HTML.
     *
     * @return The message in HTML format.
     */
    public static String buildHTMLMsg(String msg)
    {
        StringBuilder text = new StringBuilder();
        text.append("<html>");
        text.append("<head>");
        text.append(HtmlMessageBuilder.mainCSS());
        text.append("</head>");
        text.append("<body>");
        text.append("<div id=\"container\">");
        text.append(msg);
        text.append("</div>");
        text.append("</body>");
        text.append("</html>");

        return text.toString();
    }

    /**
     * Builds a standard function finishing HTML formatted message.
     *
     * @param timer The timer object, which holds the function's execution time.
     *
     * @return The message in HTML format.
     */
    public static String buildFunctionFinishMsg(Timer timer)
    {
        StringBuilder text = new StringBuilder();
        text.append("<html>");
        text.append("<head>");
        text.append(HtmlMessageBuilder.mainCSS());
        text.append("</head>");
        text.append("<body>");
        text.append("<p><b>Process concluded successfully!</b></p>");
        text.append("<p><b>");
        text.append("<u>Execution Time:</u><br/>");
        text.append("In seconds: ").append(timer.computeOperationTime(Timer.Time.SECOND)).append("<br/>");
        text.append("In minutes: ").append(timer.computeOperationTime(Timer.Time.MINUTE)).append("<br/>");
        text.append("In hours: ").append(timer.computeOperationTime(Timer.Time.HOUR));
        text.append("</b></p>");
        text.append("<p><b>");
        text.append("<u>Consumed Memory:</u><br/>");
        text.append("Memory: ").append(GeneralUtilities.getMemoryUse(true)).append("MB");
        text.append("</b></p>");
        text.append("</body>");
        text.append("</html>");

        return text.toString();
    }
}
