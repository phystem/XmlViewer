/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlviewer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultTreeModel;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Phystem
 */
public class XmlTableModel extends AbstractTableModel {

    final int rows;
    final List<XmlTreeNode> nodes;

    List<List<XmlTreeNode>> duplicates = new ArrayList<>();

    final Boolean nodeTextAlone;

    public XmlTableModel(int rows, List<XmlTreeNode> nodes, Boolean nodeTextAlone) {
        this.nodeTextAlone = nodeTextAlone;
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
        if (nodeTextAlone || columnIndex == 0) {
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
        XmlTreeNode node = duplicates.get(rowIndex).get(columnIndex - 1);
        if (nodeTextAlone) {
            return node.getText();
        }
        return node;
    }

    public XmlTreeNode getNodeValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return null;
        }
        return duplicates.get(rowIndex).get(columnIndex - 1);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (nodeTextAlone) {
            XmlTreeNode node = duplicates.get(rowIndex).get(columnIndex - 1);
            node.setText(aValue.toString());
        } else {
            super.setValueAt(aValue, rowIndex, columnIndex);
        }
    }

    public Boolean save(String mockName, File location, File validateAgainstSchema) {
        if (mockName != null && !mockName.isEmpty()) {
            List<Integer> invalid = new ArrayList<>();
            if (!mockName.contains("{index}")) {
                mockName = mockName + "{index}";
            }
            if (location != null) {
                List<XmlTreeNode> realNodes = new ArrayList<>();
                for (XmlTreeNode node : nodes) {
                    realNodes.add((XmlTreeNode) node.clone());
                }
                for (int dup = 0; dup < duplicates.size(); dup++) {
                    String fileName = mockName.replace("{index}", dup + 1 + "");
                    File xmlFile = new File(location, fileName + ".xml");

                    List<XmlTreeNode> duplicate = duplicates.get(dup);
                    replaceReal(duplicate);
                    Utils.saveFromTreeModel(new DefaultTreeModel(nodes.get(0).getRoot()),
                            xmlFile);
                    if (validateAgainstSchema != null) {
                        try {
                            if (!Utils.validate(xmlFile, validateAgainstSchema)) {
                                invalid.add(dup + 1);
                            }
                        } catch (SAXException ex) {
                            Utils.writeException(new File(location, fileName + "_error.txt"), ex);
                            invalid.add(dup + 1);
                        }
                    }
                }
                replaceReal(realNodes);
                if (!invalid.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "The following mock data are invalid against the given schema - " + invalid);
                }
                return true;
            }
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
