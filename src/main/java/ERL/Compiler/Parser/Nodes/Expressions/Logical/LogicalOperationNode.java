package ERL.Compiler.Parser.Nodes.Expressions.Logical;

import ERL.Compiler.Parser.Nodes.ASTNode;

/**
 * Represents any logical operation.
 *
 * @author Shivam Attari
 */
public class LogicalOperationNode extends ASTNode
{
    private LogicalOperator operator;
    private ASTNode leftOperand;
    private ASTNode rightOperand;

    public LogicalOperationNode(LogicalOperator operator, ASTNode leftOperand, ASTNode rightOperand, int iLine)
    {
        this.operator = operator;
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.iLine = iLine;
    }

    public LogicalOperator getOperator()
    {
        return operator;
    }

    public ASTNode getLeftOperand()
    {
        return leftOperand;
    }

    public ASTNode getRightOperand()
    {
        return rightOperand;
    }

    @Override
    public String toString()
    {
        return String.format("Logical Operation (operator: %s)", getOperator().toString());
    }
}
