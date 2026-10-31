package ERL.Compiler.Parser.Nodes.Statements;

import ERL.Compiler.Parser.Nodes.ASTNode;
import ERL.Compiler.Parser.Nodes.DataType;
import ERL.Compiler.Parser.Nodes.IdentifierNode;

import java.util.List;

/**
 * Represents an assignment of an expression node to a variable.
 *
 * @author Shivam Attari
 */
public class AssignmentNode extends ASTNode
{
    private IdentifierNode identifier;
    private List<ASTNode> indices;
    private ASTNode expression;
    private DataType arrayAssignType;

    public AssignmentNode(IdentifierNode identifier, List<ASTNode> indices, ASTNode expression, int iLine)
    {
        this.identifier = identifier;
        this.indices = indices;
        this.expression = expression;
        this.iLine = iLine;
    }

    public IdentifierNode getIdentifier()
    {
        return identifier;
    }

    public List<ASTNode> getIndices()
    {
        return indices;
    }

    public ASTNode getExpression()
    {
        return expression;
    }

    public DataType getArrayAssignType()
    {
        return arrayAssignType;
    }

    public void setArrayAssignType(DataType arrayAssignType)
    {
        this.arrayAssignType = arrayAssignType;
    }

    @Override
    public String toString()
    {
        return "Assignment";
    }
}
