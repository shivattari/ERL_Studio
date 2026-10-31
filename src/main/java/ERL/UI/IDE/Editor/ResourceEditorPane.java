package ERL.UI.IDE.Editor;

import ERL.UI.IDE.Theme;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Element;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * A simple editor for plaintext (.txt) files which automatically saves any changes.
 *
 * @author Shivam Attari
 */
public class ResourceEditorPane extends JScrollPane
{
    // Constants
    private final Color DARK_BG = new Color(30, 31, 34);

    // Instance Variables
    // ----------| Subviews |---------------------------------------------
    private JTextPane editorPane;

    private JPanel gutterPanel;
    private JTextPane lineNumbers;

    // ----------| Properties |-------------------------------------------
    private File resourceFile;
    private Font monoFont;
    private Theme theme;

    // Constructor
    public ResourceEditorPane(File resourceFile, Theme theme)
    {
        // Initial setup
        setResourceFile(resourceFile);

        loadMonoFont();

        editorPane = new JTextPane();
        editorPane.setFont(monoFont.deriveFont(14f));

        // Create gutter with line numbers
        gutterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        gutterPanel.setBackground(Color.WHITE);

        lineNumbers = new JTextPane();
        lineNumbers.setText("1");

        gutterPanel.add(lineNumbers);

        // Avoid text wrapping onto next line
        JPanel noWrapPanel = new JPanel(new BorderLayout());
        noWrapPanel.add(editorPane);

        setViewportView(noWrapPanel);
        setRowHeaderView(gutterPanel);

        // Other configuration
        setupLineNumbering();

        loadTextFromFile();
        setupAutosave();

        setTheme(theme);

        setMinimumSize(new Dimension(100, 0));
    }

    // Getters
    public File getResourceFile()
    {
        return resourceFile;
    }

    public Theme getTheme()
    {
        return theme;
    }

    // Setters
    public void setResourceFile(File resourceFile)
    {
        this.resourceFile = resourceFile;
    }

    public void setTheme(Theme theme)
    {
        this.theme = theme;

        if (editorPane != null)
        {
            switch (theme)
            {
                case LIGHT ->
                {
                    editorPane.setBackground(Color.WHITE);

                    gutterPanel.setBackground(Color.WHITE);
                    lineNumbers.setBackground(Color.WHITE);
                }
                case DARK ->
                {
                    editorPane.setBackground(DARK_BG);

                    gutterPanel.setBackground(DARK_BG);
                    lineNumbers.setBackground(DARK_BG);
                }
            }
        }

        revalidate();
        repaint();
    }

    // Methods
    // ----------| File |-------------------------------------------------
    private void loadTextFromFile()
    {
        StringBuilder szText = new StringBuilder();
        try (Scanner infile = new Scanner(getResourceFile()))
        {
            while (infile.hasNextLine())
            {
                szText.append(infile.nextLine());

                if (infile.hasNextLine())
                {
                    szText.append(System.lineSeparator());
                }
            }
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(this, "Unable to read contents of file.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        editorPane.setText(szText.toString());
    }

    private void saveTextToFile()
    {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(getResourceFile())))
        {
            bw.write(editorPane.getText());
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(this, "Unable to save contents of file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupAutosave()
    {
        editorPane.getDocument().addDocumentListener(new DocumentListener()
        {
            public void update()
            {
                saveTextToFile();
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                update();
            }

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                update();
            }
        });
    }

    // ----------| Configuration |----------------------------------------
    private void loadMonoFont()
    {
        // [18] Loading a font: https://stackoverflow.com/q/47910820
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

    private void setupLineNumbering()
    {
        lineNumbers.setFont(monoFont.deriveFont(14f));
        lineNumbers.setEditable(false);
        lineNumbers.setMargin(new Insets(2, 5, 0, 5));
        lineNumbers.setBackground(Color.WHITE);
        lineNumbers.setForeground(new Color(174, 179, 194));

        editorPane.getDocument().addDocumentListener(new DocumentListener()
        {
            public String getText()
            {
                Element root = editorPane.getDocument().getDefaultRootElement();
                int iLineCount = root.getElementCount();
                StringBuilder szText = new StringBuilder();
                for (int i = 1 ; i <= iLineCount ; i++)
                {
                    szText.append(i);
                    if (i < iLineCount)
                    {
                        szText.append(System.lineSeparator());
                    }
                }
                return szText.toString();
            }

            public void update()
            {
                SwingUtilities.invokeLater(() ->
                {
                    String szText = getText();
                    lineNumbers.setText(szText);
                    revalidate();
                    repaint();
                });
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                update();
            }

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                update();
            }
        });
    }
}
