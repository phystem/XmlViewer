/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xmlviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultFormatter;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import static xmlviewer.Utils.XML_FILE_FILTER;

/**
 *
 * @author Phystem
 */
public class XmlUI extends javax.swing.JFrame {

    String regex = "(-x (?<xpath>.+))|(-n (?<node>.+) -v (?<value>.+))|-v (?<val>.+)|(?<any>.+)";
    Pattern pattern = Pattern.compile(regex);

    DefaultListModel xmlListModel;
    TreePopupMenu popupMenu;

    Action onValueChangeOnAction;

    /**
     * Creates new form XmlUI
     */
    public XmlUI() {
        initComponents();
        initDefaults();
        initListeners();
        initShortcuts();
    }

    private void initDefaults() {
        popupMenu = new TreePopupMenu();
        xmlListModel = new DefaultListModel();
        xmlList.setModel(xmlListModel);
        xmlFileChooser.setMultiSelectionEnabled(true);

        mockPanel.setVisible(mockDataToggle.isSelected());

        setSize(700, 700);
        listTreeSplitpane.setDividerLocation(0.3);
        treeTableSplitPane.setDividerLocation(0.7);
        setLocationRelativeTo(null);
    }

    private void initListeners() {

        JFormattedTextField field = ((JSpinner.DefaultEditor) mockNum.getEditor()).getTextField();
        DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
        formatter.setCommitsOnValidEdit(true);

        Utils.disableDoubleClickEdit(xmlTree);
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

        xmlTree.setCellRenderer(new XmlTreeRenderer());

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

        searchText.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                onTextChange();
            }

            public void removeUpdate(DocumentEvent e) {
                onTextChange();
            }

            public void insertUpdate(DocumentEvent e) {
                onTextChange();
            }

        });

        mockTable.setDefaultRenderer(XmlTreeNode.class, new MockRenderer());
        mockTable.setDefaultEditor(XmlTreeNode.class, new MockCellEditor());
        mockTable.getTableHeader().setDefaultRenderer(new MockHeaderRenderer());

    }

    private void initShortcuts() {

        int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        //For List
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
        //For Tree
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

        xmlTree.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_T, shortcut), "ToggleComment");
        xmlTree.getActionMap().put("ToggleComment", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                toggleComment();
            }
        });
        //For Table
        TableUtils.installCCP(mockTable);
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
        xsdFileChooser = new javax.swing.JFileChooser();
        mockDetails = new javax.swing.JDialog();
        mockDataName = new javax.swing.JTextField();
        validateAganistSchema = new javax.swing.JCheckBox();
        createMockXmls = new javax.swing.JButton();
        xsdLocation = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        mockXMLSaveLocation = new javax.swing.JTextField();
        saveMockXmlTo = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        listTreeSplitpane = new javax.swing.JSplitPane();
        treeTableSplitPane = new javax.swing.JSplitPane();
        treeMockPane = new javax.swing.JSplitPane();
        treePanel = new javax.swing.JPanel();
        treeScrollPane = new javax.swing.JScrollPane();
        xmlTree = new javax.swing.JTree();
        searchBar = new javax.swing.JToolBar();
        searchText = new javax.swing.JTextField();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        searchCount = new javax.swing.JLabel();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        previousSearch = new javax.swing.JButton();
        nextSearch = new javax.swing.JButton();
        tablePanel = new javax.swing.JPanel();
        tableScrollPane = new javax.swing.JScrollPane();
        xmlPropTable = new javax.swing.JTable();
        mockPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        mockTable = new javax.swing.JTable();
        jToolBar1 = new javax.swing.JToolBar();
        clearMocks = new javax.swing.JButton();
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 0), new java.awt.Dimension(20, 32767));
        mockNum = new javax.swing.JSpinner();
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        jPanel2 = new javax.swing.JPanel();
        mockStart = new javax.swing.JButton();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
        jPanel1 = new javax.swing.JPanel();
        createMocks = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        xsdFileName = new javax.swing.JTextField();
        loadXsdFromFile = new javax.swing.JButton();
        nodeTextOnly = new javax.swing.JCheckBox();
        mockLeafOnly = new javax.swing.JCheckBox();
        listScrollPane = new javax.swing.JScrollPane();
        xmlList = new javax.swing.JList();
        toolBar = new javax.swing.JToolBar();
        loadXml = new javax.swing.JButton();
        saveXml = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
        addNode = new javax.swing.JButton();
        renameNode = new javax.swing.JButton();
        deleteNode = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        mockDataToggle = new javax.swing.JToggleButton();
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

        xsdFileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        xsdFileChooser.setDialogTitle("Select Xsd");
        xsdFileChooser.setFileFilter(new FileNameExtensionFilter("Xsd Files","xsd"));

        mockDetails.setTitle("Mock Creation Details");
        mockDetails.setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);

        mockDataName.setText("Mock_{index}_data");
        mockDataName.setToolTipText("<html>Use <b>{index}</b> to specify index.For ex                 <br/>                 <b>dupMock_{index}</b> will generate <b>dupMock_1.xml</b>                 <br/>                 <b>Mock_{index}_data</b> will generate <b>Mock_1_data.xml</b>                 </html>");

        validateAganistSchema.setText("Validate against the following Schema");

        createMockXmls.setText("Create Mock Xmls");
        createMockXmls.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createMockXmlsActionPerformed(evt);
            }
        });

        xsdLocation.setEditable(false);

        jLabel1.setText("Mock Data Name");

        mockXMLSaveLocation.setText(new File(System.getProperty("user.dir")).getAbsolutePath());

        saveMockXmlTo.setText("...");
        saveMockXmlTo.setToolTipText("Select Mock Xml Location");
        saveMockXmlTo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMockXmlToActionPerformed(evt);
            }
        });

        jLabel2.setText("Save Mock XML's to");

        javax.swing.GroupLayout mockDetailsLayout = new javax.swing.GroupLayout(mockDetails.getContentPane());
        mockDetails.getContentPane().setLayout(mockDetailsLayout);
        mockDetailsLayout.setHorizontalGroup(
            mockDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mockDetailsLayout.createSequentialGroup()
                .addGroup(mockDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mockDetailsLayout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addGroup(mockDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1)
                            .addComponent(validateAganistSchema)
                            .addGroup(mockDetailsLayout.createSequentialGroup()
                                .addGroup(mockDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(mockXMLSaveLocation, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(mockDataName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
                                    .addComponent(xsdLocation, javax.swing.GroupLayout.Alignment.LEADING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(saveMockXmlTo))))
                    .addGroup(mockDetailsLayout.createSequentialGroup()
                        .addGap(114, 114, 114)
                        .addComponent(createMockXmls)))
                .addGap(0, 13, Short.MAX_VALUE))
        );
        mockDetailsLayout.setVerticalGroup(
            mockDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mockDetailsLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mockDataName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(validateAganistSchema)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(xsdLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addGap(7, 7, 7)
                .addGroup(mockDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mockXMLSaveLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveMockXmlTo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addComponent(createMockXmls)
                .addGap(23, 23, 23))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Simple Xml Viewer");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        listTreeSplitpane.setResizeWeight(0.3);
        listTreeSplitpane.setOneTouchExpandable(true);

        treeTableSplitPane.setResizeWeight(0.5);
        treeTableSplitPane.setOneTouchExpandable(true);

        treeMockPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        treeMockPane.setResizeWeight(0.7);

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

        searchBar.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        searchBar.setFloatable(false);
        searchBar.setRollover(true);

        searchText.setText("Search Text");
        searchText.setToolTipText("<html>\n\nFor Xpath Search : <b>-x</b> /catalog[1]/Book[1]\n<br/>\nFor Node name and Node Text Search : <b>-n</b> nodename <b>-v</b> nodeText\n<br/>\nFor Node Text Search : <b>-v</b> nodeText\n<br/>\nFor Node name Search : any value\n<br/>\n\n\n\n\n</html>");
        searchBar.add(searchText);
        searchBar.add(filler3);

        searchCount.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        searchCount.setForeground(java.awt.Color.blue);
        searchCount.setToolTipText("Search Count");
        searchBar.add(searchCount);
        searchBar.add(filler2);

        previousSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/back.png"))); // NOI18N
        previousSearch.setToolTipText("Go To Previous Search");
        previousSearch.setFocusable(false);
        previousSearch.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        previousSearch.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        previousSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousSearchActionPerformed(evt);
            }
        });
        searchBar.add(previousSearch);

        nextSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/forward.png"))); // NOI18N
        nextSearch.setToolTipText("Go To Next Search");
        nextSearch.setFocusable(false);
        nextSearch.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        nextSearch.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        nextSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextSearchActionPerformed(evt);
            }
        });
        searchBar.add(nextSearch);

        treePanel.add(searchBar, java.awt.BorderLayout.NORTH);
        searchBar.setLayout(new BoxLayout(searchBar,BoxLayout.X_AXIS));

        treeMockPane.setTopComponent(treePanel);

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

        treeMockPane.setRightComponent(tablePanel);

        treeTableSplitPane.setLeftComponent(treeMockPane);

        mockPanel.setLayout(new java.awt.BorderLayout());

        mockTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        mockTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        mockTable.setCellSelectionEnabled(true);
        jScrollPane1.setViewportView(mockTable);

        mockPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jToolBar1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        clearMocks.setText("Clear");
        clearMocks.setFocusable(false);
        clearMocks.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        clearMocks.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        clearMocks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearMocksActionPerformed(evt);
            }
        });
        jToolBar1.add(clearMocks);
        jToolBar1.add(filler5);

        mockNum.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        mockNum.setMaximumSize(new java.awt.Dimension(50, 32767));
        mockNum.setMinimumSize(new java.awt.Dimension(50, 30));
        mockNum.setPreferredSize(new java.awt.Dimension(50, 30));
        jToolBar1.add(mockNum);
        jToolBar1.add(filler6);

        jPanel2.setLayout(new java.awt.BorderLayout());

        mockStart.setText("Start Mocking");
        mockStart.setFocusable(false);
        mockStart.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        mockStart.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        mockStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mockStartActionPerformed(evt);
            }
        });
        jPanel2.add(mockStart, java.awt.BorderLayout.CENTER);

        jToolBar1.add(jPanel2);
        jToolBar1.add(filler4);

        jPanel1.setLayout(new java.awt.BorderLayout());

        createMocks.setText("Create Mock Files");
        createMocks.setFocusable(false);
        createMocks.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        createMocks.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        createMocks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createMocksActionPerformed(evt);
            }
        });
        jPanel1.add(createMocks, java.awt.BorderLayout.CENTER);

        jToolBar1.add(jPanel1);

        mockPanel.add(jToolBar1, java.awt.BorderLayout.PAGE_START);

        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        xsdFileName.setText("Load xsd File");
        xsdFileName.setMinimumSize(new java.awt.Dimension(50, 30));
        xsdFileName.setPreferredSize(new java.awt.Dimension(200, 30));
        jPanel3.add(xsdFileName);

        loadXsdFromFile.setText("...");
        loadXsdFromFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadXsdFromFileActionPerformed(evt);
            }
        });
        jPanel3.add(loadXsdFromFile);

        nodeTextOnly.setSelected(true);
        nodeTextOnly.setText("Mock Node Text Only");
        nodeTextOnly.setFocusable(false);
        nodeTextOnly.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        nodeTextOnly.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jPanel3.add(nodeTextOnly);

        mockLeafOnly.setSelected(true);
        mockLeafOnly.setText("Mock Leaf Nodes Only");
        mockLeafOnly.setFocusable(false);
        mockLeafOnly.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        mockLeafOnly.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jPanel3.add(mockLeafOnly);

        mockPanel.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        treeTableSplitPane.setRightComponent(mockPanel);

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
        toolBar.add(jSeparator4);

        mockDataToggle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/duplicate.png"))); // NOI18N
        mockDataToggle.setText("Mock Data");
        mockDataToggle.setFocusable(false);
        mockDataToggle.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        mockDataToggle.setMaximumSize(new java.awt.Dimension(107, 43));
        mockDataToggle.setMinimumSize(new java.awt.Dimension(107, 43));
        mockDataToggle.setPreferredSize(new java.awt.Dimension(107, 43));
        mockDataToggle.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                mockDataToggleItemStateChanged(evt);
            }
        });
        toolBar.add(mockDataToggle);

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
                if (model.getXsdData() != null) {
                    xsdFileName.setText(model.getXsdData().xsd.getAbsolutePath());
                    xsdLocation.setText(xsdFileName.getText());
                }
                xmlTree.setModel(model.treeModel);
                XmlTreeNode root = (XmlTreeNode) model.treeModel.getRoot();
                xmlTree.setSelectionPath(new TreePath(root.getFirstLeaf().getPath()));
                Utils.expandTree(xmlTree, true);
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

    private void nextSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextSearchActionPerformed
        nextSearch(searchText.getText());
    }//GEN-LAST:event_nextSearchActionPerformed

    private void previousSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousSearchActionPerformed
        prevoiusSearch(searchText.getText());
    }//GEN-LAST:event_previousSearchActionPerformed

    private void mockDataToggleItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_mockDataToggleItemStateChanged
        mockPanel.setVisible(mockDataToggle.isSelected());
        if (mockDataToggle.isSelected()) {
            listTreeSplitpane.setDividerLocation(0.3);
            treeTableSplitPane.setDividerLocation(0.3);
        }
    }//GEN-LAST:event_mockDataToggleItemStateChanged

    private void mockStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mockStartActionPerformed
        initMocks();
    }//GEN-LAST:event_mockStartActionPerformed

    private void createMocksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createMocksActionPerformed
        showMockDialog();
    }//GEN-LAST:event_createMocksActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        List<File[]> files = new ArrayList<>();
        for (int i = 0; i < xmlListModel.size(); i++) {
            SimpleXmlModel xmlModel = (SimpleXmlModel) xmlListModel.get(i);
            File[] file = new File[2];
            file[0] = xmlModel.getFile();
            if (xmlModel.getXsdData() != null) {
                file[1] = xmlModel.getXsdData().xsd;
            }
            files.add(file);
        }
        RecentItems.storeRecentItems(files);
    }//GEN-LAST:event_formWindowClosing

    private void clearMocksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearMocksActionPerformed
        mockTable.setModel(new DefaultTableModel());
    }//GEN-LAST:event_clearMocksActionPerformed

    private void createMockXmlsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createMockXmlsActionPerformed
        XmlTableModel xmlTabModel = (XmlTableModel) mockTable.getModel();
        File xsdFile = validateAganistSchema.isSelected() ? new File(xsdLocation.getText()) : null;
        if (xmlTabModel.save(
                mockDataName.getText(),
                new File(mockXMLSaveLocation.getText()),
                xsdFile)) {
            System.out.println("Saved");
            JOptionPane.showMessageDialog(this, "Mocked Files are created successfully");
        } else {
            JOptionPane.showMessageDialog(this, "Couldn't Save");
        }
        mockDetails.setVisible(false);
    }//GEN-LAST:event_createMockXmlsActionPerformed

    private void saveMockXmlToActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMockXmlToActionPerformed
        File file = Utils.getFileChooser();
        if (file != null) {
            mockXMLSaveLocation.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_saveMockXmlToActionPerformed

    private void loadXsdFromFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadXsdFromFileActionPerformed
        int val = xsdFileChooser.showOpenDialog(this);
        if (val == JFileChooser.APPROVE_OPTION) {
            File selectedFile = xsdFileChooser.getSelectedFile();
            xsdFileName.setText(selectedFile.getAbsolutePath());
            xsdLocation.setText(selectedFile.getAbsolutePath());
            SimpleXmlModel xmlModel = (SimpleXmlModel) xmlListModel.get(xmlList.getSelectedIndex());
            xmlModel.loadXsd(selectedFile);
        }
    }//GEN-LAST:event_loadXsdFromFileActionPerformed

    private void showMockDialog() {
        mockDetails.pack();
        mockDetails.setLocationRelativeTo(null);
        mockDetails.setVisible(true);
    }

    private void checkAndAddFiles(File[] files) {
        if (files != null) {
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

    private void duplicateNode() {
        XmlTreeNode node = getSelectedNode();
        if (!node.isRoot()) {
            XmlTreeNode duplicate = duplicateNode(node);
            DefaultTreeModel model = (DefaultTreeModel) xmlTree.getModel();
            model.insertNodeInto(duplicate, (XmlTreeNode) node.getParent(), node.getParent().getChildCount());
        }
    }

    private XmlTreeNode duplicateNode(XmlTreeNode node) {
        XmlTreeNode duplicate = (XmlTreeNode) node.clone();
        for (int i = 0; i < node.getChildCount(); i++) {
            duplicate.add(duplicateNode((XmlTreeNode) node.getChildAt(i)));
        }
        return duplicate;
    }

    private void toggleComment() {
        XmlTreeNode[] nodes = getSelectedNodes();
        if (nodes != null) {
            for (XmlTreeNode node : nodes) {
                node.setCommented(!node.isCommented());
            }
            xmlTree.repaint();
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

    private void checkAndAdd(File xml, File xsd) {
        SimpleXmlModel sFile = new SimpleXmlModel(xml);
        sFile.loadXsd(xsd);
        for (int i = 0; i < xmlListModel.size(); i++) {
            if (xmlListModel.get(i).equals(sFile)) {
                System.out.println(xml + " - File Already Loaded");
                return;
            }
        }
        xmlListModel.addElement(sFile);
    }

    public void onTextChange() {
        String text = searchText.getText();
        if (!text.isEmpty()) {
            searchAndSelect(text);
        } else {
            searchCount.setText("");
        }
    }

    private void searchAndSelect(String text) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.matches()) {
            XmlTreeNode root = (XmlTreeNode) xmlTree.getModel().getRoot();
            int count = 0;
            XmlTreeNode snode = null;
            Enumeration e = root.preorderEnumeration();
            while (e.hasMoreElements()) {
                XmlTreeNode node = (XmlTreeNode) e.nextElement();
                if (check(matcher, node)) {
                    if (snode == null) {
                        snode = node;
                    }
                    count++;
                }
            }
            if (snode != null) {
                selectAndScroll(snode);
                searchCount.setText(count + "");
            } else {
                searchCount.setText("");
            }
        }
    }

    private Boolean check(Matcher matcher, XmlTreeNode node) {
        Boolean found;
        if (matcher.group("xpath") != null) {
            found = node.getXpath().contains(matcher.group("xpath"));
        } else if (matcher.group("node") != null) {
            found = node.toString().contains(matcher.group("node"))
                    && node.getText().contains(matcher.group("value"));
        } else if (matcher.group("val") != null) {
            found = node.getText().contains(matcher.group("val"));
        } else {
            found = node.toString().contains(matcher.group("any"));
        }
        return found;
    }

    private void nextSearch(String text) {
        if (xmlTree.getSelectionPath() != null) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.matches()) {
                XmlTreeNode curr = (XmlTreeNode) xmlTree.getSelectionPath().getLastPathComponent();
                if (curr.getNextNode() != null) {
                    curr = (XmlTreeNode) curr.getNextNode();
                } else {
                    return;
                }
                while (curr.getNextNode() != null) {
                    curr = (XmlTreeNode) curr.getNextNode();
                    if (check(matcher, curr)) {
                        selectAndScroll(curr);
                        break;
                    }
                }
            }
        } else {
            searchAndSelect(text);
        }
    }

    private void prevoiusSearch(String text) {
        if (xmlTree.getSelectionPath() != null) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.matches()) {
                XmlTreeNode curr = (XmlTreeNode) xmlTree.getSelectionPath().getLastPathComponent();
                if (curr.getNextNode() != null) {
                    curr = (XmlTreeNode) curr.getPreviousNode();
                } else {
                    return;
                }
                while (curr.getPreviousNode() != null) {
                    curr = (XmlTreeNode) curr.getPreviousNode();
                    if (check(matcher, curr)) {
                        selectAndScroll(curr);
                        break;
                    }
                }
            }
        } else {
            searchAndSelect(text);
        }
    }

    private void selectAndScroll(XmlTreeNode node) {
        TreePath path = new TreePath(node.getPath());
        xmlTree.setSelectionPath(path);
        xmlTree.scrollPathToVisible(path);
    }

    private void initMocks() {
        XmlTreeNode[] nodes = getSelectedNodes();
        if (nodes != null) {
            Set<XmlTreeNode> nodeSet = new LinkedHashSet<>();
            for (XmlTreeNode node : nodes) {
                nodeSet.add(node);
                if (!node.isLeaf()) {
                    nodeSet.addAll(Collections.list(node.children()));
                    if (mockLeafOnly.isSelected()) {
                        nodeSet.remove(node);
                    }
                }
            }
            mockTable.setModel(new XmlTableModel(
                    Integer.valueOf(mockNum.getValue().toString()),
                    new ArrayList<>(nodeSet),
                    nodeTextOnly.isSelected()));
            Utils.resizeTable(mockTable);
            if (nodeTextOnly.isSelected()) {
                mockTable.setRowHeight(20);
                mockTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                initAutoSuggest();
            }
        }
    }

    private void initAutoSuggest() {
        SimpleXmlModel xmlModel = (SimpleXmlModel) xmlListModel.get(xmlList.getSelectedIndex());
        XsdData xsdData = xmlModel.getXsdData();
        if (xsdData != null) {
            XmlTableModel model = (XmlTableModel) mockTable.getModel();
            for (int i = 1; i < mockTable.getColumnCount(); i++) {
                XmlTreeNode node = model.getNodeValueAt(0, i);
                List<String> enums = xsdData.getEnum(node.getName());
                if (enums != null) {
                    mockTable.getColumnModel().getColumn(i).setCellEditor(
                            new DefaultCellEditor(new JComboBox(enums.toArray())));
                }
            }
        }
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
                    javax.swing.UIManager.put("Button.disabledText", Color.GRAY);
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(XmlUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                XmlUI xml = new XmlUI();
                xml.setVisible(true);
                for (File[] vals : RecentItems.getRecentItems()) {
                    xml.checkAndAdd(vals[0], vals[1]);
                }
                xml.setExtendedState(XmlUI.MAXIMIZED_BOTH);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addNode;
    private javax.swing.JMenuItem addNodeMenuItem;
    private javax.swing.JCheckBoxMenuItem applyChangesMenuItem;
    private javax.swing.JButton clearMocks;
    private javax.swing.JMenuItem collapseNodes;
    private javax.swing.JButton createMockXmls;
    private javax.swing.JButton createMocks;
    private javax.swing.JButton deleteNode;
    private javax.swing.JMenuItem deleteNodesMenuItem;
    private javax.swing.JMenuItem expandNodes;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JScrollPane listScrollPane;
    private javax.swing.JSplitPane listTreeSplitpane;
    private javax.swing.JMenuItem loadMenuItem;
    private javax.swing.JButton loadXml;
    private javax.swing.JButton loadXsdFromFile;
    private javax.swing.JTextField mockDataName;
    private javax.swing.JToggleButton mockDataToggle;
    private javax.swing.JDialog mockDetails;
    private javax.swing.JCheckBox mockLeafOnly;
    private javax.swing.JSpinner mockNum;
    private javax.swing.JPanel mockPanel;
    private javax.swing.JButton mockStart;
    private javax.swing.JTable mockTable;
    private javax.swing.JTextField mockXMLSaveLocation;
    private javax.swing.JButton nextSearch;
    private javax.swing.JCheckBox nodeTextOnly;
    private javax.swing.JButton previousSearch;
    private javax.swing.JMenuItem quitMenuItem;
    private javax.swing.JMenuItem reloadMenuItem;
    private javax.swing.JButton renameNode;
    private javax.swing.JMenuItem renameNodeMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JButton saveMockXmlTo;
    private javax.swing.JButton saveXml;
    private javax.swing.JToolBar searchBar;
    private javax.swing.JLabel searchCount;
    private javax.swing.JTextField searchText;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JSplitPane treeMockPane;
    private javax.swing.JPanel treePanel;
    private javax.swing.JScrollPane treeScrollPane;
    private javax.swing.JSplitPane treeTableSplitPane;
    private javax.swing.JCheckBox validateAganistSchema;
    private javax.swing.JFileChooser xmlFileChooser;
    private javax.swing.JList xmlList;
    private javax.swing.JTable xmlPropTable;
    private javax.swing.JTree xmlTree;
    private javax.swing.JFileChooser xsdFileChooser;
    private javax.swing.JTextField xsdFileName;
    private javax.swing.JTextField xsdLocation;
    // End of variables declaration//GEN-END:variables

    class TreePopupMenu extends JPopupMenu implements ActionListener {

        public TreePopupMenu() {
            int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
            create("Add Node", KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcut));
            create("Rename Node", KeyStroke.getKeyStroke(KeyEvent.VK_F2, shortcut));
            create("Delete Nodes", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
            addSeparator();
            create("Duplicate Node", KeyStroke.getKeyStroke(KeyEvent.VK_D, shortcut));
            create("Toggle Comment", KeyStroke.getKeyStroke(KeyEvent.VK_T, shortcut));
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
                case "Duplicate Node":
                    duplicateNode();
                    break;
                case "Toggle Comment":
                    toggleComment();
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

    class MockRenderer implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof XmlTreeNode) {
                XmlTreeNode node = (XmlTreeNode) value;
                JTable nTable = new JTable(node);
                nTable.setCellSelectionEnabled(true);
                nTable.setPreferredSize(new Dimension(200, 80));

                JPanel panel = new JPanel(new BorderLayout());
                panel.add(nTable.getTableHeader(), BorderLayout.NORTH);
                panel.add(nTable, BorderLayout.CENTER);
                panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));;
                return panel;
            }
            return null;
        }

    }

    class MockHeaderRenderer implements TableCellRenderer {

        TableCellRenderer defHeaderRenderer;

        public MockHeaderRenderer() {
            defHeaderRenderer = mockTable.getTableHeader().getDefaultRenderer();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JComponent comp = (JComponent) defHeaderRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (table.getModel() instanceof XmlTableModel) {
                XmlTableModel model = (XmlTableModel) table.getModel();
                comp.setToolTipText(model.getColumnToolTip(column));
            }
            return comp;
        }

    }

    class MockCellEditor extends AbstractCellEditor implements TableCellEditor {

        JScrollPane val;
        JTable nTable;

        @Override
        public Component getTableCellEditorComponent(JTable table,
                Object value, boolean isSelected, int row, int column) {
            if (value instanceof XmlTreeNode) {
                XmlTreeNode node = (XmlTreeNode) value;
                nTable = new JTable(node);
                nTable.setCellSelectionEnabled(true);
                val = new JScrollPane(nTable);
                return val;
            }
            return null;
        }

        @Override
        public Object getCellEditorValue() {
            return val;
        }

        @Override
        public boolean stopCellEditing() {
            if (super.stopCellEditing()) {
                if (nTable != null && nTable.isEditing() && nTable.getCellEditor() != null) {
                    nTable.getCellEditor().stopCellEditing();
                }
                return true;
            }
            return false;
        }

    }

    class XmlTreeRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component comp = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (value instanceof XmlTreeNode) {
                XmlTreeNode node = (XmlTreeNode) value;
                if (node.isCommented()) {
                    comp.setForeground(Color.RED);
                }
            }
            return comp;
        }

    }
}
