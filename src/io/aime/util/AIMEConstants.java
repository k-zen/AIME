package io.aime.util;

import org.apache.hadoop.io.Text;

/**
 * Enumerations that are used inside other classes and methods in AIME.
 *
 * @author K-Zen
 */
public enum AIMEConstants
{

    AIME_KERNEL_INDEX_DIR_NAME("index"),
    AIME_CRAWLDB_DIR_NAME("webdb"),
    AIME_SEGMENTS_DIR_NAME("segments"),
    KERNEL_SEGMENTS_COMPRESSED_FILE_NAME("LastSegment.zip"),
    DEFAULT_JOB_PATH("/AIME"),
    DEFAULT_JOB_NAME(DEFAULT_JOB_PATH.getStringConstant() + "/" + "AIMEJob"),
    DEFAULT_JOB_DATA_FOLDER(DEFAULT_JOB_PATH.getStringConstant() + "/" + "data"),
    KERNEL_INDEX_PATH("/AIME/kernel/" + AIME_KERNEL_INDEX_DIR_NAME.getStringConstant()),
    KERNEL_SEGMENTS_PATH("/AIME/kernel/" + AIME_SEGMENTS_DIR_NAME.getStringConstant()),
    KERNEL_SEGMENTS_COMPRESSED_FILE_PATH(KERNEL_SEGMENTS_PATH.getStringConstant() + "/" + KERNEL_SEGMENTS_COMPRESSED_FILE_NAME.getStringConstant()),
    MAINDBASE_PATH(DEFAULT_JOB_PATH.getStringConstant() + "/" + "AIMEJob/" + AIME_CRAWLDB_DIR_NAME.getStringConstant()),
    SEGMENTDBASE_PATH(DEFAULT_JOB_PATH.getStringConstant() + "/" + "AIMEJob/" + AIME_SEGMENTS_DIR_NAME.getStringConstant()),
    LOGS_PATH("/AIME/logs"),
    TMP_PATH("/AIME/tmp"),
    SEEDS_FILE_PATH(TMP_PATH.getStringConstant() + "/urls.file"),
    // Job names
    INJECTOR_JOB_NAME("Data-Injection"), // The full name of the injection process.
    GENERATOR_JOB_NAME("Data-Generation"), // The full name of the generation process.
    FETCHER_JOB_NAME("Data-Fetching"), // The full name of the fetching process.
    PARSE_JOB_NAME("Data-Parsing"), // The full name of the document parsing process.
    UPDATE_JOB_NAME("Data-Updating"), // The full name of the main dbase update process.
    INDEXER_JOB_NAME("Data-Indexing"), // The full name of the indexing process.
    // Messages.
    NOT_AVAILABLE("N/A"),
    // Miscellaneous.
    DEFAULT_ELLIPSIS("..."),
    // Eventos.
    INFO_EVENT(1),
    WARNING_EVENT(2),
    ERROR_EVENT(3),
    // Estados del Kernel.
    KERNEL_STANDBY(1),
    KERNEL_RUNNING(2),
    // Execution type values:
    LOCAL_EXECUTION_TYPE(1),
    DISTRIBUTED_EXECUTION_TYPE(2),
    // Misc.
    AIME_VERSION("0.2"),
    DEFAULT_STRING_SEPARATOR("!@#"),
    // Window Messages:
    JOB_IS_RUNNING_MSG("Job is already running!"),
    EMPTY_DEPTH_FIELD_MSG("Depth field cannot be empty!"),
    JOB_FINISH_MSG("The job has concluded, visit the events table for more information."),
    NO_RUNNING_JOBS_MSG("There are no running jobs."),
    JOB_PAUSED_MSG("Job paused!<br/><u>Note:</u> The job will be paused on the next crawling task. The execution cannot be paused on the current task."),
    JOB_RUNNING_EXIT_MSG("There are jobs running. Pause them first!"),
    JOB_RESUMED_MSG("Job resumed!"),
    JOB_NOT_STARTED_MSG("The job was never started!"),
    ONLY_NUMERIC_CHARS_MSG("Only numeric characters are allowed!"),
    // Function methods names:
    INJECTOR_METHOD_NAME("injection"),
    GENERATOR_METHOD_NAME("generate"),
    FETCH_METHOD_NAME("fetch"),
    PARSE_METHOD_NAME("parse"),
    UPDATE_METHOD_NAME("update"),
    INDEX_METHOD_NAME("index"),
    NO_FUNCTION_METHOD_NAME("none"),
    // Metadata files
    METADATA_ENCRYPT(false),
    METADATA_KEY("tictactoe1234567"),
    METADATA_ENCODING("UTF-8"),
    METADATA_CRAWLJOB_FILENAME("0001.aime"),
    METADATA_GENERAL_FILENAME("0002.aime"),
    METADATA_FETCHER_FILENAME("0003.aime"),
    METADATA_CONSOLE_FILENAME("0004.aime"),
    // AIME Internals Misc
    ORIGINAL_CHAR_ENCODING("OriginalCharEncoding"),
    CHAR_ENCODING_FOR_CONVERSION("CharEncodingForConversion"),
    SIGNATURE_KEY("aime.content.digest"),
    SEGMENT_NAME_KEY("aime.segment.name"),
    SCORE_KEY("aime.crawl.score"),
    GENERATE_TIME_KEY("_ngt_"),
    WRITABLE_GENERATE_TIME_KEY(new Text(GENERATE_TIME_KEY.getStringConstant())),
    PROTO_STATUS_KEY("_pst_"),
    WRITABLE_PROTO_STATUS_KEY(new Text(PROTO_STATUS_KEY.getStringConstant())),
    FETCH_TIME_KEY("_ftk_"),
    FETCH_STATUS_KEY("_fst_"),
    CACHING_FORBIDDEN_KEY("caching.forbidden"), // Sites may request that search engines don't provide access to cached documents.
    CACHING_FORBIDDEN_NONE("none"), // Show both original forbidden content and summaries (default).
    CACHING_FORBIDDEN_ALL("all"), // Don't show either original forbidden content or summaries.
    CACHING_FORBIDDEN_CONTENT("content"), // Don't show original forbidden content, but show summaries.
    REPR_URL_KEY("_repr_"),
    WRITABLE_REPR_URL_KEY(new Text(REPR_URL_KEY.getStringConstant())),
    URLFILTER_ORDER("urlfilter.order"), // Name of the option containing the order of URL filters.
    // Character Encoding
    DEFAULT_CHAR_ENCODING("UTF-8"),
    METADATA_DEFAULTS_IS_CYCLE_COMPLETE(false),
    METADATA_DEFAULTS_COMPLETED_ITERATIONS(0),
    METADATA_DEFAULTS_LAST_FUNCTION("none"),
    METADATA_DEFAULTS_TIME_LAST_FUNCTION(0L);
    // Valor de la constante.
    private String c1 = "";
    private int c2 = 0;
    private boolean c3 = false;
    private Text c4 = new Text("");
    private long c5 = 0L;

    /**
     * Constructor of values.
     *
     * @param constant The value of the constant.
     */
    private AIMEConstants(String constant)
    {
        this.c1 = constant;
    }

    private AIMEConstants(int constant)
    {
        this.c2 = constant;
    }

    private AIMEConstants(boolean constant)
    {
        this.c3 = constant;
    }

    private AIMEConstants(Text constant)
    {
        this.c4 = constant;
    }

    private AIMEConstants(Long constant)
    {
        this.c5 = constant;
    }

    /**
     * Returns the value of the constant.
     *
     * @return The value of the constant.
     */
    public final String getStringConstant()
    {
        return c1;
    }

    /**
     * Returns the value of the constant.
     *
     * @return The value of the constant.
     */
    public final int getIntegerConstant()
    {
        return c2;
    }

    /**
     * Returns the value of the constant.
     *
     * @return The value of the constant.
     */
    public final boolean getBooleanConstant()
    {
        return c3;
    }

    /**
     * Returns the value of the constant.
     *
     * @return The value of the constant.
     */
    public final Text getTextConstant()
    {
        return c4;
    }

    /**
     * Returns the value of the constant.
     *
     * @return The value of the constant.
     */
    public final long getLongConstant()
    {
        return c5;
    }
}
