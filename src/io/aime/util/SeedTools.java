package io.aime.util;

import io.aime.brain.Brain;
import io.aime.brain.data.BrainXMLData;
import io.aime.brain.data.BrainXMLData.Parameter;
import io.aime.brain.data.MetadataCrawlJob;
import io.aime.brain.xml.Handler;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

/**
 * Utility class for handling seeds.
 */
public class SeedTools
{

    private static final Logger LOG = Logger.getLogger(SeedTools.class.getName());
    private static final UrlValidator VALIDATOR = new UrlValidator(new String[]{
        "http", "https", "file", "smb"
    }, UrlValidator.ALLOW_LOCAL_URLS);

    /**
     * Connects to Cerebellum, retrieves the seeds, parses them and finally returns a string array with only
     * the URLs.
     *
     * @return An array with the URLs of the seeds.
     */
    public static String[] getURLs()
    {
        TreeMap<String, TreeMap<String, String>> seeds = (TreeMap<String, TreeMap<String, String>>) Brain
                .getInstance()
                .execute(Handler
                        .makeXMLRequest(BrainXMLData
                                .newBuild()
                                .setJob(BrainXMLData.JOB_REQUEST)
                                .setClazz(MetadataCrawlJob.Data.class)
                                .setFunction(MetadataCrawlJob.Data.SEEDS.getMethodName()))).get();
        List<String> urls = new ArrayList<>();
        Iterator<String> i = seeds.keySet().iterator();
        while (i.hasNext()) {
            urls.add(i.next());
        }

        return urls.toArray(new String[urls.size()]);
    }

    /**
     * Connects to Cerebellum, retrieves the seeds, parse the new URL, adds it to the seeds
     * collection and finally updates back to Cerebellum.
     *
     * @param url The URL to add.
     */
    public static void addSeed(String url)
    {
        if (!processURL(url)) {
            return;
        }

        TreeMap<String, String> value = new TreeMap<>();
        value.put("url", url);
        value.put("score", "10.0");
        value.put("fetchinterval", "86400");

        TreeMap<String, TreeMap<String, String>> seeds = (TreeMap<String, TreeMap<String, String>>) Brain
                .getInstance()
                .execute(Handler
                        .makeXMLRequest(BrainXMLData
                                .newBuild()
                                .setJob(BrainXMLData.JOB_REQUEST)
                                .setClazz(MetadataCrawlJob.Data.class)
                                .setFunction(MetadataCrawlJob.Data.SEEDS.getMethodName()))).get();
        seeds.put(url, value);

        // Update back.
        Brain
                .getInstance()
                .execute(Handler
                        .makeXMLRequest(BrainXMLData
                                .newBuild()
                                .setJob(BrainXMLData.JOB_MERGE)
                                .setClazz(MetadataCrawlJob.Data.class)
                                .setFunction(MetadataCrawlJob.Data.SEEDS.getMethodName())
                                .setParam(Parameter
                                        .newBuild()
                                        .setType(TreeMap.class)
                                        .setData(seeds))));
    }

    /**
     * Connects to Cerebellum, retrieves the seeds, deletes the entry corresponding to the URL
     * and finally updates back to Cerebellum.
     *
     * @param url The URL to delete.
     */
    public static void removeSeed(String url)
    {
        TreeMap<String, TreeMap<String, String>> seeds = (TreeMap<String, TreeMap<String, String>>) Brain
                .getInstance()
                .execute(Handler
                        .makeXMLRequest(BrainXMLData
                                .newBuild()
                                .setJob(BrainXMLData.JOB_REQUEST)
                                .setClazz(MetadataCrawlJob.Data.class)
                                .setFunction(MetadataCrawlJob.Data.SEEDS.getMethodName()))).get();
        seeds.remove(url);

        // Update back.
        Brain
                .getInstance()
                .execute(Handler
                        .makeXMLRequest(BrainXMLData
                                .newBuild()
                                .setJob(BrainXMLData.JOB_MERGE)
                                .setClazz(MetadataCrawlJob.Data.class)
                                .setFunction(MetadataCrawlJob.Data.SEEDS.getMethodName())
                                .setParam(Parameter
                                        .newBuild()
                                        .setType(TreeMap.class)
                                        .setData(seeds))));
    }

    /**
     * Connects to Cerebellum, retrieves the seeds, deletes the entries corresponding to the URLs
     * and finally updates back to Cerebellum.
     *
     * @param urls Array of URLs to delete.
     */
    public static void removeSeed(String[] urls)
    {
        TreeMap<String, TreeMap<String, String>> seeds = (TreeMap<String, TreeMap<String, String>>) Brain
                .getInstance()
                .execute(Handler
                        .makeXMLRequest(BrainXMLData
                                .newBuild()
                                .setJob(BrainXMLData.JOB_REQUEST)
                                .setClazz(MetadataCrawlJob.Data.class)
                                .setFunction(MetadataCrawlJob.Data.SEEDS.getMethodName()))).get();
        for (String url : urls) {
            seeds.remove(url);
        }

        // Update back.
        Brain
                .getInstance()
                .execute(Handler
                        .makeXMLRequest(BrainXMLData
                                .newBuild()
                                .setJob(BrainXMLData.JOB_MERGE)
                                .setClazz(MetadataCrawlJob.Data.class)
                                .setFunction(MetadataCrawlJob.Data.SEEDS.getMethodName())
                                .setParam(Parameter
                                        .newBuild()
                                        .setType(TreeMap.class)
                                        .setData(seeds))));
    }

    /**
     * Parses a bulk of text separated by new lines, in order to extract URLs entered by the user. It uses
     * that info to add new seeds to the seeds collection. Later updates that info to Cerebellum.
     *
     * @param text The bulk of text from where to extract the seeds.
     */
    public static void importSeed(String text)
    {
        TreeMap<String, TreeMap<String, String>> seeds = new TreeMap<>();
        // Iterate here each line, and build Map.
        String[] lines = text.split("\\n");
        if (lines != null) {
            for (String url : lines) {
                if (!processURL(url)) {
                    continue;
                }

                TreeMap<String, String> value = new TreeMap<>();
                value.put("url", url);
                value.put("score", "10.0");
                value.put("fetchinterval", "86400");

                seeds.put(url, value);
            }
        }

        // Update back.
        Brain
                .getInstance()
                .execute(Handler
                        .makeXMLRequest(BrainXMLData
                                .newBuild()
                                .setJob(BrainXMLData.JOB_MERGE)
                                .setClazz(MetadataCrawlJob.Data.class)
                                .setFunction(MetadataCrawlJob.Data.SEEDS.getMethodName())
                                .setParam(Parameter
                                        .newBuild()
                                        .setType(TreeMap.class)
                                        .setData(seeds))));
    }

    /**
     * Runs a URL through a set of pre-defined filter and if all tests pass then
     * returns true.
     *
     * @param url The URL to filter.
     *
     * @return TRUE if the URL has passed, FALSE otherwise.
     */
    private static boolean processURL(String url)
    {
        // If the line is empty then continue.
        if (url.isEmpty()) {
            return false;
        }

        url = url.toLowerCase(); // Convert to lowercase.
        url = url.replaceAll("\\s+", "+"); // Replace spaces in file or URL.
        url = StringUtils.chomp(url, "/"); // Remove trailing slashes.

        return VALIDATOR.isValid(url);
    }

    /**
     * Loads a file in which every single URL from the seeds occupies one line.
     *
     * <p>
     * This file will be feed to the Injector as an FileInputFormat file, and
     * the MR job will read this file line by line, inserting each URL into the
     * Main DBase.</p>
     *
     * @param seedFile Where to put the seed file
     * @param exeType  The execution type
     */
    public static void prepareFile(String seedFile, Integer exeType)
    {
        FileSystem fs = null;
        try {
            fs = FileSystem.get(new AIMEConfiguration().create());
        }
        catch (IOException e) {
            LOG.fatal("An instance of the file system could not be obtain. Error: " + e.toString(), e);
        }

        // Cleanup previous files.
        if (exeType == AIMEConstants.LOCAL_EXECUTION_TYPE.getIntegerConstant()) {
            File f = new File(seedFile);
            if (f.isFile()) {
                f.delete();
            }
        }
        else {
            try {
                if (fs != null) {
                    fs.delete(new Path(AIMEConstants.SEEDS_FILE_PATH.getStringConstant()), true);
                }
                else {
                    throw new IOException("Pipe to Hadoop file system broken!");
                }
            }
            catch (IOException e) {
                LOG.error("The seed file in the DFS could not be deleted. Error: " + e.toString(), e);
            }
        }

        // Now load the seeds into the file, and if distributed mode, also load in the local FS,
        // latter the local version of the file will be copied into DFS for use.
        // Seed iterator.
        for (String seedURL : getURLs()) {
            GeneralUtilities.logToFile(seedFile, seedURL);
        }

        // Copy the file to DFS, so that it can be used by MapReduce jobs.
        if (exeType == AIMEConstants.DISTRIBUTED_EXECUTION_TYPE.getIntegerConstant()) {
            try {
                if (fs != null) {
                    fs.copyFromLocalFile(true, true, new Path(seedFile), new Path(seedFile));
                }
                else {
                    throw new IOException("Pipe to Hadoop file system broken!");
                }
            }
            catch (IOException e) {
                LOG.error("The seed file could not be copied into DFS. Error: " + e.toString(), e);
            }
        }
    }
}
