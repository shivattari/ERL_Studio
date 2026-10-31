package ERL.Compiler.Semantics;

import ERL.Compiler.Parser.Nodes.*;
import ERL.Compiler.Parser.Nodes.Expressions.Arithmetic.ArithmeticOperationNode;
import ERL.Compiler.Parser.Nodes.Expressions.Arithmetic.ArithmeticOperator;
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
import ERL.Issues.Error;
import ERL.Issues.Issue;
import ERL.Issues.Warning;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The AST is checked for any semantic issues, including type checking and
 * verifying that variables and subroutines are correctly used.
 *
 * @author Shivam Attari
 */
public class SemanticAnalysis
{
    // Instance Variables
    private boolean bDebug;

    private ProgramNode programNode; // Root node of AST
    private List<Issue> issues;

    private SubroutineNode[] reservedSignatures;
    private List<SubroutineNode> existingSubroutines;
    private Deque<Map<String, TypeNode>> variableStack;

    private boolean bMainExists;

    private Set<String> functionsBeingAnalyzed;
    private Map<String, DataType> functionReturnTypes;
    private Map<String, DataType> temporaryReturnTypes;

    // Constructor
    public SemanticAnalysis(ProgramNode programNode, boolean bDebug)
    {
        setProgramNode(programNode);
        setDebug(bDebug);
        setIssues(new ArrayList<>());
        setExistingSubroutines(new ArrayList<>());
        setVariableStack(new ArrayDeque<>());
        getVariableStack().push(new HashMap<>());
        setMainExists(false);
        addPredefinedSubroutines();
        setFunctionsBeingAnalyzed(new HashSet<>());
        setFunctionReturnTypes(new HashMap<>());
        setTemporaryReturnTypes(new HashMap<>());
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

    public List<Issue> getIssues()
    {
        return issues;
    }

    public SubroutineNode[] getReservedSignatures()
    {
        return reservedSignatures;
    }

    public List<SubroutineNode> getExistingSubroutines()
    {
        return existingSubroutines;
    }

    public Deque<Map<String, TypeNode>> getVariableStack()
    {
        return variableStack;
    }

    public boolean isMainExists()
    {
        return bMainExists;
    }

    public Set<String> getFunctionsBeingAnalyzed()
    {
        return functionsBeingAnalyzed;
    }

    public Map<String, DataType> getFunctionReturnTypes()
    {
        return functionReturnTypes;
    }

    public Map<String, DataType> getTemporaryReturnTypes()
    {
        return temporaryReturnTypes;
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

    public void setIssues(List<Issue> issues)
    {
        this.issues = issues;
    }

    public void setReservedSignatures(SubroutineNode[] reservedSignatures)
    {
        this.reservedSignatures = reservedSignatures;
    }

    public void setExistingSubroutines(List<SubroutineNode> existingSubroutines)
    {
        this.existingSubroutines = existingSubroutines;
    }

    public void setVariableStack(Deque<Map<String, TypeNode>> variableStack)
    {
        this.variableStack = variableStack;
    }

    public void setMainExists(boolean bMainExists)
    {
        this.bMainExists = bMainExists;
    }

    public void setFunctionsBeingAnalyzed(Set<String> functionsBeingAnalyzed)
    {
        this.functionsBeingAnalyzed = functionsBeingAnalyzed;
    }

    public void setFunctionReturnTypes(Map<String, DataType> functionReturnTypes)
    {
        this.functionReturnTypes = functionReturnTypes;
    }

    public void setTemporaryReturnTypes(Map<String, DataType> temporaryReturnTypes)
    {
        this.temporaryReturnTypes = temporaryReturnTypes;
    }

    // Methods
    // ----------| Helpers |----------------------------------------------
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

    private void addPredefinedSubroutines()
    {
        // Create reserved signature nodes for subroutines included with the ERL
        // Format: name, parameter types...
        List<List<String>> signatures = new ArrayList<>(
                List.of(
                        new ArrayList<>(List.of("input")),
                        new ArrayList<>(List.of("input", "String")),
                        new ArrayList<>(List.of("input", "int")),
                        new ArrayList<>(List.of("input", "float")),
                        new ArrayList<>(List.of("input", "boolean")),
                        new ArrayList<>(List.of("input", "char")),
                        new ArrayList<>(List.of("print")),
                        new ArrayList<>(List.of("print", "String")),
                        new ArrayList<>(List.of("print", "int")),
                        new ArrayList<>(List.of("print", "float")),
                        new ArrayList<>(List.of("print", "boolean")),
                        new ArrayList<>(List.of("print", "char")),
                        new ArrayList<>(List.of("round", "float", "int")),
                        new ArrayList<>(List.of("random", "int", "int")),
                        new ArrayList<>(List.of("str", "String")),
                        new ArrayList<>(List.of("str", "int")),
                        new ArrayList<>(List.of("str", "float")),
                        new ArrayList<>(List.of("str", "boolean")),
                        new ArrayList<>(List.of("str", "char")),
                        new ArrayList<>(List.of("int", "String")),
                        new ArrayList<>(List.of("int", "int")),
                        new ArrayList<>(List.of("int", "float")),
                        new ArrayList<>(List.of("int", "boolean")),
                        new ArrayList<>(List.of("int", "char")),
                        new ArrayList<>(List.of("float", "String")),
                        new ArrayList<>(List.of("float", "int")),
                        new ArrayList<>(List.of("float", "float")),
                        new ArrayList<>(List.of("float", "boolean")),
                        new ArrayList<>(List.of("float", "char")),
                        new ArrayList<>(List.of("bool", "String")),
                        new ArrayList<>(List.of("bool", "int")),
                        new ArrayList<>(List.of("bool", "float")),
                        new ArrayList<>(List.of("bool", "boolean")),
                        new ArrayList<>(List.of("bool", "char")),
                        new ArrayList<>(List.of("char", "String")),
                        new ArrayList<>(List.of("char", "int")),
                        new ArrayList<>(List.of("char", "float")),
                        new ArrayList<>(List.of("char", "boolean")),
                        new ArrayList<>(List.of("char", "char")),
                        new ArrayList<>(List.of("ASC", "char")),
                        new ArrayList<>(List.of("CHR", "int")),
                        new ArrayList<>(List.of("open", "String")),
                        new ArrayList<>(List.of("newFile", "String"))
                )
        );

        setReservedSignatures(new SubroutineNode[signatures.size()]);
        for (int i = 0 ; i < signatures.size() ; i++)
        {
            List<String> signature = signatures.get(i);

            List<ParameterNode> parameters = new ArrayList<>();
            for (int j = 1 ; j < signature.size() ; j++)
            {
                DataType dataType = switch (signature.get(j))
                {
                    case "String" -> DataType.STRING;
                    case "int" -> DataType.INT;
                    case "float" -> DataType.FLOAT;
                    case "boolean" -> DataType.BOOLEAN;
                    case "char" -> DataType.CHAR;
                    default -> null;
                };

                TypeNode typeNode = new TypeNode(dataType, null, false, false, 1);
                IdentifierNode identifierNode = new IdentifierNode("x" + j, 1);
                parameters.add(new ParameterNode(identifierNode, typeNode, 1));
            }

            SubroutineNode node = new SubroutineNode(
                    new IdentifierNode(signature.get(0), 1),
                    parameters,
                    null,
                    !signature.get(0).matches("print|newFile"),
                    1
            );
            getReservedSignatures()[i] = node;
        }
    }

    private TypeNode getTypeNodeFromStack(String szIdentifier)
    {
        for (Map<String, TypeNode> currentMap : getVariableStack())
        {
            TypeNode typeNode = currentMap.get(szIdentifier);
            if (typeNode != null)
            {
                return typeNode;
            }
        }
        return null;
    }

    private void addVariableToStack(String szIdentifier, TypeNode typeNode)
    {
        getVariableStack().peek().put(szIdentifier, typeNode);
    }

    private void intoScope()
    {
        getVariableStack().push(new HashMap<>());
    }

    private void outOfScope()
    {
        getVariableStack().pop();
    }

    private void createWarning(int iLine, String szDescription)
    {
        printDebug(new String[]{String.valueOf(iLine), szDescription});
        getIssues().add(new Warning(iLine, szDescription));
    }

    private void createWarning(ASTNode node, String szDescription)
    {
        printDebug(new String[]{node.toString(), szDescription});
        createWarning(node.iLine, szDescription);
    }

    private void createError(int iLine, String szDescription)
    {
        printDebug(new String[]{String.valueOf(iLine), szDescription});
        getIssues().add(new Error(iLine, szDescription));
    }

    private void createError(ASTNode node, String szDescription)
    {
        printDebug(new String[]{node.toString(), szDescription});
        createError(node.iLine, szDescription);
    }

    private void mergeErrors()
    {
        List<Issue> filteredIssues = new ArrayList<>();

        for (Issue oldIssue : getIssues())
        {
            boolean bExists = false;
            for (Issue newIssue : filteredIssues)
            {
                if (newIssue.toString().equals(oldIssue.toString()))
                {
                    bExists = true;
                    break;
                }
            }

            if (!bExists)
            {
                filteredIssues.add(oldIssue);
            }
        }

        setIssues(filteredIssues);
    }

    // ----------| Analysis |---------------------------------------------
    public void analyseProgram()
    {
        printDebug(null);

        for (ASTNode statement : getProgramNode().getStatements())
        {
            routeAnalysis(statement);
        }

        for (ASTNode statement : getProgramNode().getStatements())
        {
            if (isMainExists() || !getExistingSubroutines().isEmpty())
            {
                if (!(statement instanceof SubroutineNode
                        || statement instanceof MainNode))
                {
                    if (statement instanceof VariableDeclarationNode varNode)
                    {
                        if (varNode.getTypeNode().isGlobal())
                        {
                            continue;
                        }
                    }

                    createError(
                            statement.iLine,
                            "Statement must be contained in a subroutine or main block"
                    );
                }
            }
        }

        mergeErrors();
    }

    private void routeAnalysis(ASTNode node)
    {
        printDebug(new String[]{node.toString()});

        if (node instanceof AssignmentNode instance) analyseAssignment(instance);
        else if (node instanceof BlockNode instance) analyseBlock(instance);

        else if (node instanceof InstanceSubroutineCallNode instance) analyseInstanceSubroutineCall(instance);
        else if (node instanceof SubroutineCallNode instance) analyseSubroutineCall(instance);

        else if (node instanceof CaseNode instance) analyseCase(instance, null);
        else if (node instanceof DoUntilNode instance) analyseDoUntil(instance);
        else if (node instanceof ElseIfNode instance) analyseElseIf(instance);
        else if (node instanceof ForNode instance) analyseFor(instance);
        else if (node instanceof IfNode instance) analyseIf(instance);
        else if (node instanceof SwitchNode instance) analyseSwitch(instance);
        else if (node instanceof VariableDeclarationNode instance) analyseVariableDeclaration(instance);
        else if (node instanceof WhileNode instance) analyseWhile(instance);

        else if (node instanceof SubroutineNode instance) analyseSubroutine(instance);
        else if (node instanceof MainNode instance) analyseMain(instance);
        else if (node instanceof ParameterNode || node instanceof ReturnNode)
        {
        }

        else
        {
            createError(
                    node,
                    "Not a statement"
            );
        }
    }

    private DataType getExpressionType(ASTNode node)
    {
        printDebug(new String[]{node.toString()});

        // Literal nodes have equivalent types
        if (node instanceof IntLiteralNode) return DataType.INT;
        if (node instanceof FloatLiteralNode) return DataType.FLOAT;
        if (node instanceof StringLiteralNode) return DataType.STRING;
        if (node instanceof BooleanLiteralNode) return DataType.BOOLEAN;
        if (node instanceof CharLiteralNode) return DataType.CHAR;

        // Check arithmetic operations for correct operand types
        if (node instanceof ArithmeticOperationNode arithmeticNode)
        {
            DataType leftType = getExpressionType(arithmeticNode.getLeftOperand());
            DataType rightType = getExpressionType(arithmeticNode.getRightOperand());

            if (leftType == null || rightType == null)
            {
                return null;
            }

            // Arithmetic operations must only consist of floats and integers
            if ((leftType == DataType.FLOAT || leftType == DataType.INT)
                    && (rightType == DataType.FLOAT || rightType == DataType.INT))
            {
                boolean bIntMode = leftType == DataType.INT && rightType == DataType.INT;

                arithmeticNode.setIntMode(bIntMode);

                if (bIntMode)
                {
                    return DataType.INT;
                }

                if (arithmeticNode.getOperator() == ArithmeticOperator.MODULO)
                {
                    createError(
                            node,
                            "The 'MOD' operator can only be used on integers"
                    );
                    return null;
                }

                if (arithmeticNode.getOperator() == ArithmeticOperator.QUOTIENT)
                {
                    createError(
                            node,
                            "The 'DIV' operator can only be used on integers"
                    );
                    return null;
                }

                return DataType.FLOAT;
            }

            // Allow concatenation of strings with other types
            if (leftType == DataType.STRING || rightType == DataType.STRING)
            {
                arithmeticNode.setOperator(ArithmeticOperator.CONCATENATE);
                return DataType.STRING;
            }

            createError(
                    node,
                    "Arithmetic operation must only consist of numeric values"
            );
            return null;
        }

        // Check comparisons for correct operand types
        if (node instanceof ComparisonOperationNode comparisonNode)
        {
            DataType leftType = getExpressionType(comparisonNode.getLeftOperand());
            DataType rightType = getExpressionType(comparisonNode.getRightOperand());
            ComparisonOperator operator = comparisonNode.getOperator();

            // Comparisons can be done between any matching types
            if ((leftType == DataType.FLOAT || leftType == DataType.INT) && (rightType == DataType.FLOAT || rightType == DataType.INT))
            {
                return DataType.BOOLEAN;
            }

            if (leftType == DataType.STRING && rightType == DataType.STRING
                    || leftType == DataType.BOOLEAN && rightType == DataType.BOOLEAN
                    || leftType == DataType.CHAR && rightType == DataType.CHAR)
            {
                if (operator == ComparisonOperator.EQ || operator == ComparisonOperator.NEQ)
                {
                    return DataType.BOOLEAN;
                }
                createError(
                        node,
                        String.format(
                                "Only equality comparisons can be performed on '%s' values",
                                leftType.toString().toLowerCase()
                        )
                );
                return null;
            }

            if (leftType != null && rightType != null)
            {
                createError(
                        node,
                        String.format(
                                "Type mismatch: cannot compare '%s' with '%s'",
                                leftType.toString().toLowerCase(),
                                rightType.toString().toLowerCase()
                        )
                );
            }
            return null;
        }

        // Check logical operations (AND/OR) for correct operand types
        if (node instanceof LogicalOperationNode logicalNode)
        {
            DataType leftType = getExpressionType(logicalNode.getLeftOperand());
            DataType rightType = getExpressionType(logicalNode.getRightOperand());

            if (leftType == DataType.BOOLEAN && rightType == DataType.BOOLEAN)
            {
                return DataType.BOOLEAN;
            }

            createError(
                    node,
                    "Type mismatch: logical operations can only be performed on boolean values"
            );
            return null;
        }

        // Check unary operations for correct operand type
        if (node instanceof UnaryOperationNode unaryNode)
        {
            DataType type = getExpressionType(unaryNode.getOperand());

            if (unaryNode.getOperator() == UnaryOperator.NOT)
            {
                if (type == DataType.BOOLEAN)
                {
                    return DataType.BOOLEAN;
                }
                if (type != null)
                {
                    createError(
                            node,
                            String.format(
                                    "Type mismatch: expected 'boolean', got '%s'",
                                    type.toString().toLowerCase()
                            )
                    );
                }
                return null;
            }

            if (type == DataType.FLOAT || type == DataType.INT)
            {
                return type;
            }

            if (type != null)
            {
                createError(
                        node,
                        String.format(
                                "Type mismatch: expected a numeric value, got '%s'",
                                type.toString().toLowerCase()
                        )
                );
            }
            return null;
        }

        // Search existing variables for their type
        if (node instanceof IdentifierNode identifierNode)
        {
            TypeNode typeNode = null;

            for (Map<String, TypeNode> currentMap : getVariableStack())
            {
                typeNode = currentMap.get(identifierNode.getName());
                if (typeNode != null)
                {
                    break;
                }
            }

            if (typeNode != null)
            {
                return typeNode.getDataType();
            }

            createError(
                    node,
                    String.format(
                            "No variable exists with the name '%s'",
                            identifierNode.getName()
                    )
            );
            return null;
        }

        // Infer return type of function call
        if (node instanceof SubroutineCallNode subroutineCallNode)
        {
            String szFunctionName = subroutineCallNode.getIdentifier().getName();

            // Check if return type is already known
            if (getFunctionReturnTypes().containsKey(szFunctionName))
            {
                return getFunctionReturnTypes().get(szFunctionName);
            }

            // Check temporary return types for recursive calls
            if (getFunctionsBeingAnalyzed().contains(szFunctionName))
            {
                return getTemporaryReturnTypes().getOrDefault(szFunctionName, null);
            }

            // Find matching subroutine
            SubroutineNode matchingSubroutine = findMatchingSubroutine(subroutineCallNode);
            if (matchingSubroutine == null)
            {
                List<String> typeStrings = new ArrayList<>();

                for (ASTNode argumentNode : subroutineCallNode.getArgumentExpressions())
                {
                    DataType expressionType = getExpressionType(argumentNode);
                    typeStrings.add(expressionType != null ? expressionType.toString().toLowerCase() : "?");
                }

                StringBuilder szArgs = new StringBuilder();
                for (int i = 0 ; i < typeStrings.size() ; i++)
                {
                    szArgs.append(typeStrings.get(i));
                    if (i < typeStrings.size() - 1)
                    {
                        szArgs.append(", ");
                    }
                }

                createError(
                        node,
                        String.format(
                                "The function '%s(%s)' is not defined",
                                szFunctionName,
                                szArgs
                        )
                );
                return null;
            }

            return inferReturnType(matchingSubroutine);
        }

        // Get return type of instance function call
        if (node instanceof InstanceSubroutineCallNode instanceCallNode)
        {
            DataType instanceType = getExpressionType(instanceCallNode.getInstance());
            SubroutineCallNode callNode = instanceCallNode.getSubroutineCall();
            switch (callNode.getIdentifier().getName())
            {
                case "substring" ->
                {
                    if (instanceType == DataType.STRING)
                    {
                        if (callNode.getArgumentExpressions().size() == 2
                                && getExpressionType(callNode.getArgumentExpressions().get(0)) == DataType.INT
                                && getExpressionType(callNode.getArgumentExpressions().get(1)) == DataType.INT)
                        {
                            return DataType.STRING;
                        }
                        createError(
                                instanceCallNode,
                                "The function 'substring' requires two 'int' arguments"
                        );
                        return null;
                    }
                    createError(
                            instanceCallNode,
                            "'substring' can only be called on a 'String' value"
                    );
                }
                case "left" ->
                {
                    if (instanceType == DataType.STRING)
                    {
                        if (callNode.getArgumentExpressions().size() == 1
                                && getExpressionType(callNode.getArgumentExpressions().get(0)) == DataType.INT)
                        {
                            return DataType.STRING;
                        }
                        createError(
                                instanceCallNode,
                                "The function 'left' requires one 'int' argument"
                        );
                        return null;
                    }
                    createError(
                            instanceCallNode,
                            "'left' can only be called on a 'String' value"
                    );
                }
                case "right" ->
                {
                    if (instanceType == DataType.STRING)
                    {
                        if (callNode.getArgumentExpressions().size() == 1
                                && getExpressionType(callNode.getArgumentExpressions().get(0)) == DataType.INT)
                        {
                            return DataType.STRING;
                        }
                        createError(
                                instanceCallNode,
                                "The function 'right' requires one 'int' argument"
                        );
                        return null;
                    }
                    createError(
                            instanceCallNode,
                            "'right' can only be called on a 'String' value"
                    );
                }
                case "replace" ->
                {
                    if (instanceType == DataType.STRING)
                    {
                        if (callNode.getArgumentExpressions().size() == 2
                                && getExpressionType(callNode.getArgumentExpressions().get(0)) == DataType.STRING
                                && getExpressionType(callNode.getArgumentExpressions().get(1)) == DataType.STRING)
                        {
                            return DataType.STRING;
                        }
                        createError(
                                instanceCallNode,
                                "The function 'replace' requires two 'String' arguments"
                        );
                        return null;
                    }
                    createError(
                            instanceCallNode,
                            "'replace' can only be called on a 'String' value"
                    );
                }
                case "endOfFile" ->
                {
                    if (instanceType != DataType.FILE)
                    {
                        createError(
                                instanceCallNode,
                                "'endOfFile' can only be called on a 'File' value"
                        );
                        return null;
                    }
                    return DataType.BOOLEAN;
                }
                case "readLine" ->
                {
                    if (instanceType != DataType.FILE)
                    {
                        createError(
                                instanceCallNode,
                                "'readLine' can only be called on a 'File' value"
                        );
                        return null;
                    }
                    return DataType.STRING;
                }
                case "writeLine" ->
                {
                    if (instanceType != DataType.FILE)
                    {
                        createError(
                                instanceCallNode,
                                "'writeLine' can only be called on a 'File' value"
                        );
                        return null;
                    }
                    return DataType.STRING;
                }
                case "close" ->
                {
                    if (instanceType != DataType.FILE)
                    {
                        createError(
                                instanceCallNode,
                                "'close' can only be called on a 'File' value"
                        );
                    }
                }
                default -> createError(
                        instanceCallNode,
                        String.format(
                                "The function '%s' is not defined",
                                callNode.getIdentifier().getName()
                        )
                );
            }
            return null;
        }

        // Get type of instance property
        if (node instanceof InstancePropertyNode instancePropertyNode)
        {
            DataType instanceType = getExpressionType(instancePropertyNode.getInstance());
            switch (instancePropertyNode.getProperty().getName())
            {
                case "length" ->
                {
                    TypeNode typeNode = null;
                    if (instancePropertyNode.getInstance() instanceof IdentifierNode identifierNode)
                    {
                        typeNode = getTypeNodeFromStack(identifierNode.getName());
                    }
                    else if (instancePropertyNode.getInstance() instanceof ArrayAccessNode arrayAccessNode)
                    {
                        typeNode = getTypeNodeFromStack(arrayAccessNode.getIdentifier().getName());
                    }

                    if (instanceType == DataType.STRING
                            || (typeNode != null && typeNode.getDimensions() != null && !typeNode.getDimensions().isEmpty())
                            || instancePropertyNode.getInstance() instanceof ArrayLiteralNode)
                    {
                        return DataType.INT;
                    }
                    createError(
                            instancePropertyNode,
                            "'length' can only be retrieved for a 'String' value or an array"
                    );
                }
                case "upper" ->
                {
                    if (instanceType == DataType.STRING)
                    {
                        return DataType.STRING;
                    }
                    createError(
                            instancePropertyNode,
                            "'upper' can only be retrieved for a 'String' value"
                    );
                }
                case "lower" ->
                {
                    if (instanceType == DataType.STRING)
                    {
                        return DataType.STRING;
                    }
                    createError(
                            instancePropertyNode,
                            "'lower' can only be retrieved for a 'String' value"
                    );
                }
            }
            return null;
        }

        // Check array literal type
        if (node instanceof ArrayLiteralNode arrayLiteralNode)
        {
            if (arrayLiteralNode.getElements().isEmpty())
            {
                return null;
            }

            // Get type of first element
            DataType elementType = getExpressionType(arrayLiteralNode.getElements().get(0));
            if (elementType == null)
            {
                return null;
            }

            // All elements must be of the same type
            for (ASTNode element : arrayLiteralNode.getElements())
            {
                DataType currentType = getExpressionType(element);
                if (currentType == null)
                {
                    return null;
                }
                if (currentType != elementType)
                {
                    createError(
                            element,
                            String.format(
                                    "Array elements must all be of the same type: expected '%s', got '%s'",
                                    elementType.toString().toLowerCase(),
                                    currentType.toString().toLowerCase()
                            )
                    );
                    return null;
                }
            }

            arrayLiteralNode.setType(elementType);
            return elementType;
        }

        // Check accessing of an array
        if (node instanceof ArrayAccessNode arrayAccessNode)
        {
            String szName = arrayAccessNode.getIdentifier().getName();
            TypeNode arrayType = getTypeNodeFromStack(szName);

            if (arrayType == null)
            {
                createError(
                        node,
                        String.format(
                                "No array variable exists with the name '%s'",
                                szName
                        )
                );
                return null;
            }

            // Check if variable is array or not
            if (arrayType.getDimensions() == null || arrayType.getDimensions().isEmpty())
            {
                createError(
                        node,
                        String.format(
                                "'%s' is not an array",
                                szName
                        )
                );
                return null;
            }

            // Check indices count matches dimensions
            if (arrayAccessNode.getIndices().size() > arrayType.getDimensions().size())
            {
                createError(
                        node,
                        String.format(
                                "Incorrect number of array dimensions: expected %d, got %d",
                                arrayType.getDimensions().size(),
                                arrayAccessNode.getIndices().size()
                        )
                );
                return null;
            }

            // All indices must be integers
            for (ASTNode index : arrayAccessNode.getIndices())
            {
                DataType indexType = getExpressionType(index);
                if (indexType != DataType.INT)
                {
                    createError(
                            index,
                            String.format(
                                    "Array indices must be integers, got '%s'",
                                    indexType != null ? indexType.toString().toLowerCase() : "?"
                            )
                    );
                    return null;
                }
            }

            return arrayType.getDataType();
        }

        return null;
    }

    // ----------| ERL.AST |----------------------------------------------
    private void analyseBlock(BlockNode node)
    {
        printDebug(new String[]{node.toString()});
        for (ASTNode statement : node.getStatements())
        {
            routeAnalysis(statement);
        }
    }

    // ----------| ERL.AST.Expressions |----------------------------------
    private void analyseInstanceSubroutineCall(InstanceSubroutineCallNode node)
    {
        printDebug(new String[]{node.toString()});
        getExpressionType(node);
    }

    private void analyseSubroutineCall(SubroutineCallNode node)
    {
        printDebug(new String[]{node.toString()});
        getExpressionType(node);
    }

    // ----------| ERL.AST.Statements |-----------------------------------
    private void analyseAssignment(AssignmentNode node)
    {
        printDebug(new String[]{node.toString()});

        if (node.getExpression() == null)
        {
            // Skip checking expression for an array size declaration
            node.setArrayAssignType(getTypeNodeFromStack(node.getIdentifier().getName()).getDataType());
            return;
        }

        // Special case for files, as the type does not need to be declared for opening a file
        if (node.getExpression() instanceof SubroutineCallNode subroutineCallNode)
        {
            if (subroutineCallNode.getIdentifier().getName().equals("open"))
            {
                if (subroutineCallNode.getArgumentExpressions().size() != 1
                        || (subroutineCallNode.getArgumentExpressions().size() == 1
                        && getExpressionType(subroutineCallNode.getArgumentExpressions().get(0)) != DataType.STRING))
                {
                    createError(
                            node,
                            "The function 'open' requires one 'String' argument"
                    );
                    return;
                }

                addVariableToStack(
                        node.getIdentifier().getName(),
                        new TypeNode(
                                DataType.FILE,
                                null,
                                false,
                                false,
                                node.iLine
                        )
                );
                return;
            }
        }

        TypeNode typeNode = getTypeNodeFromStack(node.getIdentifier().getName());
        if (typeNode == null)
        {
            createError(
                    node,
                    String.format(
                            "No variable exists with the name '%s'",
                            node.getIdentifier().getName()
                    )
            );
            return;
        }

        if (node.getExpression() instanceof ArrayLiteralNode arrayLiteralNode)
        {
            if (arrayLiteralNode.getElements().isEmpty())
            {
                return;
            }
        }

        DataType expressionType = getExpressionType(node.getExpression());
        if (expressionType != typeNode.getDataType())
        {
            if (expressionType != null)
            {
                createError(
                        node,
                        String.format(
                                "Type mismatch: expected '%s', got '%s'",
                                typeNode.getDataType().toString().toLowerCase(),
                                expressionType.toString().toLowerCase()
                        )
                );
            }
        }
    }

    private void analyseCase(CaseNode node, DataType expressionType)
    {
        printDebug(new String[]{node.toString()});

        if (expressionType == null)
        {
            createError(
                    node,
                    "'case' must be part of a 'switch' statement"
            );
            return;
        }

        intoScope();
        if (node.getBlock().getStatements().isEmpty())
        {
            createWarning(node, "'case' block is empty, consider removing");
        }

        DataType caseType = getExpressionType(node.getExpression());
        if (caseType != expressionType)
        {
            if (caseType != null)
            {
                createError(
                        node,
                        String.format(
                                "Type mismatch in 'case' value: expected '%s', got '%s'",
                                expressionType.toString().toLowerCase(),
                                caseType.toString().toLowerCase()
                        )
                );
            }
        }

        analyseBlock(node.getBlock());
        outOfScope();
    }

    private void analyseDoUntil(DoUntilNode node)
    {
        printDebug(new String[]{node.toString()});

        intoScope();
        if (node.getBlock().getStatements().isEmpty())
        {
            createWarning(node, "'do-until' statement is empty, consider removing");
        }

        DataType condition = getExpressionType(node.getCondition());
        if (condition != DataType.BOOLEAN)
        {
            if (condition != null)
            {
                createError(
                        node.getCondition(),
                        String.format(
                                "Type mismatch in condition: expected 'boolean', got '%s'",
                                condition.toString().toLowerCase()
                        )
                );
            }
            outOfScope();
            return;
        }

        analyseBlock(node.getBlock());
        outOfScope();
    }

    private void analyseElseIf(ElseIfNode node)
    {
        printDebug(new String[]{node.toString()});

        if (node.getBlock().getStatements().isEmpty())
        {
            createWarning(node, "'elseif' statement is empty, consider removing");
        }

        intoScope();

        DataType condition = getExpressionType(node.getCondition());
        if (condition != DataType.BOOLEAN)
        {
            if (condition != null)
            {
                createError(
                        node,
                        String.format(
                                "Type mismatch in condition: expected 'boolean', got '%s'",
                                condition.toString().toLowerCase()
                        )
                );
            }
            outOfScope();
            return;
        }

        analyseBlock(node.getBlock());
        outOfScope();
    }

    private void analyseFor(ForNode node)
    {
        printDebug(new String[]{node.toString()});

        intoScope();

        if (node.getBlock().getStatements().isEmpty())
        {
            createWarning(node, "'for' loop is empty, consider removing");
        }

        if (getTypeNodeFromStack(node.getStart().getIdentifier().getName()) != null)
        {
            createError(
                    node,
                    String.format(
                            "A variable with the name '%s' is already defined in the scope",
                            node.getStart().getIdentifier().getName()
                    )
            );
            outOfScope();
            return;
        }

        DataType startType = getExpressionType(node.getStart().getExpression());
        if (startType != DataType.INT)
        {
            if (startType != null)
            {
                createError(
                        node,
                        String.format(
                                "Type mismatch in 'start' value: expected 'int', got '%s'",
                                startType.toString().toLowerCase()
                        )
                );
            }
            outOfScope();
            return;
        }

        addVariableToStack(
                node.getStart().getIdentifier().getName(),
                new TypeNode(
                        startType,
                        null,
                        false,
                        false,
                        node.iLine
                )
        );

        DataType endType = getExpressionType(node.getEndExpression());
        if (endType != DataType.INT)
        {
            if (endType != null)
            {
                createError(
                        node,
                        String.format(
                                "Type mismatch in 'end' value: expected 'int', got '%s'",
                                endType.toString().toLowerCase()
                        )
                );
            }
            outOfScope();
            return;
        }

        if (node.getStepExpression() != null)
        {
            DataType stepType = getExpressionType(node.getStepExpression());
            if (stepType != DataType.INT)
            {
                if (stepType != null)
                {
                    createError(
                            node,
                            String.format(
                                    "Type mismatch in 'step' value: expected 'int', got '%s'",
                                    stepType.toString().toLowerCase()
                            )
                    );
                }
                outOfScope();
                return;
            }
        }

        TypeNode nextType = getTypeNodeFromStack(node.getNextIdentifier().getName());
        if (nextType == null)
        {
            createError(
                    node.getNextIdentifier(),
                    String.format(
                            "No variable exists with the name '%s'",
                            node.getNextIdentifier().getName()
                    )
            );
            outOfScope();
            return;
        }
        else
        {
            if (nextType.getDataType() != DataType.INT)
            {
                createError(
                        node.getNextIdentifier(),
                        String.format(
                                "Type mismatch in 'next' variable: expected 'int', got '%s'",
                                nextType.getDataType().toString().toLowerCase()
                        )
                );
                outOfScope();
                return;
            }
        }

        analyseBlock(node.getBlock());
        outOfScope();
    }

    private void analyseIf(IfNode node)
    {
        printDebug(new String[]{node.toString()});

        if (node.getThenBlock().getStatements().isEmpty())
        {
            createWarning(node, "'if' statement is empty, consider removing");
        }

        intoScope();

        DataType condition = getExpressionType(node.getCondition());
        if (condition != DataType.BOOLEAN)
        {
            if (condition != null)
            {
                createError(
                        node,
                        String.format(
                                "Type mismatch in condition: expected 'boolean', got '%s'",
                                condition.toString().toLowerCase()
                        )
                );
            }
            outOfScope();
            return;
        }

        analyseBlock(node.getThenBlock());
        outOfScope();

        for (ElseIfNode elseIfNode : node.getElseIfNodes())
        {
            analyseElseIf(elseIfNode);
        }

        if (node.getElseBlock() != null)
        {
            if (node.getElseBlock().getStatements().isEmpty())
            {
                createWarning(node.getElseBlock(), "'else' statement is empty, consider removing");
            }
            intoScope();
            analyseBlock(node.getElseBlock());
            outOfScope();
        }
    }

    private void analyseSwitch(SwitchNode node)
    {
        printDebug(new String[]{node.toString()});

        if (node.getCases().isEmpty())
        {
            createWarning(node, "'switch' statement is empty, consider removing");
        }

        if (node.getCases().size() == 1)
        {
            createWarning(node, "'switch' statement only has 1 case, consider replacing with an 'if' statement");
        }

        DataType expressionType = getExpressionType(node.getExpression());

        for (CaseNode caseNode : node.getCases())
        {
            analyseCase(caseNode, expressionType);
        }
    }

    private void analyseVariableDeclaration(VariableDeclarationNode node)
    {
        if (getTypeNodeFromStack(node.getIdentifier().getName()) != null)
        {
            createError(
                    node,
                    String.format(
                            "A variable with the name '%s' is already defined in the scope",
                            node.getIdentifier().getName()
                    )
            );
            return;
        }

        if (node.getInitialExpression() != null)
        {
            if (node.getInitialExpression() instanceof ArrayLiteralNode arrayLiteralNode)
            {
                if (arrayLiteralNode.getElements().isEmpty())
                {
                    addVariableToStack(node.getIdentifier().getName(), node.getTypeNode());
                    return;
                }
            }

            DataType initType = getExpressionType(node.getInitialExpression());
            if (initType == null)
            {
                return;
            }

            if (initType != node.getTypeNode().getDataType())
            {
                createError(
                        node,
                        String.format(
                                "Type mismatch: expected '%s', got '%s'",
                                node.getTypeNode().getDataType().toString().toLowerCase(),
                                initType.toString().toLowerCase()
                        )
                );
                return;
            }
        }

        addVariableToStack(node.getIdentifier().getName(), node.getTypeNode());
    }

    private void analyseWhile(WhileNode node)
    {
        printDebug(new String[]{node.toString()});

        intoScope();
        if (node.getBlock().getStatements().isEmpty())
        {
            createWarning(node, "'while' loop is empty, consider removing");
        }

        DataType condition = getExpressionType(node.getCondition());
        if (condition != DataType.BOOLEAN)
        {
            if (condition != null)
            {
                createError(
                        node.getCondition(),
                        String.format(
                                "Type mismatch in condition: expected 'boolean', got '%s'",
                                condition.toString().toLowerCase()
                        )
                );
            }
            createError(
                    node.getCondition(),
                    "Could not analyse condition"
            );
            outOfScope();
            return;
        }

        analyseBlock(node.getBlock());
        outOfScope();
    }

    // ----------| ERL.AST.Subroutines |----------------------------------
    private boolean areTypesEquivalent(TypeNode node1, TypeNode node2)
    {
        printDebug(new String[]{node1.toString(), node2.toString()});
        return node1.getDataType() == node2.getDataType() && node1.getDimensions().size() == node2.getDimensions().size();
    }

    private void analyseSubroutine(SubroutineNode node)
    {
        printDebug(new String[]{node.toString()});

        intoScope();

        // Check if signature is already taken
        boolean bReserved = false;
        for (SubroutineNode subroutineNode : getReservedSignatures())
        {
            if (node.getIdentifier().getName().equals(subroutineNode.getIdentifier().getName()))
            {
                createError(node, "'" + subroutineNode.getIdentifier().getName() + "' is a reserved name, choose another identifier");
                bReserved = true;
                break;
            }
        }
        if (!bReserved)
        {
            for (SubroutineNode existingNode : getExistingSubroutines())
            {
                if (existingNode.getIdentifier().getName().equals(node.getIdentifier().getName()) && existingNode.getParameters().size() == node.getParameters().size())
                {
                    boolean bMatches = true;
                    for (int i = 0 ; i < existingNode.getParameters().size() ; i++)
                    {
                        ParameterNode existingParameter = existingNode.getParameters().get(i);
                        ParameterNode newParameter = node.getParameters().get(i);
                        if (!areTypesEquivalent(existingParameter.getTypeNode(), newParameter.getTypeNode()))
                        {
                            bMatches = false;
                        }
                    }

                    if (bMatches)
                    {
                        createError(node, "Subroutine is already defined, remove it or change the signature");
                    }
                }
            }
        }

        if (node.getBlock().getStatements().isEmpty())
        {
            createWarning(node, "Subroutine body is empty, consider removing");
        }

        for (ParameterNode parameterNode : node.getParameters())
        {
            addVariableToStack(parameterNode.getIdentifier().getName(), parameterNode.getTypeNode());
        }

        analyseBlock(node.getBlock());
        outOfScope();

        getExistingSubroutines().add(node);
    }

    private void analyseMain(MainNode node)
    {
        printDebug(new String[]{node.toString()});

        if (isMainExists())
        {
            createError(node, "'main' is already defined");
        }

        if (node.getBlock().getStatements().isEmpty())
        {
            createWarning(node, "'main' body is empty, consider removing");
        }
        analyseBlock(node.getBlock());
        setMainExists(true);
    }

    private ReturnPath analyzeReturnPaths(BlockNode block)
    {
        if (block != null)
        {
            printDebug(new String[]{block.toString()});
        }

        ReturnPath analyzer = new ReturnPath();
        if (block == null || block.getStatements() == null)
        {
            return analyzer;
        }

        // Create scope for local variables
        intoScope();

        boolean bHasUnconditionalReturn = false;

        for (ASTNode statement : block.getStatements())
        {
            if (bHasUnconditionalReturn)
            {
                createWarning(statement, "Unreachable code detected");
                continue;
            }

            if (statement instanceof VariableDeclarationNode varDecNode)
            {
                addVariableToStack(varDecNode.getIdentifier().getName(), varDecNode.getTypeNode());
            }
            else if (statement instanceof ReturnNode returnNode)
            {
                if (returnNode.getExpression() != null)
                {
                    DataType type = getExpressionType(returnNode.getExpression());
                    analyzer.addReturn(type);
                }
                bHasUnconditionalReturn = true;
            }
            else if (statement instanceof IfNode ifNode)
            {
                ReturnPath thenAnalyzer = analyzeReturnPaths(ifNode.getThenBlock());
                analyzer.merge(thenAnalyzer);

                boolean bAllBranchesReturn = thenAnalyzer.allPathsReturn();

                // Handle else-if branches
                for (ElseIfNode elseIfNode : ifNode.getElseIfNodes())
                {
                    ReturnPath elseIfAnalyzer = analyzeReturnPaths(elseIfNode.getBlock());
                    analyzer.merge(elseIfAnalyzer);
                    bAllBranchesReturn &= elseIfAnalyzer.allPathsReturn();
                }

                // Handle else branch
                if (ifNode.getElseBlock() != null)
                {
                    ReturnPath elseAnalyzer = analyzeReturnPaths(ifNode.getElseBlock());
                    analyzer.merge(elseAnalyzer);
                    bAllBranchesReturn &= elseAnalyzer.allPathsReturn();
                }
                else
                {
                    bAllBranchesReturn = false;
                }

                if (bAllBranchesReturn)
                {
                    bHasUnconditionalReturn = true;
                }
                analyzer.setAllPathsReturn(bAllBranchesReturn);
            }
            else if (statement instanceof SwitchNode switchNode)
            {
                boolean bAllCasesReturn = !switchNode.getCases().isEmpty();

                for (CaseNode caseNode : switchNode.getCases())
                {
                    ReturnPath caseAnalyzer = analyzeReturnPaths(caseNode.getBlock());
                    analyzer.merge(caseAnalyzer);
                    bAllCasesReturn &= caseAnalyzer.allPathsReturn();
                }

                if (bAllCasesReturn)
                {
                    bHasUnconditionalReturn = true;
                    analyzer.setAllPathsReturn(true);
                }
            }
            else if (statement instanceof WhileNode whileNode)
            {
                ReturnPath loopAnalyzer = analyzeReturnPaths(whileNode.getBlock());
                analyzer.merge(loopAnalyzer);
            }
            else if (statement instanceof DoUntilNode doUntilNode)
            {
                ReturnPath loopAnalyzer = analyzeReturnPaths(doUntilNode.getBlock());
                analyzer.merge(loopAnalyzer);
            }
            else if (statement instanceof ForNode forNode)
            {
                intoScope();
                if (forNode.getStart() != null)
                {
                    addVariableToStack(
                            forNode.getStart().getIdentifier().getName(),
                            new TypeNode(
                                    DataType.INT,
                                    null,
                                    false,
                                    false,
                                    forNode.iLine
                            )
                    );
                }
                ReturnPath loopAnalyzer = analyzeReturnPaths(forNode.getBlock());
                analyzer.merge(loopAnalyzer);
                outOfScope();
            }
        }

        analyzer.setAllPathsReturn(bHasUnconditionalReturn);
        outOfScope();
        return analyzer;
    }

    private DataType inferReturnType(SubroutineNode subroutine)
    {
        printDebug(new String[]{subroutine.toString()});

        if (!subroutine.isFunction())
        {
            return null;  // Procedures don't return values
        }

        String szFunctionName = subroutine.getIdentifier().getName();

        // Handle predefined subroutines
        switch (szFunctionName)
        {
            case "input", "str" ->
            {
                return DataType.STRING;
            }
            case "round", "float" ->
            {
                return DataType.FLOAT;
            }
            case "random", "int", "ASC" ->
            {
                return DataType.INT;
            }
            case "bool" ->
            {
                return DataType.BOOLEAN;
            }
            case "char", "CHR" ->
            {
                return DataType.CHAR;
            }
            case "newFile" ->
            {
                return DataType.FILE;
            }
        }

        // Return already inferred type if exists
        if (getFunctionReturnTypes().containsKey(szFunctionName))
        {
            return getFunctionReturnTypes().get(szFunctionName);
        }

        // Handle recursive functions
        if (getFunctionsBeingAnalyzed().contains(szFunctionName))
        {
            return getTemporaryReturnTypes().getOrDefault(szFunctionName, null);
        }

        getFunctionsBeingAnalyzed().add(szFunctionName);

        // Create scope for parameters
        intoScope();
        for (ParameterNode param : subroutine.getParameters())
        {
            addVariableToStack(param.getIdentifier().getName(), param.getTypeNode());
        }

        // Analyze return paths
        ReturnPath analyzer = analyzeReturnPaths(subroutine.getBlock());

        outOfScope();
        getFunctionsBeingAnalyzed().remove(szFunctionName);
        getTemporaryReturnTypes().remove(szFunctionName);

        if (!analyzer.hasReturned())
        {
            createError(subroutine, "Function must return a value through all branches");
            return null;
        }

        if (!analyzer.allPathsReturn())
        {
            createError(subroutine, "Not all branches return a value");
            return null;
        }

        Set<DataType> returnTypes = analyzer.getReturnTypes();
        if (returnTypes.isEmpty())
        {
            createError(subroutine, "Function must return a value through all branches");
            return null;
        }

        if (returnTypes.size() > 1)
        {
            createError(
                    subroutine,
                    String.format(
                            "Function has multiple return types: %s",
                            returnTypes.stream()
                                    .map(type -> type.toString().toLowerCase())
                                    .collect(Collectors.joining(", "))
                    )
            );
            return null;
        }

        DataType inferredType = returnTypes.iterator().next();
        getFunctionReturnTypes().put(szFunctionName, inferredType);
        return inferredType;
    }

    private SubroutineNode findMatchingSubroutine(SubroutineCallNode call)
    {
        printDebug(new String[]{call.toString()});

        List<SubroutineNode> allSubroutines = new ArrayList<>(Arrays.asList(getReservedSignatures()));
        allSubroutines.addAll(getExistingSubroutines());
        for (SubroutineNode existingSubroutine : allSubroutines)
        {
            if (call.getArgumentExpressions().size() == existingSubroutine.getParameters().size()
                    && call.getIdentifier().getName().equals(existingSubroutine.getIdentifier().getName()))
            {
                boolean bMatching = true;
                for (int i = 0 ; i < existingSubroutine.getParameters().size() ; i++)
                {
                    DataType argType = getExpressionType(call.getArgumentExpressions().get(i));
                    DataType paramType = existingSubroutine.getParameters().get(i).getTypeNode().getDataType();
                    if (argType != paramType)
                    {
                        bMatching = false;
                        break;
                    }
                }
                if (bMatching)
                {
                    return existingSubroutine;
                }
            }
        }
        return null;
    }
}
