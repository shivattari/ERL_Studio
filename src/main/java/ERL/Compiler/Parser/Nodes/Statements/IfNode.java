package ERL.Compiler.Parser.Nodes.Statements;

import ERL.Compiler.Parser.Nodes.ASTNode;
import ERL.Compiler.Parser.Nodes.BlockNode;

import java.util.List;

/**
 * Stores data of an if-statement and all its branches.
 *
 * @author Shivam Attari
 */
public class IfNode extends ASTNode
{
    private ASTNode condition;
    private BlockNode thenBlock;
    private List<ElseIfNode> elseIfNodes;
    private BlockNode elseBlock;

    public IfNode(ASTNode condition, BlockNode thenBlock, List<ElseIfNode> elseIfNodes, BlockNode elseBlock, int iLine)
    {
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elseIfNodes = elseIfNodes;
        this.elseBlock = elseBlock;
        this.iLine = iLine;
    }

    public ASTNode getCondition()
    {
        return condition;
    }

    public BlockNode getThenBlock()
    {
        return thenBlock;
    }

    public List<ElseIfNode> getElseIfNodes()
    {
        return elseIfNodes;
    }

    public BlockNode getElseBlock()
    {
        return elseBlock;
    }

    @Override
    public String toString()
    {
        return "If";
    }
}
