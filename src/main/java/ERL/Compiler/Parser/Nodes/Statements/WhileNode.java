package ERL.Compiler.Parser.Nodes.Statements;

import ERL.Compiler.Parser.Nodes.ASTNode;
import ERL.Compiler.Parser.Nodes.BlockNode;

/**
 * Represents a while-loop.
 *
 * @author Shivam Attari
 */
public class WhileNode extends ASTNode
{
    private ASTNode condition;
    private BlockNode block;

    public WhileNode(ASTNode condition, BlockNode block, int iLine)
    {
        this.condition = condition;
        this.block = block;
        this.iLine = iLine;
    }

    public ASTNode getCondition()
    {
        return condition;
    }

    public BlockNode getBlock()
    {
        return block;
    }

    @Override
    public String toString()
    {
        return "While";
    }
}
