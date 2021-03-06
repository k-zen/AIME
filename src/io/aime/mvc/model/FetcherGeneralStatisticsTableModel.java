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

public final class FetcherGeneralStatisticsTableModel extends AbstractTableModel
{

    public static final short KEY_COLUMN_ID = 0;
    public static final short VALUE_COLUMN_ID = 1;
    public static final short ACTIVE_THREADS_ROW_ID = 0;
    public static final short WAITING_THREADS_ROW_ID = 1;
    public static final short QUEUE_SIZE_ROW_ID = 2;
    public static final short FETCHING_SPEED_ROW_ID = 3;
    public static final short ERRORS_ROW_ID = 4;
    public static final short BANDWIDTH_ROW_ID = 5;
    private List<List<Object>> rows = new ArrayList<>();
    private List<String> columns = new ArrayList<>(Arrays.asList("Key", "Value"));
    private Configuration conf = new AIMEConfiguration().create();

    public FetcherGeneralStatisticsTableModel()
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
            "Active Threads", data.getActiveThreads()
        }));
        addRow(Arrays.asList(new Object[]{
            "Waiting Threads", data.getWaitingThreads()
        }));
        addRow(Arrays.asList(new Object[]{
            "Queue Size", data.getQueueSize()
        }));
        addRow(Arrays.asList(new Object[]{
            "Fetching Speed (Pages/second)",
            Math.round(((float) data.getPages() * 10) / data.getElapsed()) / 10.0
        }));
        addRow(Arrays.asList(new Object[]{
            "Errors", data.getErrors()
        }));
        addRow(Arrays.asList(new Object[]{
            "Bandwidth (Kbps)",
            Math.round((((float) data.getBytes() * 8) / 1024) / data.getElapsed())
        }));
        new Timer(conf.getInt("fetcher.general.statistics.table.model.refresh", 1000), new DataListener()).start();
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

            updateCell(data.getActiveThreads(), ACTIVE_THREADS_ROW_ID, VALUE_COLUMN_ID);
            updateCell(data.getWaitingThreads(), WAITING_THREADS_ROW_ID, VALUE_COLUMN_ID);
            updateCell(data.getQueueSize(), QUEUE_SIZE_ROW_ID, VALUE_COLUMN_ID);
            updateCell(Math.round(((float) data.getPages() * 10) / data.getElapsed()) / 10.0, FETCHING_SPEED_ROW_ID, VALUE_COLUMN_ID);
            updateCell(data.getErrors(), ERRORS_ROW_ID, VALUE_COLUMN_ID);
            updateCell(Math.round((((float) data.getBytes() * 8) / 1024) / data.getElapsed()), BANDWIDTH_ROW_ID, VALUE_COLUMN_ID);
        }
    }
}
