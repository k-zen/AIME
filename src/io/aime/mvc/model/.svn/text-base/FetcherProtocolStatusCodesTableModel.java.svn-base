package io.aime.mvc.model;

import io.aime.brain.Brain;
import io.aime.brain.data.BrainXMLData;
import io.aime.brain.data.MetadataFetcher;
import io.aime.brain.xml.Handler;
import io.aime.util.AIMEConfiguration;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;
import org.apache.hadoop.conf.Configuration;

public final class FetcherProtocolStatusCodesTableModel extends AbstractTableModel
{

    public static final short KEY_COLUMN_ID = 0;
    public static final short VALUE_COLUMN_ID = 1;
    public static final short SUCCESS_ROW_ID = 0;
    public static final short FAILED_ROW_ID = 1;
    public static final short GONE_ROW_ID = 2;
    public static final short MOVED_ROW_ID = 3;
    public static final short TEMP_MOVED_ROW_ID = 4;
    public static final short NOT_FOUND_ROW_ID = 5;
    public static final short RETRY_ROW_ID = 6;
    public static final short EXCEPTION_ROW_ID = 7;
    public static final short ACCESS_DENIED_ROW_ID = 8;
    public static final short ROBOTS_DENIED_ROW_ID = 9;
    public static final short REDIR_EXCEEDED_ROW_ID = 10;
    public static final short NOT_MODIFIED_ROW_ID = 11;
    public static final short WOULD_BLOCK_ROW_ID = 12;
    public static final short BLOCKED_ROW_ID = 13;
    public static final short UNKNOWN_ROW_ID = 14;
    private List<List<Object>> rows = new ArrayList<>();
    private List<String> columns = new ArrayList<>(Arrays.asList("Key", "Value"));
    private Configuration conf = new AIMEConfiguration().create();

    public FetcherProtocolStatusCodesTableModel()
    {
        super();
        MetadataFetcher.Data data = (MetadataFetcher.Data) Brain
                .getInstance()
                .execute(Handler
                        .makeXMLRequest(BrainXMLData
                                .newBuild()
                                .setJob(BrainXMLData.JOB_REQUEST)
                                .setClazz(MetadataFetcher.Data.class)
                                .setFunction("Data"))).get();

        addRow(Arrays.asList(new Object[]{
            "Success", data.getSuccess()
        }));
        addRow(Arrays.asList(new Object[]{
            "Failed", data.getFailed()
        }));
        addRow(Arrays.asList(new Object[]{
            "Gone", data.getGone()
        }));
        addRow(Arrays.asList(new Object[]{
            "Moved", data.getMoved()
        }));
        addRow(Arrays.asList(new Object[]{
            "Temporary Moved", data.getTempMoved()
        }));
        addRow(Arrays.asList(new Object[]{
            "Not Found", data.getNotFound()
        }));
        addRow(Arrays.asList(new Object[]{
            "Retry", data.getRetry()
        }));
        addRow(Arrays.asList(new Object[]{
            "Exception", data.getException()
        }));
        addRow(Arrays.asList(new Object[]{
            "Access Denied", data.getAccessDenied()
        }));
        addRow(Arrays.asList(new Object[]{
            "Robots Denied", data.getRobotsDenied()
        }));
        addRow(Arrays.asList(new Object[]{
            "Redirection Exceeded", data.getRedirExceeded()
        }));
        addRow(Arrays.asList(new Object[]{
            "Not Modified", data.getNotModified()
        }));
        addRow(Arrays.asList(new Object[]{
            "Would Block", data.getWouldBlock()
        }));
        addRow(Arrays.asList(new Object[]{
            "Blocked", data.getBlocked()
        }));
        addRow(Arrays.asList(new Object[]{
            "Unknown", data.getUnknown()
        }));
        new Timer(conf.getInt("fetcher.protocol.status.codes.table.model.refresh", 1000), new DataListener()).start();
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
            default:
                return false;
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

    private void addRow(List<Object> row)
    {
        rows.add(row);
        fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
    }

    private void updateCell(Object newValue, int row, int col)
    {
        rows.get(row).set(col, newValue);
        fireTableCellUpdated(row, col);
    }

    private class DataListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent evt)
        {
            MetadataFetcher.Data data = (MetadataFetcher.Data) Brain
                    .getInstance()
                    .execute(Handler
                            .makeXMLRequest(BrainXMLData
                                    .newBuild()
                                    .setJob(BrainXMLData.JOB_REQUEST)
                                    .setClazz(MetadataFetcher.Data.class)
                                    .setFunction("Data"))).get();

            updateCell(data.getSuccess(), SUCCESS_ROW_ID, VALUE_COLUMN_ID);
            updateCell(data.getFailed(), FAILED_ROW_ID, VALUE_COLUMN_ID);
            updateCell(data.getGone(), GONE_ROW_ID, VALUE_COLUMN_ID);
            updateCell(data.getMoved(), MOVED_ROW_ID, VALUE_COLUMN_ID);
            updateCell(data.getTempMoved(), TEMP_MOVED_ROW_ID, VALUE_COLUMN_ID);
            updateCell(data.getNotFound(), NOT_FOUND_ROW_ID, VALUE_COLUMN_ID);
            updateCell(data.getRetry(), RETRY_ROW_ID, VALUE_COLUMN_ID);
            updateCell(data.getException(), EXCEPTION_ROW_ID, VALUE_COLUMN_ID);
            updateCell(data.getAccessDenied(), ACCESS_DENIED_ROW_ID, VALUE_COLUMN_ID);
            updateCell(data.getRobotsDenied(), ROBOTS_DENIED_ROW_ID, VALUE_COLUMN_ID);
            updateCell(data.getRedirExceeded(), REDIR_EXCEEDED_ROW_ID, VALUE_COLUMN_ID);
            updateCell(data.getNotModified(), NOT_MODIFIED_ROW_ID, VALUE_COLUMN_ID);
            updateCell(data.getWouldBlock(), WOULD_BLOCK_ROW_ID, VALUE_COLUMN_ID);
            updateCell(data.getBlocked(), BLOCKED_ROW_ID, VALUE_COLUMN_ID);
            updateCell(data.getUnknown(), UNKNOWN_ROW_ID, VALUE_COLUMN_ID);
        }
    }
}
