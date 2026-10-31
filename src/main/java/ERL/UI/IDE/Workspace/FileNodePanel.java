package ERL.UI.IDE.Workspace;

import ERL.UI.IDE.FileType;
import ERL.UI.IDE.IDEFrame;
import ERL.UI.IDE.Theme;
import ERL.UI.IDE.Workspace.TreeModel.TreeDataModel;
import ERL.UI.IDE.Workspace.TreeModel.TreeFile;
import ERL.UI.IDE.Workspace.TreeModel.TreeFolder;
import ERL.UI.LoadImage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * A file panel for a source or resource file, which can be selected to open it.
 *
 * @author Shivam Attari
 */
public class FileNodePanel extends JPanel
{
    // Constants
    private final int ARC_SIZE = 10;
    private final int PADDING = 6;
    private final int HEIGHT = 24;

    // Instance Variables
    private TreeFile treeFile;
    private int iLevel;
    private Theme theme;

    // Constructor
    public FileNodePanel(TreeFile treeFile, int iLevel, IDEFrame ideFrame, Theme theme)
    {
        setTreeFile(treeFile);
        setLevel(iLevel);
        setTheme(theme);

        setLayout(new GridBagLayout());

        // Vertically center content
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        contentPanel.setBackground(new Color(0, 0, 0, 0));

        // Add indentation
        contentPanel.add(Box.createHorizontalStrut(14 * getLevel() + 44));

        JPanel fileIcon = new LoadImage(
                String.format("Explorer/document-%s.png", getTreeFile().getFileType() == FileType.SOURCE ? "source" : "resource"),
                new Dimension(14, 14)
        ).loadImage();
        fileIcon.setBackground(new Color(0, 0, 0, 0));

        contentPanel.add(fileIcon);
        contentPanel.add(Box.createHorizontalStrut(4));

        String szName = getTreeFile().getFile().getName();
        if (getTreeFile().getFileType() == FileType.SOURCE)
        {
            szName = szName.substring(0, szName.lastIndexOf("."));
        }

        JLabel fileLabel = new JLabel(szName);
        fileLabel.setFont(new Font(fileLabel.getFont().getFontName(), Font.PLAIN, 14));
        contentPanel.add(fileLabel);

        add(contentPanel, gbc);

        setMinimumSize(new Dimension(1, HEIGHT));
        setPreferredSize(new Dimension(Integer.MAX_VALUE, HEIGHT));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, HEIGHT));

        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                ExplorerTree tree = ideFrame.getExplorerPanel().getExplorerTree();
                markFileAsSelected(tree.getDataModel());
                tree.refreshTree();

                ideFrame.getToolBar().setToolBarMode(getTreeFile().isSelected() ? getTreeFile().getFileType() : FileType.TXT);
                ideFrame.openFile(getTreeFile().isSelected() ? getTreeFile() : null);
            }
        });
    }

    // Getters
    public TreeFile getTreeFile()
    {
        return treeFile;
    }

    public int getLevel()
    {
        return iLevel;
    }

    public Theme getTheme()
    {
        return theme;
    }

    // Setters
    public void setTreeFile(TreeFile treeFile)
    {
        this.treeFile = treeFile;
    }

    public void setLevel(int iLevel)
    {
        this.iLevel = iLevel;
    }

    public void setTheme(Theme theme)
    {
        this.theme = theme;
    }

    // Methods
    private void markFileAsSelected(TreeDataModel treeDataModel)
    {
        boolean bPrevious = getTreeFile().isSelected();
        for (TreeFolder treeFolder : treeDataModel.getRootFolders())
        {
            deselectFolder(treeFolder);
        }
        for (TreeFile tf : treeDataModel.getFiles())
        {
            tf.setSelected(false);
        }
        getTreeFile().setSelected(!bPrevious);
    }

    private void deselectFolder(TreeFolder treeFolder)
    {
        treeFolder.setSelected(false);
        for (TreeFolder subfolder : treeFolder.getSubfolders())
        {
            deselectFolder(subfolder);
        }
        for (TreeFile tf : treeFolder.getFiles())
        {
            tf.setSelected(false);
        }
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (getTreeFile().isSelected())
        {
            g2d.setColor(getTheme() == Theme.DARK ? new Color(170, 215, 255, 50) : new Color(170, 215, 255, 200));
            g2d.fill(new RoundRectangle2D.Float(PADDING, 0, getWidth() - (PADDING * 2), getHeight(), ARC_SIZE, ARC_SIZE));
        }
    }
}
