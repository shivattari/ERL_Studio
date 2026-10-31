package ERL.UI.IDE.Workspace.TreeModel;

import ERL.UI.IDE.FileType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A data model to contain folders and files for a directory.
 *
 * @author Shivam Attari
 */
public class TreeDataModel
{
    // Instance Variables
    private List<TreeFolder> rootFolders;
    private List<TreeFile> files;

    // Constructor
    public TreeDataModel()
    {
        this(null);
    }

    public TreeDataModel(File workspace)
    {
        setRootFolders(new ArrayList<>());
        setFiles(new ArrayList<>());
        refreshDataModel(workspace);
    }

    // Getters
    public List<TreeFolder> getRootFolders()
    {
        return rootFolders;
    }

    public List<TreeFile> getFiles()
    {
        return files;
    }

    // Setters
    public void setRootFolders(List<TreeFolder> rootFolders)
    {
        this.rootFolders = rootFolders;
    }

    public void setFiles(List<TreeFile> files)
    {
        this.files = files;
    }

    // Methods
    public TreeFile findSelectedFile()
    {
        for (TreeFolder rootFolder : getRootFolders())
        {
            TreeFile nestedFile = searchFolderForSelection(rootFolder);
            if (nestedFile != null)
            {
                return nestedFile;
            }
        }
        for (TreeFile file : getFiles())
        {
            if (file.isSelected())
            {
                return file;
            }
        }
        return null;
    }

    public TreeFile searchFolderForSelection(TreeFolder folder)
    {
        for (TreeFolder subFolder : folder.getSubfolders())
        {
            TreeFile nestedFile = searchFolderForSelection(subFolder);
            if (nestedFile != null)
            {
                return nestedFile;
            }
        }
        for (TreeFile file : folder.getFiles())
        {
            if (file.isSelected())
            {
                return file;
            }
        }
        return null;
    }

    public void refreshDataModel(File workspace)
    {
        if (workspace != null)
        {
            // Store current states before refresh
            Map<String, Boolean> folderExpansionStates = new HashMap<>();
            Map<String, Boolean> folderSelectionStates = new HashMap<>();
            Map<String, Boolean> fileSelectionStates = new HashMap<>();

            // Save current states, including files in subfolders
            saveStates(getRootFolders(), folderExpansionStates, folderSelectionStates, fileSelectionStates);
            saveFileStates(getFiles(), fileSelectionStates);

            // Clear and rebuild the tree
            getRootFolders().clear();
            getFiles().clear();

            // Rebuild with preserved states
            for (File file : workspace.listFiles())
            {
                if (file.isDirectory())
                {
                    TreeFolder treeFolder = createTreeFolder(file, folderExpansionStates,
                            folderSelectionStates, fileSelectionStates);
                    getRootFolders().add(treeFolder);
                }
                else
                {
                    String szExtension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
                    if (szExtension.equals("txt"))
                    {
                        TreeFile treeFile = createTreeFile(file, FileType.TXT, fileSelectionStates);
                        getFiles().add(treeFile);
                    }
                }
            }
        }
    }

    private TreeFolder createTreeFolder(File file, Map<String, Boolean> expansionStates,
                                        Map<String, Boolean> folderSelectionStates,
                                        Map<String, Boolean> fileSelectionStates)
    {
        TreeFolder folder = new TreeFolder(file);

        // Restore expansion state
        String folderPath = file.getAbsolutePath();
        folder.setExpanded(expansionStates.getOrDefault(folderPath, false));
        folder.setSelected(folderSelectionStates.getOrDefault(folderPath, false));

        // Clear default lists created by constructor
        folder.getSubfolders().clear();
        folder.getFiles().clear();

        // Rebuild with preserved states
        for (File subfile : file.listFiles())
        {
            if (subfile.isDirectory())
            {
                TreeFolder subFolder = createTreeFolder(subfile, expansionStates,
                        folderSelectionStates, fileSelectionStates);
                folder.getSubfolders().add(subFolder);
            }
            else
            {
                String szExtension = subfile.getName().substring(subfile.getName().lastIndexOf(".") + 1);
                if (szExtension.equals("txt"))
                {
                    FileType fileType = file.getName().equals("Source") ? FileType.SOURCE : FileType.TXT;
                    TreeFile treeFile = createTreeFile(subfile, fileType, fileSelectionStates);
                    folder.getFiles().add(treeFile);
                }
            }
        }

        return folder;
    }

    private TreeFile createTreeFile(File file, FileType fileType, Map<String, Boolean> fileSelectionStates)
    {
        TreeFile treeFile = new TreeFile(file, fileType);
        String filePath = file.getAbsolutePath();
        treeFile.setSelected(fileSelectionStates.getOrDefault(filePath, false));
        return treeFile;
    }

    private void saveStates(List<TreeFolder> folders, Map<String, Boolean> expansionStates,
                            Map<String, Boolean> folderSelectionStates,
                            Map<String, Boolean> fileSelectionStates)
    {
        for (TreeFolder folder : folders)
        {
            // Save folder states
            String folderPath = folder.getFile().getAbsolutePath();
            expansionStates.put(folderPath, folder.isExpanded());
            folderSelectionStates.put(folderPath, folder.isSelected());

            // Save states for files within this folder
            for (TreeFile file : folder.getFiles())
            {
                String filePath = file.getFile().getAbsolutePath();
                fileSelectionStates.put(filePath, file.isSelected());
            }

            // Recursively save states for subfolders
            saveStates(folder.getSubfolders(), expansionStates, folderSelectionStates, fileSelectionStates);
        }
    }

    private void saveFileStates(List<TreeFile> files, Map<String, Boolean> fileSelectionStates)
    {
        for (TreeFile file : files)
        {
            String filePath = file.getFile().getAbsolutePath();
            fileSelectionStates.put(filePath, file.isSelected());
        }
    }
}
