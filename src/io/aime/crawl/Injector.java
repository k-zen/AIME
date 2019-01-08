package io.aime.crawl;

import io.aime.bot.ConsoleMessage;
import io.aime.brain.Brain;
import io.aime.brain.data.BrainXMLData;
import io.aime.brain.data.BrainXMLData.Parameter;
import io.aime.brain.data.MetadataConsole;
import io.aime.brain.data.MetadataGeneral;
import io.aime.brain.xml.Handler;
import io.aime.net.URLFilter;
import io.aime.net.URLFilterException;
import io.aime.net.URLFilters;
import io.aime.net.URLNormalizers;
import io.aime.util.AIMEConfiguration;
import io.aime.util.AIMEConstants;
import io.aime.util.AIMEJob;
import io.aime.util.GeneralUtilities;
import io.aime.util.ProcessKiller;
import io.aime.util.SeedTools;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import javax.swing.SwingWorker;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

/**
 * This class takes a text file without codification, which contains URLs and
 * adds them to the general DBase of the application as records (Web Pages) to
 * be fetched.
 *
 * <p>
 * URLs text file contains the following format:<br/>
 * <ul>
 * <li>One URL per line.</li>
 * <li>Can be followed by metadata separated by tabulations.</li>
 * </ul>
 * Example: http://aime.io \t aime.score=10 \t aime.fetchInterval=2592000 \t
 * userType=open_source
 * </p>
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 */
public class Injector extends Configured implements Tool
{

    private static final Logger LOG = Logger.getLogger(Injector.class.getName());
    private static final String AIME_SCORE_MD_NAME = "aime.score";
    private static final String AIME_FETCH_INTERVAL_MD_NAME = "aime.fetchInterval";

    public Injector()
    {
    }

    public Injector(Configuration conf)
    {
        setConf(conf);
    }

    /**
     * Normalize and filter URLs.
     *
     * <p>
     * Informations to log here:</p>
     * <ul>
     * <li>Successful Injected URLs.</li>
     * <li>Failed injected URLs.</li>
     * <li>Filtered URLs.</li>
     * <li>Score average.</li>
     * </ul>
     */
    public static class InjectMapper implements Mapper<WritableComparable, Text, Text, CrawlDatum>
    {

        private URLNormalizers urlNormalizers;
        private int interval;
        private float scoreInjected;
        private URLFilter[] filters;
        private long curTime;

        @Override
        public void configure(JobConf job)
        {
            urlNormalizers = new URLNormalizers(job, URLNormalizers.SCOPE_INJECT); // Crea un nuevo normalizador de URLs para el alcance "Inject".
            interval = job.getInt("db.fetch.interval.default", 864000); // Busca el valor del intervalo de tiempo entre busquedas.
            filters = (URLFilter[]) Brain
                    .getClient(job)
                    .execute(Handler
                            .makeXMLRequest(BrainXMLData
                                    .newBuild()
                                    .setJob(BrainXMLData.JOB_REQUEST)
                                    .setClazz(MetadataGeneral.Data.class)
                                    .setFunction(MetadataGeneral.Data.FILTERS.getMethodName()))).get();
            scoreInjected = job.getFloat("db.score.injected", 1.0f); // Busca el valor de puntaje para URLs nuevas añadidas.
            curTime = job.getLong("injector.current.time", System.currentTimeMillis()); // Busca el valor de comienzo del proceso.
        }

        @Override
        public void close()
        {
        }

        @Override
        public void map(WritableComparable key, Text value, OutputCollector<Text, CrawlDatum> output, Reporter reporter) throws IOException
        {
            String url = value.toString(); // La URL y sus valores correspondientes.

            // Obtener los valores para la URL.
            float customScore = -1f;
            int customInterval = interval;
            Map<String, String> metadata = new TreeMap<>(); // Arreglo que contiene los valores extras. Key -> MetaName, Value -> MetaValue

            if (url.contains("\t")) { // Chequear si contiene valores extras.
                String[] splits = url.split("\t"); // Separar los valores.
                url = splits[0]; // Solo la URL.

                // Iterar a traves de los valores.
                for (int s = 1; s < splits.length; s++) {
                    // find separation between name and value
                    int indexEquals = splits[s].indexOf("=");

                    // Saltar cualquier URL que no cumpla con la norma propiedad=valor. Ej. http://aime.io \t aime.score1000 -> Esta URL sera salteada.
                    if (indexEquals == -1) {
                        continue;
                    }

                    String metaname = splits[s].substring(0, indexEquals); // El nombre de la propiedad.
                    String metavalue = splits[s].substring(indexEquals + 1); // El valor de la propiedad.
                    switch (metaname) {
                        case Injector.AIME_SCORE_MD_NAME:
                            try {
                                customScore = Float.parseFloat(metavalue);
                            }
                            catch (NumberFormatException nfe) {
                                LOG.warn("Error trying to parse floating point \"" + Injector.AIME_SCORE_MD_NAME + "\" for URL: " + url, nfe);
                            }
                            break;
                        case Injector.AIME_FETCH_INTERVAL_MD_NAME:
                            try {
                                customInterval = Integer.parseInt(metavalue);
                            }
                            catch (NumberFormatException nfe) {
                                LOG.warn("Error trying to parse integer \"" + Injector.AIME_FETCH_INTERVAL_MD_NAME + "\" for URL: " + url, nfe);
                            }
                            break;
                        default:
                            // Guardar los valores extras si son de otro tipo.
                            metadata.put(metaname, metavalue);
                            break;
                    }
                }
            }

            try {
                url = urlNormalizers.normalize(url, URLNormalizers.SCOPE_INJECT); // Normaliza la URL.
                url = URLFilters.filter(filters, url); // Filtra la URL.
            }
            catch (MalformedURLException | URLFilterException ex) {
                LOG.warn("Skipping: " + url, ex);
                url = null;
            }

            if (url != null) { // Si pasa los filtros.
                value.set(url);
                CrawlDatum datum = new CrawlDatum(CrawlDatum.STATUS_INJECTED, customInterval);
                datum.setFetchTime(curTime);

                // Añadir el MetaData.
                Iterator<String> keysIter = metadata.keySet().iterator();
                while (keysIter.hasNext()) {
                    String keymd = keysIter.next();
                    String valuemd = metadata.get(keymd);
                    // Guardar en un MapWritable nuevo los sgtes. valores:
                    // Key   -> Nombre de la propiedad.
                    // Value -> Valor de la propiedad.
                    datum.getMetadata().put(new Text(keymd), new Text(valuemd));
                }

                // Guardar el puntaje inicial de la URL.
                if (customScore != -1) {
                    datum.setScore(customScore);
                }
                else {
                    datum.setScore(scoreInjected);
                }

                // Deshabilitado computador de puntajes.
                // try {
                //     scfilters.injectedScore(value, datum);
                // }
                // catch (ScoringFilterException ex) {
                //     LOG.warn("Imposible asignar puntaje para URL -> " + url, ex);
                // }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Map data:");
                    LOG.debug("+ Key: " + value);
                    LOG.debug("+ Value: " + datum.toString());
                }

                output.collect(value, datum); // Enviar el resultado al colector.
            }
        }
    }

    /**
     * Combinar multiples entradas para una URL.
     */
    public static class InjectReducer implements Reducer<Text, CrawlDatum, Text, CrawlDatum>
    {

        private CrawlDatum old = new CrawlDatum();
        private CrawlDatum injected = new CrawlDatum();

        @Override
        public void configure(JobConf job)
        {
        }

        @Override
        public void close()
        {
        }

        @Override
        public void reduce(Text key, Iterator<CrawlDatum> values, OutputCollector<Text, CrawlDatum> output, Reporter reporter) throws IOException
        {
            boolean oldSet = false;

            // Iterar a traves de los valores.
            while (values.hasNext()) {
                CrawlDatum val = values.next();

                // Si el status es "inyected".
                if (val.getStatus() == CrawlDatum.STATUS_INJECTED) {
                    injected.set(val); // Copiar los valores de esta instancia en otra nueva.
                    injected.setStatus(CrawlDatum.STATUS_DB_UNFETCHED); // Cambiar el estado de "66 - inyected" a "3 - db_unfetched".
                }
                else {
                    old.set(val);
                    oldSet = true;
                }
            }

            CrawlDatum res;
            if (oldSet) {
                res = old; // No sobreescribir valores existentes.
            }
            else {
                res = injected;

                if (LOG.isInfoEnabled()) {
                    LOG.info("Adding new URL: " + GeneralUtilities.breakString(key.toString(), 0, "\n"));
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Reducer data:");
                LOG.debug("+ Key: " + key);
                LOG.debug("+ Value: " + res.toString());
            }

            output.collect(key, res); // Enviar el resultado al colector.
        }
    }

    /**
     * Inject URLs into the general DBase of the application.
     *
     * @param crawlDb Path to DBase.
     * @param urlDir  Path to URLs directory.
     */
    public void inject(Path crawlDb, Path urlDir)
    {
        final int EXECUTION_TYPE = (int) Brain
                .getClient(getConf())
                .execute(Handler
                        .makeXMLRequest(BrainXMLData
                                .newBuild()
                                .setJob(BrainXMLData.JOB_REQUEST)
                                .setClazz(MetadataGeneral.Data.class)
                                .setFunction(MetadataGeneral.Data.EXECUTION_TYPE.getMethodName()))).get();

        if (LOG.isInfoEnabled()) {
            LOG.info("DBase: " + crawlDb.toString());
            LOG.info("URLs Directory: " + urlDir.toString());
            LOG.info("Hadoop Tmp Dir: " + getConf().get("hadoop.tmp.dir", "."));
        }

        // Export seeds to file for FileInputFormat.
        SeedTools.prepareFile(AIMEConstants.SEEDS_FILE_PATH.getStringConstant(), EXECUTION_TYPE);

        // Notify the console.
        Brain
                .getClient(getConf())
                .execute(Handler
                        .makeXMLRequest(
                                BrainXMLData
                                .newBuild()
                                .setJob(BrainXMLData.JOB_MERGE)
                                .setClazz(MetadataConsole.Data.class)
                                .setFunction(MetadataConsole.Data.MESSAGE.getMethodName())
                                .setParam(Parameter
                                        .newBuild()
                                        .setType(ConsoleMessage.class)
                                        .setData(ConsoleMessage.newBuild().setSeverity(ConsoleMessage.INFO).setMessage("Exporting seeds to file for Injector's input format!")))));

        // Create a temporary DBase in a temporary location.
        Path tempDir = new Path(getConf().get("hadoop.tmp.dir", ".") + "/inject-temp-" + Integer.toString(new Random().nextInt(Integer.MAX_VALUE)));

        if (LOG.isInfoEnabled()) {
            LOG.info("Creating temporary DBase: " + tempDir.toString());
        }

        JobConf sortJob;
        try {
            sortJob = new AIMEJob(getConf());
            sortJob.setJobName(AIMEConstants.INJECTOR_JOB_NAME.getStringConstant());
            FileInputFormat.addInputPath(sortJob, urlDir);
            sortJob.setMapperClass(InjectMapper.class);
            FileOutputFormat.setOutputPath(sortJob, tempDir);
            sortJob.setOutputFormat(SequenceFileOutputFormat.class);
            sortJob.setOutputKeyClass(Text.class);
            sortJob.setOutputValueClass(CrawlDatum.class);
            sortJob.setLong("injector.current.time", System.currentTimeMillis());
            JobClient.runJob(sortJob); // RUN
        }
        catch (IOException e) {
            LOG.fatal("Fatal error creating temporary DBase.", e);
        }

        // Mix the temporary DBase with the current one.
        if (LOG.isInfoEnabled()) {
            LOG.info("Mixing DBases: " + tempDir.toString() + " <==> " + crawlDb.toString());
        }

        JobConf mergeJob = null;
        try {
            mergeJob = CrawlDB.createJob(getConf(), crawlDb);
            FileInputFormat.addInputPath(mergeJob, tempDir);
            mergeJob.setReducerClass(InjectReducer.class);
            JobClient.runJob(mergeJob); // RUN
        }
        catch (IOException e) {
            LOG.fatal("Fatal error creating DBase merging job.", e);
        }

        // Install the new merged DBase.
        try {
            CrawlDB.install(mergeJob, crawlDb);
        }
        catch (IOException e) {
            LOG.fatal("Fatal error installing new DBase.", e);
        }

        // Delete all temporary directories.
        if (LOG.isInfoEnabled()) {
            LOG.info("Cleaning temporary DBase: " + tempDir.toString());
        }

        try {
            FileSystem fs = FileSystem.get(getConf());
            fs.delete(tempDir, true);
        }
        catch (IOException e) {
            LOG.fatal("Fatal error cleaning up.", e);
        }
    }

    public static int runProcess(String[] args, SwingWorker<?, ?> runner) throws Exception
    {
        Configuration conf = new AIMEConfiguration().create();
        return ProcessKiller.checkExitCode(
                ToolRunner.run(conf, new Injector(), args),
                AIMEConstants.INJECTOR_JOB_NAME.getStringConstant(),
                runner,
                conf);
    }

    @Override
    public int run(String[] args) throws Exception
    {
        try {
            inject(new Path(args[0]), new Path(args[1]));
            return 0;
        }
        catch (Exception e) {
            LOG.fatal(StringUtils.stringifyException(e));
            return -1;
        }
    }
}
