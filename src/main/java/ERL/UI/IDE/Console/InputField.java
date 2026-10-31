package ERL.UI.IDE.Console;

import ERL.UI.IDE.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * The input field allows for text input to be entered into the VM.
 *
 * @author Shivam Attari
 */
public class InputField extends JPanel
{
    // Constants
    private final int PADDING = 8;
    private final Color DARK_BG = new Color(30, 31, 34);

    // Instance Variables
    // ----------| Subviews |---------------------------------------------
    private JTextField textField;
    private SubmitButton submitButton;
    private boolean bEnabled;

    // ----------| Properties |-------------------------------------------
    private Theme theme;

    // Constructor
    public InputField(Theme theme)
    {
        setTheme(theme);
        setEnabled(false);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Create text field
        textField = new JTextField("Enter your input here...");
        textField.setBorder(BorderFactory.createEmptyBorder());
        textField.setBackground(new Color(0, 0, 0, 0));
        textField.setForeground(getTheme() == Theme.DARK ? Color.LIGHT_GRAY : Color.DARK_GRAY);
        textField.setPreferredSize(new Dimension(0, 56));

        // [5] Text field focus: https://stackoverflow.com/a/40516250
        textField.addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusGained(FocusEvent e)
            {
                if (textField.getText().equals("Enter your input here..."))
                {
                    textField.setText("");
                    textField.setForeground(getTheme() == Theme.DARK ? Color.WHITE : Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e)
            {
                if (textField.getText().isEmpty())
                {
                    textField.setForeground(Color.GRAY);
                    textField.setText("Enter your input here...");
                }
            }
        });

        textField.addKeyListener(new KeyAdapter()
        {
            private void update()
            {
                String szText = textField.getText();
                if (isEnabled())
                {
                    submitButton.setValid(!szText.isEmpty());
                }
            }

            public void keyTyped(KeyEvent e)
            {
                update();
            }

            public void keyPressed(KeyEvent e)
            {
                update();
            }

            public void keyReleased(KeyEvent e)
            {
                update();
            }
        });

        submitButton = new SubmitButton(getTheme());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, PADDING, 0, 0);
        gbc.weightx = 0.0;
        add(Box.createRigidArea(new Dimension(15, 56)), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 24);
        add(textField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 13);
        add(submitButton, gbc);

        submitButton.addActionListener(e ->
        {
            String input = textField.getText();
            if (!input.isEmpty() && !input.equals("Enter your input here..."))
            {
                ((ConsolePanel) getParent()).submitInput(input);
                textField.setText("");
            }
        });

        // Add key listener for Enter key
        textField.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && submitButton.isEnabled())
                {
                    submitButton.doClick();
                }
            }
        });
    }

    // Getters
    public Theme getTheme()
    {
        return theme;
    }

    public boolean isEnabled()
    {
        return bEnabled;
    }

    public SubmitButton getSubmitButton()
    {
        return submitButton;
    }

    public String getText()
    {
        return textField.getText();
    }

    // Setters
    public void setTheme(Theme theme)
    {
        this.theme = theme;

        if (textField != null)
        {
            if (textField.getText().isEmpty())
            {
                textField.setForeground(Color.GRAY);
            }
            else
            {
                textField.setForeground(getTheme() == Theme.DARK ? Color.LIGHT_GRAY : Color.DARK_GRAY);
            }
        }
        if (submitButton != null)
        {
            submitButton.setTheme(theme);
        }

        revalidate();
        repaint();
    }

    public void setEnabled(boolean bEnabled)
    {
        this.bEnabled = bEnabled;
        if (submitButton != null)
        {
            submitButton.setEnabled(bEnabled);
        }
        if (textField != null)
        {
            textField.setEnabled(bEnabled);
            if (bEnabled)
            {
                textField.requestFocus();
            }
        }
        if (submitButton != null)
        {
            submitButton.setEnabled(bEnabled);
        }
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
        g2d.fillRect(0, 0, getWidth(), getHeight());

        final int HEIGHT = getHeight() - PADDING * 2;

        g2d.setColor(getTheme() == Theme.DARK ? new Color(61, 63, 65) : new Color(242, 242, 242));
        g2d.fill(new RoundRectangle2D.Float(PADDING, PADDING, getWidth() - PADDING * 2, HEIGHT, HEIGHT, HEIGHT));
    }
}