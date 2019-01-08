package io.aime.summary;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleSentencesExtractor;
import io.aime.aimemisc.datamining.StopWords;
import io.aime.util.AIMEConstants;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;

/**
 * Text or Web Page summarizer implementation class for AIME.
 *
 * @author K-Zen
 * @author Nutch.org
 */
public final class Summarizer
{

    private static final Logger LOG = Logger.getLogger(Summarizer.class.getName());
    private int sumContext;
    private Configuration conf = null;

    public Summarizer(Configuration conf)
    {
        this.setConf(conf);
    }

    public Configuration getConf()
    {
        return conf;
    }

    public void setConf(Configuration conf)
    {
        this.conf = conf;
        this.sumContext = conf.getInt("summary.context.total.words", 36);
    }

    /**
     * Builds a summary using the library Boilerpipe.
     *
     * @param content  A string containing the full source code of the page.
     * @param query    The query string.
     * @param trimText If the text should be trimmed.
     * @param limit    The maximum number of words.
     *
     * @return The summary of the page.
     */
    public String getBoilerpipeSummary(String content, String query, boolean trimText, int limit)
    {
        StringBuilder summary = new StringBuilder();
        int wordLimit = limit;

        if (!trimText)
        {
            try
            {
                return this.cleanSummaryText(ArticleSentencesExtractor.INSTANCE.getText(new StringReader(content)));
            }
            catch (BoilerpipeProcessingException e)
            {
                LOG.error("Impossible to create a summary using the library Boilerpipe. Error: " + e.toString(), e);
            }
        }

        try
        {
            StringTokenizer tokens = new StringTokenizer(this.cleanSummaryText(ArticleSentencesExtractor.INSTANCE.getText(new StringReader(content))));
            int counter = 0;
            while (tokens.hasMoreTokens() && counter < wordLimit)
            {
                summary.append(tokens.nextToken()).append(" ");
                counter++;
            }

            return summary.append(" ").append(AIMEConstants.DEFAULT_ELLIPSIS.getStringConstant()).toString();
        }
        catch (BoilerpipeProcessingException e)
        {
            LOG.error("Impossible to create a summary using the library Boilerpipe. Error: " + e.toString(), e);
        }

        return new String("");
    }

    /**
     * This method will return a summary based on the document's text.
     *
     * <p>
     * The summary will consist in the N words before the best query word and
     * N words afterwards.<br/>
     * The best words are the words that appear the most in the text
     * document's.</p>
     *
     * @param text  The document's text.
     * @param query The query string.
     * @param lang  The language of the query.
     *
     * @return The snippet or summary of the document's text.
     */
    public String getSummary(String text, String query, String lang)
    {
        StringBuilder summary = new StringBuilder();
        List<String> queryWords;
        List<String> wordList = new ArrayList<>(0);
        List<String> bestSnippets = new ArrayList<>(0);
        int maxTokens = 2000;

        try
        {
            // First break the text into tokens.
            StringTokenizer textTokens = new StringTokenizer(this.cleanSummaryText(text));
            int countTokens = 0;
            while (textTokens.hasMoreTokens() && countTokens < maxTokens)
            {
                String token = textTokens.nextToken().trim();
                wordList.add((token.length() > 40) ? token.substring(0, 39) : token);
                countTokens++;
            }

            // Break the query string, and clean them.
            queryWords = Arrays.asList(query.split("\\s+"));
            ListIterator<String> i = queryWords.listIterator();
            while (i.hasNext())
            {
                String qW = i.next().trim();
                i.set(qW);
            }

            // If the word list does not contain the query, then return the first N words
            // less than sumContext.
            boolean shouldExit = true;
            for (String q : queryWords)
            {
                if (wordList.contains(q))
                {
                    shouldExit = false;
                }
            }
            if (shouldExit)
            {
                for (String word : wordList)
                {
                    summary.append(word).append(" ");

                    if (summary.toString().split("\\s+").length > this.sumContext)
                    {
                        summary.append("...");
                        break;
                    }
                }
            }

            // If there are many words in the query, count which one is the one 
            // that appears more. Use StopWords to avoid Boolean Queries operators.
            String bestQueryWord = "";
            int maxWordCount = -1;
            for (String q : queryWords)
            {
                int wordCount = Collections.frequency(wordList, q);
                if (wordCount > maxWordCount && !StopWords.itsStopWord(q, lang))
                {
                    bestQueryWord = q;
                    maxWordCount = wordCount;
                }
            }
            query = bestQueryWord;

            // Locate all phrases where the query string occurs, and return 9 words before and 9 after.
            int lowOffset = 0;
            int highOffset = 0;
            boolean addEllipsisAtStr = true;
            while (true)
            {
                StringBuilder snippet = new StringBuilder();

                lowOffset = (((wordList.indexOf(query) - 9) + lowOffset) >= 0) ? ((wordList.indexOf(query) - 9) + lowOffset) : 0;
                highOffset = (((wordList.indexOf(query) + 9) + highOffset) <= (wordList.size() - 1)) ? ((wordList.indexOf(query) + 9) + highOffset) : wordList.size() - 1;

                // Avoid IndexOutOfBounds and infinite loops!!!!!
                if (lowOffset > (wordList.size() - 1) || highOffset > (wordList.size() - 1))
                {
                    break;
                }

                for (String word : wordList.subList(lowOffset, highOffset))
                {
                    // Check if we must add ellipsis at the start too.
                    if (!wordList.get(0).equals(word) && addEllipsisAtStr)
                    {
                        addEllipsisAtStr = false;
                        snippet.append(" ... ");
                    }

                    snippet.append(word).append(" ");
                }

                // Add ellipsis.
                snippet.append("... ");

                if (!bestSnippets.contains(snippet.toString()))
                {
                    bestSnippets.add(snippet.toString());
                }

                // Where are at the end, no more tokens. Quit.
                if (highOffset >= (wordList.size() - 1))
                {
                    break;
                }
            }

            // Add all snippets to summary.
            for (String snippet : bestSnippets)
            {
                // Do not surpass the max. amount of tokens.
                if (summary.toString().split("\\s+").length > this.sumContext)
                {
                    break;
                }

                summary.append(snippet);
            }
        }
        catch (Exception e)
        {
            // If an exception as ocurr, then return only the first N characters from the text.
            StringTokenizer textTokens = new StringTokenizer(this.cleanSummaryText(text));
            int countTokens = 0;
            while (textTokens.hasMoreTokens() && countTokens < this.sumContext)
            {
                summary.append(textTokens.nextToken().trim()).append(" ");
                countTokens++;
            }
            summary.append("...");
        }

        return summary.toString().trim();
    }

    /**
     * This method cleans up text snippets for presentation to the user. The
     * cleaning up consists in removing double whitespace, or removing unwanted
     * whitespace from between words and a symbol. Like "word , another word",
     * etc.
     *
     * @param sb The text to be cleaned up.
     *
     * @return The clean text.
     */
    private String cleanSummaryText(String sb)
    {
        String text = Entities.encode(sb);
        Pattern pattern = null;
        String regex = new String();

        // Remove double spaces.
        regex = "(?:\\s|\\&nbsp\\;|\\&\\#160\\;|\\&\\#032\\;){2,}";
        pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        text = pattern.matcher(text).replaceAll(" ");

        // Remove spaces before comas or other symbols.
        regex = "(\\S)(\\s+)(\\;|\\,|\\:|\\.|\\?|\\!)";
        pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        text = pattern.matcher(text).replaceAll("$1$3");

        return text;
    }
}
