package ERL.Compiler.Parser.Nodes.Subroutines;

import ERL.Compiler.Parser.Nodes.ASTNode;
import ERL.Compiler.Parser.Nodes.BlockNode;

/**
 * Contains the main block of code, where execution should begin from.
 *
 * @author Shivam Attari
 */
public class MainNode extends ASTNode
{
    private BlockNode block;

    public MainNode(BlockNode block, int iLine)
    {
        this.block = block;
        this.iLine = iLine;
    }

    public BlockNode getBlock()
    {
        return block;
    }

    @Override
    public String toString()
    {
        return "Main";
    }
}
