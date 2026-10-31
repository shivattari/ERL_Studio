package ERL.Compiler.Parser.Nodes.Expressions;

import ERL.Compiler.Parser.Nodes.ASTNode;
import ERL.Compiler.Parser.Nodes.IdentifierNode;

/**
 * Represents the accessing of a property of a value.
 *
 * @author Shivam Attari
 */
public class InstancePropertyNode extends ASTNode
{
    private ASTNode instance;
    private IdentifierNode property;

    public InstancePropertyNode(ASTNode instance, IdentifierNode property, int iLine)
    {
        this.instance = instance;
        this.property = property;
        this.iLine = iLine;
    }

    public ASTNode getInstance()
    {
        return instance;
    }

    public IdentifierNode getProperty()
    {
        return property;
    }

    @Override
    public String toString()
    {
        return "Instance Property";
    }
}
