package ERL.Compiler.Parser.Nodes.Expressions.Comparison;

import ERL.Compiler.Parser.Nodes.ASTNode;

/**
 * Represents any comparison operation.
 *
 * @author Shivam Attari
 */
public class ComparisonOperationNode extends ASTNode
{
    private ComparisonOperator operator;
    private ASTNode leftOperand;
    private ASTNode rightOperand;

    public ComparisonOperationNode(ComparisonOperator operator, ASTNode leftOperand, ASTNode rightOperand, int iLine)
    {
        this.operator = operator;
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.iLine = iLine;
    }

    public ComparisonOperator getOperator()
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
        return String.format("Comparison Operation (operator: %s)", getOperator().toString());
    }
}
