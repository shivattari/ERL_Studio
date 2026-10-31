package ERL.Compiler.Parser.Nodes.Subroutines;

import ERL.Compiler.Parser.Nodes.ASTNode;
import ERL.Compiler.Parser.Nodes.BlockNode;
import ERL.Compiler.Parser.Nodes.IdentifierNode;

import java.util.List;

/**
 * Stores the details of a subroutine's signature and code block.
 *
 * @author Shivam Attari
 */
public class SubroutineNode extends ASTNode
{
    private IdentifierNode identifier;
    private List<ParameterNode> parameters;
    private BlockNode block;
    private boolean isFunction = false;

    public SubroutineNode(IdentifierNode identifier, List<ParameterNode> parameters, BlockNode block, boolean isFunction, int iLine)
    {
        this.identifier = identifier;
        this.parameters = parameters;
        this.block = block;
        this.isFunction = isFunction;
        this.iLine = iLine;
    }

    public IdentifierNode getIdentifier()
    {
        return identifier;
    }

    public List<ParameterNode> getParameters()
    {
        return parameters;
    }

    public BlockNode getBlock()
    {
        return block;
    }

    public boolean isFunction()
    {
        return isFunction;
    }

    @Override
    public String toString()
    {
        return String.format("Subroutine (isFunction: %b)", isFunction());
    }
}
