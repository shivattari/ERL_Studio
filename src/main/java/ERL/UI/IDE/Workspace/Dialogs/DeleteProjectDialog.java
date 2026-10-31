package ERL.UI.IDE.Workspace.Dialogs;

import ERL.UI.IDE.IDEFrame;
import ERL.UI.LoadImage;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A dialog window to allow the user to select a project and delete it.
 *
 * @author Shivam Attari
 */
public class DeleteProjectDialog extends JDialog
{
    // Instance Variables
    // ----------| Subviews |---------------------------------------------
    private JComboBox<String> projectOption;

    // Constructor
    public DeleteProjectDialog(File workspace, IDEFrame ideFrame)
    {
        super((Frame) null, "Delete Project");

        setLayout(new BorderLayout());

        // Top banner
        JPanel bannerPanel = new LoadImage(
                "DeleteProject.png",
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

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> setVisible(false));

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e ->
        {
            for (File file : workspace.listFiles())
            {
                if (file.isDirectory())
                {
                    if (file.getName().equals(projectOption.getSelectedItem().toString()))
                    {
                        deleteProject(file);
                        break;
                    }
                }
            }

            ideFrame.setupEditor(null);
            ideFrame.getExplorerPanel().getExplorerTree().getDataModel().refreshDataModel(workspace);
            ideFrame.getExplorerPanel().getExplorerTree().refreshTree();
            ideFrame.getExplorerPanel().refreshView();
            setVisible(false);
        });

        // Set consistent button sizes
        Dimension buttonSize = new Dimension(70, 28);
        setButtonSize(cancelButton, buttonSize);
        setButtonSize(deleteButton, buttonSize);

        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        buttonPanel.add(deleteButton);

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(contentPanel, BorderLayout.CENTER);

        pack();
        setSize(600, 260);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    // Methods
    public void deleteProject(File projectFile)
    {
        if (projectFile.isDirectory())
        {
            if (projectFile.listFiles() != null)
            {
                for (File file : projectFile.listFiles())
                {
                    deleteProject(file);
                }
            }
        }
        projectFile.delete();
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
