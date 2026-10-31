package ERL.Compiler.Parser.Nodes.Expressions.Unary;

import ERL.Compiler.Parser.Nodes.ASTNode;

/**
 * Represents a unary NOT operation.
 *
 * @author Shivam Attari
 */
public class UnaryOperationNode extends ASTNode
{
    private UnaryOperator operator;
    private ASTNode operand;

    public UnaryOperationNode(UnaryOperator operator, ASTNode operand, int iLine)
    {
        this.operator = operator;
        this.operand = operand;
        this.iLine = iLine;
    }

    public UnaryOperator getOperator()
    {
        return operator;
    }

    public ASTNode getOperand()
    {
        return operand;
    }

    @Override
    public String toString()
    {
        return String.format("Unary Operation (operator: %s)", operator.toString());
    }
}
