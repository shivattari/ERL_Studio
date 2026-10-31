package ERL.Compiler.Parser;

import ERL.Compiler.Parser.Nodes.*;
import ERL.Compiler.Parser.Nodes.Expressions.Arithmetic.ArithmeticOperationNode;
import ERL.Compiler.Parser.Nodes.Expressions.*;
import ERL.Compiler.Parser.Nodes.Expressions.Comparison.ComparisonOperationNode;
import ERL.Compiler.Parser.Nodes.Expressions.Literals.*;
import ERL.Compiler.Parser.Nodes.Expressions.Logical.LogicalOperationNode;
import ERL.Compiler.Parser.Nodes.Expressions.Unary.UnaryOperationNode;
import ERL.Compiler.Parser.Nodes.Statements.*;
import ERL.Compiler.Parser.Nodes.Subroutines.MainNode;
import ERL.Compiler.Parser.Nodes.Subroutines.ParameterNode;
import ERL.Compiler.Parser.Nodes.Subroutines.SubroutineNode;

/**
 * Used to display the structure of an AST, for debugging the parser.
 *
 * @author Shivam Attari
 */
public class PrintAST
{
    // Instance Methods
    private ASTNode root;
    private int iIndent;
    private final int INDENT_SIZE = 4;

    // Constructor
    public PrintAST(ASTNode root)
    {
        setRoot(root);
        setIndent(0);
    }

    // Getters
    public int getIndent()
    {
        return iIndent;
    }

    // Setters
    public void setRoot(ASTNode root)
    {
        this.root = root;
    }

    public void setIndent(int iIndent)
    {
        this.iIndent = iIndent;
    }

    private void indent()
    {
        setIndent(getIndent() + INDENT_SIZE);
    }

    private void unindent()
    {
        int iNewIndent = getIndent() - INDENT_SIZE;
        setIndent(Math.max(iNewIndent, 0));
    }

    // Methods
    public void printTree()
    {
        gotoNode(root);
    }

    private void print(ASTNode node)
    {
        if (getIndent() == 0) System.out.printf("%2d| %s%n", node.iLine, node);
        else System.out.printf("%" + getIndent() + "s%2d| %s%n", "", node.iLine, node);
    }

    private void gotoNode(ASTNode node)
    {
        if (node instanceof AssignmentNode instance) gotoAssignment(instance);
        else if (node instanceof BlockNode instance) gotoBlock(instance);
        else if (node instanceof IdentifierNode instance) gotoIdentifier(instance);
        else if (node instanceof ProgramNode instance) gotoProgram(instance);
        else if (node instanceof TypeNode instance) gotoType(instance);

        else if (node instanceof ArrayAccessNode instance) gotoArrayAccess(instance);
        else if (node instanceof ArrayLiteralNode instance) gotoArrayLiteral(instance);
        else if (node instanceof InstancePropertyNode instance) gotoInstanceProperty(instance);
        else if (node instanceof InstanceSubroutineCallNode instance) gotoInstanceSubroutineCall(instance);
        else if (node instanceof SubroutineCallNode instance) gotoSubroutineCall(instance);
        else if (node instanceof ArithmeticOperationNode instance) gotoArithmeticOperation(instance);
        else if (node instanceof ComparisonOperationNode instance) gotoComparisonOperation(instance);
        else if (node instanceof LogicalOperationNode instance) gotoLogicalOperation(instance);
        else if (node instanceof UnaryOperationNode instance) gotoUnaryOperation(instance);

        else if (node instanceof BooleanLiteralNode instance) gotoBooleanLiteral(instance);
        else if (node instanceof CharLiteralNode instance) gotoCharLiteral(instance);
        else if (node instanceof FloatLiteralNode instance) gotoFloatLiteral(instance);
        else if (node instanceof IntLiteralNode instance) gotoIntLiteral(instance);
        else if (node instanceof StringLiteralNode instance) gotoStringLiteral(instance);

        else if (node instanceof CaseNode instance) gotoCase(instance);
        else if (node instanceof DoUntilNode instance) gotoDoUntil(instance);
        else if (node instanceof ElseIfNode instance) gotoElseIf(instance);
        else if (node instanceof ForNode instance) gotoFor(instance);
        else if (node instanceof IfNode instance) gotoIf(instance);
        else if (node instanceof ReturnNode instance) gotoReturn(instance);
        else if (node instanceof SwitchNode instance) gotoSwitch(instance);
        else if (node instanceof VariableDeclarationNode instance) gotoVariableDeclaration(instance);
        else if (node instanceof WhileNode instance) gotoWhile(instance);

        else if (node instanceof SubroutineNode instance) gotoSubroutine(instance);
        else if (node instanceof MainNode instance) gotoMain(instance);
        else if (node instanceof ParameterNode instance) gotoParameter(instance);
    }

    // ----------| ERL.AST |----------------------------------------------
    private void gotoAssignment(AssignmentNode node)
    {
        print(node);
        indent();
        gotoNode(node.getIdentifier());
        if (node.getIndices() != null)
        {
            for (ASTNode index : node.getIndices()) gotoNode(index);
        }
        gotoNode(node.getExpression());
        unindent();
    }

    private void gotoBlock(BlockNode node)
    {
        print(node);
        indent();
        for (ASTNode statement : node.getStatements()) gotoNode(statement);
        unindent();
    }

    private void gotoIdentifier(IdentifierNode node)
    {
        print(node);
    }

    private void gotoProgram(ProgramNode node)
    {
        print(node);
        indent();
        for (ASTNode statement : node.getStatements()) gotoNode(statement);
        unindent();
    }

    private void gotoType(TypeNode node)
    {
        print(node);
        indent();
        if (node.getDimensions() != null)
        {
            for (ASTNode dimension : node.getDimensions()) gotoNode(dimension);
        }
        unindent();
    }

    // ----------| ERL.AST.Expressions |----------------------------------
    private void gotoArrayAccess(ArrayAccessNode node)
    {
        print(node);
        indent();
        gotoNode(node.getIdentifier());
        for (ASTNode index : node.getIndices()) gotoNode(index);
        unindent();
    }

    private void gotoArrayLiteral(ArrayLiteralNode node)
    {
        print(node);
        indent();
        for (ASTNode element : node.getElements()) gotoNode(element);
        unindent();
    }

    private void gotoInstanceProperty(InstancePropertyNode node)
    {
        print(node);
        indent();
        gotoNode(node.getInstance());
        gotoNode(node.getProperty());
        unindent();
    }

    private void gotoInstanceSubroutineCall(InstanceSubroutineCallNode node)
    {
        print(node);
        indent();
        gotoNode(node.getInstance());
        gotoNode(node.getSubroutineCall());
        unindent();
    }

    private void gotoSubroutineCall(SubroutineCallNode node)
    {
        print(node);
        indent();
        gotoNode(node.getIdentifier());
        for (ASTNode expression : node.getArgumentExpressions()) gotoNode(expression);
        unindent();
    }

    // ----------| ERL.AST.Expressions.Arithmetic |-----------------------
    private void gotoArithmeticOperation(ArithmeticOperationNode node)
    {
        print(node);
        indent();
        gotoNode(node.getLeftOperand());
        gotoNode(node.getRightOperand());
        unindent();
    }

    // ----------| ERL.AST.Expressions.Comparison |-----------------------
    private void gotoComparisonOperation(ComparisonOperationNode node)
    {
        print(node);
        indent();
        gotoNode(node.getLeftOperand());
        gotoNode(node.getRightOperand());
        unindent();
    }

    // ----------| ERL.AST.Expressions.Logical |--------------------------
    private void gotoLogicalOperation(LogicalOperationNode node)
    {
        print(node);
        indent();
        gotoNode(node.getLeftOperand());
        gotoNode(node.getRightOperand());
        unindent();
    }

    // ----------| ERL.AST.Expressions.Unary |----------------------------
    private void gotoUnaryOperation(UnaryOperationNode node)
    {
        print(node);
        indent();
        gotoNode(node.getOperand());
        unindent();
    }

    // ----------| ERL.AST.Expressions.Literals |-------------------------
    private void gotoBooleanLiteral(BooleanLiteralNode node)
    {
        print(node);
    }

    private void gotoCharLiteral(CharLiteralNode node)
    {
        print(node);
    }

    private void gotoFloatLiteral(FloatLiteralNode node)
    {
        print(node);
    }

    private void gotoIntLiteral(IntLiteralNode node)
    {
        print(node);
    }

    private void gotoStringLiteral(StringLiteralNode node)
    {
        print(node);
    }

    // ----------| ERL.AST.Statements |-----------------------------------
    private void gotoCase(CaseNode node)
    {
        print(node);
        indent();
        gotoNode(node.getExpression());
        gotoNode(node.getBlock());
        unindent();
    }

    private void gotoDoUntil(DoUntilNode node)
    {
        print(node);
        indent();
        gotoNode(node.getCondition());
        gotoNode(node.getBlock());
        unindent();
    }

    private void gotoElseIf(ElseIfNode node)
    {
        print(node);
        indent();
        gotoNode(node.getCondition());
        gotoNode(node.getBlock());
        unindent();
    }

    private void gotoFor(ForNode node)
    {
        print(node);
        indent();
        gotoNode(node.getStart());
        gotoNode(node.getEndExpression());
        gotoNode(node.getStepExpression());
        gotoNode(node.getNextIdentifier());
        gotoNode(node.getBlock());
        unindent();
    }

    private void gotoIf(IfNode node)
    {
        print(node);
        indent();
        gotoNode(node.getCondition());
        gotoNode(node.getThenBlock());
        for (ASTNode blocks : node.getElseIfNodes()) gotoNode(blocks);
        gotoNode(node.getElseBlock());
        unindent();
    }

    private void gotoReturn(ReturnNode node)
    {
        print(node);
        indent();
        gotoNode(node.getExpression());
        unindent();
    }

    private void gotoSwitch(SwitchNode node)
    {
        print(node);
        indent();
        gotoNode(node.getExpression());
        for (ASTNode caseNode : node.getCases()) gotoNode(caseNode);
        gotoNode(node.getDefaultBlock());
        unindent();
    }

    private void gotoVariableDeclaration(VariableDeclarationNode node)
    {
        print(node);
        indent();
        gotoNode(node.getIdentifier());
        gotoNode(node.getTypeNode());
        gotoNode(node.getInitialExpression());
        unindent();
    }

    private void gotoWhile(WhileNode node)
    {
        print(node);
        indent();
        gotoNode(node.getCondition());
        gotoNode(node.getBlock());
        unindent();
    }

    // ----------| ERL.AST.Subroutines |----------------------------------
    private void gotoSubroutine(SubroutineNode node)
    {
        print(node);
        indent();
        gotoNode(node.getIdentifier());
        for (ASTNode parameter : node.getParameters()) gotoNode(parameter);
        gotoNode(node.getBlock());
        unindent();
    }

    private void gotoMain(MainNode node)
    {
        print(node);
        indent();
        gotoNode(node.getBlock());
        unindent();
    }

    private void gotoParameter(ParameterNode node)
    {
        print(node);
        indent();
        gotoNode(node.getIdentifier());
        gotoNode(node.getTypeNode());
        unindent();
    }
}
