package ERL.Compiler.Parser.Nodes.Expressions.Literals;

import ERL.Compiler.Parser.Nodes.ASTNode;

/**
 * Represents a simple String value.
 *
 * @author Shivam Attari
 */
public class StringLiteralNode extends ASTNode
{
    private String szValue;

    public StringLiteralNode(String szValue, int iLine)
    {
        this.szValue = szValue;
        this.iLine = iLine;
    }

    public String getValue()
    {
        return szValue;
    }

    @Override
    public String toString()
    {
        return String.format("String Literal (value: %s)", getValue());
    }
}
