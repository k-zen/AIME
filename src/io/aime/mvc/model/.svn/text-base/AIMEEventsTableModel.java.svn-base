package io.aime.mvc.model;

import io.aime.util.AIMEConstants;
import io.aime.util.LogEventHandler;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;
import org.apache.lucene.document.DateTools;
import org.ocpsoft.pretty.time.PrettyTime;

public final class AIMEEventsTableModel extends AbstractTableModel
{

    public static final byte ID_COLUMN_ID = 0x0;
    public static final byte TIME_COLUMN_ID = 0x1;
    public static final byte STATUS_COLUMN_ID = 0x2;
    private List<String> columns = new ArrayList<>(Arrays.asList("ID", "Time", "Status"));
    private List<List<Object>> rows = new ArrayList<>();

    public AIMEEventsTableModel()
    {
        super();
        new Timer(1000, new DataListener()).start();
    }

    @Override
    public String getColumnName(int col)
    {
        return columns.get(col);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
        switch (columnIndex)
        {
            case STATUS_COLUMN_ID:
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
        if (columnIndex < rowList.size())
        {
            return rowList.get(columnIndex);
        }

        return null;
    }

    @Override
    public boolean isCellEditable(int row, int col)
    {
        switch (col)
        {
            default:
                return false;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col)
    {
        List<Object> rowList = (List<Object>) rows.get(row);

        if (col >= rowList.size())
        {
            while (col >= rowList.size())
            {
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

    private class DataListener implements ActionListener
    {

        PrettyTime dateFormatter = new PrettyTime(new Locale("en"));

        JLabel getNewLabel(String text, String tooltip, boolean opaque, int hPosition)
        {
            JLabel l = new JLabel();
            l.setText(text);
            l.setOpaque(opaque);
            l.setHorizontalAlignment(hPosition);
            l.setToolTipText(tooltip);

            return l;
        }

        @Override
        public void actionPerformed(ActionEvent evt)
        {
            rows.clear(); // Clear all rows.

            Map<String, TreeMap<Integer, LogEventHandler>> evts = LogEventHandler.getAllEventMessages();
            synchronized (evts)
            {
                Iterator<String> it = evts.keySet().iterator();
                while (it.hasNext())
                {
                    final String key = it.next();
                    final TreeMap<Integer, LogEventHandler> info = evts.get(key);

                    String date;
                    try
                    {
                        date = dateFormatter.format(DateTools.stringToDate(DateTools.timeToString(LogEventHandler.getEventDate(key), DateTools.Resolution.MILLISECOND)));
                    }
                    catch (ParseException e)
                    {
                        date = new Date().toString();
                    }
                    JLabel type;
                    if (info.firstKey() == AIMEConstants.WARNING_EVENT.getIntegerConstant())
                    {
                        type = getNewLabel("WARNING", "WARNING", true, SwingConstants.CENTER);
                        type.setBackground(Color.YELLOW);
                        type.setForeground(Color.BLACK);
                    }
                    else if (info.firstKey() == AIMEConstants.ERROR_EVENT.getIntegerConstant())
                    {
                        type = getNewLabel("ERROR", "ERROR", true, SwingConstants.CENTER);
                        type.setBackground(Color.RED);
                        type.setForeground(Color.BLACK);
                    }
                    else
                    {
                        type = getNewLabel("OK", "OK", true, SwingConstants.CENTER);
                        type.setBackground(Color.GREEN);
                        type.setForeground(Color.BLACK);
                    }

                    // Add row.
                    addRow(Arrays.asList(new Object[]
                    {
                        LogEventHandler.getEventTitle(key),
                        date,
                        type
                    }
                    ));
                }
            }
        }
    }
}
