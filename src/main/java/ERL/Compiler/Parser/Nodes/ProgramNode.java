package ERL.Compiler.Parser.Nodes;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains a list of the base statements of the program, to be used as the root of the AST.
 *
 * @author Shivam Attari
 */
public class ProgramNode extends ASTNode
{
    private List<ASTNode> statements;

    public ProgramNode()
    {
        this.statements = new ArrayList<>();
        this.iLine = 1;
    }

    public List<ASTNode> getStatements()
    {
        return statements;
    }

    public void addStatement(ASTNode statement)
    {
        statements.add(statement);
    }

    @Override
    public String toString()
    {
        return "Program";
    }
}
