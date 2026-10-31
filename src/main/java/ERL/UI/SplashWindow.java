package ERL.UI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Create a splash screen when opening the IDE.
 *
 * @author Shivam Attari
 */
public class SplashWindow extends JWindow
{
    private BufferedImage splashImage;

    public SplashWindow()
    {
        try
        {
            splashImage = ImageIO.read(getClass().getClassLoader().getResource("IDESplashScreen.png"));
        }
        catch (Exception ignored)
        {
        }

        setSize(580, 340);
        setLocationRelativeTo(null); // Center window
        setBackground(new Color(0, 0, 0, 0)); // Transparent Background

        // Start timer to close splash screen
        // [12] Java Timer guide: https://stackoverflow.com/q/1006611
        Timer timer = new Timer(500, e -> dispose());
        timer.setRepeats(false);
        timer.start();
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(splashImage, 0, 0, 580, 340, null);
    }
}
