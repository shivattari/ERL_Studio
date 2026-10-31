package ERL.Compiler.Parser.Nodes.Expressions;

import ERL.Compiler.Parser.Nodes.ASTNode;
import ERL.Compiler.Parser.Nodes.IdentifierNode;

import java.util.List;

/**
 * Represents calling a user-defined subroutine.
 *
 * @author Shivam Attari
 */
public class SubroutineCallNode extends ASTNode
{
    private IdentifierNode identifier;
    private List<ASTNode> argumentExpressions;

    public SubroutineCallNode(IdentifierNode identifier, List<ASTNode> argumentExpressions, int iLine)
    {
        this.identifier = identifier;
        this.argumentExpressions = argumentExpressions;
        this.iLine = iLine;
    }

    public IdentifierNode getIdentifier()
    {
        return identifier;
    }

    public List<ASTNode> getArgumentExpressions()
    {
        return argumentExpressions;
    }

    @Override
    public String toString()
    {
        return "Subroutine Call";
    }
}
