package ERL.Compiler.Parser.Nodes.Expressions.Literals;

import ERL.Compiler.Parser.Nodes.ASTNode;

/**
 * Represents a simple character value.
 *
 * @author Shivam Attari
 */
public class CharLiteralNode extends ASTNode
{
    private char cValue;

    public CharLiteralNode(char cValue, int iLine)
    {
        this.cValue = cValue;
        this.iLine = iLine;
    }

    public char getValue()
    {
        return cValue;
    }

    @Override
    public String toString()
    {
        return String.format("Character Literal (value: %c)", getValue());
    }
}
