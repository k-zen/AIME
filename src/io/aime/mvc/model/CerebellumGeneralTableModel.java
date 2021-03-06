package io.aime.mvc.model;

import io.aime.brain.Brain;
import io.aime.brain.data.BrainXMLData;
import io.aime.brain.data.MetadataGeneral;
import io.aime.brain.xml.Handler;
import io.aime.util.AIMEConfiguration;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;

public final class CerebellumGeneralTableModel extends AbstractTableModel
{

    public static final byte KEY_COLUMN_ID = 0x0;
    public static final byte VALUE_COLUMN_ID = 0x1;
    public static final byte HELP_COLUMN_ID = 0x2;
    public static final byte CONN_TIME_ROW_ID = 0x0;
    private List<List<Object>> rows = new ArrayList<>();
    private List<String> columns = new ArrayList<>(Arrays.asList("Updated Value", "Value", "Help"));

    public CerebellumGeneralTableModel()
    {
        super();

        final Long SUM_CONNECT_TIME = (Long) Brain
                .getInstance()
                .execute(Handler
                        .makeXMLRequest(BrainXMLData
                                .newBuild()
                                .setJob(BrainXMLData.JOB_REQUEST)
                                .setClazz(MetadataGeneral.Data.class)
                                .setFunction(MetadataGeneral.Data.SUM_CONN_TIME.getMethodName()))).get();
        final Long COU_CONNECT_TIME = (Long) Brain
                .getInstance()
                .execute(Handler
                        .makeXMLRequest(BrainXMLData
                                .newBuild()
                                .setJob(BrainXMLData.JOB_REQUEST)
                                .setClazz(MetadataGeneral.Data.class)
                                .setFunction(MetadataGeneral.Data.COU_CONN_TIME.getMethodName()))).get();

        addRow(Arrays.asList(new Object[]{
            "Avg. Conn. Time:",
            COU_CONNECT_TIME > 0 ? (SUM_CONNECT_TIME / COU_CONNECT_TIME) : 0,
            new JLabel("?", SwingConstants.CENTER)
        }));

        new Timer(new AIMEConfiguration().create().getInt("aime.cerebellum.table.model.refresh", 1000), new DataListener()).start();
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
            case HELP_COLUMN_ID:
                return JLabel.class;
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
            case KEY_COLUMN_ID:
            case VALUE_COLUMN_ID:
            case HELP_COLUMN_ID:
                return false;
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

    private int addRow(List<Object> row)
    {
        rows.add(row);
        fireTableRowsInserted(rows.size() - 1, rows.size() - 1);

        return (rows.size() - 1);
    }

    private int updateCell(Object newValue, int row, int col)
    {
        rows.get(row).set(col, newValue);
        fireTableCellUpdated(row, col);

        return (rows.size() - 1);
    }

    private class DataListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent evt)
        {
            final Long SUM_CONNECT_TIME = (Long) Brain
                    .getInstance()
                    .execute(Handler
                            .makeXMLRequest(BrainXMLData
                                    .newBuild()
                                    .setJob(BrainXMLData.JOB_REQUEST)
                                    .setClazz(MetadataGeneral.Data.class)
                                    .setFunction(MetadataGeneral.Data.SUM_CONN_TIME.getMethodName()))).get();
            final Long COU_CONNECT_TIME = (Long) Brain
                    .getInstance()
                    .execute(Handler
                            .makeXMLRequest(BrainXMLData
                                    .newBuild()
                                    .setJob(BrainXMLData.JOB_REQUEST)
                                    .setClazz(MetadataGeneral.Data.class)
                                    .setFunction(MetadataGeneral.Data.COU_CONN_TIME.getMethodName()))).get();

            updateCell(COU_CONNECT_TIME > 0 ? (SUM_CONNECT_TIME / COU_CONNECT_TIME) : 0, CONN_TIME_ROW_ID, VALUE_COLUMN_ID);
        }
    }
}
