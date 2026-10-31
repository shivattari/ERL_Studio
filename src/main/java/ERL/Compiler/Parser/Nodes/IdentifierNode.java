package ERL.Compiler.Parser.Nodes;

/**
 * Stores the name of another node.
 *
 * @author Shivam Attari
 */
public class IdentifierNode extends ASTNode
{
    private String szName;

    public IdentifierNode(String szName, int iLine)
    {
        this.szName = szName;
        this.iLine = iLine;
    }

    public String getName()
    {
        return szName;
    }

    @Override
    public String toString()
    {
        return String.format("Identifier (name: %s)", getName());
    }
}
