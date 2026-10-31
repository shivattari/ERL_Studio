package ERL.Compiler.Parser.Nodes.Expressions;

import ERL.Compiler.Parser.Nodes.ASTNode;

/**
 * Represents calling a predefined subroutine on a value.
 *
 * @author Shivam Attari
 */
public class InstanceSubroutineCallNode extends ASTNode
{
    private ASTNode instance;
    private SubroutineCallNode subroutineCall;

    public InstanceSubroutineCallNode(ASTNode instance, SubroutineCallNode subroutineCall, int iLine)
    {
        this.instance = instance;
        this.subroutineCall = subroutineCall;
        this.iLine = iLine;
    }

    public ASTNode getInstance()
    {
        return instance;
    }

    public SubroutineCallNode getSubroutineCall()
    {
        return subroutineCall;
    }

    @Override
    public String toString()
    {
        return "Instance Subroutine Call";
    }
}
