package ERL.Compiler.Parser.Nodes.Statements;

import ERL.Compiler.Parser.Nodes.ASTNode;
import ERL.Compiler.Parser.Nodes.BlockNode;

/**
 * Represents a single case in a switch expression.
 *
 * @author Shivam Attari
 */
public class CaseNode extends ASTNode
{
    private ASTNode expression;
    private BlockNode block;

    public CaseNode(ASTNode value, BlockNode block, int iLine)
    {
        this.expression = value;
        this.block = block;
        this.iLine = iLine;
    }

    public ASTNode getExpression()
    {
        return expression;
    }

    public BlockNode getBlock()
    {
        return block;
    }

    @Override
    public String toString()
    {
        return "Case";
    }
}
