package ERL.Compiler.Parser;

import ERL.Compiler.Parser.Nodes.*;
import ERL.Compiler.Parser.Nodes.Expressions.Arithmetic.ArithmeticOperationNode;
import ERL.Compiler.Parser.Nodes.Expressions.Arithmetic.ArithmeticOperator;
import ERL.Compiler.Parser.Nodes.Expressions.*;
import ERL.Compiler.Parser.Nodes.Expressions.Comparison.ComparisonOperationNode;
import ERL.Compiler.Parser.Nodes.Expressions.Comparison.ComparisonOperator;
import ERL.Compiler.Parser.Nodes.Expressions.Literals.*;
import ERL.Compiler.Parser.Nodes.Expressions.Logical.LogicalOperationNode;
import ERL.Compiler.Parser.Nodes.Expressions.Logical.LogicalOperator;
import ERL.Compiler.Parser.Nodes.Expressions.Unary.UnaryOperationNode;
import ERL.Compiler.Parser.Nodes.Expressions.Unary.UnaryOperator;
import ERL.Compiler.Parser.Nodes.Statements.*;
import ERL.Compiler.Parser.Nodes.Subroutines.MainNode;
import ERL.Compiler.Parser.Nodes.Subroutines.ParameterNode;
import ERL.Compiler.Parser.Nodes.Subroutines.SubroutineNode;
import ERL.Compiler.Tokenizer.Token;
import ERL.Compiler.Tokenizer.TokenType;
import ERL.Issues.Error;
import ERL.Issues.Issue;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses a list of tokens into an Abstract Syntax Tree (AST).
 *
 * @author Shivam Attari
 */
public class Parser
{
    // Instance Variables
    private boolean bDebug;

    private List<Token> tokens;
    private ProgramNode programNode; // Root node of AST
    private int iCurrentTokenIndex;
    private Issue issue;

    // Constructor
    public Parser(List<Token> tokens)
    {
        this(tokens, false);
    }

    public Parser(List<Token> tokens, boolean bDebug)
    {
        setDebug(bDebug);
        setTokens(tokens);
        setIndex(0);
    }

    // Getters
    private boolean isDebug()
    {
        return bDebug;
    }

    private List<Token> getTokens()
    {
        return tokens;
    }

    public ProgramNode getProgramNode()
    {
        return programNode;
    }

    private int getIndex()
    {
        return iCurrentTokenIndex;
    }

    public Issue getIssue()
    {
        return issue;
    }

    // Setters
    private void setDebug(boolean bDebug)
    {
        this.bDebug = bDebug;
    }

    private void setTokens(List<Token> tokens)
    {
        this.tokens = tokens;
    }

    private void setProgramNode(ProgramNode programNode)
    {
        this.programNode = programNode;
    }

    private void setIndex(int iCurrentTokenIndex)
    {
        this.iCurrentTokenIndex = iCurrentTokenIndex;
    }

    private void setIssue(Issue issue)
    {
        this.issue = issue;
    }

    // Methods

    // ----------| Helpers |----------------------------------------------
    // [8] StackWalker for getting method names: https://stackoverflow.com/a/52335318
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

            System.out.printf("%s::%s(%s), token(%s, %s)%n",
                    szClassName,
                    szCallerName,
                    szParameterList,
                    currentToken().getType().toString().toLowerCase(),
                    currentToken().getValue());
        }
    }

    /**
     * Gets the token at the current index.
     *
     * @return the current token.
     */
    private Token currentToken()
    {
        return getTokens().get(isEOF() ? getTokens().size() - 1 : getIndex());
    }

    /**
     * Increments to the next index.
     */
    private void nextIndex()
    {
        setIndex(getIndex() + 1);
    }

    /**
     * Increments to the next index.
     *
     * @return the previous token.
     */
    private Token advance()
    {
        nextIndex();
        return getTokens().get(getIndex() - 1);
    }

    /**
     * Expects a token of the specified type to appear at the current index of the list.
     * The calling method should create an error if it receives {@code null}.
     *
     * @param type the type to compare to the current token.
     * @return the current token if the types match or {@code null} if not.
     */
    private Token advanceIf(TokenType type)
    {
        if (checkToken(type))
        {
            return advance();
        }
        return null;
    }

    /**
     * Expects a token of the specified type and value to appear at the current index of the list.
     * The calling method should create an error if it receives {@code null}.
     *
     * @param type    the type to compare to the current token.
     * @param szValue the value to compare to the current token.
     * @return the current token if the types match or {@code null} if not.
     */
    private Token advanceIf(TokenType type, String szValue)
    {
        if (checkToken(type, szValue))
        {
            return advance();
        }
        return null;
    }

    /**
     * Checks if the current index at the end of the token list.
     *
     * @return {@code true} if the parser at the end of the tokens.
     */
    private boolean isEOF()
    {
        return getIndex() >= getTokens().size() - 1;
    }

    /**
     * Checks the current token's type against a specified type.
     *
     * @param type the type to match against.
     * @return {@code true} if the types match.
     */
    private boolean checkToken(TokenType type)
    {
        return currentToken().getType() == type;
    }

    /**
     * Checks the current token's type and value against a specific type and value.
     *
     * @param type    the type to match against.
     * @param szValue the value to match against.
     * @return {@code true} if the types and values match.
     */
    private boolean checkToken(TokenType type, String szValue)
    {
        return checkToken(type) && currentToken().getValue().equals(szValue);
    }

    /**
     * Checks if the current token ends the current code block.
     *
     * @param type the type of the block.
     * @return {@code true} if the block is ended.
     */
    private boolean isEOB(TokenType type)
    {
        return switch (type)
        {
            // Subroutines
            case MAIN -> checkToken(TokenType.ENDMAIN);
            case PROCEDURE -> checkToken(TokenType.ENDPROCEDURE);
            case FUNCTION -> checkToken(TokenType.ENDFUNCTION);
            // Selection
            case IF, ELSEIF ->
                    checkToken(TokenType.ELSE) || checkToken(TokenType.ELSEIF) || checkToken(TokenType.ENDIF);
            case ELSE -> checkToken(TokenType.ENDIF);
            case CASE -> checkToken(TokenType.CASE) || checkToken(TokenType.DEFAULT) || checkToken(TokenType.ENDSWITCH);
            case DEFAULT -> checkToken(TokenType.ENDSWITCH);
            // Iteration
            case WHILE -> checkToken(TokenType.ENDWHILE);
            case DO -> checkToken(TokenType.UNTIL);
            case FOR -> checkToken(TokenType.NEXT);
            // Other tokens
            default -> false;
        };
    }

    /**
     * Counts the newline tokens before the current token to find the current line number.
     *
     * @return the current line number.
     */
    private int getCurrentLine()
    {
        int iLine = 1;
        for (Token token : getTokens())
        {
            if (token == currentToken())
            {
                break;
            }
            if (token.getType() == TokenType.NEWLINE)
            {
                iLine++;
            }
        }
        return iLine;
    }

    /**
     * Creates an {@code Error} for a parsing issue, noting down a description of the issue
     * and the line on which the issue occurred.
     *
     * @param szDescription the description to provide to the user
     */
    private void createError(String szDescription)
    {
        printDebug(new String[]{szDescription});
        setIssue(new Error(getCurrentLine(), szDescription));
    }

    // ----------| General |---------------------------------------------

    /**
     * Tokens are converted into an AST.
     */
    public void parse()
    {
        printDebug(null);

        ProgramNode programNode = new ProgramNode();

        while (!isEOF() && getIssue() == null)
        {
            if (!checkToken(TokenType.NEWLINE))
            {
                ASTNode newNode = switch (currentToken().getType())
                {
                    case MAIN -> parseMain();
                    case FUNCTION, PROCEDURE -> parseSubroutine();
                    default -> parseStatement();
                };

                if (newNode != null)
                { // Add new node
                    programNode.addStatement(newNode);
                }
                else
                { // Terminate parsing due to error
                    break;
                }
            }
            else
            { // Skip newline tokens
                nextIndex();
            }
        }

        setProgramNode(programNode);
    }

    /**
     * When parsing a block, all statements are added to the {@code BlockNode} until the block is terminated.
     *
     * @param type the type of block.
     * @return a {@code BlockNode} containing all enclosed nodes.
     */
    private BlockNode parseBlock(TokenType type)
    {
        printDebug(new String[]{type.toString()});

        BlockNode blockNode = new BlockNode(getCurrentLine());

        while (!isEOF() && !isEOB(type) && getIssue() == null)
        {
            ASTNode newNode = parseStatement();
            nextIndex();

            if (newNode != null)
            {
                blockNode.addStatement(newNode);
            }
            if (getIssue() != null)
            {
                return null;
            }
        }

        return blockNode;
    }

    /**
     * Parses a variable or function's identifier.
     *
     * @return a {@code IdentifierNode} containing the item's name.
     */
    private IdentifierNode parseIdentifier()
    { // Error message handled by caller
        printDebug(null);

        Token identifier = advanceIf(TokenType.IDENTIFIER);
        return identifier == null ? null : new IdentifierNode(identifier.getValue(), getCurrentLine());
    }

    /**
     * Parses a data type.
     *
     * @return a {@code TypeNode} containing its data type.
     */
    private TypeNode parseType()
    {
        printDebug(null);

        Token token = advance();

        DataType dataType = switch (token.getValue())
        {
            case "String" -> DataType.STRING;
            case "int" -> DataType.INT;
            case "float" -> DataType.FLOAT;
            case "boolean" -> DataType.BOOLEAN;
            case "char" -> DataType.CHAR;
            default -> null;
        };

        if (dataType == null)
        {
            createError("Expected data type");
            return null;
        }

        return new TypeNode(dataType, new ArrayList<>(), false, false, getCurrentLine());
    }

    /**
     * Parses a list of parameters, as part of a subroutine's signature.
     *
     * @return a list of parameter nodes, each with a type and identifier
     */
    private List<ParameterNode> parseParameters()
    {
        printDebug(null);

        List<ParameterNode> parameters = new ArrayList<>();

        Token leftBracket = advanceIf(TokenType.PARENTHESIS, "(");
        if (leftBracket == null)
        {
            createError("Missing opening bracket");
            return null;
        }

        while (!checkToken(TokenType.PARENTHESIS, ")") && !isEOF())
        {
            TypeNode paramTypeNode = parseType();
            if (paramTypeNode == null)
            {
                createError("Missing parameter type");
                return null;
            }

            IdentifierNode identifierNode = parseIdentifier();
            if (identifierNode == null)
            {
                createError("Missing parameter identifier");
                return null;
            }

            parameters.add(new ParameterNode(identifierNode, paramTypeNode, getCurrentLine()));

            if (!checkToken(TokenType.PARENTHESIS, ")"))
            {
                Token comma = advanceIf(TokenType.PARENTHESIS, ",");
                if (comma == null)
                {
                    createError("Parameters must be separated with ','");
                    return null;
                }
            }
        }

        Token rightBracket = advanceIf(TokenType.PARENTHESIS, ")");
        if (rightBracket == null)
        {
            createError("Missing closing bracket");
            return null;
        }

        return parameters;
    }

    /**
     * Parses a main code block, where execution will begin.
     *
     * @return a {@code MainNode} containing its block.
     */
    private MainNode parseMain()
    {
        printDebug(null);

        int iLine = getCurrentLine();
        advance(); // Pass over 'main' token

        BlockNode block = parseBlock(TokenType.MAIN); // Parse block
        if (block == null)
        {
            return null;
        }

        Token endMain = advanceIf(TokenType.ENDMAIN); // Check that main was ended, advance

        // Create error if block is not ended
        if (endMain == null)
        {
            createError("Expected 'endmain'");
            return null;
        }

        return new MainNode(block, iLine);
    }

    /**
     * Parses a subroutine's signature (identifier and parameters) and its enclosed statements.
     *
     * @return a {@code SubroutineNode} containing its signature, type, and block.
     */
    private SubroutineNode parseSubroutine()
    {
        printDebug(null);

        int iLine = getCurrentLine();
        Token subroutineTypeToken = advance();
        boolean bIsFunction = subroutineTypeToken.getType() == TokenType.FUNCTION;

        // Get properties
        IdentifierNode identifierNode = parseIdentifier();
        if (identifierNode == null)
        {
            createError("Expected procedure identifier");
            return null;
        }

        List<ParameterNode> parameters = parseParameters();
        if (parameters == null)
        {
            // Error created by parseParameters
            return null;
        }

        List<String> szChecked = new ArrayList<>();
        for (ParameterNode parameterNode : parameters)
        {
            for (String szName : szChecked)
            {
                if (parameterNode.getIdentifier().getName().equals(szName))
                {
                    createError("Parameter identifiers must be unique");
                    return null;
                }
            }
            szChecked.add(parameterNode.getIdentifier().getName());
        }

        BlockNode block = parseBlock(subroutineTypeToken.getType());
        Token endToken = advanceIf(bIsFunction ? TokenType.ENDFUNCTION : TokenType.ENDPROCEDURE); // Check that procedure was ended, advance
        if (endToken == null && getIssue() == null)
        {
            createError(
                    String.format(
                            "Expected '%s'",
                            bIsFunction ? "endfunction" : "endprocedure"
                    )
            );
            return null;
        }

        return new SubroutineNode(identifierNode, parameters, block, bIsFunction, iLine);
    }

    // ----------| Statements |-------------------------------------------

    /**
     * Identifies the type of statement, then parses it into the relevant node.
     *
     * @return the statement node.
     */
    private ASTNode parseStatement()
    {
        printDebug(null);

        return switch (currentToken().getType())
        {
            case IF -> parseIfStatement();
            case SWITCH -> parseSwitchStatement();
            case WHILE -> parseWhileLoop();
            case DO -> parseDoUntilLoop();
            case FOR -> parseForLoop();
            case RETURN -> parseReturnStatement();
            case ARRAY -> parseArrayDeclaration(false);
            case CONST, GLOBAL, TYPE -> parseVariableDeclaration();
            case NEWLINE -> null;
            default ->
            {
                boolean bCreateError = true;

                String szDescription = "Unexpected '" + currentToken().getValue() + "', must be associated with " +
                        switch (currentToken().getType())
                        {
                            case THEN, ELSE, ELSEIF, ENDIF -> "an 'if' statement";
                            case CASE, DEFAULT, ENDSWITCH, COLON -> "a 'switch' statement";
                            case ENDWHILE -> "a 'while' loop";
                            case UNTIL -> "a 'do-until' loop";
                            case TO, STEP, NEXT -> "a 'for' loop";
                            case ENDPROCEDURE -> "a procedure";
                            case ENDFUNCTION -> "a function";
                            case ENDMAIN -> "a 'main' block";
                            default ->
                            {
                                bCreateError = false;
                                yield "";
                            }
                        };

                if (bCreateError)
                {
                    createError(szDescription);
                    yield null;
                }

                Token nextToken = getTokens().get(getIndex() + 1);
                if (nextToken.getType() == TokenType.ASSIGN || nextToken.getValue().equals("["))
                {
                    yield parseAssignment();
                }

                yield parseExpression();
            }
        };
    }

    /**
     * Parses an if-statement along with all else-if and else branches.
     *
     * @return an {@code IfNode} containing all conditions and branches.
     */
    private IfNode parseIfStatement()
    {
        printDebug(null);

        int iLine = getCurrentLine();
        advance(); // Pass over 'if' token

        // Get condition for if-statement
        ASTNode condition = parseExpression();
        if (condition == null)
        {
            createError("'if' must be followed by a condition");
            return null;
        }

        // Condition must be followed by 'then'
        Token then = advanceIf(TokenType.THEN);
        if (then == null)
        {
            createError("Condition must be followed by 'then'");
            return null;
        }

        BlockNode thenBlock = parseBlock(TokenType.IF);

        List<ElseIfNode> elseIfNodes = new ArrayList<>();
        BlockNode elseBlock = null;

        while (!checkToken(TokenType.ENDIF) && !isEOF())
        {
            switch (currentToken().getType())
            {
                case ELSEIF ->
                {
                    if (elseBlock != null)
                    {
                        createError("'elseif' blocks must be placed before an 'else' block");
                        return null;
                    }

                    int iElseifLine = getCurrentLine();
                    advance(); // Pass over 'elseif' token

                    // Get condition for elseif-statement
                    ASTNode elseIfCondition = parseExpression();
                    if (elseIfCondition == null)
                    {
                        createError("'elseif' must be followed by a condition");
                        return null;
                    }

                    // Condition must be followed by 'then'
                    then = advanceIf(TokenType.THEN);
                    if (then == null)
                    {
                        createError("Condition must be followed by 'then'");
                        return null;
                    }

                    // Get elseif block
                    BlockNode elseIfBlock = parseBlock(TokenType.ELSEIF);

                    elseIfNodes.add(new ElseIfNode(elseIfCondition, elseIfBlock, iElseifLine));
                }
                case ELSE ->
                {
                    if (elseBlock != null)
                    {
                        createError("'if' statements can only have one 'else' block");
                        return null;
                    }

                    advance(); // Pass over 'else' token

                    // Get else block
                    elseBlock = parseBlock(TokenType.ELSE);
                }
                default ->
                {
                    return null;
                }
            }
        }

        Token endif = advanceIf(TokenType.ENDIF);
        if (endif == null)
        {
            createError("Expected 'endif'");
            return null;
        }

        return new IfNode(condition, thenBlock, elseIfNodes, elseBlock, iLine);
    }

    /**
     * Parses a switch-statement with all cases and default branch.
     *
     * @return a {@code SwitchNode} containing all cases.
     */
    private SwitchNode parseSwitchStatement()
    {
        printDebug(null);

        int iLine = getCurrentLine();
        advance(); // Pass over 'switch' token

        // Get expression to match against
        ASTNode expression = parseExpression();
        if (expression == null)
        {
            createError("'switch' must be followed by an expression");
            return null;
        }

        // Expression must be followed by ':'
        Token colon = advanceIf(TokenType.COLON);
        if (colon == null)
        {
            createError("Expression must be followed by a colon");
            return null;
        }

        List<CaseNode> caseNodes = new ArrayList<>();
        BlockNode defaultBlock = null;

        while (!checkToken(TokenType.ENDSWITCH) && !isEOF())
        {
            switch (currentToken().getType())
            {
                case CASE ->
                {
                    int iCaseLine = getCurrentLine();
                    advance(); // Pass over 'case' token

                    // Expression must match to branch
                    ASTNode caseExpression = parseExpression();
                    if (caseExpression == null)
                    {
                        createError("'case' must be followed by an expression");
                        return null;
                    }

                    // Expression must be followed by ':'
                    colon = advanceIf(TokenType.COLON);
                    if (colon == null)
                    {
                        createError("Expression must be followed by a colon");
                        return null;
                    }

                    // Get case block
                    BlockNode caseBlock = parseBlock(TokenType.CASE);
                    caseNodes.add(new CaseNode(caseExpression, caseBlock, iCaseLine));
                }
                case DEFAULT ->
                {
                    if (defaultBlock != null)
                    {
                        createError("'switch' statements can only have one 'default' block");
                        return null;
                    }

                    advance(); // Pass over 'default' token

                    // Expression must be followed by ':'
                    colon = advanceIf(TokenType.COLON);
                    if (colon == null)
                    {
                        createError("'default' must be followed by a colon");
                        return null;
                    }

                    defaultBlock = parseBlock(TokenType.DEFAULT);
                }
                case NEWLINE -> advance(); // Skip new lines
                default ->
                {
                    createError("Expected 'case' or 'default'");
                    return null;
                }
            }
        }

        Token endSwitch = advanceIf(TokenType.ENDSWITCH);
        if (endSwitch == null)
        {
            createError("Expected 'endswitch'");
            return null;
        }

        return new SwitchNode(expression, caseNodes, defaultBlock, iLine);
    }

    /**
     * Parses a while-loop with its condition and enclosed statements.
     *
     * @return a {@code WhileNode} containing its condition and block.
     */
    private WhileNode parseWhileLoop()
    {
        printDebug(null);

        int iLine = getCurrentLine();
        advance(); // Pass over 'while' token

        ASTNode condition = parseExpression();
        if (condition == null)
        {
            createError("'while' must be followed by a condition");
            return null;
        }

        BlockNode block = parseBlock(TokenType.WHILE);

        Token endWhile = advanceIf(TokenType.ENDWHILE);
        if (endWhile == null)
        {
            createError("Expected 'endwhile'");
            return null;
        }

        return new WhileNode(condition, block, iLine);
    }

    /**
     * Parses a do-until-loop with its condition and enclosed statements.
     *
     * @return a {@code DoUntilNode} containing its condition and block.
     */
    private DoUntilNode parseDoUntilLoop()
    {
        printDebug(null);

        int iLine = getCurrentLine();
        advance(); // Pass over 'do' token

        BlockNode block = parseBlock(TokenType.DO);

        Token untilToken = advanceIf(TokenType.UNTIL);
        if (untilToken == null)
        {
            createError("Expected 'until'");
            return null;
        }

        ASTNode condition = parseExpression();
        if (condition == null)
        {
            createError("'until' must be followed by a condition");
            return null;
        }

        return new DoUntilNode(condition, block, iLine);
    }

    /**
     * Parses a for-loop with its iterator details and enclosed statements.
     *
     * @return a {@code ForNode} containing its iterator details and block.
     */
    private ForNode parseForLoop()
    {
        printDebug(null);

        int iLine = getCurrentLine();
        advance(); // Pass over 'for' token

        IdentifierNode identifier = parseIdentifier();

        Token assign = advanceIf(TokenType.ASSIGN);
        if (assign == null)
        {
            createError("Expected assignment '=' operator");
            return null;
        }

        ASTNode startExpression = parseExpression();
        if (startExpression == null)
        {
            createError("Iterator must have an initial value");
            return null;
        }

        AssignmentNode startAssign = new AssignmentNode(identifier, null, startExpression, iLine);

        Token to = advanceIf(TokenType.TO);
        if (to == null)
        {
            createError("Expected 'to'");
            return null;
        }

        ASTNode endExpression = parseExpression();
        if (endExpression == null)
        {
            createError("Iterator must have an end value");
            return null;
        }

        ASTNode stepExpression = null;
        if (checkToken(TokenType.STEP))
        {
            advance();

            stepExpression = parseExpression();
            if (stepExpression == null) return null;
        }

        BlockNode block = parseBlock(TokenType.FOR);

        Token nextFor = advanceIf(TokenType.NEXT);
        if (nextFor == null)
        {
            createError("Expected 'next'");
            return null;
        }

        Token nextVar = advanceIf(TokenType.IDENTIFIER);
        if (nextVar == null)
        {
            createError("Expected variable identifier");
            return null;
        }
        IdentifierNode nextIdentifier = new IdentifierNode(nextVar.getValue(), getCurrentLine());

        return new ForNode(startAssign, endExpression, stepExpression, nextIdentifier, block, iLine);
    }

    /**
     * Parses a return-statement, used within a function to return an expression to the caller.
     *
     * @return a {@code ReturnNode} containing its expression.
     */
    private ReturnNode parseReturnStatement()
    {
        printDebug(null);

        int iLine = getCurrentLine();
        advance(); // Pass over 'return' token

        ASTNode expression = parseExpression();

        return expression == null ? null : new ReturnNode(expression, iLine);
    }

    /**
     * Parses the declaration of a variable, including any modifiers (constant or global)
     * and an initial expression, if given.
     *
     * @return a {@code VariableDeclarationNode} with modifiers and initial value.
     */
    private ASTNode parseVariableDeclaration()
    {
        printDebug(null);

        int iLine = getCurrentLine();
        boolean bIsConst = false;
        boolean bIsGlobal = false;

        // Order is: const global type
        switch (currentToken().getType())
        {
            case CONST ->
            {
                bIsConst = true;
                advance();
                if (checkToken(TokenType.GLOBAL))
                {
                    bIsGlobal = true;
                    advance();
                }
            }
            case GLOBAL ->
            {
                bIsGlobal = true;
                advance();

                if (currentToken().getType() == TokenType.ARRAY)
                {
                    return parseArrayDeclaration(true);
                }
            }
        }

        TypeNode typeNode = parseType();
        if (typeNode == null)
        {
            // Error created by parseType
            return null;
        }
        typeNode.setConst(bIsConst);
        typeNode.setGlobal(bIsGlobal);

        IdentifierNode identifierNode = parseIdentifier();
        if (identifierNode == null)
        {
            createError("Expected variable identifier");
            return null;
        }

        ASTNode initialExpression = null;
        if (checkToken(TokenType.ASSIGN))
        {
            advance();
            initialExpression = parseExpression();
            if (initialExpression == null)
            {
                if (getIssue() == null)
                {
                    createError("Expected initial value");
                }
                return null;
            }
            if (initialExpression instanceof ArrayLiteralNode)
            {
                createError("Expected 'array' at start of declaration");
            }
        }

        return new VariableDeclarationNode(identifierNode, typeNode, initialExpression, iLine);
    }

    /**
     * Parses the declaration, instantiation, and assignment of an array.
     * An array is: declared where only the number of dimensions is provided,
     * instantiated where the sizes of each dimension are provided,
     * or assigned where an array literal with all initial values are given.
     *
     * @return a {@code VariableDeclarationNode} with its initial value.
     */
    private ASTNode parseArrayDeclaration(boolean bIsGlobal)
    {
        printDebug(null);

        int iLine = getCurrentLine();
        advance(); // Pass over 'array' token

        TypeNode typeNode = parseType();
        if (typeNode == null)
        {
            return null;
        }
        typeNode.setGlobal(bIsGlobal);

        IdentifierNode identifierNode = parseIdentifier();
        if (identifierNode == null)
        {
            createError("Expected array identifier");
            return null;
        }

        return switch (currentToken().getType())
        {
            case PARENTHESIS ->
            {
                Token openingBracket = advanceIf(TokenType.PARENTHESIS, "[");
                if (openingBracket == null)
                {
                    createError("Expected '['");
                    yield null;
                }

                if (isEOF())
                {
                    createError("Expected ']' or size");
                    yield null;
                }

                if (currentToken().getValue().equals("]"))
                {
                    // No sizes should be provided, only dimensions
                    Token closingBracket = advanceIf(TokenType.PARENTHESIS, "]");
                    if (closingBracket == null)
                    {
                        createError("Expected ']'");
                        yield null;
                    }

                    int iDimensions = 1;

                    while (checkToken(TokenType.PARENTHESIS, "["))
                    {
                        advance(); // Pass over '[' token
                        closingBracket = advanceIf(TokenType.PARENTHESIS, "]");
                        if (closingBracket == null)
                        {
                            createError("Expected ']'");
                            yield null;
                        }
                        iDimensions++;
                    }

                    List<ASTNode> dimensions = new ArrayList<>();

                    for (int i = 0 ; i < iDimensions ; i++)
                    {
                        dimensions.add(new IntLiteralNode(0, getCurrentLine()));
                    }

                    typeNode.setDimensions(dimensions);

                    yield new VariableDeclarationNode(identifierNode, typeNode, null, iLine);
                }

                // Capacities must be stated for each dimension
                List<ASTNode> sizes = new ArrayList<>();

                while (!isEOF())
                {
                    ASTNode index = parseExpression();
                    if (index == null)
                    {
                        createError("Expected expression for size of dimension");
                        yield null;
                    }
                    sizes.add(index);

                    if (checkToken(TokenType.PARENTHESIS, "]"))
                    {
                        break;
                    }

                    Token comma = advanceIf(TokenType.PARENTHESIS, ",");
                    if (comma == null)
                    {
                        createError("Expected ',' between size expressions");
                        yield null;
                    }
                }

                Token closingBracket = advanceIf(TokenType.PARENTHESIS, "]");
                if (closingBracket == null)
                {
                    createError("Expected ']' after expressions");
                    yield null;
                }

                typeNode.setDimensions(sizes);

                yield new VariableDeclarationNode(identifierNode, typeNode, null, iLine);
            }
            case ASSIGN ->
            {
                advance(); // Pass over '=' token

                ArrayLiteralNode arrayLiteral = parseArrayLiteral();
                if (arrayLiteral == null)
                {
                    yield null;
                }

                // Infer dimensions of array literal
                List<ASTNode> dimensions = calculateArrayDimensions(arrayLiteral);
                if (dimensions == null)
                {
                    createError("Invalid array literal: dimensions are inconsistent");
                    yield null;
                }

                typeNode.setDimensions(dimensions);
                yield new VariableDeclarationNode(identifierNode, typeNode, arrayLiteral, iLine);
            }
            default ->
            {
                createError("Incomplete declaration: provide dimensions or initial value");
                yield null;
            }
        };
    }

    /**
     * Parses the assignment of an expression to a variable or array element.
     *
     * @return an {@code AssignmentNode} with the new expression and the corresponding variable's identifier.
     */
    private AssignmentNode parseAssignment()
    {
        printDebug(null);

        int iLine = getCurrentLine();

        IdentifierNode identifier = parseIdentifier();
        if (identifier == null)
        {
            createError("Expected variable identifier");
            return null;
        }

        List<ASTNode> indices = null;

        // Check if assigning to array
        if (checkToken(TokenType.PARENTHESIS, "["))
        {
            advance(); // Pass over '[' token
            indices = new ArrayList<>();

            if (!parseArrayIndices(indices))
            {
                return null;
            }
        }

        // Parse assignment operator
        Token assign = advanceIf(TokenType.ASSIGN);
        if (assign == null)
        {
            if (indices == null)
            {
                createError("Expected assignment '=' operator");
                return null;
            }
            return new AssignmentNode(identifier, indices, null, iLine);
        }

        // Parse right-hand expression or array literal
        ASTNode expression;
        if (checkToken(TokenType.PARENTHESIS, "["))
        {
            expression = parseArrayLiteral();
        }
        else
        {
            expression = parseExpression();
        }

        if (expression == null)
        {
            return null;
        }

        return new AssignmentNode(identifier, indices, expression, iLine);
    }

    /**
     * Parses a list of array indices, used for instantiation or referencing an element.
     *
     * @param indices the list of indices to add to.
     * @return {@code true} if no errors occurred, or {@code false} if an error occurred.
     */
    private boolean parseArrayIndices(List<ASTNode> indices)
    {
        while (!isEOF())
        {
            ASTNode index = parseExpression();
            if (index == null)
            {
                createError("Expected expression for array index");
                return false;
            }
            indices.add(index);

            if (checkToken(TokenType.PARENTHESIS, "]"))
            {
                break;
            }

            Token comma = advanceIf(TokenType.PARENTHESIS, ",");
            if (comma == null)
            {
                createError("Expected ',' between indices");
                return false;
            }
        }

        Token closingBracket = advanceIf(TokenType.PARENTHESIS, "]");
        if (closingBracket == null)
        {
            createError("Expected ']' after expressions");
            return false;
        }

        return true;
    }

    // ----------| Expressions |------------------------------------------

    /**
     * Parses an expression up the order of operations.
     * The order of operations used by the algorithm is (highest to lowest precedence):
     * Brackets, Exponents, Unary, Multiply/Divide, Add/Subtract, Comparisons, AND, OR.
     *
     * @return an expression node.
     */
    private ASTNode parseExpression()
    {
        printDebug(null);
        return parseOR();
    }

    /**
     * Parses an expression, checking if it contains logical OR operations.
     *
     * @return an expression node.
     */
    private ASTNode parseOR()
    {
        printDebug(null);

        ASTNode leftOperand = parseAND();

        while (checkToken(TokenType.LOGICAL, "OR"))
        {
            advance(); // Pass over 'OR' operator

            ASTNode rightOperand = parseAND();
            leftOperand = new LogicalOperationNode(LogicalOperator.OR, leftOperand, rightOperand, getCurrentLine());
        }

        return leftOperand;
    }

    /**
     * Parses an expression, checking if it contains logical AND operations.
     *
     * @return an expression node.
     */
    private ASTNode parseAND()
    {
        printDebug(null);

        ASTNode leftOperand = parseComparison();

        while (checkToken(TokenType.LOGICAL, "AND"))
        {
            advance(); // Pass over 'AND' operator

            ASTNode rightOperand = parseComparison();
            leftOperand = new LogicalOperationNode(LogicalOperator.AND, leftOperand, rightOperand, getCurrentLine());
        }

        return leftOperand;
    }

    /**
     * Parses an expression, checking if it contains comparison operators.
     *
     * @return an expression node.
     */
    private ASTNode parseComparison()
    {
        printDebug(null);

        ASTNode leftOperand = parseAddSubtract();

        if (checkToken(TokenType.COMPARISON))
        {
            Token operatorToken = advance();
            ASTNode right = parseAddSubtract();

            ComparisonOperator operator = switch (operatorToken.getValue())
            {
                case "<" -> ComparisonOperator.LT;
                case "<=" -> ComparisonOperator.LTE;
                case ">" -> ComparisonOperator.GT;
                case ">=" -> ComparisonOperator.GTE;
                case "==" -> ComparisonOperator.EQ;
                case "!=" -> ComparisonOperator.NEQ;
                default -> null;
            };
            return new ComparisonOperationNode(operator, leftOperand, right, getCurrentLine());
        }

        return leftOperand;
    }

    /**
     * Parses an expression, checking if it contains addition or subtraction.
     *
     * @return an expression node.
     */
    private ASTNode parseAddSubtract()
    {
        printDebug(null);

        ASTNode leftOperand = parseMultiplyDivide();

        while (checkToken(TokenType.ARITHMETIC) && currentToken().getValue().matches("[+\\-]"))
        {
            Token operatorToken = advance();
            ASTNode rightOperand = parseMultiplyDivide();

            ArithmeticOperator operator = switch (operatorToken.getValue())
            {
                case "+" -> ArithmeticOperator.ADD;
                case "-" -> ArithmeticOperator.SUBTRACT;
                default -> null;
            };

            leftOperand = new ArithmeticOperationNode(operator, leftOperand, rightOperand, getCurrentLine());
        }

        return leftOperand;
    }

    /**
     * Parses an expression, checking if it contains multiplication or division.
     *
     * @return an expression node.
     */
    private ASTNode parseMultiplyDivide()
    {
        printDebug(null);

        ASTNode leftOperand = parseExponent();

        while (checkToken(TokenType.ARITHMETIC) && currentToken().getValue().matches("\\*|/|MOD|DIV"))
        {
            Token operatorToken = advance();
            ASTNode rightOperand = parseExponent();

            ArithmeticOperator operator = switch (operatorToken.getValue())
            {
                case "*" -> ArithmeticOperator.MULTIPLY;
                case "/" -> ArithmeticOperator.DIVIDE;
                case "MOD" -> ArithmeticOperator.MODULO;
                case "DIV" -> ArithmeticOperator.QUOTIENT;
                default -> null;
            };
            leftOperand = new ArithmeticOperationNode(operator, leftOperand, rightOperand, getCurrentLine());
        }

        return leftOperand;
    }

    /**
     * Parses an expression, checking if it contains exponents.
     *
     * @return an expression node.
     */
    private ASTNode parseExponent()
    {
        printDebug(null);

        ASTNode leftOperand = parseUnary();

        while (checkToken(TokenType.ARITHMETIC, "^"))
        {
            advance(); // Pass over '^' operator
            ASTNode rightOperand = parseUnary();

            leftOperand = new ArithmeticOperationNode(ArithmeticOperator.EXPONENT, leftOperand, rightOperand, getCurrentLine());
        }

        return leftOperand;
    }

    /**
     * Parses an expression, checking if it contains unary operations.
     *
     * @return an expression node.
     */
    private ASTNode parseUnary()
    {
        printDebug(null);

        if (checkToken(TokenType.ARITHMETIC) && currentToken().getValue().matches("[+\\-]"))
        {
            Token operatorToken = advance();
            ASTNode operand = parseUnary();

            UnaryOperator operator = switch (operatorToken.getValue())
            {
                case "+" -> UnaryOperator.PLUS;
                case "-" -> UnaryOperator.MINUS;
                default -> null;
            };

            return new UnaryOperationNode(operator, operand, getCurrentLine());
        }

        if (checkToken(TokenType.LOGICAL, "NOT"))
        {
            advance(); // Pass over 'NOT' operator
            ASTNode operand = parseUnary();

            return new UnaryOperationNode(UnaryOperator.NOT, operand, getCurrentLine());
        }

        return parseSubExpression();
    }

    /**
     * Parses base components of an expression (brackets and literals).
     *
     * @return an expression node.
     */
    private ASTNode parseSubExpression()
    {
        printDebug(null);

        ASTNode expression;

        if (checkToken(TokenType.PARENTHESIS, "("))
        {
            advance(); // Pass over opening bracket
            expression = parseExpression();

            Token closingBracket = advanceIf(TokenType.PARENTHESIS, ")");
            if (closingBracket == null)
            {
                if (getIssue() == null)
                {
                    createError("Expected closing bracket");
                }
                return null;
            }
        }
        else if (checkToken(TokenType.PARENTHESIS, "["))
        {
            expression = parseArrayLiteral();
        }
        else
        {
            expression = switch (currentToken().getType())
            {
                case IDENTIFIER, TYPE -> parseIdentifierExpression();
                case STRING ->
                {
                    ASTNode newNode = new StringLiteralNode(currentToken().getValue(), getCurrentLine());
                    advance();
                    yield newNode;
                }
                case INT ->
                {
                    ASTNode newNode = new IntLiteralNode(Integer.parseInt(currentToken().getValue()), getCurrentLine());
                    advance();
                    yield newNode;
                }
                case FLOAT ->
                {
                    ASTNode newNode = new FloatLiteralNode(Float.parseFloat(currentToken().getValue()), getCurrentLine());
                    advance();
                    yield newNode;
                }
                case BOOLEAN ->
                {
                    ASTNode newNode = new BooleanLiteralNode(Boolean.parseBoolean(currentToken().getValue()), getCurrentLine());
                    advance();
                    yield newNode;
                }
                case CHAR ->
                {
                    ASTNode newNode = new CharLiteralNode(currentToken().getValue().charAt(1), getCurrentLine());
                    advance();
                    yield newNode;
                }
                default ->
                {
                    if (getIssue() == null)
                    {
                        createError("Expected expression");
                    }
                    yield null;
                }
            };
        }

        return expression == null ? null : parseInstanceChain(expression);
    }

    /**
     * Handles the parsing of an expression that begins with an identifier
     * (subroutine calls and accessing array elements).
     *
     * @return an identifier expression node
     */
    private ASTNode parseIdentifierExpression()
    {
        printDebug(null);

        ASTNode expression = null;

        // Parse initial identifier, literal, or expression
        if (checkToken(TokenType.IDENTIFIER) || checkToken(TokenType.TYPE))
        {
            if (!isEOF())
            {
                Token nextToken = getTokens().get(getIndex() + 1);

                if (nextToken.getType() == TokenType.PARENTHESIS)
                {
                    if (nextToken.getValue().equals("("))
                    {
                        expression = parseSubroutineCall();
                    }
                    else if (nextToken.getValue().equals("["))
                    {
                        expression = parseArrayAccess();
                    }
                }

                if (expression == null)
                {
                    expression = parseIdentifier();
                }
            }
        }
        else
        {
            expression = parseSubExpression();
        }

        return parseInstanceChain(expression);
    }

    /**
     * Parses a chain of instance method calls and property accessing.
     *
     * @param initialExpression The initial expression to check for instance calls
     * @return An {@code ASTNode} of the fully parsed expression with all chained calls
     */
    private ASTNode parseInstanceChain(ASTNode initialExpression)
    {
        if (initialExpression == null)
        {
            return null;
        }

        ASTNode expression = initialExpression;

        // Check for chained dot operators
        while (!isEOF() && checkToken(TokenType.DOT))
        {
            advance(); // Pass over '.' token

            if (checkToken(TokenType.IDENTIFIER))
            {
                Token nextToken = getTokens().get(getIndex() + 1);

                if (nextToken.getType() == TokenType.PARENTHESIS && nextToken.getValue().equals("("))
                {
                    expression = parseInstanceSubroutineCall(expression);
                }
                else
                {
                    expression = parseInstanceProperty(expression);
                }

                if (expression == null)
                {
                    return null;
                }
            }
            else
            {
                createError("Expected identifier after dot operator");
                return null;
            }
        }

        return expression;
    }

    /**
     * Parses a call to a subroutine on a value/variable via the dot operator.
     *
     * @param instance The instance on which to call the subroutine
     * @return a {@code InstanceSubroutineCallNode} containing the instance and call
     */
    private InstanceSubroutineCallNode parseInstanceSubroutineCall(ASTNode instance)
    {
        printDebug(null);

        // Parse the subroutine call
        SubroutineCallNode subroutineCall = parseSubroutineCall();
        if (subroutineCall == null)
        {
            return null;
        }

        return new InstanceSubroutineCallNode(instance, subroutineCall, getCurrentLine());
    }

    /**
     * Parses accessing a property of a value/variable via the dot operator.
     *
     * @param instance The instance whose property is being accessed
     * @return a {@code InstancePropertyNode} containing the instance and property
     */
    private InstancePropertyNode parseInstanceProperty(ASTNode instance)
    {
        printDebug(null);

        // Parse the property identifier
        IdentifierNode property = parseIdentifier();
        if (property == null)
        {
            createError("Expected property identifier");
            return null;
        }

        return new InstancePropertyNode(instance, property, getCurrentLine());
    }

    /**
     * Parses a call to a subroutine (function or procedure),
     * with its identifier and arguments as expressions.
     *
     * @return a {@code SubroutineCallNode} including all argument expressions.
     */
    private SubroutineCallNode parseSubroutineCall()
    {
        printDebug(null);

        IdentifierNode identifier = parseIdentifier();
        if (identifier == null)
        {
            // Special case for parsing casting functions
            Token typeNameAsFunction = advanceIf(TokenType.TYPE);
            if (typeNameAsFunction != null)
            {
                identifier = new IdentifierNode(
                        typeNameAsFunction.getValue(),
                        getCurrentLine()
                );
            }
            else
            {
                return null;
            }
        }

        Token openingBracket = advanceIf(TokenType.PARENTHESIS, "(");
        if (openingBracket == null)
        {
            createError("Expected '(' at start of subroutine call");
            return null;
        }

        List<ASTNode> arguments = new ArrayList<>();

        if (!checkToken(TokenType.PARENTHESIS, ")"))
        {
            while (!isEOF())
            {
                ASTNode argument = parseExpression();
                if (argument == null)
                {
                    createError("Expected expression for subroutine argument");
                    return null;
                }
                arguments.add(argument);

                if (checkToken(TokenType.PARENTHESIS, ")"))
                {
                    break;
                }

                Token comma = advanceIf(TokenType.PARENTHESIS, ",");
                if (comma == null)
                {
                    createError("Expected ',' between subroutine arguments");
                    return null;
                }
            }
        }

        Token closingBracket = advanceIf(TokenType.PARENTHESIS, ")");
        if (closingBracket == null)
        {
            createError("Expected ')' at end of subroutine call");
            return null;
        }

        return new SubroutineCallNode(identifier, arguments, getCurrentLine());
    }

    /**
     * Parses accessing an array at a certain index,
     * including its identifier and indices as expressions.
     *
     * @return an {@code ArrayAccessNode} including all index expressions.
     */
    private ArrayAccessNode parseArrayAccess()
    {
        printDebug(null);

        IdentifierNode identifier = parseIdentifier();

        Token openingBracket = advanceIf(TokenType.PARENTHESIS, "[");
        if (openingBracket == null)
        {
            createError("Expected '[' before expressions");
            return null;
        }

        List<ASTNode> indices = new ArrayList<>();

        if (checkToken(TokenType.PARENTHESIS, "]"))
        {
            createError("Expected expression for array index");
            return null;
        }

        if (!parseArrayIndices(indices))
        {
            return null;
        }

        return new ArrayAccessNode(identifier, indices, getCurrentLine());
    }

    /**
     * NOTE: NOT INCLUDED IN FINAL SUBMITTED VERSION, ADDED AFTER DEADLINE
     * Advances over newline tokens to skip whitespace.
     */
    private void advanceOverNewlines()
    {
        while (!isEOF())
        {
            if (checkToken(TokenType.NEWLINE))
            {
                advance();
                continue;
            }
            return;
        }
    }

    /**
     * Parses an array literal, which is a list of elements to be inserted directly as an array.
     * It consists of expressions at different indices.
     *
     * @return an {@code ArrayLiteralNode} including all expressions at each index.
     */
    private ArrayLiteralNode parseArrayLiteral()
    {
        printDebug(null);

        int iLine = getCurrentLine();

        advanceOverNewlines();
        Token openingBracket = advanceIf(TokenType.PARENTHESIS, "[");
        if (openingBracket == null)
        {
            createError("Expected '[' at start of array literal");
            return null;
        }
        advanceOverNewlines();

        List<ASTNode> elements = new ArrayList<>();

        // Handle empty array
        if (checkToken(TokenType.PARENTHESIS, "]"))
        {
            advance(); // Pass over closing bracket
            return new ArrayLiteralNode(elements, getCurrentLine());
        }

        while (!isEOF())
        {
            advanceOverNewlines();
            // Check if nested array literal or expression
            if (checkToken(TokenType.PARENTHESIS, "["))
            {
                ArrayLiteralNode subArray = parseArrayLiteral();
                if (subArray == null)
                {
                    return null;
                }

                elements.add(subArray);
            }
            else
            {
                ASTNode element = parseExpression();
                if (element == null)
                {
                    return null;
                }

                elements.add(element);
            }
            advanceOverNewlines();

            if (checkToken(TokenType.PARENTHESIS, "]"))
            {
                break;
            }

            Token comma = advanceIf(TokenType.PARENTHESIS, ",");
            if (comma == null)
            {
                createError("Expected ',' between elements");
                return null;
            }
        }

        Token closingBracket = advanceIf(TokenType.PARENTHESIS, "]");
        if (closingBracket == null)
        {
            createError("Expected ']' at end of array literal");
            return null;
        }

        return new ArrayLiteralNode(elements, iLine);
    }

    /**
     * Calculates the dimensions of an array literal, including multidimensional arrays.
     * The array literal is also checked for consistency across dimensions.
     *
     * @param arrayLiteral the {@code ArrayLiteralNode} for which dimensions are to be calculated.
     * @return a list of nodes that are equivalent to the sizes of each dimension.
     */
    private List<ASTNode> calculateArrayDimensions(ArrayLiteralNode arrayLiteral)
    {
        List<ASTNode> dimensions = new ArrayList<>();

        // Get size of the first dimension
        dimensions.add(new IntLiteralNode(arrayLiteral.getElements().size(), getCurrentLine()));

        if (arrayLiteral.getElements().isEmpty())
        {
            return dimensions;
        }

        ASTNode firstElement = arrayLiteral.getElements().get(0);

        // Calculate dimensions of multidimensional arrays
        if (firstElement instanceof ArrayLiteralNode)
        {
            List<ASTNode> nestedDimensions = calculateArrayDimensions((ArrayLiteralNode) firstElement);

            // Check for consistent dimensions
            for (ASTNode element : arrayLiteral.getElements())
            {
                if (!(element instanceof ArrayLiteralNode)
                        || ((ArrayLiteralNode) element).getElements().size() != ((IntLiteralNode) nestedDimensions.get(0)).getValue())
                {
                    return null;
                }
            }

            dimensions.addAll(nestedDimensions);
        }

        return dimensions;
    }
}
