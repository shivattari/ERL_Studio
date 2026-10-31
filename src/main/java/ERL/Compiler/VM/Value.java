package ERL.Compiler.VM;

import ERL.Compiler.Parser.Nodes.DataType;

/**
 * Stores information on a value with a type for use during execution.
 *
 * @author Shivam Attari
 */
public class Value
{
    // Instance Variables
    private DataType type;
    private Object value;
    private boolean bConst;

    // Constructor
    public Value(DataType type, Object value)
    {
        setType(type);
        setValue(value);
        setConst(false);
    }

    // Getters
    public DataType getType()
    {
        return type;
    }

    public Object getValue()
    {
        return value;
    }

    public boolean isConst()
    {
        return bConst;
    }

    // Setters
    public void setType(DataType type)
    {
        this.type = type;
    }

    public void setValue(Object value)
    {
        this.value = value;
    }

    public void setConst(boolean bConst)
    {
        this.bConst = bConst;
    }
}
