/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlviewer;

import java.awt.Component;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Phystem
 */
public class Utils {

    public final static FileFilter XML_FILE_FILTER = new FileFilter() {

        @Override
        public boolean accept(File pathname) {
            return pathname.getName().endsWith(".xml");
        }
    };

    public static DefaultTreeModel getTreeModel(File xmlPath) {
        DefaultTreeModel model;
        Document doc = loadXml(xmlPath);
        if (doc != null) {
            Element root = doc.getDocumentElement();
            XmlTreeNode rootNode = new XmlTreeNode(root);
            model = new DefaultTreeModel(rootNode);
            loadAllChildNodes(rootNode, root);
        } else {
            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Root");
            model = new DefaultTreeModel(rootNode);
        }
        return model;
    }

    private static void loadAllChildNodes(XmlTreeNode rootNode, Element rootElement) {
        NodeList nList = rootElement.getChildNodes();
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node node = nList.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                XmlTreeNode child = new XmlTreeNode((Element) node);
                rootNode.add(child);
                if (node.hasChildNodes()) {
                    loadAllChildNodes(child, (Element) node);
                }
            }
        }
    }

    public static Document loadXml(File xmlPath) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(xmlPath);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static Document getNew() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.newDocument();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static void saveFromTreeModel(DefaultTreeModel model, File xmlPath) {
        Document doc = getNew();
        XmlTreeNode rootNode = (XmlTreeNode) model.getRoot();
        doc.appendChild(createElement(rootNode, doc));
        saveXml(doc, xmlPath);
    }

    private static Element createElement(XmlTreeNode node, Document doc) {
        Element element = doc.createElement(node.getName());
        for (String[] attr : node.getAttributes()) {
            element.setAttribute(attr[0], attr[1]);
        }
        if (!node.getText().trim().isEmpty()) {
            element.setTextContent(node.getText());
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            XmlTreeNode xNode = (XmlTreeNode) node.getChildAt(i);
            element.appendChild(createElement(xNode, doc));
        }
        return element;
    }

    public static void saveXml(Document doc, File xmlPath) {
        try {
            createIfNotExists(xmlPath);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(new DOMSource(doc),
                    new StreamResult(new FileOutputStream(xmlPath)));
        } catch (IOException | TransformerException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void createIfNotExists(File xmlPath) {
        if (!xmlPath.exists()) {
            xmlPath.getParentFile().mkdirs();
            try {
                xmlPath.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void expandTree(JTree tree, boolean expand) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        expandAll(tree, new TreePath(root), expand);
    }

    private static void expandAll(JTree tree, TreePath path, boolean expand) {
        TreeNode node = (TreeNode) path.getLastPathComponent();

        if (node.getChildCount() >= 0) {
            Enumeration enumeration = node.children();
            while (enumeration.hasMoreElements()) {
                TreeNode n = (TreeNode) enumeration.nextElement();
                TreePath p = path.pathByAddingChild(n);
                expandAll(tree, p, expand);
            }
        }

        if (expand) {
            tree.expandPath(path);
        } else {
            tree.collapsePath(path);
        }
    }

    public static void resizeTable(JTable table) {
        for (int column = 0; column < table.getColumnCount(); column++) {
            TableColumn tableColumn = table.getColumnModel().getColumn(column);
            int preferredWidth = tableColumn.getMinWidth();
            int maxWidth = tableColumn.getMaxWidth();

            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
                Component c = table.prepareRenderer(cellRenderer, row, column);
                int width = c.getPreferredSize().width + table.getIntercellSpacing().width;
                preferredWidth = Math.max(preferredWidth, width);

                //  We've exceeded the maximum width, no need to check other rows
                if (preferredWidth >= maxWidth) {
                    preferredWidth = maxWidth;
                    break;
                }
            }

            tableColumn.setPreferredWidth(preferredWidth);
        }

        table.setRowHeight(100);

    }

    public static File getFileChooser() {
        JFileChooser xmlFileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
        xmlFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int val = xmlFileChooser.showSaveDialog(null);
        if (val == JFileChooser.APPROVE_OPTION) {
            return xmlFileChooser.getSelectedFile();
        }
        return null;
    }

}
