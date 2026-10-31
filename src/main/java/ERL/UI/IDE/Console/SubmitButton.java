package ERL.UI.IDE.Console;

import ERL.UI.IDE.Theme;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * This button allows the user to submit their input to the VM.
 *
 * @author Shivam Attari
 */
public class SubmitButton extends JButton
{
    // Constants
    private final int ICON_SIZE = 20;
    private final Color DARK_BG = new Color(30, 31, 34);

    // Instance Variables
    // ----------| Properties |-------------------------------------------
    private boolean bHovered;
    private Theme theme;
    private boolean bEnabled;
    private boolean bValid;

    // Constructor
    public SubmitButton(Theme theme)
    {
        setTheme(theme);
        setHovered(false);
        setEnabled(false);
        setValid(false);

        setMinimumSize(new Dimension(30, 30));
        setPreferredSize(new Dimension(30, 30));
        setMaximumSize(new Dimension(30, 30));
        setBackground(new Color(0, 0, 0, 0));
        setBorder(BorderFactory.createEmptyBorder());

        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                setHovered(true);
                revalidate();
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                setHovered(false);
                revalidate();
                repaint();
            }
        });
    }

    // Getters
    public boolean isHovered()
    {
        return bHovered;
    }

    public Theme getTheme()
    {
        return theme;
    }

    public boolean isEnabled()
    {
        return bEnabled;
    }

    public boolean isValid()
    {
        return bValid;
    }

    // Setters
    public void setHovered(boolean bHovered)
    {
        this.bHovered = bHovered;
    }

    public void setTheme(Theme theme)
    {
        this.theme = theme;

        revalidate();
        repaint();
    }

    public void setEnabled(boolean bEnabled)
    {
        this.bEnabled = bEnabled;
    }

    public void setValid(boolean bValid)
    {
        this.bValid = bValid;
    }

    // Methods
    @Override
    protected void paintComponent(Graphics g)
    {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g2d.setColor(getTheme() == Theme.DARK ? DARK_BG : Color.WHITE);
        g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getWidth(), getWidth(), getWidth()));

        BufferedImage originalImage = null;

        try
        {
            originalImage = ImageIO.read(getClass().getClassLoader().getResource("Console/arrow-up.png"));
        }
        catch (IOException ignored)
        {
        }

        // [10] Inverting an image: https://stackoverflow.com/a/8662445
        if (getTheme() == Theme.DARK)
        {
            for (int i = 0 ; i < originalImage.getWidth() ; i++)
            {
                for (int j = 0 ; j < originalImage.getHeight() ; j++)
                {
                    int iRGBA = originalImage.getRGB(i, j);
                    Color color = new Color(iRGBA, true);
                    color = new Color(
                            (255 - color.getRed()) / (isEnabled() && isValid() ? 1 : 2),
                            (255 - color.getGreen()) / (isEnabled() && isValid() ? 1 : 2),
                            (255 - color.getBlue()) / (isEnabled() && isValid() ? 1 : 2),
                            color.getAlpha()
                    );
                    originalImage.setRGB(i, j, color.getRGB());
                }
            }
        }

        // Draw image
        g2d.drawImage(
                originalImage,
                (getWidth() - ICON_SIZE) / 2,
                (getWidth() - ICON_SIZE) / 2,
                ICON_SIZE,
                ICON_SIZE,
                null
        );
    }
}
