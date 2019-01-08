package io.aime.mvc.model;

import io.aime.bot.ConsoleMessage;
import io.aime.brain.Brain;
import io.aime.brain.data.BrainXMLData;
import io.aime.brain.data.MetadataConsole;
import io.aime.brain.xml.Handler;
import io.aime.util.AIMEConfiguration;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;

public final class AIMEBotConsoleTableModel extends AbstractTableModel
{

    public static final byte TYPE_COLUMN_ID = 0x0;
    public static final byte MESSAGE_COLUMN_ID = 0x1;
    private final DataListener LISTENER = new DataListener();
    private final Timer TIMER = new Timer(
            new AIMEConfiguration().create().getInt("aime.botconsole.table.model.refresh", 1000),
            LISTENER);
    private List<String> columns = new ArrayList<>(Arrays.asList("Type", "Message"));
    private List<List<Object>> rows = new ArrayList<>();

    public AIMEBotConsoleTableModel()
    {
        super();
        start();
    }

    @Override
    public String getColumnName(int col)
    {
        return columns.get(col);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
        switch (columnIndex) {
            case TYPE_COLUMN_ID:
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
        List<Object> rowList = (List<Object>) rows.get(rowIndex);
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
        List<Object> rowList = (List<Object>) rows.get(row);

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

        return (this.rows.size() - 1);
    }

    public void start()
    {
        TIMER.start();
    }

    public void stop()
    {
        TIMER.stop();
    }

    public void showAll()
    {
        ConsoleMessage[] msgs = (ConsoleMessage[]) Brain
                .getInstance()
                .execute(Handler
                        .makeXMLRequest(BrainXMLData
                                .newBuild()
                                .setJob(BrainXMLData.JOB_REQUEST)
                                .setClazz(MetadataConsole.Data.class)
                                .setFunction(MetadataConsole.Data.ALL_MESSAGES.getMethodName()))).get();
        for (ConsoleMessage msg : msgs) {
            LISTENER.insertEvent(msg);
        }
    }

    private class DataListener implements ActionListener
    {

        JLabel getNewLabel(String text, String tooltip, boolean opaque, int hPosition)
        {
            JLabel l = new JLabel();
            l.setText(text);
            l.setOpaque(opaque);
            l.setHorizontalAlignment(hPosition);
            l.setToolTipText(tooltip);

            return l;
        }

        void insertEvent(ConsoleMessage evt)
        {
            if (!evt.isEmpty()) {
                JLabel l = null;
                switch (evt.getSeverity()) {
                    case ConsoleMessage.INFO:
                        l = getNewLabel("INFO", "Information", true, SwingConstants.CENTER);
                        l.setBackground(Color.GREEN);
                        l.setForeground(Color.BLACK);
                        break;
                    case ConsoleMessage.WARN:
                        l = getNewLabel("WARN", "Warning", true, SwingConstants.CENTER);
                        l.setBackground(Color.YELLOW);
                        l.setForeground(Color.BLACK);
                        break;
                    case ConsoleMessage.ERROR:
                        l = getNewLabel("ERROR", "Error", true, SwingConstants.CENTER);
                        l.setBackground(Color.RED);
                        l.setForeground(Color.BLACK);
                        break;
                }
                addRow(Arrays.asList(new Object[]{
                    l, evt.getMessage()
                }
                ));
            }
        }

        @Override
        public void actionPerformed(ActionEvent evt)
        {
            insertEvent((ConsoleMessage) Brain
                    .getInstance()
                    .execute(Handler
                            .makeXMLRequest(BrainXMLData
                                    .newBuild()
                                    .setJob(BrainXMLData.JOB_REQUEST)
                                    .setClazz(MetadataConsole.Data.class)
                                    .setFunction(MetadataConsole.Data.MESSAGE.getMethodName()))).get());
        }
    }
}
