package io.aime.metadata;

// Apache Commons
import org.apache.commons.lang.StringUtils;

// Lang
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

// Util
import java.util.HashMap;
import java.util.Map;

/**
 * A decorator to Metadata that adds spellchecking capabilities to property
 * names.
 *
 * <p>Currently used spelling vocabulary contains just the httpheaders from
 * {@link HttpHeaders} class.</p>
 */
public class SpellCheckedMetadata extends DocMetadata {

    /**
     * Treshold divider.
     * <code>threshold = searched.length() / TRESHOLD_DIVIDER;</code>
     */
    private static final int TRESHOLD_DIVIDER = 3;
    /**
     * Normalized name to name mapping.
     */
    private final static Map<String, String> NAMES_IDX = new HashMap<String, String>();
    /**
     * Array holding map keys.
     */
    private static String[] normalized = null;

    static {
        // Uses following array to fill the metanames index and the
        // metanames list.
        Class[] spellthese = {HTTPHeaders.class};

        for (Class spellCheckedNames : spellthese) {
            for (Field field : spellCheckedNames.getFields()) {
                int mods = field.getModifiers();

                if (Modifier.isFinal(mods) && Modifier.isPublic(mods) && Modifier.isStatic(mods) && field.getType().equals(String.class)) {
                    try {
                        String val = (String) field.get(null);
                        NAMES_IDX.put(normalize(val), val);
                    }
                    catch (Exception e) {
                        // Simply ignore...
                    }
                }
            }
        }

        normalized = NAMES_IDX.keySet().toArray(new String[NAMES_IDX.size()]);
    }

    /**
     * Normalizes String.
     *
     * @param str the string to normalize
     *
     * @return normalized String
     */
    private static String normalize(final String str) {
        char c;
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            c = str.charAt(i);

            if (Character.isLetter(c)) {
                buf.append(Character.toLowerCase(c));
            }
        }

        return buf.toString();
    }

    /**
     * Get the normalized name of metadata attribute name.
     *
     * <p>This method tries to find a well-known metadata name (one of the
     * metadata names defined in this class) that matches the specified name.
     * The matching is error tolerent. For instance,
     * <ul>
     * <li>content-type gives Content-Type</li>
     * <li>CoNtEntType gives Content-Type</li>
     * <li>ConTnTtYpe gives Content-Type</li>
     * </ul>
     * If no matching with a well-known metadata name is found, then the
     * original name is returned.</p>
     *
     * @param name Name to normalize
     *
     * @return normalized name
     */
    public static String getNormalizedName(final String name) {
        String searched = normalize(name);
        String value = NAMES_IDX.get(searched);

        if ((value == null) && (normalized != null)) {
            int threshold = searched.length() / TRESHOLD_DIVIDER;

            for (int i = 0; i < normalized.length && value == null; i++) {
                if (StringUtils.getLevenshteinDistance(searched, normalized[i]) < threshold) {
                    value = NAMES_IDX.get(normalized[i]);
                }
            }
        }

        return (value != null) ? value : name;
    }

    @Override
    public void remove(final String name) {
        super.remove(getNormalizedName(name));
    }

    @Override
    public void add(final String name, final String value) {
        super.add(getNormalizedName(name), value);
    }

    @Override
    public String[] getValues(final String name) {
        return super.getValues(getNormalizedName(name));
    }

    @Override
    public String get(final String name) {
        return super.get(getNormalizedName(name));
    }

    @Override
    public void set(final String name, final String value) {
        super.set(getNormalizedName(name), value);
    }
}
