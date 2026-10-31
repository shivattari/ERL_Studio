package ERL.UI.IDE.Workspace;

import ERL.UI.IDE.IDEFrame;
import ERL.UI.IDE.Theme;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * This panel contains an explorer tree view.
 *
 * @author Shivam Attari
 */
public class ExplorerPanel extends JPanel
{
    // Constants
    private final Color LIGHT_BG = new Color(248, 248, 248);
    private final Color DARK_BG = new Color(37, 38, 41);

    // Instance Variables
    // ----------| Subviews |---------------------------------------------
    private ExplorerTree explorerTree;

    // ----------| Properties |-------------------------------------------
    private IDEFrame ideFrame;
    private Theme theme;

    // Constructor
    public ExplorerPanel(IDEFrame ideFrame, Theme theme)
    {
        setLayout(new BorderLayout());

        this.ideFrame = ideFrame;

        explorerTree = new ExplorerTree(ideFrame.getWorkspace(), this.ideFrame, theme);
        setTheme(theme);

        setMinimumSize(new Dimension(350, 0));
    }

    // Getters
    public ExplorerTree getExplorerTree()
    {
        return explorerTree;
    }

    public Theme getTheme()
    {
        return theme;
    }

    // Setters
    public void setTheme(Theme theme)
    {
        this.theme = theme;

        refreshView();
    }

    // Methods
    public void refreshView()
    {
        removeAll();
        if (countFiles(ideFrame.getWorkspace()) > 0)
        {
            explorerTree.setTheme(getTheme());

            JScrollPane scrollPane = new JScrollPane(
                    explorerTree,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            );
            add(scrollPane, BorderLayout.CENTER);
        }
        else
        {
            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setBackground(getTheme() == Theme.DARK ? DARK_BG : LIGHT_BG);

            Box alignmentBox = Box.createVerticalBox();
            alignmentBox.setBackground(emptyPanel.getBackground());
            alignmentBox.add(Box.createVerticalGlue());

            JLabel label1 = new JLabel("Create your first project to get started");
            label1.setFont(new Font(label1.getFont().getFontName(), Font.PLAIN, 16));
            label1.setForeground(Color.LIGHT_GRAY);
            label1.setAlignmentX(Component.CENTER_ALIGNMENT);
            alignmentBox.add(label1);

            JLabel label2 = new JLabel("File > New Project");
            label2.setFont(new Font(label1.getFont().getFontName(), Font.PLAIN, 16));
            label2.setForeground(Color.LIGHT_GRAY);
            label2.setAlignmentX(Component.CENTER_ALIGNMENT);
            alignmentBox.add(label2);

            alignmentBox.add(Box.createVerticalGlue());
            emptyPanel.add(alignmentBox, BorderLayout.CENTER);
            add(emptyPanel, BorderLayout.CENTER);
        }
    }

    private int countFiles(File workspace)
    {
        int iCount = 0;
        for (File file : workspace.listFiles())
        {
            if (file.isDirectory())
            {
                iCount++;
            }
            else if (file.getName().substring(file.getName().lastIndexOf(".") + 1).equals("txt"))
            {
                iCount++;
            }
        }
        return iCount;
    }
}
