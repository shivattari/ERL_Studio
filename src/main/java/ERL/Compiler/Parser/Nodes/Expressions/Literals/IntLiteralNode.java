package ERL.Compiler.Parser.Nodes.Expressions.Literals;

import ERL.Compiler.Parser.Nodes.ASTNode;

/**
 * Represents a simple integer value.
 *
 * @author Shivam Attari
 */
public class IntLiteralNode extends ASTNode
{
    private int iValue;

    public IntLiteralNode(int iValue, int iLine)
    {
        this.iValue = iValue;
        this.iLine = iLine;
    }

    public int getValue()
    {
        return iValue;
    }

    @Override
    public String toString()
    {
        return String.format("Int Literal (value: %d)", getValue());
    }
}
