package ERL.Compiler.Parser.Nodes.Expressions;

import ERL.Compiler.Parser.Nodes.ASTNode;
import ERL.Compiler.Parser.Nodes.DataType;

import java.util.List;

/**
 * Represents an array literal defined with all its elements.
 *
 * @author Shivam Attari
 */
public class ArrayLiteralNode extends ASTNode
{
    private List<ASTNode> elements;
    private DataType type;

    public ArrayLiteralNode(List<ASTNode> elements, int iLine)
    {
        this.elements = elements;
        this.iLine = iLine;
    }

    public List<ASTNode> getElements()
    {
        return elements;
    }

    public DataType getType()
    {
        return type;
    }

    public void setType(DataType type)
    {
        this.type = type;
    }

    @Override
    public String toString()
    {
        return "Array Literal";
    }
}
