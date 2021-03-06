package io.aime.mvc.model;

import io.aime.brain.Brain;
import io.aime.brain.data.BrainXMLData;
import io.aime.brain.data.MetadataGeneral;
import io.aime.brain.xml.Handler;
import io.aime.net.URLFilter;
import io.aime.plugins.urlfilterregex.RegexRule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Model class for the table that shows the available URL filter rules.
 *
 * @author K-Zen
 */
public final class FilterURLRuleTableModel extends AbstractTableModel
{

    public static final byte PATTERN_COLUMN_ID = 0x0;
    public static final byte SIGN_COLUMN_ID = 0x1;
    public static final byte ACTION_COLUMN_ID = 0x2;
    private List<List<Object>> rows = new ArrayList<>();
    private List<String> columns = new ArrayList<>(Arrays.asList("Rule", "Allow", "Remove"));
    private List<Integer> defRulesMarker = new ArrayList<>(); // Mark which rows are default rules, because this one can't be edited.

    public FilterURLRuleTableModel()
    {
        super();
        // Load the table with default URL rules + URL rules.
        RegexRule[] defRules = (RegexRule[]) Brain
                .getInstance()
                .execute(Handler
                        .makeXMLRequest(BrainXMLData
                                .newBuild()
                                .setJob(BrainXMLData.JOB_REQUEST)
                                .setClazz(MetadataGeneral.Data.class)
                                .setFunction(MetadataGeneral.Data.DEFAULT_URL_RULE.getMethodName()))).get();
        for (RegexRule rule : defRules) {
            List<Object> row = new ArrayList<>();
            row.add(rule.getPattern());
            row.add(rule.getSign());
            row.add(false);
            defRulesMarker.add(addRow(row));
        }

        RegexRule[] cusRules = (RegexRule[]) Brain
                .getInstance()
                .execute(Handler
                        .makeXMLRequest(BrainXMLData
                                .newBuild()
                                .setJob(BrainXMLData.JOB_REQUEST)
                                .setClazz(MetadataGeneral.Data.class)
                                .setFunction(MetadataGeneral.Data.URL_RULE.getMethodName()))).get();
        for (RegexRule rule : cusRules) {
            List<Object> row = new ArrayList<>();
            row.add(rule.getPattern());
            row.add(rule.getSign());
            row.add(false);
            addRow(row);
        }
    }

    @Override
    public String getColumnName(int col)
    {
        return (String) columns.get(col);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
        switch (columnIndex) {
            case SIGN_COLUMN_ID:
                return Boolean.class;
            case ACTION_COLUMN_ID:
                return Boolean.class;
            default:
                return String.class;
        }
    }

    @Override
    public int getColumnCount()
    {
        return columns.size();
    }

    @Override
    public int getRowCount()
    {
        return rows.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        List rowList = (List) rows.get(rowIndex);
        if (columnIndex < rowList.size()) {
            return rowList.get(columnIndex);
        }

        return null;
    }

    @Override
    public boolean isCellEditable(int row, int col)
    {
        switch (col) {
            case PATTERN_COLUMN_ID:
                return false;
            case SIGN_COLUMN_ID:
                return false;
            default:
                if (defRulesMarker.contains(row)) {
                    return false;
                }

                return true;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col)
    {
        List rowList = (List) rows.get(row);

        if (col >= rowList.size()) {
            while (col >= rowList.size()) {
                rowList.add(null);
            }
        }

        rowList.set(col, value);
        fireTableCellUpdated(row, col);
    }

    private int addRow(List<Object> row)
    {
        rows.add(row);
        fireTableRowsInserted(rows.size() - 1, rows.size() - 1);

        return (rows.size() - 1);
    }

    public int addNewRow(List<Object> row)
    {
        URLFilter[] filters = (URLFilter[]) Brain
                .getInstance()
                .execute(Handler
                        .makeXMLRequest(BrainXMLData
                                .newBuild()
                                .setJob(BrainXMLData.JOB_REQUEST)
                                .setClazz(MetadataGeneral.Data.class)
                                .setFunction(MetadataGeneral.Data.FILTERS.getMethodName()))).get();
        for (URLFilter fil : filters) {
            switch (fil.getType()) {
                case URLFilter.REGEX_URL_FILTER:
                    if (fil.addURLRule((Boolean) row.get(SIGN_COLUMN_ID), row.get(PATTERN_COLUMN_ID).toString())) {
                        rows.add(row);
                    }
                    break;
            }
        }
        fireTableRowsInserted(rows.size() - 1, rows.size() - 1);

        return (rows.size() - 1);
    }

    public void deleteRow(int row)
    {
        if (row == -1) {
            return;
        }

        URLFilter[] filters = (URLFilter[]) Brain
                .getInstance()
                .execute(Handler
                        .makeXMLRequest(BrainXMLData
                                .newBuild()
                                .setJob(BrainXMLData.JOB_REQUEST)
                                .setClazz(MetadataGeneral.Data.class)
                                .setFunction(MetadataGeneral.Data.FILTERS.getMethodName()))).get();
        for (URLFilter fil : filters) {
            switch (fil.getType()) {
                case URLFilter.REGEX_URL_FILTER:
                    if (fil.removeURLRule((Boolean) rows.get(row).get(SIGN_COLUMN_ID), rows.get(row).get(PATTERN_COLUMN_ID).toString())) {
                        rows.remove(row);
                        fireTableRowsDeleted(row, row);
                    }
                    break;
            }
        }
    }
}
