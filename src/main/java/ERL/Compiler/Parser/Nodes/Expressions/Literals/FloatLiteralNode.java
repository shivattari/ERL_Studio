package ERL.Compiler.Parser.Nodes.Expressions.Literals;

import ERL.Compiler.Parser.Nodes.ASTNode;

/**
 * Represents a simple floating point value.
 *
 * @author Shivam Attari
 */
public class FloatLiteralNode extends ASTNode
{
    private float fValue;

    public FloatLiteralNode(float fValue, int iLine)
    {
        this.fValue = fValue;
        this.iLine = iLine;
    }

    public float getValue()
    {
        return fValue;
    }

    @Override
    public String toString()
    {
        return String.format("Float Literal (value: %f)", getValue());
    }
}
