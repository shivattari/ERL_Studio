package ERL.Compiler.VM;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

/**
 * Allows the VM to operate on external files, including reading and writing.
 *
 * @author Shivam Attari
 */
public class FileHandler
{
    private File resourceFolder;
    private String szFileName;
    private String szTextContents;
    private int iCurrentLine;
    private boolean bWriteMode;

    // Constructor
    public FileHandler(String szFileName, File resourceFolder)
    {
        setResourceFolder(resourceFolder);
        setFileName(szFileName);
        readFile();
        setCurrentLine(0);
        setWriteMode(false);
    }


    // Getters
    public File getResourceFolder()
    {
        return resourceFolder;
    }

    public String getFileName()
    {
        return szFileName;
    }

    public String getTextContent()
    {
        return szTextContents;
    }

    public int getCurrentLine()
    {
        return iCurrentLine;
    }

    public boolean isWriteMode()
    {
        return bWriteMode;
    }

    // Setters
    public void setResourceFolder(File resourceFolder)
    {
        this.resourceFolder = resourceFolder;
    }

    public void setFileName(String szFileName)
    {
        this.szFileName = szFileName;
    }

    public void setTextContent(String szFileContent)
    {
        this.szTextContents = szFileContent;
    }

    public void setCurrentLine(int iCurrentLine)
    {
        this.iCurrentLine = iCurrentLine;
    }

    public void setWriteMode(boolean bWriteMode)
    {
        this.bWriteMode = bWriteMode;
    }

    // Methods
    private File getActualFile()
    {
        for (File file : resourceFolder.listFiles())
        {
            if (file.getName().equals(szFileName))
            {
                return file;
            }
        }
        return null;
    }

    private void readFile()
    {
        try (Scanner file = new Scanner(getActualFile()))
        {
            StringBuilder szContent = new StringBuilder();
            while (file.hasNextLine())
            {
                szContent.append(file.nextLine()).append("\n");
            }
            setTextContent(szContent.toString());
        }
        catch (Exception e)
        {
            System.err.println("Runtime error: Unable to read from file.");
        }
    }

    public String readLine()
    {
        String[] szLines = getTextContent().split("\n");
        if (getCurrentLine() < szLines.length)
        {
            setCurrentLine(getCurrentLine() + 1);
            return szLines[getCurrentLine() - 1];
        }
        return "";
    }

    public void writeLine(String szLine)
    {
        if (isWriteMode())
        { // Append
            setTextContent(getTextContent() + szLine + "\n");
        }
        else
        { // Overwrite
            setTextContent(szLine + "\n");
            setCurrentLine(1);
            setWriteMode(true);
        }

        // Save new contents to file
        try
        {
            FileWriter file = new FileWriter(getActualFile());
            file.write(getTextContent());
            file.close();
        }
        catch (Exception e)
        {
            System.err.println("Runtime error: Unable to write to file.");
        }
    }

    public boolean isEOF()
    {
        String[] szLines = getTextContent().split("\n");
        return getCurrentLine() >= szLines.length;
    }
}
