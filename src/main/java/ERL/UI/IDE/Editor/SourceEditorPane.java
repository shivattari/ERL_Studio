package ERL.UI.IDE.Editor;

import ERL.Compiler.Compiler;
import ERL.Compiler.Tokenizer.Token;
import ERL.Compiler.Tokenizer.TokenType;
import ERL.Compiler.Tokenizer.Tokenizer;
import ERL.Issues.Issue;
import ERL.UI.IDE.IDEToolBar;
import ERL.UI.IDE.Issues.IssueGutterPanel;
import ERL.UI.IDE.Theme;

import javax.swing.Timer;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.*;

import static java.util.Map.entry;

/**
 * A text editor pane which highlights tokens of OCR ERL code, and highlights occurrences of the selected identifier.
 * It also automatically saves any changes made to the code.
 *
 * @author Shivam Attari
 */
public class SourceEditorPane extends JScrollPane
{
    // Constants
    private final Color DARK_BG = new Color(30, 31, 34);

    // Instance Variables
    // ----------| Subviews |---------------------------------------------
    private JTextPane editorPane;

    private JPanel gutterPanel;
    private IssueGutterPanel issueGutterPanel;
    private JTextPane lineNumbers;

    // ----------| Properties |-------------------------------------------
    private File sourceFile;
    private Font monoFont;
    private Theme theme;

    private IDEToolBar mainToolBar;
    private List<Issue> issues;
    private Tokenizer tokenizer;

    private DefaultHighlighter.DefaultHighlightPainter occurrenceHighlighter;
    private Object[] currentHighlights;

    private Timer autosaveTimer;

    // Constructor
    public SourceEditorPane(File sourceFile, Theme theme, IDEToolBar mainToolBar)
    {
        // Initial setup
        setSourceFile(sourceFile);
        this.mainToolBar = mainToolBar;
        setIssues(new ArrayList<>());

        loadMonoFont();

        editorPane = new JTextPane();
        editorPane.setFont(monoFont.deriveFont(14f));
        occurrenceHighlighter = new DefaultHighlighter.DefaultHighlightPainter(new Color(235, 236, 252));
        currentHighlights = new Object[0];

        // Create gutter with line numbers and error symbols
        gutterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        gutterPanel.setBackground(Color.WHITE);

        issueGutterPanel = new IssueGutterPanel(new ArrayList<>(), getTheme());
        lineNumbers = new JTextPane();
        lineNumbers.setText("1");

        gutterPanel.add(issueGutterPanel);
        gutterPanel.add(lineNumbers);

        // Avoid text wrapping onto next line
        JPanel noWrapPanel = new JPanel(new BorderLayout());
        noWrapPanel.add(editorPane);

        setViewportView(noWrapPanel);
        setRowHeaderView(gutterPanel);

        // Other configuration
        setSyntaxColors();
        setupLineNumbering();
        setupListeners();

        loadTextFromFile();
        setupAutosave();

        processCode();
        setTheme(theme);

        setMinimumSize(new Dimension(100, 0));
    }

    // Getters
    public File getSourceFile()
    {
        return sourceFile;
    }

    public Theme getTheme()
    {
        return theme;
    }

    public List<Issue> getIssues()
    {
        return issues;
    }

    public String getText()
    {
        return editorPane.getText();
    }

    public Object[] getCurrentHighlights()
    {
        return currentHighlights;
    }

    // Setters
    public void setSourceFile(File sourceFile)
    {
        this.sourceFile = sourceFile;
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

                    occurrenceHighlighter = new DefaultHighlighter.DefaultHighlightPainter(new Color(235, 236, 252));
                }
                case DARK ->
                {
                    editorPane.setBackground(DARK_BG);

                    gutterPanel.setBackground(DARK_BG);
                    lineNumbers.setBackground(DARK_BG);

                    occurrenceHighlighter = new DefaultHighlighter.DefaultHighlightPainter(new Color(55, 55, 55));
                }
            }

            setSyntaxColors();

            highlightSyntax();
            highlightOccurrences();
        }

        if (issueGutterPanel != null)
        {
            issueGutterPanel.setTheme(theme);
        }

        revalidate();
        repaint();
    }

    public void setIssues(List<Issue> issues)
    {
        this.issues = issues;
    }

    public void setCurrentHighlights(Object[] currentHighlights)
    {
        this.currentHighlights = currentHighlights;
    }

    // Methods
    // ----------| File |-------------------------------------------------
    private void loadTextFromFile()
    {
        StringBuilder text = new StringBuilder();
        try (Scanner infile = new Scanner(getSourceFile()))
        {
            while (infile.hasNextLine())
            {
                text.append(infile.nextLine());

                if (infile.hasNextLine())
                {
                    text.append(System.lineSeparator());
                }
            }
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(this, "Unable to read contents of file.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        editorPane.setText(text.toString());
    }

    private void saveTextToFile()
    {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(getSourceFile())))
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
        // Timer used to delay autosave until multiple quick changes are made.
        autosaveTimer = new Timer(500, e ->
        {
            autosaveTimer.stop();

            // Save file in another thread to avoid text editor delays
            SwingWorker<Void, Void> autosaveWorker = new SwingWorker<>()
            {
                @Override
                protected Void doInBackground()
                {
                    saveTextToFile();
                    return null;
                }
            };
            autosaveWorker.execute();
        });
        autosaveTimer.setRepeats(false);

        // Restart the timer when ERL code is modified so that quick sequential changes are saved all at once
        editorPane.getDocument().addDocumentListener(new DocumentListener()
        {
            private void scheduleAutosave()
            {
                if (autosaveTimer.isRunning())
                {
                    autosaveTimer.restart();
                }
                else
                {
                    autosaveTimer.start();
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                scheduleAutosave();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                scheduleAutosave();
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                scheduleAutosave();
            }
        });
    }

    // ----------| Configuration |----------------------------------------
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

    private void setupLineNumbering()
    {
        lineNumbers.setFont(monoFont.deriveFont(14f));
        lineNumbers.setEditable(false);
        lineNumbers.setMargin(new Insets(2, 5, 0, 5));
        lineNumbers.setBackground(Color.WHITE);
        lineNumbers.setForeground(new Color(174, 179, 194));

        editorPane.getDocument().addDocumentListener(new DocumentListener()
        {
            public int countLines()
            {
                Element root = editorPane.getDocument().getDefaultRootElement();
                return root.getElementCount();
            }

            public String getText()
            {
//                Element root = editorPane.getDocument().getDefaultRootElement();
//                int iLineCount = root.getElementCount();
                int iLineCount = countLines();
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
                    issueGutterPanel.setLineCount(countLines());
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

    private void processCode()
    {
        // Perform highlighting immediately
        highlightSyntax();
        highlightOccurrences();

        // Compile in another thread to avoid text editor delays
        SwingWorker<List<Issue>, Void> compileWorker = new SwingWorker<>()
        {
            @Override
            protected List<Issue> doInBackground()
            {
                Compiler compiler = new Compiler(false);
                String szCode = editorPane.getText();
                compiler.setSourceCode(szCode);
                compiler.compile();

                // Return issues
                return new ArrayList<>(compiler.getIssues());
            }

            @Override
            protected void done()
            {
                try
                {
                    // Get issues found during compilation
                    List<Issue> newIssues = get();

                    // Refresh issues interfaces
                    setIssues(newIssues);
                    issueGutterPanel.setIssues(getIssues());
                    issueGutterPanel.addIssueLabels();
                    issueGutterPanel.hideCustomTooltip();

                    mainToolBar.setIssues(getIssues());
                    mainToolBar.configureIssueSummaryButton();

                    revalidate();
                    repaint();
                }
                catch (Exception ignored)
                {
                }
            }
        };

        // Start the background compilation.
        compileWorker.execute();
    }

    private void setupListeners()
    {
        editorPane.addKeyListener(new KeyAdapter()
        {
            // Replace tabs with 4 spaces
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyChar() == '\t')
                {
                    e.consume();
                    try
                    {
                        editorPane.getDocument().insertString(editorPane.getCaretPosition(), "    ", null);
                    }
                    catch (Exception ignored)
                    {
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
                SwingUtilities.invokeLater(() -> processCode());
            }
        });

        // Replace tabs with 4 spaces
        editorPane.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                replaceTabs(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                replaceTabs(e);
            }

            private void replaceTabs(DocumentEvent e)
            {
                try
                {
                    String szText = editorPane.getText();
                    String szNewText = szText.replace("\t", "    ");
                    if (!szText.equals(szNewText))
                    {
                        SwingUtilities.invokeLater(() ->
                        {
                            editorPane.setText(szNewText);
                        });
                    }
                }
                catch (Exception ignored)
                {
                }
            }
        });

        editorPane.addCaretListener(e -> SwingUtilities.invokeLater(this::highlightOccurrences));
    }

    // [13] Highlighter guide: https://stackoverflow.com/a/9500100
    private void highlightOccurrences()
    {
        // Clear editor of existing highlights before refreshing
        Highlighter highlighter = editorPane.getHighlighter();
        for (Object highlight : getCurrentHighlights())
        {
            highlighter.removeHighlight(highlight);
        }

        int iPosition = editorPane.getCaretPosition();
        String szText = editorPane.getText().replace("\r\n", "\n");
        List<Object> newHighlights = new ArrayList<>();

        int iOffset = 0;
        String szIdentifier = null;

        for (Token token : tokenizer.getTokens())
        {
            // Skip comments
            while (iOffset < szText.length() - 1 && szText.charAt(iOffset) == '/' && szText.charAt(iOffset + 1) == '/')
            {
                iOffset = szText.indexOf('\n', iOffset);
                if (iOffset == -1)
                {
                    iOffset = szText.length();
                    break;
                }
                iOffset++; // Pass over newline
            }

            // Find the identifier under the text cursor
            if (token.getType() != TokenType.NEWLINE)
            {
                if (iPosition >= iOffset && iPosition <= (iOffset + token.getValue().length()) && token.getType() == TokenType.IDENTIFIER)
                {
                    szIdentifier = token.getValue();
                    break;
                }
                iOffset += token.getValue().length();
            }

            // Skip any whitespace
            while (iOffset < szText.length() && Character.isWhitespace(szText.charAt(iOffset)))
            {
                iOffset++;
            }
        }

        if (szIdentifier != null)
        {
            iOffset = 0;
            for (Token token : tokenizer.getTokens())
            {
                // Skip comments
                while (iOffset < szText.length() - 1 && szText.charAt(iOffset) == '/' && szText.charAt(iOffset + 1) == '/')
                {
                    iOffset = szText.indexOf('\n', iOffset);
                    if (iOffset == -1)
                    {
                        iOffset = szText.length();
                        break;
                    }
                    iOffset++; // Pass over newline
                }

                // Highlight all occurrences
                if (token.getType() != TokenType.NEWLINE)
                {
                    if (token.getType() == TokenType.IDENTIFIER && token.getValue().equals(szIdentifier))
                    {
                        try
                        {
                            newHighlights.add(
                                    highlighter.addHighlight(
                                            iOffset,
                                            iOffset + token.getValue().length(),
                                            occurrenceHighlighter
                                    ));
                        }
                        catch (BadLocationException ignored)
                        {
                        }
                    }
                    iOffset += token.getValue().length();
                }
                while (iOffset < szText.length() && Character.isWhitespace(szText.charAt(iOffset)))
                {
                    iOffset++;
                }
            }
        }

        setCurrentHighlights(newHighlights.toArray());
    }

    // ----------| Highlighting |-----------------------------------------
    private void setSyntaxColors()
    {
        StyledDocument doc = editorPane.getStyledDocument();
        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        Map<String, Color[]> colorMap = new HashMap<>(Map.ofEntries(
                entry("Type", new Color[]{new Color(0, 51, 179), new Color(207, 142, 109)}),
                entry("Sub Name", new Color[]{new Color(0, 98, 122), new Color(87, 170, 247)}),
                entry("Literal", new Color[]{new Color(23, 73, 244), new Color(42, 172, 184)}),
                entry("Block", new Color[]{new Color(135, 16, 148), new Color(199, 125, 187)}),
                entry("String", new Color[]{new Color(6, 125, 23), new Color(106, 171, 115)}),
                entry("Comment", new Color[]{new Color(140, 140, 140), new Color(122, 126, 133)}),
                entry("Unknown", new Color[]{new Color(245, 0, 0), new Color(247, 84, 100)})
        ));

        for (var entry : colorMap.entrySet())
        {
            Style newStyle = doc.addStyle(entry.getKey(), defaultStyle);
            StyleConstants.setForeground(newStyle, entry.getValue()[getTheme() == Theme.DARK ? 1 : 0]);
        }
    }

    // [14] Substring styling: https://stackoverflow.com/a/33979017
    private void highlightSyntax()
    {
        // Make all newlines into '\n'
        StyledDocument doc = editorPane.getStyledDocument();
        String szText = editorPane.getText().replace("\r\n", "\n");

        // Reset styles
        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        doc.setCharacterAttributes(0, doc.getLength(), defaultStyle, true);

        // Tokenize the text
        tokenizer = new Tokenizer(szText, false);
        tokenizer.tokenize();

        int iOffset = 0;
        List<Token> tokens = tokenizer.getTokens();
        int iTokenIndex = 0;

        while (iOffset < szText.length())
        {
            // Check for comments
            if (iOffset < szText.length() - 1 && szText.charAt(iOffset) == '/' && szText.charAt(iOffset + 1) == '/')
            {
                int iEndOfComment = szText.indexOf('\n', iOffset);
                if (iEndOfComment == -1)
                {
                    iEndOfComment = szText.length();
                }
                doc.setCharacterAttributes(iOffset, iEndOfComment - iOffset, doc.getStyle("Comment"), true);
                iOffset = iEndOfComment;
                continue;
            }

            // Highlight tokens
            if (iTokenIndex < tokens.size())
            {
                Token token = tokens.get(iTokenIndex);
                if (token.getType() != TokenType.NEWLINE)
                {
                    // Find position of token in text, skipping whitespace and newlines
                    while (iOffset < szText.length() && (Character.isWhitespace(szText.charAt(iOffset)) || szText.charAt(iOffset) == '\r'))
                    {
                        iOffset++;
                    }

                    if (iTokenIndex + 1 < tokens.size())
                    {
                        Token nextToken = tokens.get(iTokenIndex + 1);
                        Style style;

                        if (token.getType() == TokenType.IDENTIFIER && nextToken.getValue().equals("("))
                        { // Special case for subroutine identifiers
                            style = doc.getStyle("Sub Name");
                        }
                        else
                        {
                            style = getTokenStyle(token.getType());
                        }

                        doc.setCharacterAttributes(iOffset, token.getValue().length(), style, true);
                        iOffset += token.getValue().length();
                    }
                }
                else
                {
                    do
                    { // Skip whitespace after newlines
                        iOffset++;
                    }
                    while (iOffset < szText.length() && (szText.charAt(iOffset) == ' ' || szText.charAt(iOffset) == '\t' || szText.charAt(iOffset) == '\r'));
                }
                iTokenIndex++;
            }
            else
            {
                iOffset++;
            }
        }
    }

    private Style getTokenStyle(TokenType type)
    {
        StyledDocument doc = editorPane.getStyledDocument();
        return switch (type)
        {
            case TYPE, ARRAY, CONST, GLOBAL, IF, THEN, ELSE, ELSEIF, ENDIF, SWITCH, CASE, DEFAULT, ENDSWITCH, WHILE,
                 ENDWHILE,
                 DO, UNTIL, FOR, TO, STEP, NEXT, RETURN -> doc.getStyle("Type");
            case STRING, CHAR -> doc.getStyle("String");
            case INT, FLOAT, BOOLEAN -> doc.getStyle("Literal");
            case PROCEDURE, ENDPROCEDURE, FUNCTION, ENDFUNCTION, MAIN, ENDMAIN -> doc.getStyle("Block");
            case IDENTIFIER, ASSIGN, LOGICAL, ARITHMETIC, COMPARISON, PARENTHESIS, NEWLINE, COLON, DOT ->
                    StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
            case UNKNOWN -> doc.getStyle("Unknown");
        };
    }
}