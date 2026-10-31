package ERL.Compiler.Bytecode;

import ERL.Compiler.Parser.Nodes.*;
import ERL.Compiler.Parser.Nodes.Expressions.Arithmetic.ArithmeticOperationNode;
import ERL.Compiler.Parser.Nodes.Expressions.*;
import ERL.Compiler.Parser.Nodes.Expressions.Comparison.ComparisonOperationNode;
import ERL.Compiler.Parser.Nodes.Expressions.Comparison.ComparisonOperator;
import ERL.Compiler.Parser.Nodes.Expressions.Literals.*;
import ERL.Compiler.Parser.Nodes.Expressions.Logical.LogicalOperationNode;
import ERL.Compiler.Parser.Nodes.Expressions.Unary.UnaryOperationNode;
import ERL.Compiler.Parser.Nodes.Expressions.Unary.UnaryOperator;
import ERL.Compiler.Parser.Nodes.Statements.*;
import ERL.Compiler.Parser.Nodes.Subroutines.MainNode;
import ERL.Compiler.Parser.Nodes.Subroutines.ParameterNode;
import ERL.Compiler.Parser.Nodes.Subroutines.SubroutineNode;

import java.util.ArrayList;
import java.util.List;

/**
 * This class generates bytecode instructions from an AST, which can be executed by the corresponding VM.
 *
 * @author Shivam Attari
 */
public class BytecodeGenerator
{
    // Instance Variables
    private boolean bDebug;

    private ProgramNode programNode; // Root node of AST
    private String szBytecode;

    private List<String> variables;
    private List<String> globalVariables;
    private List<String> parameters;

    private int iCurrentLabel;

    public BytecodeGenerator(ProgramNode programNode, boolean bDebug)
    {
        setDebug(bDebug);
        setProgramNode(programNode);
        setBytecode("");
        setVariables(new ArrayList<>());
        setGlobalVariables(new ArrayList<>());
        setParameters(new ArrayList<>());
        setCurrentLabel(1);
    }

    // Getters
    public boolean isDebug()
    {
        return bDebug;
    }

    public ProgramNode getProgramNode()
    {
        return programNode;
    }

    public String getBytecode()
    {
        return szBytecode;
    }

    public List<String> getVariables()
    {
        return variables;
    }

    public List<String> getGlobalVariables()
    {
        return globalVariables;
    }

    public List<String> getParameters()
    {
        return parameters;
    }

    public int getCurrentLabel()
    {
        return iCurrentLabel;
    }

    // Setters
    public void setDebug(boolean bDebug)
    {
        this.bDebug = bDebug;
    }

    public void setProgramNode(ProgramNode programNode)
    {
        this.programNode = programNode;
    }

    public void setBytecode(String szBytecode)
    {
        this.szBytecode = szBytecode;
    }

    public void setVariables(List<String> variables)
    {
        this.variables = variables;
    }

    public void setGlobalVariables(List<String> globalVariables)
    {
        this.globalVariables = globalVariables;
    }

    public void setParameters(List<String> currentParameters)
    {
        this.parameters = currentParameters;
    }

    public void setCurrentLabel(int iCurrentLabel)
    {
        this.iCurrentLabel = iCurrentLabel;
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
                    szParameterList);
        }
    }

    public void generate()
    {
        printDebug(new String[]{});

        // Collect all global variables BEFORE processing
        for (ASTNode node : getProgramNode().getStatements())
        {
            if (node instanceof VariableDeclarationNode varDecl)
            {
                getGlobalVariables().add(varDecl.getIdentifier().getName());
            }
        }

        StringBuilder szCode = new StringBuilder();

        for (ASTNode node : getProgramNode().getStatements())
        {
            boolean bAddLines = !(node instanceof SubroutineNode || node instanceof MainNode);

            if (bAddLines)
            {
                String[] szCodes = routeGeneration(node).split("\n");
                for (String szSubCode : szCodes)
                {
                    if (!szSubCode.isBlank())
                    {
                        szCode.append(node.iLine)
                                .append(" ")
                                .append(szSubCode)
                                .append("\n");
                    }
                }
            }
            else
            {
                szCode.append(routeGeneration(node));
            }

            szCode.append("\n");
        }

        setBytecode(szCode.toString());

        StringBuilder szFinalBytecode = new StringBuilder();
        for (String szLine : getBytecode().split("\n"))
        {
            if (!szLine.isBlank())
            {
                szFinalBytecode.append(szLine).append("\n");
            }
        }

        setBytecode(szFinalBytecode.toString());
    }

    private String generateStatements(List<ASTNode> statements, boolean bAddLines)
    {
        printDebug(new String[]{});

        StringBuilder szCode = new StringBuilder();
        for (int i = 0 ; i < statements.size() ; i++)
        {
            if (bAddLines)
            {
                String[] szCodes = routeGeneration(statements.get(i)).split("\n");

                for (String szSubCode : szCodes)
                {
                    if (!szSubCode.isBlank())
                    {
                        szCode.append(statements.get(i).iLine)
                                .append(" ")
                                .append(szSubCode)
                                .append("\n");
                    }
                }
            }
            else
            {
                szCode.append(routeGeneration(statements.get(i)));
            }

            if (i < statements.size() - 1)
            {
                szCode.append("\n");
            }
        }
        return szCode.toString();
    }

    private String routeGeneration(ASTNode node)
    {
        printDebug(new String[]{node.toString()});

        if (node instanceof BlockNode instance) return generateBlock(instance, false);
        if (node instanceof MainNode instance) return generateMain(instance);
        if (node instanceof SubroutineNode instance) return generateSubroutineDefinition(instance);
        if (node instanceof SubroutineCallNode instance) return generateSubroutineCall(instance, false);
        if (node instanceof InstanceSubroutineCallNode instance) return generateInstanceSubroutineCall(instance);
        if (node instanceof InstancePropertyNode instance) return generateInstanceProperty(instance);
        if (node instanceof ReturnNode instance) return generateReturn(instance);
        if (node instanceof VariableDeclarationNode instance) return generateVariableDeclaration(instance);
        if (node instanceof AssignmentNode instance) return generateAssignment(instance);
        if (node instanceof IdentifierNode instance) return generateLoadVariable(instance);
        if (node instanceof IfNode instance) return generateIf(instance);
        if (node instanceof SwitchNode instance) return generateSwitch(instance);
        if (node instanceof ForNode instance) return generateFor(instance);
        if (node instanceof WhileNode instance) return generateWhile(instance);
        if (node instanceof DoUntilNode instance) return generateDoUntil(instance);
        if (node instanceof ArithmeticOperationNode instance) return generateArithmeticOperation(instance);
        if (node instanceof ComparisonOperationNode instance) return generateComparisonOperation(instance);
        if (node instanceof LogicalOperationNode instance) return generateLogicalOperation(instance);
        if (node instanceof UnaryOperationNode instance) return generateUnaryOperation(instance);
        if (node instanceof StringLiteralNode instance) return generateStringLiteral(instance);
        if (node instanceof IntLiteralNode instance) return generateIntLiteral(instance);
        if (node instanceof FloatLiteralNode instance) return generateFloatLiteral(instance);
        if (node instanceof BooleanLiteralNode instance) return generateBooleanLiteral(instance);
        if (node instanceof CharLiteralNode instance) return generateCharLiteral(instance);
        if (node instanceof ArrayAccessNode instance) return generateArrayAccess(instance);
        if (node instanceof ArrayLiteralNode instance)
        { // Generate bytecode for array literals which are not assigned to variables
            StringBuilder szTemp = new StringBuilder();

            ArrayLiteralNode currentInstance = instance;
            List<Integer> iSizes = new ArrayList<>();

            while (true)
            {
                if (currentInstance.getElements() != null && !currentInstance.getElements().isEmpty())
                {
                    iSizes.add(currentInstance.getElements().size());
                    if (currentInstance.getElements().get(0) instanceof ArrayLiteralNode newInstance)
                    {
                        currentInstance = newInstance;
                        continue;
                    }
                }
                break;
            }

            for (int iSize : iSizes)
            {
                szTemp.append("iconst ")
                        .append(iSize)
                        .append("\n");
            }

            switch (instance.getType())
            {
                case STRING -> szTemp.append("sarray");
                case INT -> szTemp.append("iarray");
                case FLOAT -> szTemp.append("farray");
                case BOOLEAN -> szTemp.append("barray");
                case CHAR -> szTemp.append("carray");
            }

            szTemp.append(" ")
                    .append(iSizes.size())
                    .append("\n");

            generateArrayLiteral(instance, szTemp, -1, new ArrayList<>());
            return szTemp.toString();
        }
        return "";
    }

    private String generateMain(MainNode node)
    {
        printDebug(new String[]{node.toString()});

        setVariables(new ArrayList<>());
        setParameters(new ArrayList<>());
        return generateStatements(node.getBlock().getStatements(), true).trim();
    }

    private String generateSubroutineDefinition(SubroutineNode node)
    {
        printDebug(new String[]{node.toString()});

        StringBuilder szCode = new StringBuilder();
        szCode.append(node.iLine)
                .append(" Subroutine: ")
                .append(node.getIdentifier().getName())
                .append(" ");

        setParameters(new ArrayList<>());
        setVariables(new ArrayList<>());

        for (int i = 0 ; i < node.getParameters().size() ; i++)
        {
            ParameterNode param = node.getParameters().get(i);
            szCode.append(param.getTypeNode().getDataType().toString().toLowerCase());
            if (i < node.getParameters().size() - 1)
            {
                szCode.append(" ");
            }
            getParameters().add(param.getIdentifier().getName());
        }

        szCode.append("\n")
                .append(generateBlock(node.getBlock(), true))
                .append("\n")
                .append(node.iLine)
                .append(" Endsub");

        return szCode.toString();
    }

    private String generateBlock(BlockNode node, boolean bAddLines)
    {
        printDebug(new String[]{node.toString()});
        return generateStatements(node.getStatements(), bAddLines);
    }

    private String generateSubroutineCall(SubroutineCallNode node, boolean bInstance)
    {
        printDebug(new String[]{node.toString()});

        String[][] szPredefined = {
                {"input", "input"},
                {"print", "print"},
                {"str", "tostring"},
                {"int", "toint"},
                {"float", "tofloat"},
                {"bool", "tobool"},
                {"ASC", "asc"},
                {"CHR", "chr"},
                {"round", "round"},
                {"random", "irandom"},
                {"open", "fileopen"},
                {"newFile", "filenew"}
        };
        String[][] szEnvironmentIdentifiers = {
                {"length", "length"},
                {"substring", "substring"},
                {"left", "sleft"},
                {"right", "sright"},
                {"upper", "supper"},
                {"lower", "slower"},
                {"replace", "sreplace"},
                {"sqrt", "sqrt"},
                {"pi", "pi"},
                {"endOfFile", "iseof"},
                {"readLine", "filereadline"},
                {"writeLine", "filewriteline"},
                {"close", "fileclose"}
        };

        StringBuilder szCode = new StringBuilder();

        for (ASTNode argumentExpression : node.getArgumentExpressions())
        {
            szCode.append(routeGeneration(argumentExpression)).append("\n");

            if (node.getIdentifier().getName().equals("input"))
            {
                szCode.append("print\n");
            }
        }

        if (bInstance)
        {
            for (String[] szEnvId : szEnvironmentIdentifiers)
            {
                if (node.getIdentifier().getName().equals(szEnvId[0]))
                {
                    szCode.append(szEnvId[1]);
                    return szCode.toString();
                }
            }
        }

        String szPredefinedName = "";
        for (String[] szPreName : szPredefined)
        {
            if (node.getIdentifier().getName().equals(szPreName[0]))
            {
                szPredefinedName = szPreName[1];
                break;
            }
        }

        if (szPredefinedName.isEmpty())
        {
            szCode.append("call ");
            szCode.append(node.getIdentifier().getName());
        }
        else
        {
            szCode.append(szPredefinedName);
        }

        return szCode.toString().trim();
    }

    private String generateInstanceSubroutineCall(InstanceSubroutineCallNode node)
    {
        printDebug(new String[]{node.toString()});
        return routeGeneration(node.getInstance())
                + "\n"
                + generateSubroutineCall(node.getSubroutineCall(), true);
    }

    private String generateInstanceProperty(InstancePropertyNode node)
    {
        printDebug(new String[]{node.toString()});

        StringBuilder szCode = new StringBuilder();

        SubroutineCallNode subCall = new SubroutineCallNode(node.getProperty(), new ArrayList<>(), node.iLine);

        szCode.append(routeGeneration(node.getInstance()))
                .append("\n")
                .append(generateSubroutineCall(subCall, true));

        return szCode.toString();
    }

    private String generateReturn(ReturnNode node)
    {
        printDebug(new String[]{node.toString()});
        return routeGeneration(node.getExpression()) + "\nreturn";
    }

    /**
     * Generates the bytecode for declaring a variable, including array declaration.
     *
     * @param node the node to generate bytecode from
     * @return the corresponding bytecode for the node
     */
    private String generateVariableDeclaration(VariableDeclarationNode node)
    {
        printDebug(new String[]{node.toString()});

        String szVarName = node.getIdentifier().getName();

        int iIndex = -1;

        // Check if it's a global variable (being declared at program level)
        if (getGlobalVariables().contains(szVarName))
        {
            iIndex = getGlobalVariables().indexOf(szVarName);
        }
        else
        {
            // It's a local variable in a subroutine
            for (int i = 0 ; i < getVariables().size() ; i++)
            {
                if (getVariables().get(i).equals(szVarName))
                {
                    iIndex = getGlobalVariables().size() + getParameters().size() + i;
                    break;
                }
            }

            if (iIndex == -1)
            {
                getVariables().add(szVarName);
                iIndex = getGlobalVariables().size() + getParameters().size() + getVariables().size() - 1;
            }
        }

        StringBuilder szCode = new StringBuilder();

        if (node.getTypeNode().isConst())
        {
            szCode.append("final\n");
        }

        List<ASTNode> dimensions = node.getTypeNode().getDimensions();
        if (node.getTypeNode().getDimensions() != null && !dimensions.isEmpty())
        {
            // Array declaration
            for (ASTNode dimension : dimensions)
            {
                szCode.append(routeGeneration(dimension))
                        .append("\n");
            }

            switch (node.getTypeNode().getDataType())
            {
                case STRING -> szCode.append("sarray");
                case INT -> szCode.append("iarray");
                case FLOAT -> szCode.append("farray");
                case BOOLEAN -> szCode.append("barray");
                case CHAR -> szCode.append("carray");
            }

            szCode.append(" ")
                    .append(dimensions.size())
                    .append("\nstore ")
                    .append(iIndex);

            // Parse array literal
            if (node.getInitialExpression() != null)
            {
                szCode.append("\n");

                // Generate all assignments
                generateArrayLiteral((ArrayLiteralNode) node.getInitialExpression(), szCode, iIndex, new ArrayList<>());
            }
        }
        else
        {
            if (node.getInitialExpression() == null)
            {
                switch (node.getTypeNode().getDataType())
                {
                    case STRING -> szCode.append("sconst  ");
                    case INT -> szCode.append("iconst 0");
                    case FLOAT -> szCode.append("fconst 0.0");
                    case BOOLEAN -> szCode.append("bconst false");
                    case CHAR -> szCode.append("cconst  ");
                }
            }
            else
            {
                szCode.append(routeGeneration(node.getInitialExpression()));
            }

            szCode.append("\nstore ")
                    .append(iIndex);
        }

        return szCode.toString();
    }

    private String generateAssignment(AssignmentNode node)
    {
        printDebug(new String[]{node.toString()});

        if (node.getExpression() instanceof SubroutineCallNode subroutineCallNode)
        {
            if (subroutineCallNode.getIdentifier().getName().equals("open"))
            {
                return generateVariableDeclaration(
                        new VariableDeclarationNode(
                                node.getIdentifier(),
                                new TypeNode(
                                        DataType.FILE,
                                        null,
                                        false,
                                        false,
                                        node.iLine
                                ),
                                subroutineCallNode,
                                node.iLine
                        )
                );
            }
        }

        StringBuilder szCode = new StringBuilder();
        String szVarName = node.getIdentifier().getName();
        int iIndex;

        // Check globals first, then parameters, then local variables
        if (getGlobalVariables().contains(szVarName))
        {
            iIndex = getGlobalVariables().indexOf(szVarName);
        }
        else if (getParameters().contains(szVarName))
        {
            iIndex = getGlobalVariables().size() + getParameters().indexOf(szVarName);
        }
        else
        {
            iIndex = getGlobalVariables().size() + getParameters().size() + getVariables().indexOf(szVarName);
        }

        // Special handling for array literal assignment
        if (node.getExpression() instanceof ArrayLiteralNode arrayLiteral)
        {
            if (node.getIndices() != null && !node.getIndices().isEmpty())
            {
                // Assigning array literal to array index - get base array position
                szCode.append("load ")
                        .append(iIndex)
                        .append("\n");

                // Push indices except last one
                for (int i = 0 ; i < node.getIndices().size() ; i++)
                {
                    szCode.append(routeGeneration(node.getIndices().get(i)))
                            .append("\n");
                }

                // Generate assignments for array literal elements
                generateArrayLiteral(arrayLiteral, szCode, iIndex, new ArrayList<>(node.getIndices()));
                return szCode.toString();
            }
            else
            {
                // Get dimensions of new array literal
                List<ASTNode> dimensions = new ArrayList<>();

                if (arrayLiteral.getElements() != null && !arrayLiteral.getElements().isEmpty())
                {
                    ASTNode currentElement = arrayLiteral.getElements().get(0);
                    while (currentElement instanceof ArrayLiteralNode array)
                    {
                        dimensions.add(
                                new IntLiteralNode(
                                        array.getElements().size(),
                                        array.iLine
                                )
                        );

                        if (array.getElements() != null && !array.getElements().isEmpty())
                        {
                            currentElement = array.getElements().get(0);
                        }
                        else
                        {
                            currentElement = null;
                        }
                    }
                }

                // Assigning to whole array variable
                return generateVariableDeclaration(new VariableDeclarationNode(
                        node.getIdentifier(),
                        new TypeNode(
                                arrayLiteral.getType(),
                                dimensions,
                                false,
                                false,
                                arrayLiteral.iLine
                        ),
                        arrayLiteral,
                        arrayLiteral.iLine
                ));
            }
        }

        // Regular assignment logic for non-array literals
        if (node.getIndices() != null && !node.getIndices().isEmpty())
        {
            // Special handling for resetting array with dimensions
            if (node.getExpression() == null)
            {
                for (ASTNode index : node.getIndices())
                {
                    szCode.append(routeGeneration(index))
                            .append("\n");
                }

                switch (node.getArrayAssignType())
                {
                    case STRING -> szCode.append("sarray");
                    case INT -> szCode.append("iarray");
                    case FLOAT -> szCode.append("farray");
                    case BOOLEAN -> szCode.append("barray");
                    case CHAR -> szCode.append("carray");
                }

                szCode.append(" ")
                        .append(node.getIndices().size())
                        .append("\nstore ")
                        .append(iIndex)
                        .append("");

                return szCode.toString();
            }

            // Array element assignment
            szCode.append("load ")
                    .append(iIndex)
                    .append("\n");

            // Push all indices onto the stack
            for (ASTNode index : node.getIndices())
            {
                szCode.append(routeGeneration(index))
                        .append("\n");
            }

            // Generate the value to assign
            szCode.append(routeGeneration(node.getExpression()))
                    .append("\nastore pop");
        }
        else
        {
            // Simple variable assignment
            szCode.append(routeGeneration(node.getExpression()))
                    .append("\nstore ")
                    .append(iIndex);
        }

        return szCode.toString();
    }

    private String generateLoadVariable(IdentifierNode node)
    {
        printDebug(new String[]{node.toString()});

        String szVarName = node.getName();
        int iIndex;

        // Check if it's a global variable first
        if (getGlobalVariables().contains(szVarName))
        {
            iIndex = getGlobalVariables().indexOf(szVarName);
        }
        else if (getParameters().contains(szVarName))
        {
            iIndex = getGlobalVariables().size() + getParameters().indexOf(szVarName);
        }
        else if (getVariables().contains(szVarName))
        {
            iIndex = getGlobalVariables().size() + getParameters().size() + getVariables().indexOf(szVarName);
        }
        else
        {
            // Shouldn't reach here, but fallback
            iIndex = 0;
        }

        return "load " + iIndex;
    }

    // ----------| Control Flow |-----------------------------------------
    private String generateIf(IfNode node)
    {
        printDebug(new String[]{node.toString()});
        StringBuilder szCode = new StringBuilder();

        int iElseIfCount = node.getElseIfNodes().size();
        boolean bHasElse = node.getElseBlock() != null;

        // Reserve labels for else-if and else blocks
        int iReserved = iElseIfCount + (bHasElse ? 3 : 2);

        int iStartLabel = getCurrentLabel();
        int iElseIfStart = iStartLabel + 1;
        int iElseLabel = bHasElse ? iStartLabel + iElseIfCount + 1 : -1;
        int iEndLabel = iStartLabel + iElseIfCount + (bHasElse ? 2 : 1);

        // Pass over reserved labels so that nested statements have unique labels
        setCurrentLabel(iStartLabel + iReserved);

        // Generate if condition code
        szCode.append(routeGeneration(node.getCondition()))
                .append("\nif Label")
                .append(iStartLabel)
                .append("\n");

        // Generate elseif condition code
        for (int i = 0 ; i < iElseIfCount ; i++)
        {
            int iCurrentLabel = iElseIfStart + i;
            szCode.append(routeGeneration(node.getElseIfNodes().get(i).getCondition()))
                    .append("\nif Label")
                    .append(iCurrentLabel)
                    .append("\n");
        }

        // Jump to either else block or end label
        szCode.append("goto Label")
                .append(bHasElse ? iElseLabel : iEndLabel)
                .append("\n");

        // Generate then block
        szCode.append("Label")
                .append(iStartLabel)
                .append("\n")
                .append(routeGeneration(node.getThenBlock()))
                .append("\n")
                .append("goto Label")
                .append(iEndLabel)
                .append("\n");

        // Generate elseif blocks
        for (int i = 0 ; i < iElseIfCount ; i++)
        {
            int iCurrentLabel = iElseIfStart + i;
            szCode.append("Label")
                    .append(iCurrentLabel)
                    .append("\n")
                    .append(routeGeneration(node.getElseIfNodes().get(i).getBlock()))
                    .append("\n")
                    .append("goto Label")
                    .append(iEndLabel)
                    .append("\n");
        }

        // Generate else block
        if (bHasElse)
        {
            szCode.append("Label")
                    .append(iElseLabel)
                    .append("\n")
                    .append(routeGeneration(node.getElseBlock()))
                    .append("\n");
        }

        // End of the entire if block
        szCode.append("Label")
                .append(iEndLabel);

        return szCode.toString();
    }

    private String generateSwitch(SwitchNode node)
    {
        printDebug(new String[]{node.toString()});

        StringBuilder szCode = new StringBuilder();

        szCode.append(routeGeneration(node.getExpression()))
                .append("\nswitch\n");

        int iBlockLabel = getCurrentLabel();
        int iListLabel = iBlockLabel;
        setCurrentLabel(getCurrentLabel() + node.getCases().size() + (node.getDefaultBlock() == null ? 1 : 2));

        // Generate case labels
        for (CaseNode caseNode : node.getCases())
        {
            szCode.append(getLiteralValue(caseNode.getExpression()))
                    .append(" Label")
                    .append(iListLabel)
                    .append("\n");
            iListLabel++;
        }

        // Generate default label
        if (node.getDefaultBlock() != null)
        {
            szCode.append("default Label")
                    .append(iListLabel)
                    .append("\n");
            iListLabel++;
        }

        // Generate end label
        szCode.append("end Label")
                .append(iListLabel)
                .append("\n");

        // Generate case blocks
        for (CaseNode caseNode : node.getCases())
        {
            szCode.append("Label")
                    .append(iBlockLabel)
                    .append("\n")
                    .append(routeGeneration(caseNode.getBlock()))
                    .append("\ngoto Label")
                    .append(iListLabel)
                    .append("\n");
            iBlockLabel++;
        }

        // Generate default block
        if (node.getDefaultBlock() != null)
        {
            szCode.append("Label")
                    .append(iBlockLabel)
                    .append("\n")
                    .append(routeGeneration(node.getDefaultBlock()))
                    .append("\ngoto Label")
                    .append(iListLabel)
                    .append("\n");
            iBlockLabel++;
        }

        szCode.append("Label")
                .append(iBlockLabel);

        return szCode.toString();
    }

    private String generateFor(ForNode node)
    {
        printDebug(new String[]{node.toString()});

        StringBuilder szCode = new StringBuilder();

        int iStartLabel = getCurrentLabel();
        setCurrentLabel(getCurrentLabel() + 2);

        VariableDeclarationNode varDecNode = new VariableDeclarationNode(
                node.getStart().getIdentifier(),
                new TypeNode(
                        DataType.INT,
                        null,
                        false,
                        false,
                        node.iLine),
                node.getStart().getExpression(),
                node.iLine);
        szCode.append(routeGeneration(varDecNode))
                .append("\n")
                .append("Label")
                .append(iStartLabel)
                .append("\n");

        // Determine the comparison operator based on the step value
        ComparisonOperator compOp = ComparisonOperator.GT;
        if (node.getStepExpression() != null && node.getStepExpression() instanceof UnaryOperationNode unaryNode)
        {
            if (unaryNode.getOperator() == UnaryOperator.MINUS && unaryNode.getOperand() instanceof IntLiteralNode)
            {
                compOp = ComparisonOperator.LT;
            }
        }
        ComparisonOperationNode compOpNode = new ComparisonOperationNode(
                compOp,
                node.getStart().getIdentifier(),
                node.getEndExpression(),
                node.iLine);

        szCode.append(routeGeneration(compOpNode))
                .append("if Label")
                .append(iStartLabel + 1)
                .append("\n")
                .append(routeGeneration(node.getBlock()))
                .append("\n")
                .append(node.getStepExpression() == null ? "iconst 1" : routeGeneration(node.getStepExpression()))
                .append("\nload ")
                .append(getGlobalVariables().size() + getParameters().size() + getVariables().indexOf(node.getStart().getIdentifier().getName()))
                .append("\niadd\nstore ")
                .append(getGlobalVariables().size() + getParameters().size() + getVariables().indexOf(node.getStart().getIdentifier().getName()))
                .append("\ngoto Label")
                .append(iStartLabel)
                .append("\nLabel")
                .append(iStartLabel + 1);

        return szCode.toString();
    }

    private String generateWhile(WhileNode node)
    {
        printDebug(new String[]{node.toString()});

        StringBuilder szCode = new StringBuilder();

        int iStartLabel = getCurrentLabel();
        setCurrentLabel(getCurrentLabel() + 2);

        szCode.append("Label")
                .append(iStartLabel)
                .append("\n")
                .append(routeGeneration(node.getCondition()))
                .append("\nboolnot\nif Label")
                .append(iStartLabel + 1)
                .append("\n")
                .append(routeGeneration(node.getBlock()))
                .append("\ngoto Label")
                .append(iStartLabel)
                .append("\nLabel")
                .append(iStartLabel + 1);

        return szCode.toString();
    }

    private String generateDoUntil(DoUntilNode node)
    {
        printDebug(new String[]{node.toString()});

        StringBuilder szCode = new StringBuilder();

        int iStartLabel = getCurrentLabel();
        setCurrentLabel(getCurrentLabel() + 2);

        szCode.append("Label")
                .append(iStartLabel)
                .append("\n")
                .append(routeGeneration(node.getBlock()))
                .append("\n")
                .append(routeGeneration(node.getCondition()))
                .append("\nif Label")
                .append(iStartLabel + 1)
                .append("\ngoto Label")
                .append(iStartLabel)
                .append("\nLabel")
                .append(iStartLabel + 1);

        return szCode.toString();
    }

    // ----------| Arithmetic |-------------------------------------------
    private String generateArithmeticOperation(ArithmeticOperationNode node)
    {
        printDebug(new String[]{node.toString()});

        StringBuilder szCode = new StringBuilder();

        String szOperator = "";

        if (node.isIntMode())
        {
            szOperator = switch (node.getOperator())
            {
                case ADD -> "iadd";
                case SUBTRACT -> "isub";
                case MULTIPLY -> "imul";
                case DIVIDE -> "idiv";
                case EXPONENT -> "iexp";
                case MODULO -> "imod";
                case QUOTIENT -> "iquo";
                case CONCATENATE -> "concat";
            };
        }
        else
        {
            szOperator = switch (node.getOperator())
            {
                case ADD -> "fadd";
                case SUBTRACT -> "fsub";
                case MULTIPLY -> "fmul";
                case DIVIDE -> "fdiv";
                case EXPONENT -> "fexp";
                case CONCATENATE -> "concat";
                default -> "fdiv";
            };
        }

        szCode.append(routeGeneration(node.getLeftOperand()))
                .append("\n")
                .append(routeGeneration(node.getRightOperand()))
                .append("\n")
                .append(szOperator);

        return szCode.toString();
    }

    // ----------| Comparison |-------------------------------------------
    private String generateComparisonOperation(ComparisonOperationNode node)
    {
        printDebug(new String[]{node.toString()});

        StringBuilder szCode = new StringBuilder();

        szCode.append(routeGeneration(node.getLeftOperand()))
                .append("\n")
                .append(routeGeneration(node.getRightOperand()))
                .append("\n")
                .append(switch (node.getOperator())
                {
                    case LT -> "cmplt";
                    case LTE -> "cmplte";
                    case GT -> "cmpgt";
                    case GTE -> "cmpgte";
                    case EQ -> "cmpeq";
                    case NEQ -> "cmpneq";
                })
                .append("\n");

        return szCode.toString();
    }

    // ----------| Logical |----------------------------------------------
    private String generateLogicalOperation(LogicalOperationNode node)
    {
        printDebug(new String[]{node.toString()});

        StringBuilder szCode = new StringBuilder();

        szCode.append(routeGeneration(node.getLeftOperand()))
                .append("\n")
                .append(routeGeneration(node.getRightOperand()))
                .append("\n")
                .append(switch (node.getOperator())
                {
                    case AND -> "booland";
                    case OR -> "boolor";
                })
                .append("\n");

        return szCode.toString();
    }

    private String generateUnaryOperation(UnaryOperationNode node)
    {
        printDebug(new String[]{node.toString()});

        StringBuilder szCode = new StringBuilder();

        szCode.append(routeGeneration(node.getOperand()))
                .append("\n")
                .append(switch (node.getOperator())
                {
                    case PLUS, MINUS -> "ineg";
                    case NOT -> "boolnot";
                });

        return szCode.toString();
    }

    // ----------| Literals |---------------------------------------------
    private String generateStringLiteral(StringLiteralNode node)
    {
        printDebug(new String[]{node.toString()});
        return "sconst " + node.getValue().substring(1, node.getValue().length() - 1);
    }

    private String generateIntLiteral(IntLiteralNode node)
    {
        printDebug(new String[]{node.toString()});
        return "iconst " + node.getValue();
    }

    private String generateFloatLiteral(FloatLiteralNode node)
    {
        printDebug(new String[]{node.toString()});
        return "fconst " + node.getValue();
    }

    private String generateBooleanLiteral(BooleanLiteralNode node)
    {
        printDebug(new String[]{node.toString()});
        return "bconst " + node.getValue();
    }

    private String generateCharLiteral(CharLiteralNode node)
    {
        printDebug(new String[]{node.toString()});
        return "cconst " + node.getValue();
    }

    private String getLiteralValue(ASTNode expression)
    {
        printDebug(new String[]{expression.toString()});
        if (expression instanceof StringLiteralNode) return ((StringLiteralNode) expression).getValue();
        if (expression instanceof IntLiteralNode) return String.valueOf(((IntLiteralNode) expression).getValue());
        if (expression instanceof FloatLiteralNode) return String.valueOf(((FloatLiteralNode) expression).getValue());
        if (expression instanceof BooleanLiteralNode)
            return String.valueOf(((BooleanLiteralNode) expression).getValue());
        if (expression instanceof CharLiteralNode) return String.valueOf(((CharLiteralNode) expression).getValue());
        return "";
    }

    // ----------| Arrays |-----------------------------------------------
    private void generateArrayLiteral(ArrayLiteralNode literalNode, StringBuilder szCode, int iCurrentArray, List<ASTNode> baseIndices)
    {
        printDebug(new String[]{literalNode.toString()});
        for (int i = 0 ; i < literalNode.getElements().size() ; i++)
        {
            ASTNode elementNode = literalNode.getElements().get(i);

            if (elementNode instanceof ArrayLiteralNode literalElementNode)
            {
                // Copy base indices for next level
                List<ASTNode> newIndices = new ArrayList<>(baseIndices);
                newIndices.add(new IntLiteralNode(i, literalNode.iLine));
                generateArrayLiteral(literalElementNode, szCode, iCurrentArray, newIndices);
            }
            else
            {
                // Generate array store instruction
                if (iCurrentArray != -1)
                {
                    szCode.append("load ")
                            .append(iCurrentArray)
                            .append("\n");
                }

                // Push all base indices
                for (ASTNode index : baseIndices)
                {
                    szCode.append(routeGeneration(index))
                            .append("\n");
                }

                // Push current index
                szCode.append("iconst ")
                        .append(i)
                        .append("\n")
                        .append(routeGeneration(elementNode))
                        .append("\nastore ")
                        .append(iCurrentArray == -1 ? "peek" : "pop")
                        .append("\n");
            }
        }
    }

    private String generateArrayAccess(ArrayAccessNode node)
    {
        printDebug(new String[]{node.toString()});
        StringBuilder szCode = new StringBuilder();

        // Load the array variable
        szCode.append(generateLoadVariable(node.getIdentifier()))
                .append("\n");

        // Push all indices onto the stack
        for (ASTNode index : node.getIndices())
        {
            szCode.append(routeGeneration(index))
                    .append("\n");
        }

        // Add array load instruction
        szCode.append("aload");

        return szCode.toString();
    }
}
