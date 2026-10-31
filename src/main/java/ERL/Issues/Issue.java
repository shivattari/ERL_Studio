package ERL.Issues;

/**
 * Stores a line number and description for an error.
 *
 * @author Shivam Attari
 */
public class Issue
{
    // Instance Variables
    private int iLine;
    private String szDescription;

    // Constructors
    public Issue()
    {
        this(0, "");
    }

    public Issue(int iLine, String szDescription)
    {
        setLine(iLine);
        setDescription(szDescription);
    }

    // Getters
    public int getLine()
    {
        return iLine;
    }

    public String getDescription()
    {
        return szDescription;
    }

    // Setters
    public void setLine(int iLine)
    {
        this.iLine = iLine;
    }

    public void setDescription(String szDescription)
    {
        this.szDescription = szDescription;
    }

    // Methods
    @Override
    public String toString()
    {
        return String.format("Issue on line %d: %s", getLine(), getDescription());
    }

    public void printMessage()
    {
        System.err.print(this);
    }
}
