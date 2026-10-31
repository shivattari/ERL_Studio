package ERL.UI.IDE.Workspace;

import ERL.UI.IDE.IDEFrame;
import ERL.UI.IDE.Theme;
import ERL.UI.IDE.Workspace.TreeModel.TreeDataModel;
import ERL.UI.IDE.Workspace.TreeModel.TreeFile;
import ERL.UI.IDE.Workspace.TreeModel.TreeFolder;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * A tree view of the workspace folder, with dropdown folders and selectable files.
 *
 * @author Shivam Attari
 */
public class ExplorerTree extends JPanel
{
    // Constants
    private final Color LIGHT_BG = new Color(248, 248, 248);
    private final Color DARK_BG = new Color(37, 38, 41);

    // Instance Variables
    // ----------| Properties |-------------------------------------------
    private IDEFrame ideFrame;
    private TreeDataModel dataModel;
    private Theme theme;

    // Constructor
    public ExplorerTree(File workspace, IDEFrame ideFrame, Theme theme)
    {
        setDataModel(new TreeDataModel(workspace));
        this.ideFrame = ideFrame;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        refreshTree();

        setTheme(theme);
    }

    // Getters
    public TreeDataModel getDataModel()
    {
        return dataModel;
    }

    public Theme getTheme()
    {
        return theme;
    }

    // Setters
    public void setDataModel(TreeDataModel dataModel)
    {
        this.dataModel = dataModel;
    }

    public void setTheme(Theme theme)
    {
        this.theme = theme;

        setBackground(theme == Theme.DARK ? DARK_BG : LIGHT_BG);
    }

    // Methods
    public void refreshTree()
    {
        removeAll();

        for (TreeFolder treeFolder : getDataModel().getRootFolders())
        {
            addFolderToView(treeFolder, 0);
        }

        for (TreeFile treeFile : getDataModel().getFiles())
        {
            addFileToView(treeFile, 0);
        }

        add(Box.createVerticalGlue());

        revalidate();
        repaint();
    }

    private void addFolderToView(TreeFolder treeFolder, int iLevel)
    {
        FolderNodePanel folderNodePanel = new FolderNodePanel(treeFolder, iLevel, this, getTheme());
        add(folderNodePanel);

        if (treeFolder.isExpanded())
        {
            for (TreeFolder subfolder : treeFolder.getSubfolders())
            {
                addFolderToView(subfolder, iLevel + 1);
            }

            for (TreeFile treeFile : treeFolder.getFiles())
            {
                addFileToView(treeFile, iLevel);
            }
        }
    }

    private void addFileToView(TreeFile treeFile, int iLevel)
    {
        FileNodePanel fileNodePanel = new FileNodePanel(treeFile, iLevel, ideFrame, getTheme());
        add(fileNodePanel);
    }
}
