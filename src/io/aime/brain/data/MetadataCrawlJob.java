package io.aime.brain.data;

import io.aime.aimemisc.io.FileStoring;
import io.aime.bot.Reports;
import io.aime.brain.Brain;
import io.aime.brain.xml.Handler;
import io.aime.crawl.CrawlDB;
import io.aime.crawl.Generator;
import io.aime.crawl.Injector;
import io.aime.exceptions.MethodStillRunningException;
import io.aime.fetcher.Fetcher;
import io.aime.indexer.Indexer;
import io.aime.parse.ParseSegment;
import io.aime.util.AIMEConfiguration;
import io.aime.util.AIMEConstants;
import io.aime.util.GeneralUtilities;
import io.aime.util.HtmlMessageBuilder;
import io.aime.util.LogEventHandler;
import io.aime.util.Timer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SwingWorker;
import net.apkc.emma.tasks.Task;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.StringUtils;
import org.apache.log4j.Logger;

/**
 * This serializable class will hold all meta data about a crawling job.
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @see <a href="http://en.wikipedia.org/wiki/Singleton_pattern">Singleton Pattern</a>
 */
public class MetadataCrawlJob extends Metadata
{

    private static final Logger LOG = Logger.getLogger(MetadataCrawlJob.class.getName());
    private static volatile MetadataCrawlJob _INSTANCE = new MetadataCrawlJob();
    /** Mark if this instance is empty. If TRUE then we must load data from file. */
    private static volatile boolean isEmpty = true;
    private Data data = new Data();

    public static MetadataCrawlJob getInstance()
    {
        return _INSTANCE;
    }

    @Override
    protected void updateInstance(Object newInstance)
    {
        if (newInstance != null) {
            _INSTANCE = (MetadataCrawlJob) newInstance;
        }
    }

    @Override
    public MetadataCrawlJob setData(Object data)
    {
        this.data = (Data) data;
        return this;
    }

    @Override
    public MetadataCrawlJob.Data getData()
    {
        return data;
    }

    @Override
    public Object getEmptyData()
    {
        return new Data();
    }

    @Override
    public void internalWrite(DataOutput out) throws IOException
    {
        data.write(out);
    }

    @Override
    public void internalRead(DataInput in) throws IOException
    {
        Data d = new Data();
        d.internalRead(in);
        data = d;
    }

    @Override
    public MetadataCrawlJob read()
    {
        if (isEmpty) {
            File f = new File(AIMEConstants.DEFAULT_JOB_DATA_FOLDER.getStringConstant() + "/" + AIMEConstants.METADATA_CRAWLJOB_FILENAME.getStringConstant());
            getInstance().updateInstance((MetadataCrawlJob) FileStoring.getInstance().readFromFile(
                    f,
                    AIMEConstants.METADATA_ENCRYPT.getBooleanConstant(),
                    AIMEConstants.METADATA_KEY.getStringConstant(),
                    AIMEConstants.METADATA_ENCODING.getStringConstant()));
            isEmpty = false;
        }

        return getInstance();
    }

    @Override
    public void merge(Metadata newData)
    {
        FileStoring.getInstance().writeToFile(
                new File(AIMEConstants.DEFAULT_JOB_DATA_FOLDER.getStringConstant() + "/" + AIMEConstants.METADATA_CRAWLJOB_FILENAME.getStringConstant()),
                (MetadataCrawlJob) newData,
                AIMEConstants.METADATA_ENCRYPT.getBooleanConstant(),
                AIMEConstants.METADATA_KEY.getStringConstant(),
                AIMEConstants.METADATA_ENCODING.getStringConstant());
    }

    /**
     * This serializable class will hold all meta data information about AIME.
     *
     * <ul>
     * <li>Initialization data</li>
     * <li>CrawlJob meta data</li>
     * <li>Seeds</li>
     * </ul>
     */
    public static class Data extends SerializableData
    {

        public static final MetadataMethod FUNCTIONS_TO_EXECUTE = MetadataMethod.newBuild().setMethodName("FunctionsToExecute");
        public static final MetadataMethod IS_CYCLE_COMPLETE = MetadataMethod.newBuild().setMethodName("IsCycleComplete");
        public static final MetadataMethod COMPLETED_ITERATIONS = MetadataMethod.newBuild().setMethodName("CompletedIterations");
        public static final MetadataMethod LAST_FUNCTION = MetadataMethod.newBuild().setMethodName("LastFunction");
        public static final MetadataMethod TIME_LAST_FUNCTION = MetadataMethod.newBuild().setMethodName("TimeLastFunction");
        public static final MetadataMethod CURRENT_DEPTH = MetadataMethod.newBuild().setMethodName("CurrentDepth");
        public static final MetadataMethod TIMER = MetadataMethod.newBuild().setMethodName("Timer");
        public static final MetadataMethod RUNNING = MetadataMethod.newBuild().setMethodName("Running");
        public static final MetadataMethod STARTED = MetadataMethod.newBuild().setMethodName("Started");
        public static final MetadataMethod CURRENT_RUNNING_FUNCTION = MetadataMethod.newBuild().setMethodName("CurrentRunningFunction");
        public static final MetadataMethod ELAPSED_TIME = MetadataMethod.newBuild().setMethodName("ElapsedTime");
        public static final MetadataMethod SEEDS = MetadataMethod.newBuild().setMethodName("Seeds");
        public static final MetadataMethod PAUSE = MetadataMethod.newBuild().setMethodName("Pause");
        public static final MetadataMethod RESUME = MetadataMethod.newBuild().setMethodName("Resume");
        // ### DATA
        private List<String> functionsToExecute = new LinkedList<>(Arrays.asList(new String[]{
            AIMEConstants.INJECTOR_METHOD_NAME.getStringConstant(),
            AIMEConstants.GENERATOR_METHOD_NAME.getStringConstant(),
            AIMEConstants.FETCH_METHOD_NAME.getStringConstant(),
            AIMEConstants.PARSE_METHOD_NAME.getStringConstant(),
            AIMEConstants.UPDATE_METHOD_NAME.getStringConstant(),
            AIMEConstants.INDEX_METHOD_NAME.getStringConstant()
        }));
        private Boolean isCycleComplete = AIMEConstants.METADATA_DEFAULTS_IS_CYCLE_COMPLETE.getBooleanConstant(); // If an entire iteration is complete.
        private Integer completedIterations = AIMEConstants.METADATA_DEFAULTS_COMPLETED_ITERATIONS.getIntegerConstant(); // How many complete iterations where performed.
        private String lastFunction = AIMEConstants.METADATA_DEFAULTS_LAST_FUNCTION.getStringConstant(); // The last function that was executed completelly.
        private Long timeLastFunction = AIMEConstants.METADATA_DEFAULTS_TIME_LAST_FUNCTION.getLongConstant(); // The time in milliseconds when the last function was finished.
        private AtomicInteger currentDepth = new AtomicInteger(0);
        private Timer timer = new Timer();
        private Boolean running = false;
        private Boolean started = false;
        private String currentRunningFunction = AIMEConstants.NO_FUNCTION_METHOD_NAME.getStringConstant();
        private Long elapsedTime = 0L;
        /**
         * <p>
         * How seeds work...?</p>
         * <p>
         * Properties of seeds:
         * <ul>
         * <li>Seeds can be a domain name or host. i.e. http://example.com, https://www.example.com, http://hello.example.com</li>
         * <li>Seeds can be a file. i.e. file:///Users/akc</li>
         * <li>Seeds must be prefixed with the corresponding protocol. i.e. http://example.com, https://hello.example.com, file:///Users/akc</li>
         * </ul>
         * </p>
         *
         * <p>
         * Example of an entry for a seed:
         * <pre>
         * [http://www.abc.com] => [TreeMap[String,String]]
         *    url => http://www.abc.com
         *    score => 10.0
         *    fetchinterval => 86400
         * </pre>
         * </p>
         */
        private TreeMap<String, TreeMap<String, String>> seeds = new TreeMap<>();
        // ### DATA
        // ### NON-DATA
        private boolean isFunctionRunning = false;
        private boolean pause = false;
        // ### NON-DATA

        public static Data newBuild()
        {
            return new Data();
        }

        @Override
        public MetadataCrawlJob.Data getData()
        {
            return this;
        }

        // ### DATA FUNCTIONS
        public Data setFunctionsToExecute(String[] functionsToExecute)
        {
            this.functionsToExecute = new LinkedList<>(Arrays.asList((String[]) functionsToExecute));
            return this;
        }

        public List<String> getFunctionsToExecute()
        {
            return functionsToExecute;
        }

        public Data setIsCycleComplete(Boolean isCycleComplete)
        {
            this.isCycleComplete = isCycleComplete;
            return this;
        }

        public Boolean getIsCycleComplete()
        {
            return isCycleComplete;
        }

        public Data setCompletedIterations(Integer completedIterations)
        {
            this.completedIterations = completedIterations;
            return this;
        }

        public Integer getCompletedIterations()
        {
            return completedIterations;
        }

        public Data setLastFunction(String lastFunction)
        {
            this.lastFunction = lastFunction;
            return this;
        }

        public String getLastFunction()
        {
            return lastFunction;
        }

        public Data setTimeLastFunction(Long timeLastFunction)
        {
            this.timeLastFunction = timeLastFunction;
            return this;
        }

        public Long getTimeLastFunction()
        {
            return timeLastFunction;
        }

        public Integer getCurrentDepth()
        {
            return currentDepth.get();
        }

        public Timer getTimer()
        {
            return timer;
        }

        public Boolean getRunning()
        {
            return running;
        }

        public Boolean getStarted()
        {
            return started;
        }

        public String getCurrentRunningFunction()
        {
            return currentRunningFunction;
        }

        public Long getElapsedTime() throws MethodStillRunningException
        {
            if (isFunctionRunning) {
                throw new MethodStillRunningException();
            }
            else {
                return elapsedTime;
            }
        }

        public Data setSeeds(TreeMap<String, TreeMap<String, String>> seeds)
        {
            this.seeds = seeds;
            return this;
        }

        public TreeMap<String, TreeMap<String, String>> getSeeds()
        {
            return seeds;
        }
        // ### DATA FUNCTIONS

        // ### NON-DATA FUNCTIONS
        /**
         * Sent the order to pause the execution of the next method.
         *
         * <p>
         * It's impossible to pause a running method, but we can still pause the
         * execution of the next method.
         * </p>
         */
        public void pause()
        {
            pause = true;
            isFunctionRunning = false;
        }

        /**
         * This method will resume a paused thread. It can not resume a stopped
         * thread.
         */
        public void resume()
        {
            pause = false;
            isFunctionRunning = true;
            synchronized (this) { // Grab the object's monitor or intrinsic lock by synchronizing on the instance.
                notifyAll(); // Notify all threads waiting and release the intrinsic lock.
            }
        }

        /**
         * Runs a given method.
         *
         * @param method      The name of the method.
         * @param processName The name of the function.
         * @param params      Parameter's array for the method.
         */
        private void runMethod(String method, String processName, Object[] params)
        {
            Timer t = new Timer();
            t.starTimer();
            currentRunningFunction = processName;

            Class<?> clazz;
            try {
                clazz = Class.forName(this.getClass().getName());
                Method m = (params.length > 2)
                           ? clazz.getDeclaredMethod(
                                method,
                                new Class<?>[]{
                                    String.class,
                                    SwingWorker.class,
                                    Integer.class
                                })
                           : clazz.getDeclaredMethod(
                                method,
                                new Class<?>[]{
                                    String.class, Integer.class
                                });

                // Pause the execution of this method.
                while (pause) {
                    synchronized (this) { // Grab the object's monitor or intrinsic lock by synchronizing on the instance.
                        try {
                            wait(); // Holds the thread and releases all intrinsic locks.
                        }
                        catch (InterruptedException ex) {
                            LOG.error("Error waiting on current thread. Error: " + ex.toString(), ex);
                        }
                    }
                }

                isFunctionRunning = true; // Mark as running method.
                m.invoke(clazz.newInstance(), params); // Run the method.
            }
            catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                LOG.fatal("Error running process: " + processName + ". Error: " + ex.toString(), ex);
            }
            finally {
                isFunctionRunning = false; // Marcar que un metodo no se esta ejecutando.
                t.endTimer();
                elapsedTime = t.getExecutionTime();
            }
        }

        /**
         * Grab info from this class meta data file if exists, merge the value of
         * the last function, and write back the contents to file.
         *
         * <p>
         * Overwrite previous data.
         * </p>
         *
         * @param function The last ran function.
         */
        private void markLastFunction(String function)
        {
            lastFunction = function;
            timeLastFunction = System.currentTimeMillis();

            // Remove this function from the list of functions to execute, unless a full cycle is complete, then re-add all functions
            // except "Injection".
            functionsToExecute.remove(function);
            if (functionsToExecute.isEmpty()) {
                functionsToExecute.add(AIMEConstants.GENERATOR_METHOD_NAME.getStringConstant());
                functionsToExecute.add(AIMEConstants.FETCH_METHOD_NAME.getStringConstant());
                functionsToExecute.add(AIMEConstants.PARSE_METHOD_NAME.getStringConstant());
                functionsToExecute.add(AIMEConstants.UPDATE_METHOD_NAME.getStringConstant());
                functionsToExecute.add(AIMEConstants.INDEX_METHOD_NAME.getStringConstant());
            }
        }

        /**
         * Launch a new injector process.
         *
         * @param crawlHome The path to the crawling dir
         * @param runner    The runner thread
         * @param exeType   The execution type
         *
         * @throws Exception If the process cannot be done.
         */
        private void injection(String crawlHome, SwingWorker<?, ?> runner, Integer exeType) throws Exception
        {
            int result; // Determines the outcome of the function.

            if (runner.isCancelled()) { // If the thread was interrupted, then return.
                return;
            }

            if (LOG.isInfoEnabled()) {
                LOG.info("### Running Process: " + AIMEConstants.INJECTOR_JOB_NAME.getStringConstant() + " ###");
            }

            String[] parameters = new String[]{
                (crawlHome + "/" + AIMEConstants.AIME_CRAWLDB_DIR_NAME.getStringConstant()),
                (AIMEConstants.SEEDS_FILE_PATH.getStringConstant())
            };

            Timer t = new Timer();
            t.starTimer();
            result = Injector.runProcess(parameters, runner); // Run the job and wait the outcome.
            t.endTimer();
            // If outcome is error, then interrupt the thread.
            if (result != 0) {
                runner.cancel(true);
            }
            // Else mark all as finished.
            else {
                markLastFunction(AIMEConstants.INJECTOR_METHOD_NAME.getStringConstant()); // Mark the last runned function.
                LogEventHandler.addNewEvent(
                        new LogEventHandler(
                                AIMEConstants.INJECTOR_JOB_NAME.getStringConstant(),
                                HtmlMessageBuilder.buildFunctionFinishMsg(t)),
                        AIMEConstants.INFO_EVENT.getIntegerConstant());
            }
        }

        /**
         * Launch a new generator process.
         *
         * @param crawlHome The path to the crawling dir.
         * @param runner    The runner thread.
         * @param exeType   The execution type
         *
         * @throws Exception If the process cannot be done.
         */
        private void generate(String crawlHome, SwingWorker<?, ?> runner, Integer exeType) throws Exception
        {
            int result; // Determines the outcome of the function.

            if (runner.isCancelled()) { // If the thread was interrupted, then return.
                return;
            }

            if (LOG.isInfoEnabled()) {
                LOG.info("### Running Process: " + AIMEConstants.GENERATOR_JOB_NAME.getStringConstant() + " ###");
            }

            String[] parameters = new String[]{
                (crawlHome + "/" + AIMEConstants.AIME_CRAWLDB_DIR_NAME.getStringConstant()),
                (crawlHome + "/" + AIMEConstants.AIME_SEGMENTS_DIR_NAME.getStringConstant())
            };

            Timer t = new Timer();
            t.starTimer();
            result = Generator.runProcess(parameters, runner); // Run the job and wait the outcome.
            t.endTimer();
            // If outcome is error, then interrupt the thread.
            if (result != 0) {
                runner.cancel(true);
            }
            // Else mark all as finished.
            else {
                markLastFunction(AIMEConstants.GENERATOR_METHOD_NAME.getStringConstant()); // Mark the last runned function.
                LogEventHandler.addNewEvent(
                        new LogEventHandler(
                                AIMEConstants.GENERATOR_JOB_NAME.getStringConstant(),
                                HtmlMessageBuilder.buildFunctionFinishMsg(t)),
                        AIMEConstants.INFO_EVENT.getIntegerConstant());
            }
        }

        /**
         * Launch a new fetching process.
         *
         * @param crawlHome The path to the crawling dir.
         * @param runner    The runner thread.
         * @param exeType   The execution type
         *
         * @throws Exception If the process cannot be done.
         */
        private void fetch(String crawlHome, SwingWorker<?, ?> runner, Integer exeType) throws Exception
        {
            int result; // Determines the outcome of the function.

            if (runner.isCancelled()) { // If the thread was interrupted, then return.
                return;
            }

            if (LOG.isInfoEnabled()) {
                LOG.info("### Running Process: " + AIMEConstants.FETCHER_JOB_NAME.getStringConstant() + " ###");
            }

            String[] parameters = null;
            if (exeType == AIMEConstants.LOCAL_EXECUTION_TYPE.getIntegerConstant()) {
                parameters = new String[]{
                    GeneralUtilities.lastFileModified(crawlHome + "/" + AIMEConstants.AIME_SEGMENTS_DIR_NAME.getStringConstant()).getAbsolutePath(),
                    "-noParsing"
                };
            }
            else {
                Path choice = null;
                try {
                    FileSystem fs = FileSystem.get(new AIMEConfiguration().create());
                    FileStatus[] fst = fs.listStatus(new Path(crawlHome + "/" + AIMEConstants.AIME_SEGMENTS_DIR_NAME.getStringConstant()));
                    long lastMod = Long.MIN_VALUE;

                    for (FileStatus file : fst) {
                        if (file.getModificationTime() > lastMod) {
                            choice = file.getPath();
                            lastMod = file.getModificationTime();
                        }
                    }

                    parameters = new String[]{
                        (choice != null) ? choice.toString() : null,
                        "-noParsing"
                    };
                }
                catch (IOException e) {
                    LOG.fatal("Error trying to find the last segment in DFS. Error: " + e.toString(), e);
                }
            }

            Timer t = new Timer();
            t.starTimer();
            result = Fetcher.runProcess(parameters, runner); // Run the job and wait the outcome.
            t.endTimer();
            // If outcome is error, then interrupt the thread.
            if (result != 0) {
                runner.cancel(true);
            }
            // Else mark all as finished.
            else {
                markLastFunction(AIMEConstants.FETCH_METHOD_NAME.getStringConstant()); // Mark the last runned function.
                LogEventHandler.addNewEvent(
                        new LogEventHandler(
                                AIMEConstants.FETCHER_JOB_NAME.getStringConstant(),
                                HtmlMessageBuilder.buildFunctionFinishMsg(t)),
                        AIMEConstants.INFO_EVENT.getIntegerConstant());
            }
        }

        /**
         * Launch a new parsing process.
         *
         * @param crawlHome The path to the crawling dir
         * @param runner    The runner thread
         * @param exeType   The execution type
         *
         * @throws Exception If the process cannot be done.
         */
        private void parse(String crawlHome, SwingWorker<?, ?> runner, Integer exeType) throws Exception
        {
            int result; // Determines the outcome of the function.

            if (runner.isCancelled()) { // If the thread was interrupted, then return.
                return;
            }

            if (LOG.isInfoEnabled()) {
                LOG.info("### Running Process: " + AIMEConstants.PARSE_JOB_NAME.getStringConstant() + " ###");
            }

            String[] parameters = null;
            if (exeType == AIMEConstants.LOCAL_EXECUTION_TYPE.getIntegerConstant()) {
                parameters = new String[]{
                    GeneralUtilities.lastFileModified(crawlHome + "/" + AIMEConstants.AIME_SEGMENTS_DIR_NAME.getStringConstant()).getAbsolutePath()
                };
            }
            else {
                Path choice = null;
                try {
                    FileSystem fs = FileSystem.get(new AIMEConfiguration().create());
                    FileStatus[] fst = fs.listStatus(new Path(crawlHome + "/" + AIMEConstants.AIME_SEGMENTS_DIR_NAME.getStringConstant()));
                    long lastMod = Long.MIN_VALUE;

                    for (FileStatus file : fst) {
                        if (file.getModificationTime() > lastMod) {
                            choice = file.getPath();
                            lastMod = file.getModificationTime();
                        }
                    }

                    parameters = new String[]{
                        (choice != null) ? choice.toString() : null
                    };
                }
                catch (IOException e) {
                    LOG.fatal("Error trying to find the last segment in DFS. Error: " + e.toString(), e);
                }
            }

            Timer t = new Timer();
            t.starTimer();
            result = ParseSegment.runProcess(parameters, runner); // Run the job and wait the outcome.
            t.endTimer();
            // If outcome is error, then interrupt the thread.
            if (result != 0) {
                runner.cancel(true);
            }
            // Else mark all as finished.
            else {
                markLastFunction(AIMEConstants.PARSE_METHOD_NAME.getStringConstant()); // Mark the last runned function.
                LogEventHandler.addNewEvent(
                        new LogEventHandler(
                                AIMEConstants.PARSE_JOB_NAME.getStringConstant(),
                                HtmlMessageBuilder.buildFunctionFinishMsg(t)),
                        AIMEConstants.INFO_EVENT.getIntegerConstant());
            }
        }

        /**
         * Launch a new merge process.
         *
         * @param crawlHome The path to the crawling dir
         * @param runner    The runner thread
         * @param exeType   The execution type
         *
         * @throws Exception If the process cannot be done.
         */
        private void update(String crawlHome, SwingWorker<?, ?> runner, Integer exeType) throws Exception
        {
            int result; // Determines the outcome of the function.

            if (runner.isCancelled()) { // If the thread was interrupted, then return.
                return;
            }

            if (LOG.isInfoEnabled()) {
                LOG.info("### Running Process: " + AIMEConstants.UPDATE_JOB_NAME.getStringConstant() + " ###");
            }

            String[] parameters = null;
            if (exeType == AIMEConstants.LOCAL_EXECUTION_TYPE.getIntegerConstant()) {
                parameters = new String[]{
                    (crawlHome + "/" + AIMEConstants.AIME_CRAWLDB_DIR_NAME.getStringConstant()),
                    GeneralUtilities.lastFileModified(crawlHome + "/" + AIMEConstants.AIME_SEGMENTS_DIR_NAME.getStringConstant()).getAbsolutePath(),
                    "-filter",
                    "-normalize"
                };
            }
            else {
                Path choice = null;
                try {
                    FileSystem fs = FileSystem.get(new AIMEConfiguration().create());
                    FileStatus[] fst = fs.listStatus(new Path(crawlHome + "/" + AIMEConstants.AIME_SEGMENTS_DIR_NAME.getStringConstant()));
                    long lastMod = Long.MIN_VALUE;

                    for (FileStatus file : fst) {
                        if (file.getModificationTime() > lastMod) {
                            choice = file.getPath();
                            lastMod = file.getModificationTime();
                        }
                    }

                    parameters = new String[]{
                        (crawlHome + "/" + AIMEConstants.AIME_CRAWLDB_DIR_NAME.getStringConstant()),
                        (choice != null) ? choice.toString() : null,
                        "-filter",
                        "-normalize"
                    };
                }
                catch (IOException e) {
                    LOG.fatal("Error trying to find the last segment in DFS. Error: " + e.toString(), e);
                }
            }

            Timer t = new Timer();
            t.starTimer();
            result = CrawlDB.runProcess(parameters, runner); // Run the job and wait the outcome.
            t.endTimer();
            // If outcome is error, then interrupt the thread.
            if (result != 0) {
                runner.cancel(true);
            }
            // Else mark all as finished.
            else {
                markLastFunction(AIMEConstants.UPDATE_METHOD_NAME.getStringConstant()); // Mark the last runned function.
                LogEventHandler.addNewEvent(
                        new LogEventHandler(
                                AIMEConstants.UPDATE_JOB_NAME.getStringConstant(),
                                HtmlMessageBuilder.buildFunctionFinishMsg(t)),
                        AIMEConstants.INFO_EVENT.getIntegerConstant());
            }
        }

        /**
         * Launch a new indexer process.
         *
         * @param crawlHome The path to the crawling dir
         * @param runner    The runner thread
         * @param exeType   The execution type
         *
         * @throws Exception If the process cannot be done.
         */
        private void index(String crawlHome, SwingWorker<?, ?> runner, Integer exeType) throws Exception
        {
            int result; // Determines the outcome of the function.

            if (runner.isCancelled()) { // If the thread was interrupted, then return.
                return;
            }

            if (LOG.isInfoEnabled()) {
                LOG.info("### Running Process: " + AIMEConstants.INDEXER_JOB_NAME.getStringConstant() + " ###");
            }

            Timer t = new Timer();
            t.starTimer();
            result = Indexer.runProcess(new String[0], runner, exeType); // Run the job and wait the outcome.
            t.endTimer();
            // If outcome is error, then interrupt the thread.
            if (result != 0) {
                runner.cancel(true);
            }
            // Else mark all as finished.
            else {
                markLastFunction(AIMEConstants.INDEX_METHOD_NAME.getStringConstant()); // Mark the last runned function.
                LogEventHandler.addNewEvent(
                        new LogEventHandler(
                                AIMEConstants.INDEXER_JOB_NAME.getStringConstant(),
                                HtmlMessageBuilder.buildFunctionFinishMsg(t)),
                        AIMEConstants.INFO_EVENT.getIntegerConstant());
            }
        }

        /**
         * This method will run a new crawling job, or will resume an old one.
         *
         * @param depth    The depth of the crawler.
         * @param sendmail If it should send notifications by email.
         * @param runner   The thread that launch this method.
         */
        public void runJob(int depth, boolean sendmail, Task runner)
        {
            started = true;
            running = true; // Mark as active.
            timer.starTimer(); // Start the overall timer.
            isCycleComplete = false; // Mark this cycle as uncompleted.

            final int EXECUTION_TYPE = (int) Brain.getClient(new AIMEConfiguration().create()).execute(Handler.makeXMLRequest(BrainXMLData.newBuild().setJob(BrainXMLData.JOB_REQUEST).setClazz(MetadataGeneral.Data.class).setFunction(MetadataGeneral.Data.EXECUTION_TYPE.getMethodName()))).get();
            try {
                if (functionsToExecute.contains(AIMEConstants.INJECTOR_METHOD_NAME.getStringConstant())) {
                    runMethod(
                            AIMEConstants.INJECTOR_METHOD_NAME.getStringConstant(),
                            AIMEConstants.INJECTOR_JOB_NAME.getStringConstant(),
                            new Object[]{
                                AIMEConstants.DEFAULT_JOB_NAME.getStringConstant(),
                                runner,
                                EXECUTION_TYPE
                            });
                }
                runner.reportProgress(16);

                int start = completedIterations;
                for (int i = start; i < (start + ((depth > 0) ? depth : (Integer.MAX_VALUE - start))); i++) {
                    // Store the value of the current depth.
                    currentDepth.set(i + 1);
                    // Start the iteration timer.
                    Timer t = new Timer();
                    t.starTimer();

                    if (functionsToExecute.contains(AIMEConstants.GENERATOR_METHOD_NAME.getStringConstant())) {
                        runMethod(
                                AIMEConstants.GENERATOR_METHOD_NAME.getStringConstant(),
                                AIMEConstants.GENERATOR_JOB_NAME.getStringConstant(),
                                new Object[]{
                                    AIMEConstants.DEFAULT_JOB_NAME.getStringConstant(),
                                    runner,
                                    EXECUTION_TYPE
                                });
                    }
                    runner.reportProgress(32);

                    if (functionsToExecute.contains(AIMEConstants.FETCH_METHOD_NAME.getStringConstant())) {
                        runMethod(
                                AIMEConstants.FETCH_METHOD_NAME.getStringConstant(),
                                AIMEConstants.FETCHER_JOB_NAME.getStringConstant(),
                                new Object[]{
                                    AIMEConstants.DEFAULT_JOB_NAME.getStringConstant(),
                                    runner,
                                    EXECUTION_TYPE
                                });
                    }
                    runner.reportProgress(48);

                    if (functionsToExecute.contains(AIMEConstants.PARSE_METHOD_NAME.getStringConstant())) {
                        runMethod(
                                AIMEConstants.PARSE_METHOD_NAME.getStringConstant(),
                                AIMEConstants.PARSE_JOB_NAME.getStringConstant(),
                                new Object[]{
                                    AIMEConstants.DEFAULT_JOB_NAME.getStringConstant(),
                                    runner,
                                    EXECUTION_TYPE
                                });
                    }
                    runner.reportProgress(64);

                    if (functionsToExecute.contains(AIMEConstants.UPDATE_METHOD_NAME.getStringConstant())) {
                        runMethod(
                                AIMEConstants.UPDATE_METHOD_NAME.getStringConstant(),
                                AIMEConstants.UPDATE_JOB_NAME.getStringConstant(),
                                new Object[]{
                                    AIMEConstants.DEFAULT_JOB_NAME.getStringConstant(),
                                    runner,
                                    EXECUTION_TYPE
                                });
                    }
                    runner.reportProgress(80);

                    if (functionsToExecute.contains(AIMEConstants.INDEX_METHOD_NAME.getStringConstant())) {
                        runMethod(
                                AIMEConstants.INDEX_METHOD_NAME.getStringConstant(),
                                AIMEConstants.INDEXER_JOB_NAME.getStringConstant(),
                                new Object[]{
                                    AIMEConstants.DEFAULT_JOB_NAME.getStringConstant(),
                                    runner,
                                    EXECUTION_TYPE
                                });
                    }
                    runner.reportProgress(100);

                    // Stop the iteration timer.
                    t.endTimer();
                    // Send reports.
                    if (sendmail) {
                        Reports.sendMailReport(new AIMEConfiguration().create(), i, AIMEConstants.DEFAULT_JOB_NAME.getStringConstant(), t.computeOperationTime(Timer.Time.SECOND), 0.0d, 1);
                    }
                    // Update AIME metadata.
                    completedIterations++;
                }

                // Mark this jobs as done.
                isCycleComplete = true;
                lastFunction = AIMEConstants.NO_FUNCTION_METHOD_NAME.getStringConstant();
                // End the overall timer.
                timer.endTimer();
                // Send reports.
                if (sendmail) {
                    Reports.sendMailReport(new AIMEConfiguration().create(), 0, AIMEConstants.DEFAULT_JOB_NAME.getStringConstant(), 0.0d, timer.computeOperationTime(Timer.Time.SECOND), 2);
                }
            }
            catch (Exception e) {
                LOG.fatal(StringUtils.stringifyException(e));
            }
            finally {
                running = false; // Mark as standby.
                started = false;
            }
        }
        // ### NON-DATA FUNCTIONS

        // ### SERIALIZATION FUNCTIONS
        @Override
        protected void internalWrite(DataOutput out) throws IOException
        {
            out.writeInt(functionsToExecute.size());
            for (String function : functionsToExecute) {
                out.writeUTF(function);
            }

            out.writeBoolean(isCycleComplete);
            out.writeInt(completedIterations);
            out.writeUTF(lastFunction);
            out.writeLong(timeLastFunction);
            out.writeInt(currentDepth.get());
            timer.write(out);
            out.writeBoolean(running);
            out.writeBoolean(started);
            out.writeUTF(currentRunningFunction);
            out.writeLong(elapsedTime);
            out.writeBoolean(isFunctionRunning);
            out.writeBoolean(pause);

            out.writeInt(seeds.size());
            for (Entry<String, TreeMap<String, String>> seed : seeds.entrySet()) {
                String key = seed.getKey();
                TreeMap<String, String> value = seed.getValue();

                out.writeUTF(key);
                out.writeInt(value.size());
                for (Entry<String, String> seedData : value.entrySet()) {
                    String k = seedData.getKey();
                    String v = seedData.getValue();

                    out.writeUTF(k);
                    out.writeUTF(v);
                }
            }
        }

        @Override
        protected void internalRead(DataInput in) throws IOException
        {
            int l1 = in.readInt();
            functionsToExecute = new LinkedList<>();
            for (int k = 0; k < l1; k++) {
                functionsToExecute.add(in.readUTF());
            }

            isCycleComplete = in.readBoolean();
            completedIterations = in.readInt();
            lastFunction = in.readUTF();
            timeLastFunction = in.readLong();
            currentDepth = new AtomicInteger(in.readInt());
            timer = Timer.read(in);
            running = in.readBoolean();
            started = in.readBoolean();
            currentRunningFunction = in.readUTF();
            elapsedTime = in.readLong();
            isFunctionRunning = in.readBoolean();
            pause = in.readBoolean();

            int l2 = in.readInt();
            seeds = new TreeMap<>();
            for (int k = 0; k < l2; k++) {
                TreeMap<String, String> seed = new TreeMap<>();
                String key = in.readUTF();
                int length = in.readInt();
                for (int j = 0; j < length; j++) {
                    seed.put(in.readUTF(), in.readUTF());
                }

                seeds.put(key, seed);
            }
        }
        // ### SERIALIZATION FUNCTIONS
    }
}
