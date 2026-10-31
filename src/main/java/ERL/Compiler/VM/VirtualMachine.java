package ERL.Compiler.VM;

import ERL.Compiler.Parser.Nodes.DataType;
import ERL.Issues.Error;
import ERL.UI.IDE.Console.ConsolePanel;

import javax.swing.*;
import java.io.File;
import java.lang.reflect.Array;
import java.util.*;

/**
 * The virtual machine (VM) executes the specific bytecode language created for this IDE.
 * <p>
 * Sources that assisted me with making the VM multithreaded:
 * [6] Volatile and synchronized: https://www.javamex.com/tutorials/synchronization_volatile.shtml
 * [7] Multithreading guide: https://www.geeksforgeeks.org/multithreading-in-java/
 *
 * @author Shivam Attari
 */
public class VirtualMachine implements Runnable
{
    // Instance Variables
    // ----------| Views |------------------------------------------------
    private ConsolePanel consolePanel;

    // ----------| Properties |-------------------------------------------
    private File resourceFolder;
    private boolean bDebug;
    private String szBytecode;
    private Deque<Value> operandStack;
    private List<Subroutine> subroutines;
    private boolean bFinalMode;

    private int iCurrentLine;
    private Error error;

    // ----------| Thread |-----------------------------------------------
    private boolean bWaitingForInput;
    private Map<Integer, Value> currentMemory;
    private String[] szCurrentLines;
    private int iCurrentIndex;
    private Thread VMThread;
    private final Object inputLock = new Object();
    private String szPendingInput = null;
    private volatile boolean bRunning = true;

    // Constructor
    public VirtualMachine(String szBytecode, File resourceFolder, ConsolePanel consolePanel, boolean bDebug)
    {
        setBytecode(szBytecode);
        setResourceFolder(resourceFolder);
        setDebug(bDebug);
        this.consolePanel = consolePanel;
        setWaitingForInput(false);
        setVMThread(new Thread(this));
    }

    // Getters
    public File getResourceFolder()
    {
        return resourceFolder;
    }

    public boolean isDebug()
    {
        return bDebug;
    }

    public String getBytecode()
    {
        return szBytecode;
    }

    public Deque<Value> getOperandStack()
    {
        return operandStack;
    }

    public List<Subroutine> getSubroutines()
    {
        return subroutines;
    }

    public boolean isFinalMode()
    {
        return bFinalMode;
    }

    public int getCurrentLine()
    {
        return iCurrentLine;
    }

    public Error getError()
    {
        return error;
    }

    public boolean isRunning()
    {
        return bRunning;
    }

    public boolean isWaitingForInput()
    {
        return bWaitingForInput;
    }

    public Map<Integer, Value> getCurrentMemory()
    {
        return currentMemory;
    }

    public String[] getCurrentLines()
    {
        return szCurrentLines;
    }

    public int getCurrentIndex()
    {
        return iCurrentIndex;
    }

    public Thread getVMThread()
    {
        return VMThread;
    }

    public Object getInputLock()
    {
        return inputLock;
    }

    public String getPendingInput()
    {
        return szPendingInput;
    }

    // Setters
    public void setResourceFolder(File resourceFolder)
    {
        this.resourceFolder = resourceFolder;
    }

    public void setDebug(boolean bDebug)
    {
        this.bDebug = bDebug;
    }

    public void setBytecode(String szBytecode)
    {
        this.szBytecode = szBytecode;
    }

    public void setOperandStack(Deque<Value> operandStack)
    {
        this.operandStack = operandStack;
    }

    public void setSubroutines(List<Subroutine> subroutines)
    {
        this.subroutines = subroutines;
    }

    public void setFinalMode(boolean bFinalMode)
    {
        this.bFinalMode = bFinalMode;
    }

    public void setCurrentLine(int iCurrentLine)
    {
        this.iCurrentLine = iCurrentLine;
    }

    public void setError(Error error)
    {
        this.error = error;
    }

    public void setWaitingForInput(boolean bWaitingForInput)
    {
        this.bWaitingForInput = bWaitingForInput;
    }

    public void setCurrentMemory(Map<Integer, Value> currentMemory)
    {
        this.currentMemory = currentMemory;
    }

    public void setCurrentLines(String[] szCurrentLines)
    {
        this.szCurrentLines = szCurrentLines;
    }

    public void setCurrentIndex(int iCurrentIndex)
    {
        this.iCurrentIndex = iCurrentIndex;
    }

    public void setVMThread(Thread vmThread)
    {
        this.VMThread = vmThread;
    }

    public void setPendingInput(String szPendingInput)
    {
        this.szPendingInput = szPendingInput;
    }

    public void setRunning(boolean bRunning)
    {
        this.bRunning = bRunning;
    }

    // Methods
    private void printDebug(String[] szParameters)
    {
        if (isDebug())
        {
            String szCallerName = StackWalker.getInstance()
                    .walk(s -> s.skip(1).findFirst())
                    .get()
                    .getMethodName();
            String szClassName = getClass().getSimpleName();

            StringBuilder szParameterList = new StringBuilder();

            if (szParameters != null)
            {
                for (int i = 0 ; i < szParameters.length ; i++)
                {
                    if (szParameters[i] != null)
                    {
                        szParameterList
                                .append(szParameters[i])
                                .append(i < szParameters.length - 1 ? ", " : "");
                    }
                }
            }

            System.out.printf("%s::%s(%s)%n",
                    szClassName,
                    szCallerName,
                    szParameterList
            );
        }
    }

    public void execute()
    {
        printDebug(null);
        SwingUtilities.invokeLater(() ->
        {
            // Reset UI on EDT
            consolePanel.clear();
        });

        // Reset all states
        setSubroutines(new ArrayList<>());
        setOperandStack(new ArrayDeque<>());
        setCurrentLine(1);
        setError(null);

        setCurrentMemory(new HashMap<>());
        setCurrentLines(getBytecode().split("\n"));
        setCurrentIndex(0);
        setRunning(true);
        consolePanel.updateToolBar();

        // Start VM on new thread
        setVMThread(new Thread(this, "VM-Thread"));
        getVMThread().setDaemon(true);
        getVMThread().start();
    }

    @Override
    public void run()
    {
        try
        {
            continueExecution();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        stop();
    }

    private void continueExecution()
    {
        while (getCurrentIndex() < getCurrentLines().length && isRunning())
        {
            try
            {
                // Delay so that thread is not continuously running
                Thread.sleep(1);

                int nextLine = routeExecution(getCurrentMemory(), getCurrentLines(), getCurrentIndex());

                if (!isRunning())
                {
                    break;
                }

                if (nextLine == Integer.MAX_VALUE - 1)
                {
                    break;
                }

                setCurrentIndex(nextLine + 1);
            }
            catch (InterruptedException e)
            {
                if (!isRunning())
                {
                    break;
                }
            }
        }
    }

    private int routeExecution(Map<Integer, Value> memory, String[] szLines, int iLine)
    {
        printDebug(new String[]{Integer.toString(iLine)});

        if (getError() != null)
        {
            return Integer.MAX_VALUE - 1;
        }

        String[] szLine = szLines[iLine].split(" ", 3);
        if (szLine.length < 2) return iLine;

        setCurrentLine(Integer.parseInt(szLine[0]));

        switch (szLine[1])
        {
            // Memory
            case "sconst" -> pushConst(DataType.STRING, szLine[2]);
            case "iconst" -> pushConst(DataType.INT, szLine[2]);
            case "fconst" -> pushConst(DataType.FLOAT, szLine[2]);
            case "bconst" -> pushConst(DataType.BOOLEAN, szLine[2]);
            case "cconst" -> pushConst(DataType.CHAR, szLine[2]);
            case "store" -> store(Integer.parseInt(szLine[2]), memory);
            case "load" -> load(Integer.parseInt(szLine[2]), memory);
            case "final" -> setFinalMode(true);
            // Arrays
            case "sarray" -> pushArray(DataType.STRING, Integer.parseInt(szLine[2]));
            case "iarray" -> pushArray(DataType.INT, Integer.parseInt(szLine[2]));
            case "farray" -> pushArray(DataType.FLOAT, Integer.parseInt(szLine[2]));
            case "barray" -> pushArray(DataType.BOOLEAN, Integer.parseInt(szLine[2]));
            case "carray" -> pushArray(DataType.CHAR, Integer.parseInt(szLine[2]));
            case "astore" -> astore(szLine[2]);
            case "aload" -> aload();
            case "length" -> length(); // Also used for strings
            // String operations
            case "substring" -> substring();
            case "sleft" -> sleft();
            case "sright" -> sright();
            case "supper" -> supper();
            case "slower" -> slower();
            case "sreplace" -> sreplace();
            case "concat" -> concat();
            // Integer operations
            case "iadd" -> iadd();
            case "isub" -> isub();
            case "ineg" -> ineg();
            case "imul" -> imul();
            case "idiv", "iquo" -> idiv();
            case "iexp" -> iexp();
            case "imod" -> imod();
            case "irandom" -> irandom();
            // Float operations
            case "fadd" -> fadd();
            case "fsub" -> fsub();
            case "fneg" -> fneg();
            case "fmul" -> fmul();
            case "fdiv" -> fdiv();
            case "fexp" -> fexp();
            case "pi" -> pi();
            case "round" -> round();
            // Logical operations
            case "cmpeq", "cmpneq", "cmplt", "cmplte", "cmpgt", "cmpgte", "booland", "boolor" -> compare(szLine[1]);
            case "boolnot" -> boolnot();
            // File operations
            case "filenew" -> filenew();
            case "fileopen" -> fileopen();
            case "iseof" -> iseof();
            case "filewriteline" -> filewriteline();
            case "filereadline" -> filereadline();
            case "fileclose" -> fileclose(memory);
            // Casting
            case "tostring" -> tostring();
            case "toint" -> toint();
            case "tofloat" -> tofloat();
            case "tobool" -> tobool();
            case "asc" -> asc();
            case "chr" -> chr();
            // Control flow
            case "if" ->
            {
                return ifStatement(szLines, iLine);
            }
            case "switch" ->
            {
                return switchStatement(szLines, iLine);
            }
            case "goto" ->
            {
                return gotoLabel(szLine[2], szLines);
            }
            case "return" ->
            {
                return Integer.MAX_VALUE - 1;
            }
            // Subroutines
            case "Subroutine:" ->
            {
                return createSubroutine(szLines, iLine);
            }
            case "call" ->
            {
                for (int i = 0 ; i < getSubroutines().size() ; i++)
                {
                    if (getSubroutines().get(i).getName().equals(szLine[2]))
                    {
                        callSubroutine(getSubroutines().get(i));
                    }
                }
            }
            // I/O
            case "input" -> input();
            case "print" -> print();
        }

        return iLine;
    }

    // ----------| Memory |-----------------------------------------------
    // "[lineNumber] {s|i|f|b|c}const [value]"
    // Pushes the constant onto the operand stack
    private void pushConst(DataType type, String szValue)
    {
        printDebug(new String[]{type.toString(), szValue});

        Value value = new Value(type, switch (type)
        {
            case STRING -> szValue;
            case INT -> Integer.parseInt(szValue);
            case FLOAT -> Double.parseDouble(szValue);
            case BOOLEAN -> Boolean.parseBoolean(szValue);
            case CHAR -> szValue.charAt(0);
            default -> null;
        });
        getOperandStack().push(value);
    }

    // "[lineNumber] store [memoryLocation]"
    // Stack must contain: value
    // Pops value from the operand stack and stores it at the specified location
    private void store(int iLocation, Map<Integer, Value> memory)
    {
        printDebug(new String[]{Integer.toString(iLocation)});

        Value existingValue = memory.get(iLocation);

        if (existingValue != null)
        {
            if (existingValue.isConst())
            {
                overwriteConstantError();
                return;
            }
        }

        Value value = getOperandStack().pop();
        if (isFinalMode())
        {
            value.setConst(true);
            setFinalMode(false);
        }
        memory.put(iLocation, value);
    }

    // "[lineNumber] load [memoryLocation]"
    // Copies the value at the specified location and pushes it onto the operand stack
    private void load(int iLocation, Map<Integer, Value> memory)
    {
        printDebug(new String[]{Integer.toString(iLocation)});
        getOperandStack().push(memory.get(iLocation));
    }

    // ----------| Arrays |-----------------------------------------------
    // "[lineNumber] {s|i|f|b|c}array [numberOfDimensions]"
    // Stack must contain: sizes of dimensions
    // Pushes the array onto the operand stack
    private void pushArray(DataType type, int iDimensions)
    {
        printDebug(new String[]{type.toString(), Integer.toString(iDimensions)});

        int[] iSizes = new int[iDimensions];

        for (int i = 0 ; i < iDimensions ; i++)
        {
            iSizes[iDimensions - 1 - i] = (int) getOperandStack().pop().getValue();
        }

        getOperandStack().push(new Value(DataType.ARRAY, Array.newInstance(Value.class, iSizes)));
    }

    // "[lineNumber] astore [pop/peek]"
    // Stack must contain: array, indices, value
    // Pops array, indices and value from the operand stack and stores the value at that location in the array
    private void astore(String szMode)
    {
        printDebug(new String[]{szMode});

        Value value = getOperandStack().pop();

        List<Integer> iIndices = new ArrayList<>();
        Value array = getArrayOperands(iIndices, szMode);

        if (array != null && !iIndices.isEmpty())
        {
            Object currentArray = array.getValue();

            // Navigate to the target array position
            for (int i = 0 ; i < iIndices.size() - 1 ; i++)
            {
                try
                {
                    currentArray = Array.get(currentArray, iIndices.get(i));
                }
                catch (ArrayIndexOutOfBoundsException e)
                {
                    arrayOutOfBounds(iIndices.get(i), Array.getLength(currentArray));
                    return;
                }
            }

            // Set the value at the final position
            int iIndex = iIndices.get(iIndices.size() - 1);
            try
            {
                Array.set(currentArray, iIndex, value);
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                arrayOutOfBounds(iIndex, Array.getLength(currentArray));
            }
        }
    }

    // "[lineNumber] aload"
    // Stack must contain: array, indices
    // Copies the array at the specified location and pushes it onto the operand stack
    private void aload()
    {
        printDebug(null);

        List<Integer> iIndices = new ArrayList<>();
        Value array = getArrayOperands(iIndices, "pop");

        if (array != null && !iIndices.isEmpty())
        {
            Object currentArray = array.getValue();

            // Navigate to the target array position
            for (int i = 0 ; i < iIndices.size() - 1 ; i++)
            {
                try
                {
                    currentArray = Array.get(currentArray, iIndices.get(i));
                }
                catch (ArrayIndexOutOfBoundsException e)
                {
                    arrayOutOfBounds(iIndices.get(i), Array.getLength(currentArray));
                    return;
                }
            }

            // Get the value at the final position
            int iIndex = iIndices.get(iIndices.size() - 1);
            try
            {
                Object valueAtIndex = Array.get(currentArray, iIndex);
                if (valueAtIndex instanceof Value value)
                {
                    getOperandStack().push(value);
                }
                else
                {
                    getOperandStack().push(new Value(DataType.ARRAY, valueAtIndex));
                }
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                arrayOutOfBounds(iIndex, Array.getLength(currentArray));
            }
        }
    }

    private Value getArrayOperands(List<Integer> iIndices, String szMode)
    {
        printDebug(null);

        while (!getOperandStack().isEmpty())
        {
            Value currentValue = getOperandStack().pop();
            if (currentValue.getType() == DataType.ARRAY)
            {
                if (szMode.equals("peek"))
                {
                    getOperandStack().push(currentValue);
                }

                Collections.reverse(iIndices);
                return currentValue;
            }
            iIndices.add((int) currentValue.getValue());
        }
        return null;
    }

    // ----------| String Operations |------------------------------------
    // "[lineNumber] length"
    // Stack must contain: string value or array
    // Pops value from operand stack and pushes the length of it onto the stack
    private void length()
    {
        printDebug(null);

        Object value = getOperandStack().pop().getValue();

        if (value instanceof String)
        {
            getOperandStack().push(new Value(DataType.INT, ((String) value).length()));
        }
        else
        {
            getOperandStack().push(new Value(DataType.INT, ((Object[]) value).length));
        }
    }

    // "[lineNumber] substring"
    // Stack must contain: string value, start integer, length integer
    // Pops required values from operand stack and pushes the substring onto the stack
    private void substring()
    {
        printDebug(null);

        int iLength = (int) getOperandStack().pop().getValue();
        int iStart = (int) getOperandStack().pop().getValue();
        String szValue = (String) getOperandStack().pop().getValue();
        getOperandStack().push(new Value(DataType.STRING, szValue.substring(iStart, iStart + iLength)));
    }

    // "[lineNumber] sleft"
    // Stack must contain: string value, length integer
    // Pops required values from operand stack and pushes the substring from the left onto the stack
    private void sleft()
    {
        printDebug(null);

        int iLength = (int) getOperandStack().pop().getValue();
        String szValue = (String) getOperandStack().pop().getValue();
        getOperandStack().push(new Value(DataType.STRING, szValue.substring(0, iLength)));
    }

    // "[lineNumber] sright"
    // Stack must contain: string value, length integer
    // Pops required values from operand stack and pushes the substring from the right onto the stack
    private void sright()
    {
        printDebug(null);

        int iLength = (int) getOperandStack().pop().getValue();
        String szValue = (String) getOperandStack().pop().getValue();
        getOperandStack().push(new Value(DataType.STRING, szValue.substring(szValue.length() - iLength)));
    }

    // "[lineNumber] supper"
    // Stack must contain: string value
    // Pops string from operand stack and pushes the transformed string onto the stack
    private void supper()
    {
        printDebug(null);

        String szValue = (String) getOperandStack().pop().getValue();
        getOperandStack().push(new Value(DataType.STRING, szValue.toUpperCase()));
    }

    // "[lineNumber] slower"
    // Stack must contain: string value
    // Pops string from operand stack and pushes the transformed string onto the stack
    private void slower()
    {
        printDebug(null);

        String szValue = (String) getOperandStack().pop().getValue();
        getOperandStack().push(new Value(DataType.STRING, szValue.toLowerCase()));
    }

    // "[lineNumber] sreplace"
    // Stack must contain: string value, regex string, replace with value
    // Pops required values from operand stack and pushes the transformed string onto the stack
    private void sreplace()
    {
        printDebug(null);

        String szReplaceWith = (String) getOperandStack().pop().getValue();
        String szRegex = (String) getOperandStack().pop().getValue();
        String szValue = (String) getOperandStack().pop().getValue();
        getOperandStack().push(new Value(DataType.STRING, szValue.replaceAll(szRegex, szReplaceWith)));
    }

    // "[lineNumber] concat"
    // Stack must contain: string value, string value
    // Pops strings from operand stack and pushes the combined string onto the stack
    private void concat()
    {
        printDebug(null);

        String szRight = String.valueOf(getOperandStack().pop().getValue());
        String szLeft = String.valueOf(getOperandStack().pop().getValue());
        getOperandStack().push(new Value(DataType.STRING, szLeft + szRight));
    }

    // ----------| Integer Operations |-----------------------------------
    // "[lineNumber] iadd"
    // Stack must contain: integer value, integer value
    // Pops required values from operand stack and pushes the result onto the stack
    private void iadd()
    {
        printDebug(null);

        int iRight = (int) getOperandStack().pop().getValue();
        int iLeft = (int) getOperandStack().pop().getValue();
        getOperandStack().push(new Value(DataType.INT, iLeft + iRight));
    }

    // "[lineNumber] isub"
    // Stack must contain: integer value, integer value
    // Pops required values from operand stack and pushes the result onto the stack
    private void isub()
    {
        printDebug(null);

        int iRight = (int) getOperandStack().pop().getValue();
        int iLeft = (int) getOperandStack().pop().getValue();
        getOperandStack().push(new Value(DataType.INT, iLeft - iRight));
    }

    // "[lineNumber] ineg"
    // Stack must contain: integer value, integer value
    // Pops required values from operand stack and pushes the result onto the stack
    private void ineg()
    {
        printDebug(null);

        int iOperand = (int) getOperandStack().pop().getValue();
        getOperandStack().push(new Value(DataType.INT, -iOperand));
    }

    // "[lineNumber] imul"
    // Stack must contain: integer value, integer value
    // Pops required values from operand stack and pushes the result onto the stack
    private void imul()
    {
        printDebug(null);

        int iLeft = (int) getOperandStack().pop().getValue();
        int iRight = (int) getOperandStack().pop().getValue();
        getOperandStack().push(new Value(DataType.INT, iLeft * iRight));
    }

    // "[lineNumber] idiv"
    // "[lineNumber] iquo"
    // Stack must contain: integer value, integer value
    // Pops required values from operand stack and pushes the result onto the stack
    private void idiv()
    {
        printDebug(null);

        int iRight = (int) getOperandStack().pop().getValue();
        int iLeft = (int) getOperandStack().pop().getValue();

        if (iRight == 0)
        {
            divideByZeroError();
            return;
        }

        getOperandStack().push(new Value(DataType.INT, iLeft / iRight));
    }

    // "[lineNumber] iexp"
    // Stack must contain: integer value, integer value
    // Pops required values from operand stack and pushes the result onto the stack
    private void iexp()
    {
        printDebug(null);

        int iRight = (int) getOperandStack().pop().getValue();
        int iLeft = (int) getOperandStack().pop().getValue();
        getOperandStack().push(new Value(DataType.INT, Math.pow(iLeft, iRight)));
    }

    // "[lineNumber] imod"
    // Stack must contain: integer value, integer value
    // Pops required values from operand stack and pushes the result onto the stack
    private void imod()
    {
        printDebug(null);

        int iRight = (int) getOperandStack().pop().getValue();
        int iLeft = (int) getOperandStack().pop().getValue();
        getOperandStack().push(new Value(DataType.INT, iLeft % iRight));
    }

    // "[lineNumber] irandom"
    // Stack must contain: integer value, integer value
    // Pops required values from operand stack and pushes the result onto the stack
    private void irandom()
    {
        printDebug(null);
        int iUpper = (int) getOperandStack().pop().getValue();
        int iLower = (int) getOperandStack().pop().getValue();

        if (iUpper < iLower)
        {
            runtimeError("The upper bound must be greater than the lower bound when generating random numbers");
            return;
        }

        getOperandStack().push(new Value(DataType.INT, new Random().nextInt(iUpper - iLower) + iLower));
    }

    // ----------| Float Operations |-------------------------------------
    // "[lineNumber] fadd"
    // Stack must contain: float value, float value
    // Pops required values from operand stack and pushes the result onto the stack
    private void fadd()
    {
        printDebug(null);

        double fRight = Double.parseDouble(String.valueOf(getOperandStack().pop().getValue()));
        double fLeft = Double.parseDouble(String.valueOf(getOperandStack().pop().getValue()));
        getOperandStack().push(new Value(DataType.FLOAT, fLeft + fRight));
    }

    // "[lineNumber] fsub"
    // Stack must contain: float value, float value
    // Pops required values from operand stack and pushes the result onto the stack
    private void fsub()
    {
        printDebug(null);

        double fRight = Double.parseDouble(String.valueOf(getOperandStack().pop().getValue()));
        double fLeft = Double.parseDouble(String.valueOf(getOperandStack().pop().getValue()));
        getOperandStack().push(new Value(DataType.FLOAT, fLeft - fRight));
    }

    // "[lineNumber] fneg"
    // Stack must contain: float value, float value
    // Pops required values from operand stack and pushes the result onto the stack
    private void fneg()
    {
        printDebug(null);

        double fOperand = Double.parseDouble(String.valueOf(getOperandStack().pop().getValue()));
        getOperandStack().push(new Value(DataType.FLOAT, -fOperand));
    }

    // "[lineNumber] fmul"
    // Stack must contain: float value, float value
    // Pops required values from operand stack and pushes the result onto the stack
    private void fmul()
    {
        printDebug(null);

        double fLeft = Double.parseDouble(String.valueOf(getOperandStack().pop().getValue()));
        double fRight = Double.parseDouble(String.valueOf(getOperandStack().pop().getValue()));
        getOperandStack().push(new Value(DataType.FLOAT, fLeft * fRight));
    }

    // "[lineNumber] fdiv"
    // Stack must contain: float value, float value
    // Pops required values from operand stack and pushes the result onto the stack
    private void fdiv()
    {
        printDebug(null);

        double fRight = Double.parseDouble(String.valueOf(getOperandStack().pop().getValue()));
        double fLeft = Double.parseDouble(String.valueOf(getOperandStack().pop().getValue()));

        if (fRight == 0d)
        {
            divideByZeroError();
            return;
        }

        getOperandStack().push(new Value(DataType.FLOAT, fLeft / fRight));
    }

    // "[lineNumber] fexp"
    // Stack must contain: float value, float value
    // Pops required values from operand stack and pushes the result onto the stack
    private void fexp()
    {
        printDebug(null);

        double fRight = Double.parseDouble(String.valueOf(getOperandStack().pop().getValue()));
        double fLeft = Double.parseDouble(String.valueOf(getOperandStack().pop().getValue()));
        getOperandStack().push(new Value(DataType.FLOAT, Math.pow(fLeft, fRight)));
    }

    // "[lineNumber] pi"
    // Pushes the pi constant onto the operand stack
    private void pi()
    {
        printDebug(null);
        getOperandStack().push(new Value(DataType.FLOAT, Math.PI));
    }

    // "[lineNumber] round"
    // Stack must contain: float value, integer for decimal places
    // Pops required values from operand stack and pushes the result onto the stack
    private void round()
    {
        printDebug(null);

        int iPlaces = (int) getOperandStack().pop().getValue();
        double fValue = (double) getOperandStack().pop().getValue();
        String szRounded = String.format("%." + iPlaces + "f", fValue);
        getOperandStack().push(new Value(DataType.FLOAT, Double.parseDouble(szRounded)));
    }

    // ----------| Logical Operations |-----------------------------------
    // "[lineNumber] {cmp{eq|neq|lt|lte|gt|gte}|bool{and|or}}"
    // Stack must contain: value, value
    // Pops required values from operand stack and pushes the result onto the stack
    private void compare(String szOperator)
    {
        printDebug(new String[]{szOperator});

        String szOperand2 = getOperandStack().pop().getValue().toString();
        String szOperand1 = getOperandStack().pop().getValue().toString();
        boolean bResult = switch (szOperator)
        {
            case "cmpeq" -> szOperand1.equals(szOperand2);
            case "cmpneq" -> !szOperand1.equals(szOperand2);
            case "cmplt" -> Double.parseDouble(szOperand1) < Double.parseDouble(szOperand2);
            case "cmplte" -> Double.parseDouble(szOperand1) <= Double.parseDouble(szOperand2);
            case "cmpgt" -> Double.parseDouble(szOperand1) > Double.parseDouble(szOperand2);
            case "cmpgte" -> Double.parseDouble(szOperand1) >= Double.parseDouble(szOperand2);
            case "booland" -> Boolean.parseBoolean(szOperand1) && Boolean.parseBoolean(szOperand2);
            case "boolor" -> Boolean.parseBoolean(szOperand1) || Boolean.parseBoolean(szOperand2);
            default -> false;
        };
        getOperandStack().push(new Value(DataType.BOOLEAN, bResult));
    }

    // "[lineNumber] boolnot"
    // Stack must contain: boolean value
    // Pops value from operand stack and pushes the result onto the stack
    private void boolnot()
    {
        printDebug(null);

        boolean bResult = !((boolean) getOperandStack().pop().getValue());
        getOperandStack().push(new Value(DataType.BOOLEAN, bResult));
    }

    // ----------| File Operations |--------------------------------------
    // "[lineNumber] filenew"
    // Stack must contain: string value for name
    // Pops string from operand stack and creates the file with the specified name
    private void filenew()
    {
        printDebug(null);

        String szName = (String) getOperandStack().pop().getValue();
        try
        {
            File file = new File(getResourceFolder(), szName);
            file.createNewFile();
        }
        catch (Exception e)
        {
            System.err.println("Runtime error: Unable to create new file.");
        }
    }

    // "[lineNumber] fileopen"
    // Stack must contain: string value for name
    // Pops string from operand stack and pushes the file handler for that file onto the stack
    private void fileopen()
    {
        printDebug(null);

        String szName = (String) getOperandStack().pop().getValue();
        FileHandler fh = new FileHandler(szName, getResourceFolder());
        getOperandStack().push(new Value(DataType.FILE, fh));
    }

    // "[lineNumber] iseof"
    // Stack must contain: file handler
    // Pops file from operand stack and pushes boolean (true if end of file, false otherwise) onto the stack
    private void iseof()
    {
        printDebug(null);

        FileHandler fh = (FileHandler) getOperandStack().pop().getValue();
        getOperandStack().push(new Value(DataType.BOOLEAN, fh.isEOF()));
    }

    // "[lineNumber] filewriteline"
    // Stack must contain: file handler, string value for line contents
    // Pops required values from operand stack and writes the line to the file
    private void filewriteline()
    {
        printDebug(null);

        String szLine = (String) getOperandStack().pop().getValue();
        FileHandler fh = (FileHandler) getOperandStack().pop().getValue();
        fh.writeLine(szLine);
    }

    // "[lineNumber] filereadline"
    // Stack must contain: file handler
    // Pops file from operand stack and pushes the line contents onto the stack
    private void filereadline()
    {
        printDebug(null);

        FileHandler fh = (FileHandler) getOperandStack().pop().getValue();
        getOperandStack().push(new Value(DataType.STRING, fh.readLine()));
    }

    // "[lineNumber] fileclose"
    // Stack must contain: file handler
    // Pops file from operand stack and closes the file
    private void fileclose(Map<Integer, Value> memory)
    {
        printDebug(null);

        FileHandler fh = (FileHandler) getOperandStack().pop().getValue();

        for (Map.Entry<Integer, Value> entry : memory.entrySet())
        {
            if (entry.getValue().getValue().equals(fh))
            {
                memory.remove(entry.getKey());
                break;
            }
        }
    }

    // ----------| Type Casting |-----------------------------------------
    // "[lineNumber] tostring"
    // Stack must contain: value
    // Pops value from operand stack and pushes the string equivalent onto the stack
    private void tostring()
    {
        printDebug(null);

        Object value = getOperandStack().pop().getValue();
        getOperandStack().push(new Value(DataType.STRING, value.toString()));
    }

    // "[lineNumber] toint"
    // Stack must contain: value
    // Pops value from operand stack and pushes the integer equivalent onto the stack
    private void toint()
    {
        printDebug(null);

        Object value = getOperandStack().pop().getValue();

        int iValue = 0;

        try
        {
            //iValue = Integer.parseInt(value.toString());
            iValue = Math.round(Float.parseFloat(value.toString()));
        }
        catch (Exception e)
        {
            castingError(value.toString(), "int");
            return;
        }

        getOperandStack().push(new Value(DataType.INT, iValue));
    }

    // "[lineNumber] tofloat"
    // Stack must contain: value
    // Pops value from operand stack and pushes the float equivalent onto the stack
    private void tofloat()
    {
        printDebug(null);

        Object value = getOperandStack().pop().getValue();

        float fValue = 0;

        try
        {
            fValue = Float.parseFloat(value.toString());
        }
        catch (Exception e)
        {
            castingError(value.toString(), "float");
            return;
        }

        getOperandStack().push(new Value(DataType.FLOAT, fValue));
    }

    // "[lineNumber] tobool"
    // Stack must contain: value
    // Pops value from operand stack and pushes the boolean equivalent onto the stack
    private void tobool()
    {
        printDebug(null);

        Object value = getOperandStack().pop().getValue();

        boolean bValue = false;

        try
        {
            bValue = Boolean.parseBoolean(value.toString());
        }
        catch (Exception e)
        {
            castingError(value.toString(), "boolean");
            return;
        }

        getOperandStack().push(new Value(DataType.BOOLEAN, bValue));
    }

    // "[lineNumber] asc"
    // Stack must contain: character value
    // Pops value from operand stack and pushes the ASCII integer onto the stack
    private void asc()
    {
        printDebug(null);

        char cValue = (char) getOperandStack().pop().getValue();
        getOperandStack().push(new Value(DataType.INT, (int) cValue));
    }

    // "[lineNumber] chr"
    // Stack must contain: integer value
    // Pops value from operand stack and pushes the ASCII character onto the stack
    private void chr()
    {
        printDebug(null);

        int iValue = (int) getOperandStack().pop().getValue();
        getOperandStack().push(new Value(DataType.CHAR, (char) iValue));
    }

    // ----------| Control Flow |-----------------------------------------
    // "[lineNumber] if Label[integer]"
    // Stack must contain: boolean value for condition
    // Pops value from operand stack and if true, jumps to specified label
    private int ifStatement(String[] szLines, int iStart)
    {
        printDebug(new String[]{Integer.toString(iStart)});

        boolean bCondition = (boolean) getOperandStack().pop().getValue();

        if (bCondition)
        {
            return gotoLabel(szLines[iStart].split(" ")[2], szLines);
        }
        return iStart;
    }

    // "[lineNumber] switch"
    // Stack must contain: value for condition
    // Pops value from operand stack and compares it to cases, jumps to matching label
    // Example: will jump to Label2
    // 1 iconst 1
    // 2 switch
    // 3 0 Label1
    // 5 1 Label2
    // 7 default Label4
    // 9 end Label5
    // ...
    // 5 Label2
    // ...
    private int switchStatement(String[] szLines, int iStart)
    {
        printDebug(new String[]{Integer.toString(iStart)});

        Value condition = getOperandStack().pop();

        for (int i = iStart ; i < szLines.length ; i++)
        {
            String[] szLine = szLines[i].split(" ");

            if (szLine.length > 1 && (szLine[1].equals(condition.getValue().toString()) || szLine[1].equals("default") || szLine[1].equals("end")))
            {
                return gotoLabel(szLine[2], szLines);
            }
        }
        return szLines.length;
    }

    // "[lineNumber] goto Label[integer]"
    // Jumps to the specified label in the bytecode
    private int gotoLabel(String szLabel, String[] szLines)
    {
        printDebug(new String[]{szLabel});

        for (int i = 0 ; i < szLines.length ; i++)
        {
            String szInstruction = szLines[i];
            String[] szComponents = szInstruction.split(" ");
            if (szComponents.length == 2 && szComponents[1].equals(szLabel))
            {
                return i;
            }
        }
        return szLines.length;
    }

    // ----------| Subroutines |------------------------------------------
    // "[lineNumber] Subroutine: [name] [type1, type2, ...]"
    // Creates a Subroutine object with all properties and adds it to the list of all subroutines
    private int createSubroutine(String[] szLines, int iStart)
    {
        printDebug(new String[]{Integer.toString(iStart)});

        String[] szSignature = szLines[iStart].split(" ");

        int i;
        for (i = iStart; i < szLines.length ; i++)
        {
            String[] szLine = szLines[i].split(" ");
            if (szLine.length == 2 && szLine[1].equals("Endsub"))
            {
                break;
            }
        }

        String[] szParameters;
        if (szSignature.length == 3)
        {
            szParameters = new String[0];
        }
        else
        {
            szParameters = Arrays.copyOfRange(szSignature, 3, szSignature.length);
        }

        Subroutine newSubroutine = new Subroutine(
                szSignature[2],
                szParameters,
                Arrays.copyOfRange(szLines, iStart + 1, i)
        );
        getSubroutines().add(newSubroutine);
        return i;
    }

    // "[lineNumber] call [name] [type1, type2, ...]"
    // Stack must contain: all arguments to be passed to the subroutine
    // Executes the specified subroutine
    private void callSubroutine(Subroutine subroutine)
    {
        printDebug(new String[]{subroutine.getName()});

        Map<Integer, Value> subMemory = new HashMap<>();
        for (int i = subroutine.getParameters().length - 1 ; i >= 0 ; i--)
        {
            subMemory.put(i, getOperandStack().pop());
        }

        String[] szPreviousLines = getCurrentLines();
        Map<Integer, Value> previousMemory = getCurrentMemory();
        int iPreviousIndex = getCurrentIndex();

        setCurrentLines(subroutine.getBodyLines());
        setCurrentMemory(subMemory);
        setCurrentIndex(0);

        continueExecution();
        if (!isWaitingForInput())
        {
            // Restore state if not waiting for input
            setCurrentLines(szPreviousLines);
            setCurrentMemory(previousMemory);
            setCurrentIndex(iPreviousIndex);
        }
    }

    // ----------| Input/Output |-----------------------------------------
    // "[lineNumber] input"
    // Pauses execution, waits for input from the user
    private void input()
    {
        printDebug(null);
        setWaitingForInput(true);
        SwingUtilities.invokeLater(() -> consolePanel.enableInput());

        synchronized (getInputLock())
        {
            try
            {
                while (getPendingInput() == null && isRunning())
                {
                    inputLock.wait();
                }
                if (isRunning() && getPendingInput() != null)
                {
                    getOperandStack().push(new Value(DataType.STRING, getPendingInput()));
                    setPendingInput(null);
                }
            }
            catch (InterruptedException e)
            {
                if (!isRunning())
                {
                    return;
                }
            }
        }
        setWaitingForInput(false);
    }

    public void submitInput(String input)
    {
        synchronized (getInputLock())
        {
            setPendingInput(input);
            getInputLock().notify();
        }
    }

    // "[lineNumber] print"
    // Stack must contain: string value
    // Pops value from operand stack and prints it to the console
    private void print()
    {
        final String szText = getOperandStack().pop().getValue().toString();
        SwingUtilities.invokeLater(() -> consolePanel.print(szText));
    }

    // ----------| Error Handling |-----------------------------------------
    private void runtimeError(String szDescription)
    {
        printDebug(new String[]{szDescription});
        setError(new Error(getCurrentLine(), szDescription + "\nExecution terminated"));
    }

    private void arrayOutOfBounds(int iIndex, int iLength)
    {
        printDebug(new String[]{Integer.toString(iIndex)});
        runtimeError(
                String.format(
                        "Attempted to access index '%d' for array of length '%d'",
                        iIndex,
                        iLength
                )
        );
    }

    private void divideByZeroError()
    {
        printDebug(new String[]{});
        runtimeError("Attempted to divide by zero");
    }

    private void overwriteConstantError()
    {
        printDebug(new String[]{});
        runtimeError("Attempted to overwrite constant");
    }

    private void castingError(String szValue, String szType)
    {
        printDebug(new String[]{szValue, szType});
        runtimeError(
                String.format(
                        "Failed to cast value '%s' to type '%s'",
                        szValue,
                        szType
                )
        );
    }

    public void stop()
    {
        setRunning(false);
        consolePanel.updateToolBar();

        if (getError() != null)
        {
            SwingUtilities.invokeLater(() ->
                    consolePanel.print(
                            String.format(
                                    "<!> Error from line %d: %s",
                                    getError().getLine(),
                                    getError().getDescription()
                            )
                    )
            );
        }

        if (getVMThread() != null)
        {
            getVMThread().interrupt();
        }
    }
}
