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
import java.util.List;

/**
 * A tooltip which displays a list of issues that are present on a line.
 *
 * @author Shivam Attari
 */
public class IssueTooltip extends JPanel
{
    // Constants
    private final int ARC_SIZE = SystemInfo.isWindows ? 20 : 16;
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

    // Constructor
    public IssueTooltip(List<Issue> issues, Theme theme)
    {
        setIssues(issues);
        setLayout(null);
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
        removeAll();

        this.issues = issues;
        updateSize();
        revalidate();
        repaint();
    }

    public void setTheme(Theme theme)
    {
        this.theme = theme;

        revalidate();
        repaint();
    }

    // Methods
    private void updateSize()
    {
        FontMetrics fm = getFontMetrics(getFont());
        int iMaxWidth = 0;
        int iHeight = PADDING * 2;

        for (int i = 0 ; i < getIssues().size() ; i++)
        {
            Issue issue = getIssues().get(i);
            int iItemWidth = ITEM_PADDING * 2 + ICON_SIZE + 5 + fm.stringWidth(issue.getDescription());
            iMaxWidth = Math.max(iMaxWidth, iItemWidth);
            iHeight += ICON_SIZE + ITEM_PADDING * 2 + (i < getIssues().size() - 1 ? 6 : 1);
        }

        setPreferredSize(new Dimension(iMaxWidth + PADDING * 2 + (SystemInfo.isWindows ? 4 : 0), iHeight));
    }

    @Override
    protected void paintComponent(Graphics g)
    {

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Draw main tooltip background
        g2d.setColor(new Color(210, 210, 210, getTheme() == Theme.DARK ? 50 : 255));
        g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), ARC_SIZE, ARC_SIZE));
        g2d.setColor(getTheme() == Theme.DARK ? BACKGROUND_COLOR_DARK : BACKGROUND_COLOR_LIGHT);
        g2d.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, ARC_SIZE - 2, ARC_SIZE - 2));

        int iY = PADDING;
        for (Issue issue : getIssues())
        {
            int iItemHeight = Math.max(ICON_SIZE, g2d.getFontMetrics().getHeight()) + ITEM_PADDING * 2 + (SystemInfo.isWindows ? 1 : 0);

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
            g2d.setColor(getTheme() == Theme.DARK ? Color.WHITE : Color.BLACK);
            g2d.drawString(issue.getDescription(),
                    PADDING + ITEM_PADDING + ICON_SIZE + 5,
                    iY + ITEM_PADDING + g2d.getFontMetrics().getAscent()
            );

            iY += iItemHeight + 5;
        }

        g2d.dispose();
    }
}
