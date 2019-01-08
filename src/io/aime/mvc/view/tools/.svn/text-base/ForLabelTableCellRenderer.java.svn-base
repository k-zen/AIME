package io.aime.mvc.view.tools;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class ForLabelTableCellRenderer extends DefaultTableCellRenderer
{

    private ForLabelTableCellRenderer()
    {
    }

    public static final ForLabelTableCellRenderer newBuild()
    {
        return new ForLabelTableCellRenderer();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        return (JLabel) value;
    }
}
