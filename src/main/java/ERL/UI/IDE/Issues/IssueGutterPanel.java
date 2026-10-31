package ERL.UI.IDE.Issues;

import ERL.Issues.Issue;
import ERL.UI.IDE.Theme;
import ERL.UI.LoadImage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An icon to display against incorrect code, and display a tooltip of the description upon hovering.
 *
 * @author Shivam Attari
 */
public class IssueGutterPanel extends JPanel
{
    // Constants
    private final int LINE_HEIGHT = 20;
    private final int ICON_SIZE = 16;
    private static final int TOOLTIP_DELAY = 200;

    // Instance Variables
    private List<Issue> issues;
    private int iLineCount;
    private JWindow currentPopup;
    private javax.swing.Timer showPopupTimer;
    private Theme theme;

    // Constructor
    public IssueGutterPanel(List<Issue> issues, Theme theme)
    {
        setIssues(issues);
        setLineCount(1);
        setLayout(null);
        setBackground(new Color(0, 0, 0, 0));
        addIssueLabels();
        setTheme(theme);
    }

    // Getters
    public List<Issue> getIssues()
    {
        return issues;
    }

    public int getLineCount()
    {
        return iLineCount;
    }

    public Theme getTheme()
    {
        return theme;
    }

    // Setters
    public void setIssues(List<Issue> issues)
    {
        this.issues = issues;
    }

    public void setLineCount(int iLineCount)
    {
        this.iLineCount = iLineCount;
    }

    public void setTheme(Theme theme)
    {
        this.theme = theme;
    }

    // Methods
    public void addIssueLabels()
    {
        removeAll();

        Map<Integer, List<Issue>> lineMap = new HashMap<>();
        for (Issue issue : getIssues())
        {
            List<Issue> lineList = lineMap.getOrDefault(issue.getLine(), new ArrayList<>());
            lineList.add(issue);
            lineMap.put(issue.getLine(), lineList);
        }

        for (Map.Entry<Integer, List<Issue>> entry : lineMap.entrySet())
        {
            boolean bContainsError = false;
            for (Issue issue : entry.getValue())
            {
                if (issue instanceof ERL.Issues.Error)
                {
                    bContainsError = true;
                    break;
                }
            }

            JPanel iconPanel = new LoadImage(
                    bContainsError ? "Issues/error.png" : "Issues/warning.png",
                    new Dimension(ICON_SIZE, ICON_SIZE)
            ).loadImage();
            iconPanel.setBackground(new Color(0, 0, 0, 0));

            iconPanel.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseEntered(MouseEvent e)
                {
                    startTooltipTimer(iconPanel, entry.getValue(), e.getXOnScreen(), e.getYOnScreen());
                }

                @Override
                public void mouseExited(MouseEvent e)
                {
                    cancelTooltipTimer();
                    hideCustomTooltip();
                }
            });

            int yPosition = (entry.getKey() - 1) * LINE_HEIGHT + ((LINE_HEIGHT - ICON_SIZE) / 2);
            iconPanel.setBounds(0, yPosition, ICON_SIZE, ICON_SIZE);
            add(iconPanel);
        }

        revalidate();
        repaint();
    }

    private void showCustomTooltip(Component invoker, List<Issue> lineIssues, int iX, int iY)
    {
        if (currentPopup != null)
        {
            return;
        }

        // Create the popup window
        JWindow popupWindow = new JWindow(SwingUtilities.getWindowAncestor(invoker));
        popupWindow.setType(Window.Type.POPUP);
        popupWindow.setBackground(new Color(0, 0, 0, 0));

        // Create tooltip panel
        JPanel tooltipPanel = new IssueTooltip(lineIssues, getTheme());
        tooltipPanel.setOpaque(false);
        tooltipPanel.setBackground(new Color(0, 0, 0, 0));

        // Add tooltip to window
        popupWindow.setContentPane(tooltipPanel);
        popupWindow.pack();

        // Make the window transparent
        popupWindow.setBackground(new Color(0, 0, 0, 0));

        // Position and show the window
        popupWindow.setLocation(iX, iY);
        popupWindow.setVisible(true);

        currentPopup = popupWindow;
    }

    public void hideCustomTooltip()
    {
        if (currentPopup != null)
        {
            currentPopup.dispose();
        }
        currentPopup = null;
    }

    private void startTooltipTimer(Component invoker, List<Issue> lineIssues, int iX, int iY)
    {
        if (showPopupTimer != null && showPopupTimer.isRunning())
        {
            // If timer is already running, restart it
            showPopupTimer.restart();
        }
        else
        {
            showPopupTimer = new javax.swing.Timer(TOOLTIP_DELAY, e -> showCustomTooltip(invoker, lineIssues, iX, iY));
            showPopupTimer.setRepeats(false);
            showPopupTimer.start();
        }
    }

    private void cancelTooltipTimer()
    {
        if (showPopupTimer != null)
        {
            showPopupTimer.stop();
        }
    }

    @Override
    public Dimension getPreferredSize()
    {
        if (getIssues().isEmpty())
        {
            return new Dimension(ICON_SIZE, 0);
        }
        return new Dimension(ICON_SIZE, iLineCount * LINE_HEIGHT);
    }
}
