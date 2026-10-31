package ERL.UI;

import ERL.UI.IDE.IDEFrame;
import ERL.UI.IDE.Workspace.Dialogs.CustomFolderChooser;
import com.formdev.flatlaf.util.SystemInfo;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * The user must select their workspace directory on startup.
 *
 * @author Shivam Attari
 */
public class WorkspaceSelector extends JFrame
{
    private File selectedWorkspace;

    public WorkspaceSelector()
    {
        // Initial setup
        setTitle("ERL Studio");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        ImageIcon appIcon = new ImageIcon(WorkspaceSelector.class.getClassLoader().getResource("ERLIcon.png"));
        setIconImage(appIcon.getImage());

        // Top banner
        JPanel bannerPanel = new LoadImage(
                "WorkspaceBanner.png",
                new Dimension(600, 100)
        ).loadImage();
        add(bannerPanel, BorderLayout.NORTH);

        // Main content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Workspace selection components
        JPanel workspacePanel = new JPanel(new BorderLayout(5, 5));
        JLabel label = new JLabel("Workspace: ");

        // Path field
        JTextField pathField = new JTextField();
        pathField.setEditable(false);
        pathField.setFocusable(false);

        // Browse button
        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(e ->
        {
            if (SystemInfo.isMacOS)
            {
                JFileChooser chooser = new JFileChooser();

                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setDialogTitle("Select Workspace Directory");

                // Confirmation action
                if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
                {
                    selectedWorkspace = chooser.getSelectedFile();
                    pathField.setText(selectedWorkspace.getAbsolutePath());
                }
            }
            else
            {
                // NOTE: Adjust root directory as required
                File startDirectory = new File("C:\\");

                CustomFolderChooser chooser = new CustomFolderChooser(this, startDirectory);
                selectedWorkspace = chooser.showDialog();
                pathField.setText(selectedWorkspace.getAbsolutePath());
            }
        });

        // Add components
        JPanel browsePanel = new JPanel(new BorderLayout(5, 0));
        browsePanel.add(pathField, BorderLayout.CENTER);
        browsePanel.add(browseButton, BorderLayout.EAST);

        workspacePanel.add(label, BorderLayout.NORTH);
        workspacePanel.add(browsePanel, BorderLayout.CENTER);
        contentPanel.add(workspacePanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton cancelButton = new JButton("Cancel");
        JButton launchButton = new JButton("Launch IDE");
        getRootPane().setDefaultButton(launchButton);

        // Set consistent button sizes
        Dimension buttonSize = new Dimension(100, 28);
        setButtonSize(cancelButton, buttonSize);
        setButtonSize(launchButton, buttonSize);

        cancelButton.addActionListener(e -> System.exit(0));

        launchButton.addActionListener(e ->
        {
            if (selectedWorkspace == null)
            {
                JOptionPane.showMessageDialog(
                        this,
                        "Please select a workspace directory.",
                        "No Workspace Selected",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // Launch IDE
            IDEFrame ideFrame = new IDEFrame(selectedWorkspace);
            ideFrame.setIconImage(appIcon.getImage());
            ideFrame.setVisible(true);

            dispose();
        });

        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        buttonPanel.add(launchButton);

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(contentPanel, BorderLayout.CENTER);

        // Set frame dimensions
        pack();
        setSize(600, SystemInfo.isWindows ? 274 : 266);
        setLocationRelativeTo(null);
    }

    // Helper method to properly set button dimensions
    private void setButtonSize(JButton button, Dimension size)
    {
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
        button.putClientProperty("JComponent.sizeVariant", "regular");
        button.setMargin(new Insets(0, 0, 0, 0));
    }
}
