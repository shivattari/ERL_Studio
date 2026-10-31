package ERL.UI.IDE;

import ERL.UI.LoadImage;

import javax.swing.*;
import java.awt.*;

public class AboutDialog extends JDialog
{
    public AboutDialog()
    {
        super((Frame) null, "About");

        setLayout(new BorderLayout());

        // Top banner
        JPanel bannerPanel = new LoadImage(
                "AboutBanner.png",
                new Dimension(600, 100)
        ).loadImage();
        add(bannerPanel, BorderLayout.NORTH);

        // Main content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add components
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Built for Gravesend Grammar School.");
        JLabel infoLabel = new JLabel("For educational use only.");
        JLabel copyrightLabel = new JLabel("ERL Studio by Shivam Attari is licensed under CC BY-NC-SA 4.0.");
        infoPanel.add(titleLabel);
        infoPanel.add(infoLabel);
        infoPanel.add(copyrightLabel);

        contentPanel.add(infoPanel, BorderLayout.NORTH);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> setVisible(false));

        // Set consistent button sizes
        Dimension buttonSize = new Dimension(70, 28);
        setButtonSize(closeButton, buttonSize);

        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(closeButton);

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(contentPanel, BorderLayout.CENTER);

        pack();
        setSize(600, 260);
        setResizable(false);
        setLocationRelativeTo(null);
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
