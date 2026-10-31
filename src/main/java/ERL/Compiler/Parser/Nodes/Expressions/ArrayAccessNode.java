package ERL.Compiler.Parser.Nodes.Expressions;

import ERL.Compiler.Parser.Nodes.ASTNode;
import ERL.Compiler.Parser.Nodes.IdentifierNode;

import java.util.List;

/**
 * Represents accessing of an array via its indices.
 *
 * @author Shivam Attari
 */
public class ArrayAccessNode extends ASTNode
{
    private IdentifierNode identifier;
    private List<ASTNode> indices;

    public ArrayAccessNode(IdentifierNode identifier, List<ASTNode> indices, int iLine)
    {
        this.identifier = identifier;
        this.indices = indices;
        this.iLine = iLine;
    }

    public IdentifierNode getIdentifier()
    {
        return identifier;
    }

    public List<ASTNode> getIndices()
    {
        return indices;
    }

    @Override
    public String toString()
    {
        return "Array Access";
    }
}
