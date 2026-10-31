package ERL.Issues;

/**
 * Stores warning details created by the compiler.
 *
 * @author Shivam Attari
 */
public class Warning extends Issue
{
    public Warning()
    {
        super(0, "");
    }

    public Warning(int iLine, String szDescription)
    {
        super(iLine, szDescription);
    }

    @Override
    public String toString()
    {
        return String.format("Warning on line %d: %s", getLine(), getDescription());
    }
}
