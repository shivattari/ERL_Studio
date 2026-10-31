package ERL.UI.IDE.Console;

import ERL.Compiler.VM.VirtualMachine;
import ERL.UI.IDE.Theme;
import ERL.UI.LoadImage;

import javax.swing.*;
import java.awt.*;

/**
 * This panel acts as the console area for input/output for the VM.
 *
 * @author Shivam Attari
 */
public class ConsolePanel extends JPanel
{
    // Constants
    private final Color DARK_BG = new Color(30, 31, 34);

    // Instance Variables
    // ----------| Subviews |---------------------------------------------
    private JTextPane outputPane;
    private InputField inputField;
    private VirtualMachine currentVM;
    private JToolBar toolBar;
    private JButton trashButton;
    private JButton stopButton;

    // ----------| Properties |-------------------------------------------
    private Font monoFont;
    private Theme theme;

    // Constructor
    public ConsolePanel(Theme theme)
    {
        setTheme(theme);
        loadMonoFont();

        outputPane = new JTextPane();
        outputPane.setEditable(false);
        outputPane.setBackground(getTheme() == Theme.DARK ? DARK_BG : Color.WHITE);
        outputPane.setFont(monoFont.deriveFont(14f));

        inputField = new InputField(getTheme());

        JScrollPane scrollPane = new JScrollPane(outputPane);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);

        toolBar = new JToolBar();
        add(toolBar, BorderLayout.NORTH);
        setupToolBar();

        setMinimumSize(new Dimension(100, 0));
    }

    // Getters
    public Theme getTheme()
    {
        return theme;
    }

    public VirtualMachine getVM()
    {
        return currentVM;
    }

    // Setters
    public void setTheme(Theme theme)
    {
        this.theme = theme;

        if (outputPane != null)
        {
            outputPane.setBackground(theme == Theme.DARK ? DARK_BG : Color.WHITE);
        }
        if (inputField != null)
        {
            inputField.setTheme(theme);
        }
        if (toolBar != null)
        {
            updateToolBar();
        }
    }

    // Methods
    private void loadMonoFont()
    {
        try
        {
            monoFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader().getResource("JetBrainsMono-Regular.ttf").openStream());
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(monoFont);
        }
        catch (Exception e)
        {
            monoFont = new Font("Monospaced", Font.PLAIN, 12);
        }
    }

    private void setupToolBar()
    {
        toolBar.add(Box.createRigidArea(new Dimension(2, 34)));

        stopButton = new JButton();
        stopButton.addActionListener(e ->
        {
            if (currentVM != null)
            {
                if (currentVM.isRunning())
                {
                    currentVM.stop();
                }
            }
        });

        trashButton = new JButton();
        trashButton.addActionListener(e -> clear());

        toolBar.add(stopButton);
        toolBar.add(trashButton);

        toolBar.add(Box.createRigidArea(new Dimension(2, 34)));

        updateToolBar();

        revalidate();
        repaint();
    }

    public void updateToolBar()
    {
        if (currentVM != null)
        {
            configureButton(
                    stopButton,
                    String.format(
                            "ToolBar/stop%s.png",
                            currentVM.isRunning() ? "" : "-disabled"
                    )
            );
        }
        else
        {
            configureButton(
                    stopButton,
                    "ToolBar/stop-disabled.png"
            );
        }

        configureButton(trashButton, "ToolBar/trash.png");

        revalidate();
        repaint();
    }

    private void configureButton(AbstractButton button, String szPath)
    {
        button.removeAll();
        JPanel iconPanel = new LoadImage(
                szPath,
                new Dimension(20, 20),
                getTheme() == Theme.DARK && !szPath.equals("ToolBar/stop.png")
        ).loadImage();
        iconPanel.setBackground(new Color(0, 0, 0, 0));
        button.setLayout(new BorderLayout());
        button.add(iconPanel, BorderLayout.CENTER);
        button.setMinimumSize(new Dimension(30, 28));
        button.setMaximumSize(new Dimension(30, 28));
    }

    public void print(String szText)
    {
        SwingUtilities.invokeLater(() ->
        {
            outputPane.setText(outputPane.getText() + szText + "\n");
            // Auto-scroll to bottom
            outputPane.setCaretPosition(outputPane.getDocument().getLength());
            revalidate();
            repaint();
        });
    }

    public void clear()
    {
        SwingUtilities.invokeLater(() ->
        {
            outputPane.setText("");
            revalidate();
            repaint();
        });
    }

    public void setVM(VirtualMachine vm)
    {
        if (this.currentVM != null)
        {
            this.currentVM.stop();
        }
        this.currentVM = vm;
    }

    public void enableInput()
    {
        SwingUtilities.invokeLater(() ->
        {
            inputField.setEnabled(true);
            inputField.getSubmitButton().setEnabled(true);
        });
    }

    public void submitInput(String input)
    {
        print(">>> " + input);
        inputField.setEnabled(false);
        inputField.getSubmitButton().setEnabled(false);
        if (currentVM != null)
        {
            currentVM.submitInput(input);
        }
    }
}
