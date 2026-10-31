package ERL.UI.IDE;

import ERL.Compiler.Compiler;
import ERL.Compiler.VM.VirtualMachine;
import ERL.Issues.Issue;
import ERL.UI.IDE.Console.ConsolePanel;
import ERL.UI.IDE.Editor.ResourceEditorPane;
import ERL.UI.IDE.Editor.SourceEditorPane;
import ERL.UI.IDE.Workspace.Dialogs.DeleteProjectDialog;
import ERL.UI.IDE.Workspace.Dialogs.ItemDialog;
import ERL.UI.IDE.Workspace.Dialogs.ItemType;
import ERL.UI.IDE.Workspace.ExplorerPanel;
import ERL.UI.IDE.Workspace.TreeModel.TreeFile;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.util.SystemInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * The main frame of the IDE, which manages all main graphical components.
 *
 * @author Shivam Attari
 */
public class IDEFrame extends JFrame
{
    // Constants
    private final Color DARK_BG = new Color(30, 31, 34);
    private final String SZ_CONFIG_PATH = "config.bin";

    // Instance Variables
    // ----------| Subviews |---------------------------------------------
    private JComponent editorComponent;
    private ConsolePanel consolePanel;
    private ExplorerPanel explorerPanel;
    private IDEToolBar toolBar;

    private JSplitPane explorerSplitPane;
    private JSplitPane contentSplitPane;

    // ----------| Properties |-------------------------------------------
    private File workspace;
    private Theme theme;

    // Constructor
    public IDEFrame(File workspace)
    {
        setWorkspace(workspace);
        loadTheme();

        // Configure main toolbar
        toolBar = new IDEToolBar(getTheme());
        add(toolBar, BorderLayout.NORTH);

        // macOS toolbar styling
        if (SystemInfo.isMacOS)
        {
            // Expand window content into window title bar and make title bar transparent
            getRootPane().putClientProperty("apple.awt.fullWindowContent", true);
            getRootPane().putClientProperty("apple.awt.transparentTitleBar", true);

            // Hide window title
            getRootPane().putClientProperty("apple.awt.windowTitleVisible", false);

            // Increase space around window buttons
            rootPane.putClientProperty(FlatClientProperties.MACOS_WINDOW_BUTTONS_SPACING, FlatClientProperties.MACOS_WINDOW_BUTTONS_SPACING_MEDIUM);
        }

        // Setup main content
        explorerPanel = new ExplorerPanel(this, getTheme());

        // Setup split views
        consolePanel = new ConsolePanel(getTheme());

        contentSplitPane = new JSplitPane();
        contentSplitPane.setLeftComponent(editorComponent);
        contentSplitPane.setRightComponent(consolePanel);

        explorerSplitPane = new JSplitPane();
        explorerSplitPane.setRightComponent(contentSplitPane);

        refreshSplitViews();

        toolBar.getConsoleButton().addItemListener(I ->
        {
            refreshSplitViews();
            explorerSplitPane.setDividerLocation(0.25);
            contentSplitPane.setDividerLocation(0.5);
        });
        toolBar.getWorkspaceButton().addItemListener(I ->
        {
            refreshSplitViews();
            contentSplitPane.setDividerLocation(0.5);
            explorerSplitPane.setDividerLocation(0.25);
        });
        toolBar.getPlayButton().addActionListener(I ->
        {
            if (editorComponent instanceof SourceEditorPane sourceEditorPane)
            {
                toolBar.getConsoleButton().setSelected(true);

                Compiler compiler = new Compiler(false);
                compiler.setSourceCode(sourceEditorPane.getText());
                compiler.compile();

                if (compiler.getIssues().isEmpty())
                {
                    File resourceFolder = null;

                    for (File file : explorerPanel.getExplorerTree().getDataModel().findSelectedFile().getFile().getParentFile().getParentFile().listFiles())
                    {
                        if (file.getName().equals("Resources"))
                        {
                            resourceFolder = file;
                            break;
                        }
                    }

                    VirtualMachine vm = new VirtualMachine(
                            compiler.getBytecode(),
                            resourceFolder,
                            consolePanel,
                            false
                    );
                    consolePanel.setVM(vm);
                    vm.execute();

                    reloadExplorer();
                }
            }
        });


        add(explorerSplitPane, BorderLayout.CENTER);

        openSelectedFile();
        createMenuBar();

        pack(); // Required so that content is immediately visible without having to move/resize
        setBounds(getScreenBounds());
        setMinimumSize(new Dimension(600, 450));
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        revalidate();
        repaint();

        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                if (consolePanel != null && consolePanel.getVM() != null)
                {
                    consolePanel.getVM().stop();
                    try
                    {
                        // Allow time for VM to close
                        Thread.sleep(100);
                    }
                    catch (InterruptedException ignored)
                    {
                    }
                }
            }
        });

        setTheme(getTheme()); // Refresh theming
    }

    // Getters
    public File getWorkspace()
    {
        return workspace;
    }

    public Theme getTheme()
    {
        return theme;
    }

    public ExplorerPanel getExplorerPanel()
    {
        return explorerPanel;
    }

    public IDEToolBar getToolBar()
    {
        return toolBar;
    }

    // Setters
    public void setWorkspace(File workspace)
    {
        this.workspace = workspace;
    }

    public void setTheme(Theme theme)
    {
        this.theme = theme;

        switch (theme)
        {
            case LIGHT ->
            {
                FlatLightLaf.setup();
            }
            case DARK ->
            {
                FlatDarkLaf.setup();
            }
        }

        if (editorComponent != null)
        {
            if (editorComponent instanceof SourceEditorPane sourceEditorPane)
            {
                sourceEditorPane.setTheme(theme);
            }
            else if (editorComponent instanceof ResourceEditorPane resourceEditorPane)
            {
                resourceEditorPane.setTheme(theme);
            }
            else
            {
                if (contentSplitPane != null)
                {
                    setupEditor(null);
                }
            }
        }

        if (explorerPanel != null)
        {
            explorerPanel.setTheme(theme);
            explorerPanel.getExplorerTree().refreshTree();
        }

        if (toolBar != null)
        {
            toolBar.setTheme(theme);
        }

        boolean bShowingConsole = false;
        if (consolePanel != null)
        {
            bShowingConsole = toolBar.getConsoleButton().isSelected();

            toolBar.getConsoleButton().setSelected(true);
            consolePanel.setTheme(theme);
        }

        // Refresh view
        SwingUtilities.updateComponentTreeUI(this);

        if (consolePanel != null)
        {
            toolBar.getConsoleButton().setSelected(bShowingConsole);
        }

        try
        {
            writeBit(theme == Theme.LIGHT ? 0 : 1);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // Methods

    /**
     * 0 (false) = light theme
     * 1 (true) = dark theme
     */
    private void loadTheme()
    {
        try
        {
            int iBit = readBit();
            setTheme(iBit == 0 ? Theme.LIGHT : Theme.DARK);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public int readBit() throws IOException
    {
        File file = new File(workspace, SZ_CONFIG_PATH);

        if (!file.exists())
        {
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw"))
            {
                raf.write(0);
            }
            return 0;
        }

        try (RandomAccessFile raf = new RandomAccessFile(file, "r"))
        {
            int iByte = raf.read();
            return (iByte >>> 7) & 1;
        }
    }

    public void writeBit(int iBit) throws IOException
    {
        File file = new File(workspace, SZ_CONFIG_PATH);
        if (!file.exists())
        {
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw"))
            {
                raf.write(0);
            }
        }

        try (RandomAccessFile raf = new RandomAccessFile(file, "rw"))
        {
            int iByte = raf.read();
            if (iByte == -1)
            {
                iByte = 0;
            }

            if (iBit == 1)
            {
                iByte = iByte | 0x80;
            }
            else
            {
                iByte = iByte & 0x7F;
            }

            raf.seek(0);
            raf.write(iByte);
        }
    }

    private Rectangle getScreenBounds()
    {
        GraphicsEnvironment graphics = GraphicsEnvironment.getLocalGraphicsEnvironment();
        return graphics.getMaximumWindowBounds();
    }

    private void refreshSplitViews()
    {
        boolean bShowExplorer = toolBar.getWorkspaceButton().isSelected();
        boolean bShowConsole = toolBar.getConsoleButton().isSelected();

        contentSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        contentSplitPane.setRightComponent(bShowConsole ? consolePanel : null);
        explorerSplitPane.setLeftComponent(bShowExplorer ? explorerPanel : null);
    }

    private void createMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");

        JMenuItem newProjectItem = new JMenuItem("New Project");
        newProjectItem.addActionListener(e -> newProject());

        JMenuItem newSourceFileItem = new JMenuItem("New Source File");
        newSourceFileItem.addActionListener(e -> itemDialog(ItemType.SOURCE));

        JMenuItem newResourceItem = new JMenuItem("New Resource");
        newResourceItem.addActionListener(e -> itemDialog(ItemType.RESOURCE));

        JMenuItem reloadItem = new JMenuItem("Reload from Disk");
        reloadItem.addActionListener(e -> reloadExplorer());

        fileMenu.add(newProjectItem);
        fileMenu.add(newSourceFileItem);
        fileMenu.add(newResourceItem);
        fileMenu.add(reloadItem);
        fileMenu.addSeparator();

        JMenuItem renameProjectItem = new JMenuItem("Rename Project");
        renameProjectItem.addActionListener(e -> itemDialog(ItemType.PROJECT));

        JMenuItem deleteProjectItem = new JMenuItem("Delete Project");
        deleteProjectItem.addActionListener(e -> deleteProjectDialog());

        fileMenu.add(renameProjectItem);
        fileMenu.add(deleteProjectItem);

        if (explorerPanel.getExplorerTree().getDataModel().findSelectedFile() != null)
        {
            JMenuItem renameFileItem = new JMenuItem("Rename File");
            renameFileItem.addActionListener(e -> renameFile());

            JMenuItem deleteFileItem = new JMenuItem("Delete File");
            deleteFileItem.addActionListener(e -> deleteFile());

            fileMenu.addSeparator();
            fileMenu.add(renameFileItem);
            fileMenu.add(deleteFileItem);
        }

        fileMenu.addSeparator();
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> presentAbout());
        fileMenu.add(aboutItem);

        JMenu viewMenu = new JMenu("View");

        JMenuItem themeItem = new JMenuItem(
                String.format(
                        "Switch to %s Mode",
                        getTheme() == Theme.LIGHT ? "Dark" : "Light"
                )
        );
        themeItem.addActionListener(e ->
        {
            setTheme(getTheme() == Theme.DARK ? Theme.LIGHT : Theme.DARK);
            themeItem.setText(
                    String.format(
                            "Switch to %s Mode",
                            getTheme() == Theme.DARK ? "Light" : "Dark"
                    )
            );
        });

        viewMenu.add(themeItem);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);

        if (SystemInfo.isMacOS)
        {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }

        setJMenuBar(menuBar);
    }

    // ----------| File Actions |-----------------------------------------
    public void openSelectedFile()
    {
        TreeFile selectedFile = null;
        if (explorerPanel != null)
        {
            selectedFile = explorerPanel.getExplorerTree().getDataModel().findSelectedFile();
        }

        setupEditor(selectedFile);
    }

    public void openFile(TreeFile file)
    {
        setupEditor(file);
        createMenuBar();
    }

    public void setupEditor(TreeFile file)
    {
        if (file == null)
        {
            JPanel emptyPanel = new JPanel(new BorderLayout());
            JLabel emptyLabel = new JLabel("Open a file to edit it here");

            emptyLabel.setFont(new Font(emptyLabel.getFont().getFontName(), Font.PLAIN, 16));
            emptyLabel.setForeground(Color.LIGHT_GRAY);
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);

            emptyPanel.add(emptyLabel, BorderLayout.CENTER);
            emptyPanel.setBackground(getTheme() == Theme.DARK ? DARK_BG : Color.WHITE);
            emptyPanel.setMinimumSize(new Dimension(350, 0));

            editorComponent = emptyPanel;
        }
        else
        {
            switch (file.getFileType())
            {
                case SOURCE -> editorComponent = new SourceEditorPane(file.getFile(), getTheme(), getToolBar());
                case TXT -> editorComponent = new ResourceEditorPane(file.getFile(), getTheme());
            }
        }
        contentSplitPane.setLeftComponent(editorComponent);
    }

    private void newProject()
    {
        String szProjectName = JOptionPane.showInputDialog("Enter Project Name: ");

        if (szProjectName != null && !szProjectName.isEmpty())
        {
            for (File f : getWorkspace().listFiles())
            {
                if (f.getName().equals(szProjectName))
                {
                    JOptionPane.showMessageDialog(this, "Project already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            File newProjectDir = new File(getWorkspace(), szProjectName);
            newProjectDir.mkdir();

            File newSourceDir = new File(newProjectDir, "Source");
            newSourceDir.mkdir();

            File newResourceDir = new File(newProjectDir, "Resources");
            newResourceDir.mkdir();

            reloadExplorer();
        }

        setTheme(getTheme());
    }

    private void itemDialog(ItemType itemType)
    {
        if (isWorkspaceEmpty())
        {
            JOptionPane.showMessageDialog(
                    this,
                    String.format("Create a project before %s.", itemType == ItemType.PROJECT ? "renaming one" : "creating a file"),
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        ItemDialog newItemDialog = new ItemDialog(getWorkspace(), itemType, explorerPanel.getExplorerTree(), this);
        newItemDialog.setVisible(true);
    }

    private void deleteProjectDialog()
    {
        if (isWorkspaceEmpty())
        {
            JOptionPane.showMessageDialog(this, "Create a project before deleting one.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DeleteProjectDialog deleteProjectDialog = new DeleteProjectDialog(getWorkspace(), this);
        deleteProjectDialog.setVisible(true);
    }

    private void renameFile()
    {
        TreeFile currentFile = explorerPanel.getExplorerTree().getDataModel().findSelectedFile();

        String szFileName = JOptionPane.showInputDialog("Enter New File Name: ", currentFile.getFile().getName().substring(0, currentFile.getFile().getName().lastIndexOf('.')));

        if (szFileName != null && !szFileName.isEmpty() && !(szFileName + ".txt").equals(currentFile.getFile().getName()))
        {
            for (File file : currentFile.getFile().getParentFile().listFiles())
            {
                if (file.getName().equals(szFileName + ".txt"))
                {
                    JOptionPane.showMessageDialog(this, "A file in your project already exists with the name \"" + szFileName + "\".", "Name Taken", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            File newFile = new File(currentFile.getFile().getParentFile(), szFileName + ".txt");
            currentFile.getFile().renameTo(newFile);

            currentFile.getFile().delete();
            currentFile.setFile(newFile);

            setupEditor(null);
            reloadExplorer();
        }
    }

    private void deleteFile()
    {
        TreeFile currentFile = explorerPanel.getExplorerTree().getDataModel().findSelectedFile();

        // [15] Java Option Dialog: https://stackoverflow.com/a/8396900
        int iOption = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete \"" + currentFile.getFile().getName() + "\"?", "Delete File", JOptionPane.YES_NO_OPTION);

        if (iOption == JOptionPane.YES_OPTION)
        {
            currentFile.getFile().delete();
            setupEditor(null);
            reloadExplorer();
        }
    }

    private void reloadExplorer()
    {
        explorerPanel.getExplorerTree().getDataModel().refreshDataModel(getWorkspace());
        explorerPanel.getExplorerTree().refreshTree();
        explorerPanel.refreshView();

        revalidate();
        repaint();
    }

    private boolean isWorkspaceEmpty()
    {
        for (File file : getWorkspace().listFiles())
        {
            if (file.isDirectory())
            {
                return false;
            }
        }
        return true;
    }

    private void presentAbout()
    {
        AboutDialog aboutDialog = new AboutDialog();
        aboutDialog.setVisible(true);
    }
}
