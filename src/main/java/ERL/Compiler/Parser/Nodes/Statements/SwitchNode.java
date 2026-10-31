package ERL.Compiler.Parser.Nodes.Statements;

import ERL.Compiler.Parser.Nodes.ASTNode;
import ERL.Compiler.Parser.Nodes.BlockNode;

import java.util.List;

/**
 * Represents a switch-statement and stores its cases.
 *
 * @author Shivam Attari
 */
public class SwitchNode extends ASTNode
{
    private ASTNode expression;
    private List<CaseNode> cases;
    private BlockNode defaultBlock;

    public SwitchNode(ASTNode expression, List<CaseNode> cases, BlockNode defaultBlock, int iLine)
    {
        this.expression = expression;
        this.cases = cases;
        this.defaultBlock = defaultBlock;
        this.iLine = iLine;
    }

    public ASTNode getExpression()
    {
        return expression;
    }

    public List<CaseNode> getCases()
    {
        return cases;
    }

    public BlockNode getDefaultBlock()
    {
        return defaultBlock;
    }

    @Override
    public String toString()
    {
        return "Switch";
    }
}
