package ERL.UI.IDE.Workspace.TreeModel;

import ERL.UI.IDE.FileType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A folder data model to store data about a specified folder and its children.
 *
 * @author Shivam Attari
 */
public class TreeFolder
{
    // Instance Variables
    private List<TreeFolder> subfolders;
    private List<TreeFile> files;
    private File file;
    private boolean bExpanded;
    private boolean bSelected;

    // Constructor
    public TreeFolder(File file)
    {
        setFile(file);
        setSubfolders(new ArrayList<>());
        setFiles(new ArrayList<>());
        setExpanded(false);
        setSelected(false);

        for (File subfile : getFile().listFiles())
        {
            if (subfile.isDirectory())
            {
                TreeFolder treeFolder = new TreeFolder(subfile);
                subfolders.add(treeFolder);
            }
            else
            {
                String szExtension = subfile.getName().substring(subfile.getName().lastIndexOf(".") + 1);

                if (szExtension.equals("txt"))
                {
                    FileType fileType = getFile().getName().equals("Source") ? FileType.SOURCE : FileType.TXT;

                    TreeFile treeFile = new TreeFile(subfile, fileType);
                    getFiles().add(treeFile);
                }
            }
        }
    }

    // Getters
    public List<TreeFolder> getSubfolders()
    {
        return subfolders;
    }

    public List<TreeFile> getFiles()
    {
        return files;
    }

    public File getFile()
    {
        return file;
    }

    public boolean isExpanded()
    {
        return bExpanded;
    }

    public boolean isSelected()
    {
        return bSelected;
    }

    // Setters
    public void setSubfolders(List<TreeFolder> subfolders)
    {
        this.subfolders = subfolders;
    }

    public void setFiles(List<TreeFile> files)
    {
        this.files = files;
    }

    public void setFile(File file)
    {
        this.file = file;
    }

    public void setExpanded(boolean bExpanded)
    {
        this.bExpanded = bExpanded;
    }

    public void setSelected(boolean bSelected)
    {
        this.bSelected = bSelected;
    }
}
