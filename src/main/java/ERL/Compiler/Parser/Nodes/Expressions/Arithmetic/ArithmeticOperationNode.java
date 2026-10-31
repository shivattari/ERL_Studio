package ERL.Compiler.Parser.Nodes.Expressions.Arithmetic;

import ERL.Compiler.Parser.Nodes.ASTNode;

/**
 * Represents any arithmetic operation.
 *
 * @author Shivam Attari
 */
public class ArithmeticOperationNode extends ASTNode
{
    private ArithmeticOperator operator;
    private ASTNode leftOperand;
    private ASTNode rightOperand;
    private boolean bIntMode;

    public ArithmeticOperationNode(ArithmeticOperator operator, ASTNode leftOperand, ASTNode rightOperand, int iLine)
    {
        this.operator = operator;
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.iLine = iLine;
        setIntMode(true);
    }

    public ArithmeticOperator getOperator()
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

    public boolean isIntMode()
    {
        return bIntMode;
    }

    public void setOperator(ArithmeticOperator operator)
    {
        this.operator = operator;
    }

    public void setIntMode(boolean bIntMode)
    {
        this.bIntMode = bIntMode;
    }

    @Override
    public String toString()
    {
        return String.format("Arithmetic Operation (operator: %s)", getOperator().toString());
    }
}
