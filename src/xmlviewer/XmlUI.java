/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlviewer;

import com.sun.glass.events.KeyEvent;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import static xmlviewer.Utils.XML_FILE_FILTER;

/**
 *
 * @author Phystem
 */
public class XmlUI extends javax.swing.JFrame {

    DefaultListModel xmlListModel;
    TreePopupMenu popupMenu;

    Action onValueChangeOnAction;

    /**
     * Creates new form XmlUI
     */
    public XmlUI() {
        initComponents();
        popupMenu = new TreePopupMenu();
        xmlListModel = new DefaultListModel();
        xmlList.setModel(xmlListModel);
        xmlFileChooser.setMultiSelectionEnabled(true);

        setSize(700, 700);
        listTreeSplitpane.setDividerLocation(0.5);
        treeTableSplitPane.setDividerLocation(0.7);
        setLocationRelativeTo(null);
        initListeners();
    }

    private void initListeners() {
        xmlTree.getCellEditor().addCellEditorListener(new CellEditorListener() {

            @Override
            public void editingStopped(ChangeEvent e) {
                renameNode();
            }

            @Override
            public void editingCanceled(ChangeEvent e) {

            }
        });

        xmlTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = xmlTree.getClosestRowForLocation(e.getX(), e.getY());
                    xmlTree.setSelectionRow(row);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        xmlTree.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcut), "AddNew");
        xmlTree.getActionMap().put("AddNew", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                addNodeActionPerformed(null);
            }
        });
        xmlTree.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "Delete");
        xmlTree.getActionMap().put("Delete", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                deleteNodeActionPerformed(null);
            }
        });

        xmlList.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "Delete");
        xmlList.getActionMap().put("Delete", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (xmlList.getSelectedIndices().length > 0) {
                    int[] indices = xmlList.getSelectedIndices();
                    Arrays.sort(indices);
                    for (int i = indices.length - 1; i >= 0; i--) {
                        xmlListModel.remove(indices[i]);
                    }
                }
            }
        });

        xmlList.setTransferHandler(new TransferHandler() {

            @Override
            public boolean canImport(TransferHandler.TransferSupport info) {
                return info.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferHandler.TransferSupport info) {
                if (!info.isDrop()) {
                    return false;
                }

                if (!canImport(info)) {
                    return false;
                }

                // Check for FileList flavor
                if (!info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    displayDropLocation("List doesn't accept a drop of this type.");
                    return false;
                }

                // Get the fileList that is being dropped.
                Transferable t = info.getTransferable();
                List<File> data;
                try {
                    data = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                    if (data == null) {
                        return false;
                    }
                } catch (Exception e) {
                    return false;
                }
                checkAndAddFiles((File[]) data.toArray());
                return true;
            }

            private void displayDropLocation(String string) {
                System.out.println(string);
            }
        });

        onValueChangeOnAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (applyChangesMenuItem.isSelected()) {
                    modifyValueInOthers();
                }
            }
        };

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        xmlFileChooser = new javax.swing.JFileChooser();
        listTreeSplitpane = new javax.swing.JSplitPane();
        treeTableSplitPane = new javax.swing.JSplitPane();
        treePanel = new javax.swing.JPanel();
        treeScrollPane = new javax.swing.JScrollPane();
        xmlTree = new javax.swing.JTree();
        tablePanel = new javax.swing.JPanel();
        tableScrollPane = new javax.swing.JScrollPane();
        xmlPropTable = new javax.swing.JTable();
        listScrollPane = new javax.swing.JScrollPane();
        xmlList = new javax.swing.JList();
        toolBar = new javax.swing.JToolBar();
        loadXml = new javax.swing.JButton();
        saveXml = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
        addNode = new javax.swing.JButton();
        renameNode = new javax.swing.JButton();
        deleteNode = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        loadMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        reloadMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        quitMenuItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        addNodeMenuItem = new javax.swing.JMenuItem();
        renameNodeMenuItem = new javax.swing.JMenuItem();
        deleteNodesMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        applyChangesMenuItem = new javax.swing.JCheckBoxMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        expandNodes = new javax.swing.JMenuItem();
        collapseNodes = new javax.swing.JMenuItem();

        xmlFileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        xmlFileChooser.setDialogTitle("Select any XML files or folder which contains XML");
        xmlFileChooser.setFileFilter(new FileNameExtensionFilter("Xml Files","xml"));
        xmlFileChooser.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Simple Xml Viewer");

        treeTableSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        treePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Xml Tree View"));
        treePanel.setLayout(new java.awt.BorderLayout());

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        xmlTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        xmlTree.setEditable(true);
        xmlTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                xmlTreeValueChanged(evt);
            }
        });
        treeScrollPane.setViewportView(xmlTree);

        treePanel.add(treeScrollPane, java.awt.BorderLayout.CENTER);

        treeTableSplitPane.setTopComponent(treePanel);

        tablePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Node Details"));
        tablePanel.setLayout(new java.awt.BorderLayout());

        xmlPropTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Name", null},
                {"Value", null},
                {"Xpath", null},
                {"Atrribute", null}
            },
            new String [] {
                "Attribute", "Value"
            }
        ));
        tableScrollPane.setViewportView(xmlPropTable);
        if (xmlPropTable.getColumnModel().getColumnCount() > 0) {
            xmlPropTable.getColumnModel().getColumn(0).setMinWidth(70);
            xmlPropTable.getColumnModel().getColumn(0).setPreferredWidth(70);
            xmlPropTable.getColumnModel().getColumn(0).setMaxWidth(100);
        }

        tablePanel.add(tableScrollPane, java.awt.BorderLayout.CENTER);

        treeTableSplitPane.setRightComponent(tablePanel);

        listTreeSplitpane.setRightComponent(treeTableSplitPane);

        xmlList.setBorder(javax.swing.BorderFactory.createTitledBorder("Xml Files"));
        xmlList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        xmlList.setToolTipText("<html>\nYou can drag and drop xml files/folders in to the list as well.<br/>\nRemove Unwanted Xmls by selecting them and press the Delete key\n</html>");
        xmlList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                xmlListValueChanged(evt);
            }
        });
        listScrollPane.setViewportView(xmlList);

        listTreeSplitpane.setLeftComponent(listScrollPane);

        getContentPane().add(listTreeSplitpane, java.awt.BorderLayout.CENTER);

        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        loadXml.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/load.png"))); // NOI18N
        loadXml.setText("Load Xml");
        loadXml.setFocusable(false);
        loadXml.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        loadXml.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadXmlActionPerformed(evt);
            }
        });
        toolBar.add(loadXml);

        saveXml.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/save.png"))); // NOI18N
        saveXml.setText("Save Xml");
        saveXml.setFocusable(false);
        saveXml.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        saveXml.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveXmlActionPerformed(evt);
            }
        });
        toolBar.add(saveXml);
        toolBar.add(filler1);

        addNode.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/add.png"))); // NOI18N
        addNode.setText("Add Node");
        addNode.setToolTipText("Add a node to the selected Node [Ctrl/Cmd + N]");
        addNode.setFocusable(false);
        addNode.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        addNode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNodeActionPerformed(evt);
            }
        });
        toolBar.add(addNode);

        renameNode.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit.png"))); // NOI18N
        renameNode.setText("Rename Node");
        renameNode.setToolTipText("Rename the selected Node [F2]");
        renameNode.setFocusable(false);
        renameNode.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        renameNode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renameNodeActionPerformed(evt);
            }
        });
        toolBar.add(renameNode);

        deleteNode.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/delete.png"))); // NOI18N
        deleteNode.setText("Delete Node");
        deleteNode.setToolTipText("Delete the selected nodes [Delete]");
        deleteNode.setFocusable(false);
        deleteNode.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        deleteNode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteNodeActionPerformed(evt);
            }
        });
        toolBar.add(deleteNode);

        getContentPane().add(toolBar, java.awt.BorderLayout.NORTH);

        jMenu1.setText("File");

        loadMenuItem.setText("Load");
        loadMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(loadMenuItem);

        saveMenuItem.setText("Save");
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(saveMenuItem);

        reloadMenuItem.setText("Reload");
        reloadMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(reloadMenuItem);
        jMenu1.add(jSeparator2);

        quitMenuItem.setText("Quit");
        quitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(quitMenuItem);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Options");

        addNodeMenuItem.setText("Add Node");
        addNodeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNodeMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(addNodeMenuItem);

        renameNodeMenuItem.setText("Rename Node");
        renameNodeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renameNodeMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(renameNodeMenuItem);

        deleteNodesMenuItem.setText("Delete Nodes");
        deleteNodesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteNodesMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(deleteNodesMenuItem);
        jMenu2.add(jSeparator1);

        applyChangesMenuItem.setText("Apply NLC to All");
        applyChangesMenuItem.setToolTipText("Apply Node Level Changes like Node Text change,Attribute value changes to other loaded xmls");
        jMenu2.add(applyChangesMenuItem);
        jMenu2.add(jSeparator3);

        expandNodes.setText("Expand All");
        expandNodes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                expandNodesActionPerformed(evt);
            }
        });
        jMenu2.add(expandNodes);

        collapseNodes.setText("Collapse All");
        collapseNodes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                collapseNodesActionPerformed(evt);
            }
        });
        jMenu2.add(collapseNodes);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void loadXmlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadXmlActionPerformed
        int val = xmlFileChooser.showOpenDialog(this);
        if (val == JFileChooser.APPROVE_OPTION) {
            File[] selectedFile = xmlFileChooser.getSelectedFiles();
            checkAndAddFiles(selectedFile);
        }
    }//GEN-LAST:event_loadXmlActionPerformed

    private void xmlListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_xmlListValueChanged
        if (!evt.getValueIsAdjusting()) {
            if (xmlList.getSelectedIndex() != -1) {
                SimpleXmlModel model = (SimpleXmlModel) xmlListModel.get(xmlList.getSelectedIndex());
                xmlTree.setModel(model.treeModel);
                XmlTreeNode root = (XmlTreeNode) model.treeModel.getRoot();
                xmlTree.setSelectionPath(new TreePath(root.getFirstLeaf().getPath()));
            }
        }
    }//GEN-LAST:event_xmlListValueChanged

    private void xmlTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_xmlTreeValueChanged
        Object previous = evt.getOldLeadSelectionPath();
        if (previous != null) {
            if (previous instanceof XmlTreeNode) {
                XmlTreeNode previousNode = (XmlTreeNode) previous;
                previousNode.setOnValueChangeAction(null);
            }
        }
        Object selected = evt.getPath().getLastPathComponent();
        if (selected instanceof XmlTreeNode) {
            XmlTreeNode selectedNode = (XmlTreeNode) selected;
            xmlPropTable.setModel(selectedNode);
            selectedNode.setOnValueChangeAction(onValueChangeOnAction);
        }
    }//GEN-LAST:event_xmlTreeValueChanged

    private void addNodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNodeActionPerformed
        XmlTreeNode node = getSelectedNode();
        if (node != null) {
            DefaultTreeModel model = (DefaultTreeModel) xmlTree.getModel();
            XmlTreeNode nNode = new XmlTreeNode("New Node");
            model.insertNodeInto(nNode, node, node.getChildCount());
            addNodeInOthers(node.getXpath(), nNode.getXpath());
        }
    }//GEN-LAST:event_addNodeActionPerformed

    private void deleteNodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteNodeActionPerformed
        XmlTreeNode[] nodes = getSelectedNodes();
        if (nodes != null) {
            DefaultTreeModel model = (DefaultTreeModel) xmlTree.getModel();
            for (XmlTreeNode node : nodes) {
                deleteNodeInOthers(node.getXpath());
                model.removeNodeFromParent(node);
            }
        }
    }//GEN-LAST:event_deleteNodeActionPerformed

    private void renameNodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renameNodeActionPerformed
        XmlTreeNode node = getSelectedNode();
        if (node != null) {
            xmlTree.startEditingAtPath(new TreePath(node.getPath()));
        }
    }//GEN-LAST:event_renameNodeActionPerformed

    private void saveXmlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveXmlActionPerformed
        xmlTree.stopEditing();
        for (int i = 0; i < xmlListModel.size(); i++) {
            SimpleXmlModel sModel = (SimpleXmlModel) xmlListModel.get(i);
            sModel.save();
        }
        System.out.println("Saved successfully");
    }//GEN-LAST:event_saveXmlActionPerformed

    private void loadMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadMenuItemActionPerformed
        loadXmlActionPerformed(evt);
    }//GEN-LAST:event_loadMenuItemActionPerformed

    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
        saveXmlActionPerformed(evt);
    }//GEN-LAST:event_saveMenuItemActionPerformed

    private void reloadMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadMenuItemActionPerformed
        xmlTree.stopEditing();
        for (int i = 0; i < xmlListModel.size(); i++) {
            SimpleXmlModel sModel = (SimpleXmlModel) xmlListModel.get(i);
            sModel.reload();
        }
        xmlList.setSelectedIndex(xmlListModel.getSize() - 1);
    }//GEN-LAST:event_reloadMenuItemActionPerformed

    private void quitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitMenuItemActionPerformed
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }//GEN-LAST:event_quitMenuItemActionPerformed

    private void addNodeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNodeMenuItemActionPerformed
        addNodeActionPerformed(evt);
    }//GEN-LAST:event_addNodeMenuItemActionPerformed

    private void renameNodeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renameNodeMenuItemActionPerformed
        renameNodeActionPerformed(evt);
    }//GEN-LAST:event_renameNodeMenuItemActionPerformed

    private void deleteNodesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteNodesMenuItemActionPerformed
        deleteNodeActionPerformed(evt);
    }//GEN-LAST:event_deleteNodesMenuItemActionPerformed

    private void expandNodesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expandNodesActionPerformed
        Utils.expandTree(xmlTree, true);
    }//GEN-LAST:event_expandNodesActionPerformed

    private void collapseNodesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_collapseNodesActionPerformed
        Utils.expandTree(xmlTree, false);
    }//GEN-LAST:event_collapseNodesActionPerformed

    private void checkAndAddFiles(File[] files) {
        for (File file : files) {
            if (file.isDirectory()) {
                File[] xmlFiles = file.listFiles(XML_FILE_FILTER);
                if (xmlFiles != null) {
                    for (File xmlFile : xmlFiles) {
                        checkAndAdd(xmlFile);
                    }
                    xmlList.setSelectedIndex(xmlListModel.getSize() - 1);
                }
            } else if (file.getName().endsWith(".xml")) {
                checkAndAdd(file);
                xmlList.setSelectedIndex(xmlListModel.getSize() - 1);
            }
        }
    }

    private void addNodeInOthers(String parentXpath, String childXpath) {
        int index = xmlList.getSelectedIndex();
        for (int i = 0; i < xmlListModel.size(); i++) {
            if (i != index) {
                DefaultTreeModel model = ((SimpleXmlModel) xmlListModel.get(i)).treeModel;
                XmlTreeNode node = getMatchingNodeByXpath(model, parentXpath);
                if (node != null) {
                    Boolean doContinue = false;
                    for (int j = 0; j < node.getChildCount(); j++) {
                        if (((XmlTreeNode) node.getChildAt(j)).getXpath()
                                .equals(childXpath)) {
                            doContinue = true;
                            break;
                        }
                    }
                    if (doContinue) {
                        continue;
                    }
                    while (true) {
                        XmlTreeNode nNode = new XmlTreeNode("New Node");
                        model.insertNodeInto(nNode, node, node.getChildCount());
                        if (nNode.getXpath().equals(childXpath)) {
                            break;
                        }
                    }
                }
            }
        }
    }

    private XmlTreeNode getMatchingNodeByXpath(DefaultTreeModel model, String xpath) {
        XmlTreeNode root = (XmlTreeNode) model.getRoot();
        Enumeration e = root.preorderEnumeration();
        while (e.hasMoreElements()) {
            XmlTreeNode node = (XmlTreeNode) e.nextElement();
            if (node.getXpath().equals(xpath)) {
                return node;
            }
        }
        return null;
    }

    private void deleteNodeInOthers(String xpath) {
        int index = xmlList.getSelectedIndex();
        for (int i = 0; i < xmlListModel.size(); i++) {
            if (i != index) {
                DefaultTreeModel model = ((SimpleXmlModel) xmlListModel.get(i)).treeModel;
                XmlTreeNode node = getMatchingNodeByXpath(model, xpath);
                if (node != null) {
                    model.removeNodeFromParent(node);
                }
            }
        }
    }

    private void renameNode() {
        XmlTreeNode node = getSelectedNode();
        String value = xmlTree.getCellEditor().getCellEditorValue().toString();
        if (!node.getName().equals(value)) {
            renameNodeInOthers(node.getXpath(), value);
            node.setName(value);
            ((DefaultTreeModel) xmlTree.getModel()).reload(node);
        }
    }

    private void renameNodeInOthers(String xpath, String newVal) {
        int index = xmlList.getSelectedIndex();
        for (int i = 0; i < xmlListModel.size(); i++) {
            if (i != index) {
                DefaultTreeModel model = ((SimpleXmlModel) xmlListModel.get(i)).treeModel;
                XmlTreeNode node = getMatchingNodeByXpath(model, xpath);
                if (node != null) {
                    node.setName(newVal);
                }
            }
        }
    }

    private void modifyValueInOthers() {
        XmlTreeNode node = getSelectedNode();
        modifyValueInOthers(node.getXpath());
    }

    private void modifyValueInOthers(String xpath) {
        int index = xmlList.getSelectedIndex();
        for (int i = 0; i < xmlListModel.size(); i++) {
            if (i != index) {
                DefaultTreeModel model = ((SimpleXmlModel) xmlListModel.get(i)).treeModel;
                XmlTreeNode node = getMatchingNodeByXpath(model, xpath);
                if (node != null) {
                    node.modifyValueByAction(onValueChangeOnAction);
                }
            }
        }
    }

    private XmlTreeNode getSelectedNode() {
        XmlTreeNode[] nodes = getSelectedNodes();
        if (nodes != null) {
            return nodes[0];
        }
        return null;
    }

    private XmlTreeNode[] getSelectedNodes() {
        TreePath[] paths = xmlTree.getSelectionPaths();
        if (paths != null && paths.length > 0) {
            XmlTreeNode[] nodes = new XmlTreeNode[paths.length];
            int i = 0;
            for (TreePath path : paths) {
                Object node = path.getLastPathComponent();
                if (node instanceof XmlTreeNode) {
                    nodes[i++] = (XmlTreeNode) node;
                }
            }
            return nodes;
        }
        return null;
    }

    private void checkAndAdd(File file) {
        SimpleXmlModel sFile = new SimpleXmlModel(file);
        for (int i = 0; i < xmlListModel.size(); i++) {
            if (xmlListModel.get(i).equals(sFile)) {
                System.out.println(file + " - File Already Loaded");
                return;
            }
        }
        xmlListModel.addElement(sFile);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(XmlUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(XmlUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(XmlUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(XmlUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                XmlUI xml = new XmlUI();
                xml.setVisible(true);
                xml.loadXmlActionPerformed(null);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addNode;
    private javax.swing.JMenuItem addNodeMenuItem;
    private javax.swing.JCheckBoxMenuItem applyChangesMenuItem;
    private javax.swing.JMenuItem collapseNodes;
    private javax.swing.JButton deleteNode;
    private javax.swing.JMenuItem deleteNodesMenuItem;
    private javax.swing.JMenuItem expandNodes;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JScrollPane listScrollPane;
    private javax.swing.JSplitPane listTreeSplitpane;
    private javax.swing.JMenuItem loadMenuItem;
    private javax.swing.JButton loadXml;
    private javax.swing.JMenuItem quitMenuItem;
    private javax.swing.JMenuItem reloadMenuItem;
    private javax.swing.JButton renameNode;
    private javax.swing.JMenuItem renameNodeMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JButton saveXml;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JPanel treePanel;
    private javax.swing.JScrollPane treeScrollPane;
    private javax.swing.JSplitPane treeTableSplitPane;
    private javax.swing.JFileChooser xmlFileChooser;
    private javax.swing.JList xmlList;
    private javax.swing.JTable xmlPropTable;
    private javax.swing.JTree xmlTree;
    // End of variables declaration//GEN-END:variables

    class TreePopupMenu extends JPopupMenu implements ActionListener {

        public TreePopupMenu() {
            int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
            create("Add Node", KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcut));
            create("Rename Node", KeyStroke.getKeyStroke(KeyEvent.VK_F2, shortcut));
            create("Delete Nodes", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
            addSeparator();
            create("Expand All", null);
            create("Collapse All", null);
        }

        private void create(String text, KeyStroke key) {
            JMenuItem item = new JMenuItem(text);
            item.setAccelerator(key);
            item.addActionListener(this);
            add(item);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()) {
                case "Add Node":
                    addNodeActionPerformed(null);
                    break;
                case "Rename Node":
                    renameNodeActionPerformed(null);
                    break;
                case "Delete Nodes":
                    deleteNodeActionPerformed(null);
                    break;
                case "Expand All":
                    Utils.expandTree(xmlTree, true);
                    break;
                case "Collapse All":
                    Utils.expandTree(xmlTree, false);
                    break;
            }
        }

    }
}
