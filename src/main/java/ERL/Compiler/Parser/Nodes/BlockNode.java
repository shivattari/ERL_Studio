package ERL.Compiler.Parser.Nodes;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores a list of statements that are contained by another node.
 *
 * @author Shivam Attari
 */
public class BlockNode extends ASTNode
{
    private List<ASTNode> statements;

    public BlockNode(int iLine)
    {
        this.statements = new ArrayList<>();
        this.iLine = iLine;
    }

    public void addStatement(ASTNode statement)
    {
        statements.add(statement);
    }

    public List<ASTNode> getStatements()
    {
        return statements;
    }

    @Override
    public String toString()
    {
        return "Block";
    }
}
