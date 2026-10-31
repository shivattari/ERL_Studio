package ERL.UI.IDE;

import ERL.Issues.Error;
import ERL.Issues.Issue;
import ERL.UI.IDE.Issues.IssueSummaryPanel;
import ERL.UI.LoadImage;
import com.formdev.flatlaf.util.SystemInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * The main toolbar of the window, allowing the user to control the program quickly and easily.
 *
 * @author Shivam Attari
 */
public class IDEToolBar extends JToolBar
{
    // Instance Variables
    // ----------| Subviews |---------------------------------------------
    private JToggleButton workspaceButton;
    private JToggleButton consoleButton;
    private JButton playButton;
    private JButton issueSummaryButton;
    private IssueSummaryPanel issueSummaryPopover;

    // ----------| Subviews |---------------------------------------------
    private boolean bSummaryPopoverShowing;

    // ----------| Properties |-------------------------------------------
    private List<Issue> issues;
    private Theme theme;

    // Constructor
    public IDEToolBar(Theme theme)
    {
        setSummaryPopoverShowing(false);
        setTheme(theme);

        // Buttons
        workspaceButton = new JToggleButton();
        configureButton(workspaceButton, "ToolBar/folder.png");
        workspaceButton.setSelected(true);

        consoleButton = new JToggleButton();
        configureButton(consoleButton, "ToolBar/queue-list.png");

        playButton = new JButton();
        configureButton(playButton, "ToolBar/play.png");

        // Issue Summary
        issueSummaryPopover = new IssueSummaryPanel(getTheme());

        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        contentPanel.setBackground(new Color(0, 0, 0, 0));

        issueSummaryButton = new JButton();
        issueSummaryButton.setLayout(new BoxLayout(issueSummaryButton, BoxLayout.Y_AXIS));
        issueSummaryButton.add(Box.createVerticalGlue());
        issueSummaryButton.add(contentPanel);

        setIssues(new ArrayList<>());
        configureIssueSummaryButton();

        // Layout
        setFloatable(false);
        setToolBarMode(FileType.TXT);

        // Listen for clicks regarding popover
        issueSummaryButton.addActionListener(e ->
        {
            if (isSummaryPopoverShowing())
            {
                hideSummaryPopover();
            }
            else
            {
                showSummaryPopover();
            }

            revalidate();
            repaint();
        });

        // [4] Positioning a JFrame: https://coderanch.com/t/335331/java/JFrame-positioning
        // [16] Mouse event coordinates: https://stackoverflow.com/q/43073365
        Toolkit.getDefaultToolkit().addAWTEventListener(event ->
        {
            if (isSummaryPopoverShowing() && event.getID() == MouseEvent.MOUSE_PRESSED)
            {
                MouseEvent mouseEvent = (MouseEvent) event;
                Point click = mouseEvent.getPoint();
                SwingUtilities.convertPointToScreen(click, mouseEvent.getComponent());

                Point popoverLocation = issueSummaryPopover.getLocationOnScreen();
                Rectangle popoverRect = new Rectangle(
                        popoverLocation.x,
                        popoverLocation.y,
                        issueSummaryPopover.getWidth(),
                        issueSummaryPopover.getHeight()
                );
                Rectangle buttonRect = issueSummaryButton.getBounds();

                if (!popoverRect.contains(click) && !buttonRect.contains(click))
                {
                    hideSummaryPopover();
                    mouseEvent.consume(); // Prevent click propagation
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);
    }


    // Getters
    public boolean isSummaryPopoverShowing()
    {
        return bSummaryPopoverShowing;
    }

    public JToggleButton getWorkspaceButton()
    {
        return workspaceButton;
    }

    public JToggleButton getConsoleButton()
    {
        return consoleButton;
    }

    public JButton getPlayButton()
    {
        return playButton;
    }

    public List<Issue> getIssues()
    {
        return issues;
    }

    public Theme getTheme()
    {
        return theme;
    }

    // Setters
    public void setSummaryPopoverShowing(boolean bSummaryPopoverShowing)
    {
        this.bSummaryPopoverShowing = bSummaryPopoverShowing;
    }

    public void setIssues(List<Issue> issues)
    {
        this.issues = issues;
    }

    public void setTheme(Theme theme)
    {
        this.theme = theme;

        if (issueSummaryPopover != null)
        {
            issueSummaryPopover.setTheme(theme);
        }
        if (issueSummaryButton != null && getIssues() != null)
        {
            configureIssueSummaryButton();
        }
        if (workspaceButton != null)
        {
            configureButton(workspaceButton, "ToolBar/folder.png");
        }
        if (consoleButton != null)
        {
            configureButton(consoleButton, "ToolBar/queue-list.png");
        }
    }

    // Methods
    public void setToolBarMode(FileType fileType)
    {
        removeAll();

        if (SystemInfo.isMacOS)
        {
            add(Box.createRigidArea(new Dimension(74, 34)));
        }

        add(workspaceButton);
        switch (fileType)
        {
            case SOURCE ->
            {
                add(issueSummaryButton);
                add(Box.createHorizontalGlue());
                add(playButton);
            }
            case TXT ->
            {
                add(workspaceButton);
                add(Box.createHorizontalGlue());
            }
        }
        add(consoleButton);
        add(Box.createRigidArea(new Dimension(2, 0)));

        revalidate();
        repaint();
    }

    private void configureButton(AbstractButton button, String szPath)
    {
        button.removeAll();
        JPanel iconPanel = new LoadImage(
                szPath,
                new Dimension(20, 20),
                getTheme() == Theme.DARK && !szPath.equals("ToolBar/play.png")
        ).loadImage();
        iconPanel.setBackground(new Color(0, 0, 0, 0));
        button.setLayout(new BorderLayout());
        button.add(iconPanel, BorderLayout.CENTER);
        button.setMinimumSize(new Dimension(26, 28));
        button.setMaximumSize(new Dimension(26, 28));
    }

    private JPanel getContentPanel()
    {
        for (Component component : issueSummaryButton.getComponents())
        {
            if (component instanceof JPanel)
            {
                return (JPanel) component;
            }
        }
        return null;
    }

    public void configureIssueSummaryButton()
    {
        getContentPanel().removeAll();

        int iErrorCount = 0;
        int iWarningCount = 0;
        for (Issue issue : getIssues())
        {
            if (issue instanceof Error) iErrorCount++;
            else iWarningCount++;
        }

        FontMetrics fm = getFontMetrics(issueSummaryButton.getFont());
        int iWidth = 0;
        if (iErrorCount > 0)
        {
            addSubSummary("Issues/error.png", Integer.toString(iErrorCount));
            iWidth += fm.stringWidth(Integer.toString(iErrorCount)) + 32;
            if (iWarningCount > 0)
            {
                getContentPanel().add(Box.createHorizontalStrut(12));
            }
        }
        if (iWarningCount > 0)
        {
            addSubSummary("Issues/warning.png", Integer.toString(iWarningCount));
            iWidth += fm.stringWidth(Integer.toString(iWarningCount)) + 32;
        }
        if ((iWarningCount + iErrorCount) == 0)
        {
            addSubSummary("Issues/check.png", "No Issues");
            iWidth = fm.stringWidth("No Issues") + 32;
        }

        issueSummaryPopover.setIssues(getIssues());
        issueSummaryPopover.updateSize();

        issueSummaryButton.setMinimumSize(new Dimension(iWidth, 28));
        issueSummaryButton.setPreferredSize(new Dimension(iWidth, 28));
        issueSummaryButton.setMaximumSize(new Dimension(iWidth, 28));
        issueSummaryButton.setSize(new Dimension(iWidth, 28));

        revalidate();
        repaint();
    }

    private void addSubSummary(String szIconPath, String szText)
    {
        JPanel summaryPanel = new JPanel(new BorderLayout(4, 0));
        summaryPanel.setBackground(new Color(0, 0, 0, 0));

        JLabel summaryLabel = new JLabel(szText);
        summaryLabel.setBackground(new Color(0, 0, 0, 0));
        summaryLabel.setForeground(getTheme() == Theme.DARK ? Color.LIGHT_GRAY : Color.DARK_GRAY);

        FontMetrics fm = getFontMetrics(summaryLabel.getFont());
        summaryLabel.setMinimumSize(new Dimension(fm.stringWidth(szText), 16));
        summaryLabel.setPreferredSize(new Dimension(fm.stringWidth(szText), 16));
        summaryLabel.setMaximumSize(new Dimension(fm.stringWidth(szText), 16));
        summaryLabel.setSize(new Dimension(fm.stringWidth(szText), 16));

        JPanel iconPanel = new LoadImage(
                szIconPath,
                new Dimension(16, 16)
        ).loadImage();
        iconPanel.setBackground(new Color(0, 0, 0, 0));

        summaryPanel.add(iconPanel, BorderLayout.WEST);
        summaryPanel.add(summaryLabel, BorderLayout.CENTER);

        summaryPanel.setPreferredSize(new Dimension(summaryPanel.getPreferredSize().width, 16));

        getContentPanel().add(summaryPanel);
    }

    private void showSummaryPopover()
    {
        if (!isSummaryPopoverShowing())
        {
            issueSummaryPopover.refreshIssueList();

            // Position popover below button
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            Point frameLocation = parentFrame.getLocationOnScreen();

            Point buttonLocation = issueSummaryButton.getLocationOnScreen();
            issueSummaryPopover.setLocation(
                    buttonLocation.x - frameLocation.x,
                    buttonLocation.y - frameLocation.y
            );

            // Add popover to layered pane
            JLayeredPane layeredPane = getRootPane().getLayeredPane();
            layeredPane.add(issueSummaryPopover, JLayeredPane.POPUP_LAYER);

            issueSummaryPopover.setVisible(true);

            setSummaryPopoverShowing(true);

            revalidate();
            repaint();
        }
    }

    private void hideSummaryPopover()
    {
        if (isSummaryPopoverShowing())
        {
            issueSummaryPopover.setVisible(false);
            getRootPane().getLayeredPane().remove(issueSummaryPopover);
            setSummaryPopoverShowing(false);

            revalidate();
            repaint();
        }
    }
}
