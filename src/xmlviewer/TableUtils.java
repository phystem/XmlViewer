/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlviewer;

/**
 *
 * @author Phystem
 */
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;

/**
 *
 * @author 394173
 */
public class TableUtils {

    private static final String LINE_BREAK = "\n";
    private static final String CELL_BREAK = "\t";
    private static final Clipboard CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();

    public static void installCCP(JTable table) {
        table.addKeyListener(new ClipboardKeyAdapter(table));
    }

    public static void copyToClipboard(JTable table, boolean isCut) {
        int numCols = table.getSelectedColumnCount();
        int numRows = table.getSelectedRowCount();
        int[] rowsSelected = table.getSelectedRows();
        int[] colsSelected = table.getSelectedColumns();
        if (numRows != rowsSelected[rowsSelected.length - 1] - rowsSelected[0] + 1 || numRows != rowsSelected.length
                || numCols != colsSelected[colsSelected.length - 1] - colsSelected[0] + 1 || numCols != colsSelected.length) {

            Logger.getLogger(TableUtils.class.getName()).info("Invalid Copy Selection");
            return;
        }

        StringBuilder excelStr = new StringBuilder();
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                excelStr.append(escape(table.getValueAt(rowsSelected[i], colsSelected[j])));
                if (isCut) {
                    table.setValueAt("", rowsSelected[i], colsSelected[j]);
                }
                if (j < numCols - 1) {
                    excelStr.append(CELL_BREAK);
                }
            }
            excelStr.append(LINE_BREAK);
        }

        StringSelection sel = new StringSelection(excelStr.toString());

        CLIPBOARD.setContents(sel, sel);
    }

    private static String escape(Object cell) {
        return Objects.toString(cell.toString(), "").replace(LINE_BREAK, " ").replace(CELL_BREAK, " ");
    }

    public static void pasteFromClipboard(JTable table) {
        int startRow = table.getSelectedRows()[0];
        int startCol = table.getSelectedColumns()[0];

        String pasteString;
        try {
            pasteString = (String) (CLIPBOARD.getContents(null).getTransferData(DataFlavor.stringFlavor));
        } catch (Exception e) {
            Logger.getLogger(TableUtils.class.getName()).log(Level.WARNING, "Invalid Paste Type", e);
            return;
        }
        String[] lines = pasteString.split(LINE_BREAK);

        for (int i = 0; i < lines.length; i++) {
            String[] cells = lines[i].split(CELL_BREAK);
            for (int j = 0; j < cells.length; j++) {
                if (table.getRowCount() <= startRow + i) {
                    return;
                }
                if (table.getRowCount() > startRow + i && table.getColumnCount() > startCol + j) {
                    table.setValueAt(cells[j], startRow + i, startCol + j);
                }
            }
        }
    }
}

class ClipboardKeyAdapter extends KeyAdapter {

    private final JTable table;

    public ClipboardKeyAdapter(JTable table) {
        this.table = table;
    }

    @Override
    public void keyReleased(KeyEvent event) {
        if (event.isControlDown()) {
            switch (event.getKeyCode()) {
                case KeyEvent.VK_C:
                    // Copy
                    cancelEditing();
                    TableUtils.copyToClipboard(table, false);
                    table.repaint();
                    break;
                case KeyEvent.VK_X:
                    // Cut
                    cancelEditing();
                    TableUtils.copyToClipboard(table, true);
                    table.repaint();
                    break;
                case KeyEvent.VK_V:
                    // Paste
                    cancelEditing();
                    TableUtils.pasteFromClipboard(table);
                    table.repaint();
                    break;
                default:
                    break;
            }
        }

    }

    private void cancelEditing() {
        if (table.getCellEditor() != null) {
            table.getCellEditor().cancelCellEditing();
        }
    }

}
