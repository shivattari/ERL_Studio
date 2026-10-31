package ERL.Compiler.Parser.Nodes.Subroutines;

import ERL.Compiler.Parser.Nodes.ASTNode;
import ERL.Compiler.Parser.Nodes.IdentifierNode;
import ERL.Compiler.Parser.Nodes.TypeNode;

/**
 * Stores the identifier and type data for a subroutine parameter.
 *
 * @author Shivam Attari
 */
public class ParameterNode extends ASTNode
{
    private IdentifierNode identifier;
    private TypeNode typeNode;

    public ParameterNode(IdentifierNode identifier, TypeNode typeNode, int iLine)
    {
        this.identifier = identifier;
        this.typeNode = typeNode;
        this.iLine = iLine;
    }

    public IdentifierNode getIdentifier()
    {
        return identifier;
    }

    public TypeNode getTypeNode()
    {
        return typeNode;
    }

    @Override
    public String toString()
    {
        return "Parameter";
    }
}
