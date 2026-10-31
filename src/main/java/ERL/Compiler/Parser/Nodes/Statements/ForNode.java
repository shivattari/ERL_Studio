package ERL.Compiler.Parser.Nodes.Statements;

import ERL.Compiler.Parser.Nodes.ASTNode;
import ERL.Compiler.Parser.Nodes.BlockNode;
import ERL.Compiler.Parser.Nodes.IdentifierNode;

/**
 * Represents a for-loop.
 *
 * @author Shivam Attari
 */
public class ForNode extends ASTNode
{
    private AssignmentNode start;
    private ASTNode endExpression;
    private ASTNode stepExpression;
    private IdentifierNode nextIdentifier;
    private BlockNode block;

    public ForNode(AssignmentNode start, ASTNode endExpression, ASTNode stepExpression, IdentifierNode nextIdentifier, BlockNode block, int iLine)
    {
        this.start = start;
        this.endExpression = endExpression;
        this.stepExpression = stepExpression;
        this.nextIdentifier = nextIdentifier;
        this.block = block;
        this.iLine = iLine;
    }

    public AssignmentNode getStart()
    {
        return start;
    }

    public ASTNode getEndExpression()
    {
        return endExpression;
    }

    public ASTNode getStepExpression()
    {
        return stepExpression;
    }

    public IdentifierNode getNextIdentifier()
    {
        return nextIdentifier;
    }

    public BlockNode getBlock()
    {
        return block;
    }

    @Override
    public String toString()
    {
        return "For";
    }
}
