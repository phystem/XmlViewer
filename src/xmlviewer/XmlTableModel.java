/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlviewer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author Phystem
 */
public class XmlTableModel extends AbstractTableModel {

    final int rows;
    final List<XmlTreeNode> nodes;

    List<List<XmlTreeNode>> duplicates = new ArrayList<>();

    public XmlTableModel(int rows, List<XmlTreeNode> nodes) {
        this.rows = rows;
        this.nodes = nodes;
        duplicate();
    }

    private void duplicate() {
        for (int i = 0; i < rows; i++) {
            List<XmlTreeNode> dupNodes = new ArrayList<>();
            for (XmlTreeNode node : nodes) {
                dupNodes.add((XmlTreeNode) node.clone());
            }
            duplicates.add(dupNodes);
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return String.class;
        }
        return XmlTreeNode.class;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "Index";
        }
        return nodes.get(column - 1).toString();
    }

    public String getColumnToolTip(int column) {
        if (column == 0) {
            return null;
        }
        return nodes.get(column - 1).getXpath();
    }

    @Override
    public int getRowCount() {
        return rows;
    }

    @Override
    public int getColumnCount() {
        return nodes.size() + 1;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return false;
        }
        return true;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return rowIndex + 1;
        }
        return duplicates.get(rowIndex).get(columnIndex - 1);
    }

    public Boolean save() {
        File location = Utils.getFileChooser();
        if (location != null) {
            List<XmlTreeNode> realNodes = new ArrayList<>();
            for (XmlTreeNode node : nodes) {
                realNodes.add((XmlTreeNode) node.clone());
            }

            for (int dup = 0; dup < duplicates.size(); dup++) {
                List<XmlTreeNode> duplicate = duplicates.get(dup);
                replaceReal(duplicate);
                Utils.saveFromTreeModel(new DefaultTreeModel(nodes.get(0).getRoot()), new File(location, "Mock_" + dup));
            }
            replaceReal(realNodes);
            return true;
        }
        return false;
    }

    private void replaceReal(List<XmlTreeNode> duplicate) {
        for (int i = 0; i < duplicate.size(); i++) {
            XmlTreeNode dupNode = duplicate.get(i);
            XmlTreeNode realNode = nodes.get(i);
            for (int row = 0; row < dupNode.getRowCount(); row++) {
                for (int column = 0; column < dupNode.getColumnCount(); column++) {
                    realNode.setValueAt(dupNode.getValueAt(row, column), row, column);
                }
            }
        }
    }

}
