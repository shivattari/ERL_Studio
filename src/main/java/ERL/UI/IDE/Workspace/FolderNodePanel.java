package ERL.UI.IDE.Workspace;

import ERL.UI.IDE.Theme;
import ERL.UI.IDE.Workspace.TreeModel.TreeFolder;
import ERL.UI.LoadImage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A folder panel which contains other files and folders. It can be expanded/collapsed to show/hide its children.
 *
 * @author Shivam Attari
 */
public class FolderNodePanel extends JPanel
{
    // Constants
    private final int HEIGHT = 24;

    // Instance Variables
    private TreeFolder treeFolder;
    private int iLevel;
    private Theme theme;

    // Constructor
    public FolderNodePanel(TreeFolder treeFolder, int iLevel, ExplorerTree tree, Theme theme)
    {
        setTreeFolder(treeFolder);
        setLevel(iLevel);
        setTheme(theme);

        setLayout(new GridBagLayout());
        setBackground(new Color(0, 0, 0, 0));

        // Vertically center content
        // [9] Guide to GridBagLayout: https://stackoverflow.com/q/51978116
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
        contentPanel.add(Box.createHorizontalStrut(14 * getLevel() + 6));

        JPanel expandButtonPanel = getExpandButtonPanel(tree);

        contentPanel.add(expandButtonPanel);
        contentPanel.add(Box.createHorizontalStrut(8));

        JPanel folderIcon = new LoadImage(
                getTreeFolder().isExpanded() ? "Explorer/folder-open.png" : "ToolBar/folder.png",
                new Dimension(14, 14),
                getTheme() == Theme.DARK
        ).loadImage();
        folderIcon.setBackground(new Color(0, 0, 0, 0));

        contentPanel.add(folderIcon);
        contentPanel.add(Box.createHorizontalStrut(4));

        JLabel folderLabel = new JLabel(getTreeFolder().getFile().getName());
        folderLabel.setFont(new Font(folderLabel.getFont().getFontName(), Font.PLAIN, 14));
        contentPanel.add(folderLabel);

        add(contentPanel, gbc);

        setMinimumSize(new Dimension(1, HEIGHT));
        setPreferredSize(new Dimension(Integer.MAX_VALUE, HEIGHT));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, HEIGHT));
    }

    // Getters
    public TreeFolder getTreeFolder()
    {
        return treeFolder;
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
    public void setTreeFolder(TreeFolder treeFolder)
    {
        this.treeFolder = treeFolder;
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
    private JPanel getExpandButtonPanel(ExplorerTree tree)
    {
        JPanel expandButtonPanel = new LoadImage(
                String.format("Explorer/chevron-%s.png", getTreeFolder().isExpanded() ? "down" : "right"),
                new Dimension(12, 12),
                getTheme() == Theme.DARK
        ).loadImage();
        expandButtonPanel.setBackground(new Color(0, 0, 0, 0));
        expandButtonPanel.setMinimumSize(new Dimension(12, 12));
        expandButtonPanel.setPreferredSize(new Dimension(12, 12));
        expandButtonPanel.setMaximumSize(new Dimension(12, 12));

        expandButtonPanel.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                getTreeFolder().setExpanded(!getTreeFolder().isExpanded());
                tree.refreshTree();
            }
        });

        return expandButtonPanel;
    }
}
