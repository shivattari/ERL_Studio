package ERL.UI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * A class to load an image from the resources folder into Java as a JPanel.
 *
 * @author Shivam Attari
 */
public class LoadImage
{
    // Instance Variables
    private String szPath;
    private Dimension dimension;
    private boolean bInvert;

    // Constructor
    public LoadImage(String szPath, Dimension dimension)
    {
        this(szPath, dimension, false);
    }

    public LoadImage(String szPath, Dimension dimension, boolean bInvert)
    {
        setPath(szPath);
        setDimension(dimension);
        setInvert(bInvert);
    }

    // Getters
    public String getPath()
    {
        return szPath;
    }

    public Dimension getDimension()
    {
        return dimension;
    }

    public boolean isInvert()
    {
        return bInvert;
    }

    // Setters
    public void setPath(String szPath)
    {
        this.szPath = szPath;
    }

    public void setDimension(Dimension dimension)
    {
        this.dimension = dimension;
    }

    public void setInvert(boolean bInvert)
    {
        this.bInvert = bInvert;
    }

    // Methods
    public JPanel loadImage()
    {
        try
        {
            BufferedImage originalImage = ImageIO.read(LoadImage.class.getClassLoader().getResource(getPath()));

            // [10] Inverting an image: https://stackoverflow.com/a/8662445
            if (bInvert)
            {
                for (int i = 0 ; i < originalImage.getWidth() ; i++)
                {
                    for (int j = 0 ; j < originalImage.getHeight() ; j++)
                    {
                        int iRGBA = originalImage.getRGB(i, j);
                        Color color = new Color(iRGBA, true);
                        color = new Color(
                                255 - color.getRed(),
                                255 - color.getGreen(),
                                255 - color.getBlue(),
                                color.getAlpha()
                        );
                        originalImage.setRGB(i, j, color.getRGB());
                    }
                }
            }

            int iNewWidth = getDimension().width;
            int iNewHeight = getDimension().height;

            // [11] Loading a high-quality image: https://docs.oracle.com/javase/tutorial/2d/advanced/quality.html
            return new JPanel()
            {
                @Override
                protected void paintComponent(Graphics g)
                {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();

                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    g2d.drawImage(originalImage, 0, 0, iNewWidth, iNewHeight, null);
                }

                @Override
                public Dimension getPreferredSize()
                {
                    return new Dimension(iNewWidth, iNewHeight);
                }
            };
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
