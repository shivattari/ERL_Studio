package ERL.Compiler.Parser.Nodes.Statements;

import ERL.Compiler.Parser.Nodes.ASTNode;

/**
 * Represents a return statement of a function.
 *
 * @author Shivam Attari
 */
public class ReturnNode extends ASTNode
{
    private ASTNode expression;

    public ReturnNode(ASTNode expression, int iLine)
    {
        this.expression = expression;
        this.iLine = iLine;
    }

    public ASTNode getExpression()
    {
        return expression;
    }

    @Override
    public String toString()
    {
        return "Return";
    }
}
