package ERL.Compiler.Semantics;

import ERL.Compiler.Parser.Nodes.DataType;

import java.util.HashSet;
import java.util.Set;

/**
 * Stores the data on paths through a function. This is used to track if they return in a valid manner.
 *
 * @author Shivam Attari
 */
public class ReturnPath
{
    private Set<DataType> returnTypes;
    private boolean bHasReturned;
    private boolean bAllPathsReturn;

    public ReturnPath()
    {
        this.returnTypes = new HashSet<>();
        bHasReturned = false;
        bAllPathsReturn = false;
    }

    public Set<DataType> getReturnTypes()
    {
        return returnTypes;
    }

    public boolean hasReturned()
    {
        return bHasReturned;
    }

    public boolean allPathsReturn()
    {
        return bAllPathsReturn;
    }

    public void addReturn(DataType type)
    {
        if (type != null)
        {
            returnTypes.add(type);
        }
        bHasReturned = true;
    }

    public void merge(ReturnPath other)
    {
        returnTypes.addAll(other.returnTypes);
        bHasReturned = this.bHasReturned || other.bHasReturned;
    }

    public void setAllPathsReturn(boolean value)
    {
        bAllPathsReturn = value;
    }
}
