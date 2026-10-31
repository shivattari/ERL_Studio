package ERL.Compiler.Parser.Nodes.Expressions.Literals;

import ERL.Compiler.Parser.Nodes.ASTNode;

/**
 * Represents a simple boolean value.
 *
 * @author Shivam Attari
 */
public class BooleanLiteralNode extends ASTNode
{
    private boolean bValue;

    public BooleanLiteralNode(boolean bValue, int iLine)
    {
        this.bValue = bValue;
        this.iLine = iLine;
    }

    public boolean getValue()
    {
        return bValue;
    }

    @Override
    public String toString()
    {
        return String.format("Boolean Literal (value: %b)", getValue());
    }
}
