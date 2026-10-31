package ERL.Compiler.Tokenizer;

/**
 * Represents the smallest elements of source code with a meaning.
 *
 * @author Shivam Attari
 */
public class Token
{
    // Instance Variables
    private TokenType type;
    private String szValue;

    // Constructor
    public Token(TokenType type, String szValue)
    {
        setType(type);
        setValue(szValue);
    }

    // Getters
    public TokenType getType()
    {
        return type;
    }

    public String getValue()
    {
        return szValue;
    }

    // Setters
    public void setType(TokenType type)
    {
        this.type = type;
    }

    public void setValue(String szValue)
    {
        this.szValue = szValue;
    }
}
