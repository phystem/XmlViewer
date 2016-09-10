/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlviewer;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Phystem
 */
public class XmlTreeNode extends DefaultMutableTreeNode implements TableModel {

    private String name;
    private String text;
    private final List<String[]> attributes = new ArrayList<>();

    public XmlTreeNode(Element element) {
        this.name = element.getNodeName();
        this.text = getFirstLevelTextContent(element);
        for (int i = 0; i < element.getAttributes().getLength(); i++) {
            Node node = element.getAttributes().item(i);
            attributes.add(new String[]{node.getNodeName(), node.getTextContent()});
        }
    }

    public XmlTreeNode(String name) {
        this.name = name;
        this.text = "";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public void addTableModelListener(TableModelListener l) {

    }

    @Override
    public void removeTableModelListener(TableModelListener l) {

    }

    @Override
    public int getRowCount() {
        return 1 + 1 + attributes.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return !(columnIndex == 0 && rowIndex < 2) && !(columnIndex == 1 && rowIndex == 1);
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "Property";
        }
        return "Value";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            switch (rowIndex) {
                case 0:
                    return "NodeText";
                case 1:
                    return "Xpath";
                default:
                    return attributes.get(rowIndex - 2)[0];
            }
        } else {
            switch (rowIndex) {
                case 0:
                    return text;
                case 1:
                    return getXpath(this);
                default:
                    return attributes.get(rowIndex - 2)[1];
            }
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            switch (rowIndex) {
                case 0:
                case 1:
                default:
                    attributes.get(rowIndex - 2)[0] = aValue.toString();
                    break;
            }
        } else {
            switch (rowIndex) {
                case 0:
                    text = aValue.toString();
                    break;
                case 1:
                    break;
                default:
                    attributes.get(rowIndex - 2)[1] = aValue.toString();
                    break;
            }
        }
    }

    private String getXpath(XmlTreeNode node) {
        return getElementXpath(node);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getXpath() {
        return getXpath(this);
    }

    public List<String[]> getAttributes() {
        return attributes;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return name + " [" + getElementIndex(this) + "]";
    }

    private static String getElementXpath(XmlTreeNode elt) {
        String path = "";
        try {
            for (; elt != null; elt = (XmlTreeNode) elt.getParent()) {
                int idx = getElementIndex(elt);
                String xname = elt.name;

                if (idx >= 1) {
                    xname += "[" + idx + "]";
                }
                path = "/" + xname + path;
            }
        } catch (Exception ee) {
        }
        return path;
    }

    private static int getElementIndex(XmlTreeNode original) {
        int count = 1;

        for (XmlTreeNode node = (XmlTreeNode) original.getPreviousSibling(); node != null;
                node = (XmlTreeNode) node.getPreviousSibling()) {
            if (node.name.equals(original.name)) {
                count++;
            }
        }

        return count;
    }

    private static String getFirstLevelTextContent(Node node) {
        NodeList list = node.getChildNodes();
        StringBuilder textContent = new StringBuilder();
        for (int i = 0; i < list.getLength(); ++i) {
            Node child = list.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                textContent.append(child.getTextContent().trim());
            }
        }
        return textContent.toString().trim();
    }

}