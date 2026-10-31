package ERL.Compiler.VM;

/**
 * Stores bytecode information on a defined subroutine.
 *
 * @author Shivam Attari
 */
public class Subroutine
{
    private String szName;
    private String[] szParameters;
    private String[] szBodyLines;

    public Subroutine(String szName, String[] szParameters, String[] szBodyLines)
    {
        setName(szName);
        setParameters(szParameters);
        setBodyLines(szBodyLines);
    }

    // Getters
    public String getName()
    {
        return szName;
    }

    public String[] getParameters()
    {
        return szParameters;
    }

    public String[] getBodyLines()
    {
        return szBodyLines;
    }

    // Setters
    public void setName(String szName)
    {
        this.szName = szName;
    }

    public void setParameters(String[] szParameters)
    {
        this.szParameters = szParameters;
    }

    public void setBodyLines(String[] szBodyLines)
    {
        this.szBodyLines = szBodyLines;
    }
}