package ERL.Compiler.Parser.Nodes;

import java.util.List;

/**
 * Stores variable type information.
 *
 * @author Shivam Attari
 */
public class TypeNode extends ASTNode
{
    private DataType dataType;
    private List<ASTNode> dimensions; // Arrays only, holds the size of each dimension
    private boolean isConst;
    private boolean isGlobal;

    public TypeNode(DataType dataType, List<ASTNode> dimensions, boolean isConst, boolean isGlobal, int iLine)
    {
        this.dataType = dataType;
        this.dimensions = dimensions;
        this.isConst = isConst;
        this.isGlobal = isGlobal;
        this.iLine = iLine;
    }

    public DataType getDataType()
    {
        return dataType;
    }

    public List<ASTNode> getDimensions()
    {
        return dimensions;
    }

    public void setDimensions(List<ASTNode> dimensions)
    {
        this.dimensions = dimensions;
    }

    public boolean isConst()
    {
        return isConst;
    }

    public boolean isGlobal()
    {
        return isGlobal;
    }

    public void setGlobal(boolean global)
    {
        isGlobal = global;
    }

    public void setConst(boolean aConst)
    {
        isConst = aConst;
    }

    @Override
    public String toString()
    {
        return String.format("Type (dataType: %s, isConst: %b, isGlobal: %b)", getDataType().name(), isConst(), isGlobal());
    }
}
