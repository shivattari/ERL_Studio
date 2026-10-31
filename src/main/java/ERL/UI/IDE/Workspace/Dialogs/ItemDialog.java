package ERL.UI.IDE.Workspace.Dialogs;

import ERL.UI.IDE.IDEFrame;
import ERL.UI.IDE.Workspace.ExplorerTree;
import ERL.UI.LoadImage;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A dialog window to allow the user to manage different types of files or a project.
 *
 * @author Shivam Attari
 */
public class ItemDialog extends JDialog
{
    // Instance Variables
    private IDEFrame ideFrame;

    // ----------| Subviews |---------------------------------------------
    private JComboBox<String> projectOption;
    private JTextField nameField;

    // Constructor
    public ItemDialog(File workspace, ItemType itemType, ExplorerTree explorerTree, IDEFrame ideFrame)
    {
        super((Frame) null, itemType == ItemType.PROJECT ? "Rename Project" : "New Item");

        setLayout(new BorderLayout());

        this.ideFrame = ideFrame;

        // Top banner
        JPanel bannerPanel = new LoadImage(
                String.format("%s.png", itemType == ItemType.SOURCE ? "NewSourceFile" : itemType == ItemType.RESOURCE ? "NewResource" : "RenameProject"),
                new Dimension(600, 100)
        ).loadImage();
        add(bannerPanel, BorderLayout.NORTH);

        // Main content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add components
        List<String> szProjectOptions = new ArrayList<>();
        for (File file : workspace.listFiles())
        {
            if (file.isDirectory())
            {
                szProjectOptions.add(file.getName());
            }
        }
        szProjectOptions.sort(String.CASE_INSENSITIVE_ORDER);

        projectOption = new JComboBox<>(szProjectOptions.toArray(new String[0]));
        if (!szProjectOptions.isEmpty())
        {
            projectOption.setSelectedIndex(0);
        }

        JLabel projectLabel = new JLabel("Project: ");
        JPanel projectPanel = new JPanel(new BorderLayout(5, 5));
        projectPanel.add(projectLabel, BorderLayout.NORTH);
        projectPanel.add(projectOption, BorderLayout.CENTER);
        contentPanel.add(projectPanel, BorderLayout.NORTH);

        nameField = new JTextField();
        JLabel nameLabel = new JLabel(
                String.format(
                        "%s name: ",
                        itemType == ItemType.PROJECT ? "Project" : "File"
                )
        );
        JPanel namePanel = new JPanel(new BorderLayout(5, 5));
        namePanel.add(nameLabel, BorderLayout.NORTH);
        namePanel.add(nameField, BorderLayout.CENTER);
        contentPanel.add(namePanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> setVisible(false));

        JButton createButton = itemType == ItemType.PROJECT
                ? getRenameButton(workspace, explorerTree)
                : getCreateButton(workspace, itemType, explorerTree);
        getRootPane().setDefaultButton(createButton);

        // Set consistent button sizes
        Dimension buttonSize = new Dimension(70, 28);
        setButtonSize(cancelButton, buttonSize);
        setButtonSize(createButton, buttonSize);

        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        buttonPanel.add(createButton);

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(contentPanel, BorderLayout.CENTER);

        pack();
        setSize(600, 324);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    // Methods
    private JButton getCreateButton(File workspace, ItemType itemType, ExplorerTree explorerTree)
    {
        JButton createButton = new JButton("Create");
        createButton.addActionListener(e ->
        {
            String szProject = projectOption.getSelectedItem().toString();
            String szName = nameField.getText();
            boolean bNameExists = false;

            if (szProject.isEmpty())
            {
                JOptionPane.showMessageDialog(
                        this,
                        "Select a project to place the file into.",
                        "No Project Selected",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            File projectDir = new File(workspace, projectOption.getSelectedItem().toString());
            File sourceDir = new File(projectDir, itemType == ItemType.SOURCE ? "Source" : "Resources");

            for (File file : sourceDir.listFiles())
            {
                if (file.getName().equals(szName + ".txt"))
                {
                    bNameExists = true;
                    break;
                }
            }

            if (szName.isEmpty())
            {
                JOptionPane.showMessageDialog(this, "Enter a name for the new file.", "Blank Name", JOptionPane.ERROR_MESSAGE);
            }
            else if (bNameExists)
            {
                JOptionPane.showMessageDialog(this, "A file in your project already exists with the name \"" + szName + "\".", "Name Taken", JOptionPane.ERROR_MESSAGE);
            }
            else
            {
                File newSourceFile = new File(sourceDir, nameField.getText() + ".txt");

                try
                {
                    newSourceFile.createNewFile();
                    explorerTree.getDataModel().refreshDataModel(workspace);
                    explorerTree.refreshTree();
                    setVisible(false);
                }
                catch (IOException ex)
                {
                    JOptionPane.showMessageDialog(this, "An error was encountered when creating the file.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        return createButton;
    }

    private JButton getRenameButton(File workspace, ExplorerTree explorerTree)
    {
        JButton renameButton = new JButton("Rename");
        renameButton.addActionListener(e ->
        {
            String szProject = projectOption.getSelectedItem().toString();
            String szName = nameField.getText();

            if (szProject.isEmpty())
            {
                JOptionPane.showMessageDialog(
                        this,
                        "Select a project to rename.",
                        "No Project Selected",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (szName.isEmpty())
            {
                JOptionPane.showMessageDialog(this, "Enter a name for the project.", "Blank Name", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!szProject.equals(szName))
            {
                File existingFile = null;
                boolean bNameExists = false;

                for (File file : workspace.listFiles())
                {
                    if (file.getName().equals(szProject))
                    {
                        existingFile = file;
                    }
                    if (file.getName().equals(szName))
                    {
                        bNameExists = true;
                        break;
                    }
                }

                if (bNameExists)
                {
                    JOptionPane.showMessageDialog(this, "A project with the name \"" + szName + "\" already exists.", "Name Taken", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (existingFile != null)
                {
                    // Rename project
                    File newFile = new File(workspace, szName);
                    existingFile.renameTo(newFile);
                    existingFile.delete();

                    explorerTree.getDataModel().refreshDataModel(workspace);
                    explorerTree.refreshTree();

                    ideFrame.setupEditor(null);
                    setVisible(false);
                }
            }
        });
        return renameButton;
    }

    private void setButtonSize(JButton button, Dimension size)
    {
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
        button.putClientProperty("JComponent.sizeVariant", "regular");
        button.setMargin(new Insets(0, 0, 0, 0));
    }
}
