package ERL.UI.IDE.Issues;

import ERL.Issues.Error;
import ERL.Issues.Issue;
import ERL.UI.IDE.Theme;
import com.formdev.flatlaf.util.SystemInfo;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays a list of all issues present in the open source file.
 *
 * @author Shivam Attari
 */
public class IssueSummaryPanel extends JPanel
{
    // Constants
    private final int ARC_SIZE = 16;
    private final int ITEM_ARC_SIZE = 12;
    private final int PADDING = 4;
    private final int ITEM_PADDING = 4;
    private final int ICON_SIZE = 16;
    private final Color BACKGROUND_COLOR_LIGHT = new Color(235, 235, 235);
    private final Color ITEM_BACKGROUND_COLOR_LIGHT = new Color(250, 250, 250);
    private final Color BACKGROUND_COLOR_DARK = new Color(35, 35, 35, 255);
    private final Color ITEM_BACKGROUND_COLOR_DARK = new Color(60, 60, 60, 235);

    // Instance Variables
    private List<Issue> issues;
    private Theme theme;

    // Constructors
    public IssueSummaryPanel(Theme theme)
    {
        setIssues(new ArrayList<>());
        setTheme(theme);
    }

    // Getters
    public List<Issue> getIssues()
    {
        return issues;
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

    public void setTheme(Theme theme)
    {
        this.theme = theme;

        revalidate();
        repaint();
    }

    // Methods
    public void refreshIssueList()
    {
        removeAll();
        updateSize();
        revalidate();
        repaint();
    }

    public void updateSize()
    {
        // [17] Size of font: https://stackoverflow.com/q/46583412
        FontMetrics fm = getFontMetrics(getFont());
        int iMaxWidth = 0;
        int iHeight = PADDING * 2;

        for (int i = 0 ; i < getIssues().size() ; i++)
        {
            Issue issue = getIssues().get(i);
            int iItemWidth = ITEM_PADDING * 5 + ICON_SIZE + 5 + fm.stringWidth(issue.getDescription()) + fm.stringWidth("Line " + issue.getLine());
            iMaxWidth = Math.max(iMaxWidth, iItemWidth);
            iHeight += ICON_SIZE + ITEM_PADDING * 2 + (i < getIssues().size() - 1 ? 6 : 1);
        }

        if (getIssues().isEmpty())
        {
            iMaxWidth = ITEM_PADDING * 2 + ICON_SIZE + 5 + fm.stringWidth("No Issues");
            iHeight += ICON_SIZE + ITEM_PADDING * 2 + 1;
        }

        setSize(new Dimension(iMaxWidth + PADDING * 2 + (SystemInfo.isWindows ? 2 : 0), iHeight - (SystemInfo.isWindows ? 1 : 0)));
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        FontMetrics fm = g2d.getFontMetrics();

        // Draw main tooltip background
        g2d.setColor(new Color(210, 210, 210, getTheme() == Theme.DARK ? 50 : 255));
        g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), ARC_SIZE, ARC_SIZE));
        g2d.setColor(getTheme() == Theme.DARK ? BACKGROUND_COLOR_DARK : BACKGROUND_COLOR_LIGHT);
        g2d.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, ARC_SIZE - 2, ARC_SIZE - 2));

        int iY = PADDING;

        if (getIssues().isEmpty())
        {
            int iItemHeight = Math.max(ICON_SIZE, fm.getHeight()) + ITEM_PADDING * 2 + (SystemInfo.isWindows ? 1 : 0);

            // Draw item background
            g2d.setColor(getTheme() == Theme.DARK ? ITEM_BACKGROUND_COLOR_DARK : ITEM_BACKGROUND_COLOR_LIGHT);
            g2d.fill(new RoundRectangle2D.Float(
                    PADDING, iY, getWidth() - PADDING * 2, iItemHeight, ITEM_ARC_SIZE, ITEM_ARC_SIZE
            ));

            BufferedImage originalImage = null;

            try
            {
                originalImage = ImageIO.read(getClass().getClassLoader().getResource("Issues/check.png"));
            }
            catch (IOException ignored)
            {
            }

            // Draw image
            g2d.drawImage(originalImage,
                    PADDING + ITEM_PADDING,
                    iY + ITEM_PADDING,
                    ICON_SIZE,
                    ICON_SIZE,
                    null);

            // Draw text
            g2d.setColor(getTheme() == Theme.DARK ? Color.WHITE : Color.BLACK);
            g2d.drawString("No Issues",
                    PADDING + ITEM_PADDING + ICON_SIZE + 5,
                    iY + ITEM_PADDING + g2d.getFontMetrics().getAscent()
            );
        }

        for (Issue issue : getIssues())
        {
            int iItemHeight = Math.max(ICON_SIZE, fm.getHeight()) + ITEM_PADDING * 2;

            // Draw item background
            g2d.setColor(getTheme() == Theme.DARK ? ITEM_BACKGROUND_COLOR_DARK : ITEM_BACKGROUND_COLOR_LIGHT);
            g2d.fill(new RoundRectangle2D.Float(
                    PADDING, iY, getWidth() - PADDING * 2, iItemHeight, ITEM_ARC_SIZE, ITEM_ARC_SIZE
            ));

            // Draw image
            BufferedImage originalImage = null;

            try
            {
                if (issue instanceof Error)
                {
                    originalImage = ImageIO.read(getClass().getClassLoader().getResource("Issues/error.png"));
                }
                else
                {
                    originalImage = ImageIO.read(getClass().getClassLoader().getResource("Issues/warning.png"));
                }
            }
            catch (IOException ignored)
            {
            }

            g2d.drawImage(originalImage,
                    PADDING + ITEM_PADDING,
                    iY + ITEM_PADDING,
                    ICON_SIZE,
                    ICON_SIZE,
                    null);

            // Draw text
            g2d.setColor(getTheme() == Theme.DARK ? Color.LIGHT_GRAY : Color.DARK_GRAY);
            g2d.drawString("Line " + issue.getLine(),
                    PADDING + ITEM_PADDING + ICON_SIZE + 5,
                    iY + ITEM_PADDING + fm.getAscent());

            g2d.setColor(getTheme() == Theme.DARK ? Color.WHITE : Color.BLACK);
            g2d.drawString(issue.getDescription(),
                    PADDING * 3 + ITEM_PADDING + ICON_SIZE + 5 + fm.stringWidth("Line " + issue.getLine()),
                    iY + ITEM_PADDING + fm.getAscent());

            iY += iItemHeight + (SystemInfo.isWindows ? 6 : 5);
        }

        g2d.dispose();
    }
}
