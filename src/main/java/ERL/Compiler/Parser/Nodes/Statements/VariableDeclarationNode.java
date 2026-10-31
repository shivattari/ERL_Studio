package ERL.Compiler.Parser.Nodes.Statements;

import ERL.Compiler.Parser.Nodes.ASTNode;
import ERL.Compiler.Parser.Nodes.IdentifierNode;
import ERL.Compiler.Parser.Nodes.TypeNode;

/**
 * Represents a variable declaration, including its identifier, type and a possible expression.
 *
 * @author Shivam Attari
 */
public class VariableDeclarationNode extends ASTNode
{
    private IdentifierNode identifier;
    private TypeNode typeNode;
    private ASTNode initialExpression;

    public VariableDeclarationNode(IdentifierNode identifier, TypeNode typeNode, ASTNode initialExpression, int iLine)
    {
        this.identifier = identifier;
        this.typeNode = typeNode;
        this.initialExpression = initialExpression;
        this.iLine = iLine;
    }

    public IdentifierNode getIdentifier()
    {
        return identifier;
    }

    public TypeNode getTypeNode()
    {
        return typeNode;
    }

    public ASTNode getInitialExpression()
    {
        return initialExpression;
    }

    @Override
    public String toString()
    {
        return String.format("Variable Declaration");
    }
}
