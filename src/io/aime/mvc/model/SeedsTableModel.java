package io.aime.mvc.model;

import io.aime.util.SeedTools;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;

/**
 * Model class for the table that shows the available site filter rules.
 * <br/>
 * <p>
 * Table Model Structure:</p>
 * <pre>
 * List => Index 0 => List => Index 0 => String  [URL]
 *                            Index 1 => Boolean [Remove]
 * </pre>
 *
 * @author K-Zen
 */
public final class SeedsTableModel extends AbstractTableModel
{

    public static final byte SEED_COLUMN_ID = 0x0;
    public static final byte REMOVE_COLUMN_ID = 0x1;
    private List<List<Object>> rows = new ArrayList<>();
    private List<String> columns = new ArrayList<>(Arrays.asList("Seed", "Remove"));

    public SeedsTableModel()
    {
        super();
        new Timer(1000, new DataListener()).start();
    }

    @Override
    public String getColumnName(int col)
    {
        return (String) columns.get(col);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
        switch (columnIndex)
        {
            case REMOVE_COLUMN_ID:
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
            case SEED_COLUMN_ID:
                return false;
            default:
                return true;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col)
    {
        List rowList = (List) rows.get(row);

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
        synchronized (rows)
        {
            // Check all rows if this URL is not there.
            for (List r : rows)
            {
                if (r.get(SEED_COLUMN_ID).equals(row.get(SEED_COLUMN_ID)))
                {
                    return -1;
                }
            }

            if (!rows.contains(row))
            {
                rows.add(row);
                fireTableRowsInserted(rows.size() - 1, rows.size() - 1);

                return (rows.size() - 1);
            }
            else
            {
                return -1;
            }
        }
    }

    /**
     * Remove rows in bulk. Always iterate backwards in order to remove only the specified elements.
     * Example:
     * If we remove the index 0 first and then the index 1, this is what happens:
     * <pre>
     * FIRST ITERATION:
     * Index 0 => Element A [REMOVE]
     * Index 1 => Element B
     * ...
     * SECOND ITERATION:
     * Index 0 => Element B [REMOVE]
     * Index 1 => Element C
     * ...
     * THIRD ITERATION:
     * Index 0 => Element C [REMOVE] => This item should not be removed.
     * ...
     * And so on the entire list will be removed.
     * </pre>
     * But if we iterate backwards, this is what happens:
     * <pre>
     * Index N => Element N [PASS ELEMENTS]
     * ...
     * Index 1 => Element B [REMOVE] => All previous elements arrange themselfs to the new indexes.
     *                                  Element C falls here.
     * ...
     * NEXT ITERATION BACKWARDS.
     * Index 0 => Element A [REMOVE] => All previous elements arrange themselfs to the new indexes.
     *                                  Element C falls here.
     * ...
     * NEXT ITERATION BACKWARDS. NO MORE ELEMENTS TO ITERATE, THE LOOP ENDS.
     * </pre>
     *
     * <pre>
     * Example:
     * ArrayLocation    TableRowIndex
     *   Array[0]    =>      1
     *   Array[1]    =>      3
     *   Array[2]    =>      10
     * </pre>
     *
     * @param rs Array of rows to remove.
     */
    public void deleteRows(int[] rs)
    {
        synchronized (rows)
        {
            List<String> urls = new ArrayList<>();
            if (rs.length == 0)
            {
                return;
            }

            // Iterate backwards.
            ListIterator<List<Object>> i = rows.listIterator(rows.size());
            while (i.hasPrevious())
            {
                List<Object> e = i.previous();
                int index = rows.indexOf(e);
                for (int r : rs)
                {
                    if (r == index)
                    {
                        urls.add(rows.get(r).get(SEED_COLUMN_ID).toString());
                        i.remove();
                    }
                }
            }

            SeedTools.removeSeed(urls.toArray(new String[0]));
            fireTableDataChanged();
        }
    }

    private class DataListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent evt)
        {
            for (String url : SeedTools.getURLs())
            {
                addRow(Arrays.asList(new Object[]
                {
                    url, false
                }));
            }
        }
    }
}
