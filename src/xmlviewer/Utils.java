/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlviewer;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Comment;
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
            Element nodeElement = null;
            Boolean isCommented = rootNode.isCommented();
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                nodeElement = (Element) node;
            } else if (node.getNodeType() == Node.COMMENT_NODE) {
                nodeElement = parseXmlFromComment(((Comment) node).getData());
                isCommented = true;
            }
            if (nodeElement != null) {
                XmlTreeNode child = new XmlTreeNode(nodeElement);
                child.setCommented(isCommented);
                rootNode.add(child);
                if (nodeElement.hasChildNodes()) {
                    loadAllChildNodes(child, nodeElement);
                }
            }
        }
    }

    private static Element parseXmlFromComment(String comment) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(comment.getBytes("UTF-8")));
            return doc.getDocumentElement();
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
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

    public static Document saveFromTreeModel(DefaultTreeModel model, File xmlPath) {
        Document doc = getNew();
        XmlTreeNode rootNode = (XmlTreeNode) model.getRoot();
        doc.appendChild(createElement(rootNode, doc));
        saveXml(doc, xmlPath);
        return doc;
    }

    private static Node createElement(XmlTreeNode node, Document doc) {
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
        if (node.getParent() != null && !((XmlTreeNode) node.getParent()).isCommented()
                && node.isCommented()) {
            return elementToComment(element, doc);
        }
        return element;
    }

    private static Node elementToComment(Element element, Document doc) {
        String comment = "";
        try {
            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transformer = transFactory.newTransformer();
            StringWriter buffer = new StringWriter();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(element),
                    new StreamResult(buffer));
            comment = buffer.toString();
        } catch (TransformerException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return doc.createComment(comment);
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

    public static Boolean validate(Document document, File xsd) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Source schemaFile = new StreamSource(xsd);
            Schema schema = factory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            validator.validate(new DOMSource(document));
            return true;
        } catch (SAXException | IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
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

    public static void disableDoubleClickEdit(JTree tree) {
        DefaultTreeCellEditor editor = new DefaultTreeCellEditor(tree, (DefaultTreeCellRenderer) tree.getCellRenderer()) {
            @Override
            public boolean isCellEditable(EventObject event) {
                if (event instanceof MouseEvent) {
                    return false;
                }
                return super.isCellEditable(event);
            }
        };
        tree.setCellEditor(editor);
    }

    public static void main(String[] args) {
        XsdData xsd=new XsdData(new File("C:\\Users\\Phystem\\Documents\\data.xsd"));
        System.out.println(xsd.getEnum("Color"));
        System.out.println(xsd.getEnum("Colo"));
        System.out.println(xsd.getEnum("Size"));
    }
}
