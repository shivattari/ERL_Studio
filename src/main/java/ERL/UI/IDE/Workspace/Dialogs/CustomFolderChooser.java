package ERL.UI.IDE.Workspace.Dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomFolderChooser extends JDialog
{
    private File baseDirectory, currentDirectory, selectedFolder;
    private JComboBox<File> pathComboBox;
    private JButton upOneLevelButton;
    private JList<File> folderList;
    private DefaultListModel<File> listModel;
    private JTextField pathTextField;

    public CustomFolderChooser(Frame parent, File baseDirectory)
    {
        super(parent, "Select Folder", true);

        try
        {
            this.baseDirectory = baseDirectory.getCanonicalFile();
        }
        catch (IOException e)
        {
            this.baseDirectory = baseDirectory;
        }
        currentDirectory = this.baseDirectory;

        setupComponents();
        refreshDirectory();
        setSize(500, 400);
        setLocationRelativeTo(parent);
    }

    private void setupComponents()
    {
        setLayout(new BorderLayout(5, 5));

        // Dropdown for going up path
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Look In:"));

        pathComboBox = new JComboBox<>();
        pathComboBox.setPreferredSize(new Dimension(200, 25));

        pathComboBox.setRenderer(new DefaultListCellRenderer()
        {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int iIndex, boolean bSelected, boolean bFocused)
            {
                super.getListCellRendererComponent(list, value, iIndex, bSelected, bFocused);
                if (value instanceof File file)
                {
                    String szName = file.getName();
                    setText(szName.isEmpty() ? file.getAbsolutePath() : szName);
                }
                return this;
            }
        });

        pathComboBox.addActionListener(e ->
        {
            File selectedFile = (File) pathComboBox.getSelectedItem();
            if (selectedFile != null && !selectedFile.equals(currentDirectory))
            {
                navigateToDirectory(selectedFile);
            }
        });

        topPanel.add(pathComboBox);
        add(topPanel, BorderLayout.NORTH);

        // List of subfolders
        listModel = new DefaultListModel<>();
        folderList = new JList<>(listModel);
        folderList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        folderList.setCellRenderer(new DefaultListCellRenderer()
        {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int iIndex, boolean bSelected, boolean bFocused)
            {
                super.getListCellRendererComponent(list, value, iIndex, bSelected, bFocused);
                if (value instanceof File file)
                {
                    String szName = file.getName().isEmpty() ? file.getAbsolutePath() : file.getName();
                    setText(szName);
                    setIcon(UIManager.getIcon("FileView.directoryIcon"));
                }
                return this;
            }
        });
        folderList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    File selectedFile = folderList.getSelectedValue();
                    if (selectedFile != null && selectedFile.isDirectory())
                    {
                        navigateToDirectory(selectedFile);
                    }
                }
            }
        });
        add(new JScrollPane(folderList), BorderLayout.CENTER);

        // Text field for user entered path and buttons
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        pathTextField = new JTextField();
        pathTextField.addActionListener(e -> navigateToTextFieldPath());
        bottomPanel.add(pathTextField);
        bottomPanel.add(Box.createVerticalStrut(5));

        JPanel buttonsPanel = new JPanel(new BorderLayout());
        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        upOneLevelButton = new JButton("Up One Level");
        upOneLevelButton.addActionListener(e ->
        {
            File parentFolder = currentDirectory.getParentFile();
            if (parentFolder != null && isWithinBase(parentFolder))
            {
                navigateToDirectory(parentFolder);
            }
        });
        JButton newFolderButton = new JButton("New Folder");
        newFolderButton.addActionListener(e -> createNewFolder());
        leftButtons.add(upOneLevelButton);
        leftButtons.add(newFolderButton);

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e ->
        {
            selectedFolder = null;
            dispose();
        });
        JButton selectButton = new JButton("Select");
        selectButton.addActionListener(e ->
        {
            File selectedFile = folderList.getSelectedValue();
            selectedFolder = (selectedFile != null) ? selectedFile : currentDirectory;
            dispose();
        });
        getRootPane().setDefaultButton(selectButton);
        rightButtons.add(cancelButton);
        rightButtons.add(selectButton);

        buttonsPanel.add(leftButtons, BorderLayout.WEST);
        buttonsPanel.add(rightButtons, BorderLayout.EAST);
        bottomPanel.add(buttonsPanel);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void refreshDirectory()
    {
        // Refresh dropdown
        List<File> pathChain = getPathChain(currentDirectory);
        DefaultComboBoxModel<File> comboModel = new DefaultComboBoxModel<>();
        pathChain.forEach(comboModel::addElement);
        pathComboBox.setModel(comboModel);
        pathComboBox.setSelectedItem(currentDirectory);

        // Refresh list
        listModel.clear();
        File[] subFolders = currentDirectory.listFiles(File::isDirectory);
        if (subFolders != null)
        {
            Arrays.sort(subFolders, (f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()));
            for (File file : subFolders)
            {
                listModel.addElement(file);
            }
        }
        pathTextField.setText(currentDirectory.getAbsolutePath());
        upOneLevelButton.setEnabled(!currentDirectory.equals(baseDirectory));
    }

    private List<File> getPathChain(File folder)
    {
        List<File> chain = new ArrayList<>();
        for (File f = folder ; f != null && isWithinBase(f) ; f = f.getParentFile())
        {
            chain.add(0, f);
            if (f.equals(baseDirectory))
            {
                break;
            }
        }
        return chain;
    }

    private void navigateToDirectory(File folder)
    {
        if (folder != null && folder.isDirectory() && isWithinBase(folder))
        {
            currentDirectory = folder;
            refreshDirectory();
        }
        else
        {
            JOptionPane.showMessageDialog(this, "Invalid directory.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void navigateToTextFieldPath()
    {
        String szPath = pathTextField.getText().trim();
        File folder = new File(szPath);
        if (!folder.isAbsolute())
        {
            folder = new File(currentDirectory, szPath);
        }

        try
        {
            folder = folder.getCanonicalFile();
            if (folder.isDirectory() && isWithinBase(folder))
            {
                navigateToDirectory(folder);
            }
            else
            {
                JOptionPane.showMessageDialog(this, "Invalid path.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(this, "Error accessing path.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createNewFolder()
    {
        String szName = JOptionPane.showInputDialog(this, "New folder name:");
        if (szName != null && !szName.trim().isEmpty())
        {
            File newFolder = new File(currentDirectory, szName.trim());
            if (newFolder.exists())
            {
                JOptionPane.showMessageDialog(this, "Folder exists.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            else if (newFolder.mkdir())
            {
                refreshDirectory();

                for (int i = 0 ; i < listModel.getSize() ; i++)
                {
                    File file = listModel.getElementAt(i);
                    try
                    {
                        if (file.getCanonicalPath().equals(newFolder.getCanonicalPath()))
                        {
                            folderList.setSelectedIndex(i);
                            folderList.ensureIndexIsVisible(i);
                            break;
                        }
                    }
                    catch (IOException ignored)
                    {
                    }
                }
            }
            else
            {
                JOptionPane.showMessageDialog(this, "Failed to create folder.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean isWithinBase(File folder)
    {
        try
        {
            File base = baseDirectory.getCanonicalFile();
            File directory = folder.getCanonicalFile();

            String szBasePath = base.getAbsolutePath();
            String szDirectoryPath = directory.getAbsolutePath();

            if (szDirectoryPath.equals(szBasePath))
            {
                return true;
            }

            if (!szBasePath.endsWith(File.separator))
            {
                szBasePath += File.separator;
            }

            return szDirectoryPath.startsWith(szBasePath);
        }
        catch (IOException e)
        {
            return false;
        }
    }

    public File showDialog()
    {
        setVisible(true);
        return selectedFolder;
    }
}