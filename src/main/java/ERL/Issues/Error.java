package ERL.Issues;

/**
 * Stores error details created by the compiler/VM.
 *
 * @author Shivam Attari
 */
public class Error extends Issue
{
    public Error()
    {
        super(0, "");
    }

    public Error(int iLine, String szDescription)
    {
        super(iLine, szDescription);
    }

    @Override
    public String toString()
    {
        return String.format("Error on line %d: %s", getLine(), getDescription());
    }
}
