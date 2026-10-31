package ERL.UI.IDE.Workspace.TreeModel;

import ERL.UI.IDE.FileType;

import java.io.File;

/**
 * A file data model to store data about a specified file.
 *
 * @author Shivam Attari
 */
public class TreeFile
{
    // Instance Variables
    private File file;
    private FileType fileType;
    private boolean bSelected;

    // Constructor
    public TreeFile(File file, FileType fileType)
    {
        setFile(file);
        setFileType(fileType);
        setSelected(false);
    }

    // Getters
    public boolean isSelected()
    {
        return bSelected;
    }

    public FileType getFileType()
    {
        return fileType;
    }

    public File getFile()
    {
        return file;
    }

    // Setters
    public void setFile(File file)
    {
        this.file = file;
    }

    public void setFileType(FileType fileType)
    {
        this.fileType = fileType;
    }

    public void setSelected(boolean bSelected)
    {
        this.bSelected = bSelected;
    }
}
