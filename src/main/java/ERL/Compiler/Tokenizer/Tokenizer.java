package ERL.Compiler.Tokenizer;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts OCR ERL source code into a list of tokens.
 *
 * @author Shivam Attari
 */
public class Tokenizer
{
    // Instance Variables
    private boolean bDebug;
    private String szSourceCode;
    private List<Token> tokens;

    // Constructor
    public Tokenizer(String szSourceCode, boolean bDebug)
    {
        setSourceCode(szSourceCode);
        setDebug(bDebug);
    }

    // Getters
    public boolean isDebug()
    {
        return bDebug;
    }

    public String getSourceCode()
    {
        return szSourceCode;
    }

    public List<Token> getTokens()
    {
        return tokens;
    }

    // Setters
    public void setDebug(boolean bDebug)
    {
        this.bDebug = bDebug;
    }

    public void setSourceCode(String szSourceCode)
    {
        this.szSourceCode = szSourceCode;
    }

    public void setTokens(List<Token> tokens)
    {
        this.tokens = tokens;
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

    /**
     * Converts the source code given to the {@code ERL.Tokenizer} into a list of tokens.
     * <p>
     * The process involves iterating through each line of {@code szSourceCode} and then iterating through each
     * character of the line, ignoring whitespace and comments, and then finding the index of the character where it no
     * longer provides a known token type.
     * If an unknown character is found, the longest token of an unknown token type is found.
     * At the end of each line, a token with the type of {@code NEWLINE} is added to the list.
     * </p>
     * <p>
     * After the tokenization process, the local {@code tokens} list is copied to the instance variable.
     * </p>
     */
    public void tokenize()
    {
        printDebug(new String[]{""});

        List<Token> tempTokens = new ArrayList<>();

        for (String szLine : getSourceCode().split("\n"))
        {
            int i = 0;
            while (i < szLine.length())
            {
                // Skip whitespace (spaces/tabs)
                while (i < szLine.length() && Character.isWhitespace(szLine.charAt(i)))
                {
                    i++;
                }
                if (i >= szLine.length())
                {
                    break;
                }

                // Ignore comments
                if ((i + 1) < szLine.length() && szLine.charAt(i) == '/' && szLine.charAt(i + 1) == '/')
                {
                    break;
                }

                // Find string literals
                if (szLine.charAt(i) == '"')
                {
                    int j = i + 1;
                    boolean bEscaped = false;
                    boolean bValid = false;

                    while (j < szLine.length())
                    {
                        if (bEscaped)
                        {
                            bEscaped = false;
                        }
                        else if (szLine.charAt(j) == '\\')
                        {
                            bEscaped = true;
                        }
                        else if (szLine.charAt(j) == '"')
                        {
                            bValid = true;
                            j++;
                            break;
                        }
                        j++;
                    }

                    if (bValid)
                    { // Add valid string literal
                        tempTokens.add(new Token(TokenType.STRING, szLine.substring(i, j)));
                        i = j;
                    }
                    else
                    { // Invalid string literal, treat as unknown
                        tempTokens.add(new Token(TokenType.UNKNOWN, szLine.substring(i, j)));
                        i++;
                    }

                    continue;
                }

                int j = i + 1;
                TokenType lastValidType = TokenType.UNKNOWN;
                int iLastValidJ = i;

                while (j <= szLine.length())
                {
                    String szPotential = szLine.substring(i, j);
                    TokenType type = getTokenType(szPotential);

                    if (type != TokenType.UNKNOWN)
                    {
                        lastValidType = type;
                        iLastValidJ = j;
                        j++;
                    }
                    else if (iLastValidJ > i)
                    { // Valid token ends
                        break;
                    }
                    else
                    {
                        j++;
                    }
                }

                if (iLastValidJ > i)
                { // Valid token found
                    String szValue = szLine.substring(i, iLastValidJ);
                    tempTokens.add(new Token(lastValidType, szValue));
                    i = iLastValidJ;
                }
                else
                { // Unknown token built
                    j = i + 1;

                    while (j < szLine.length())
                    {
                        if (Character.isWhitespace(szLine.charAt(j)))
                        {
                            break;
                        }

                        String szPotential = szLine.substring(i, j + 1);
                        if (getTokenType(szPotential) != TokenType.UNKNOWN)
                        {
                            break;
                        }

                        j++;
                    }

                    tempTokens.add(new Token(TokenType.UNKNOWN, szLine.substring(i, j)));
                    i = j;
                }
            }

            tempTokens.add(new Token(TokenType.NEWLINE, ""));
        }

        setTokens(tempTokens);
    }

    /**
     * Determines the type of token from the string representation of the OCR ERL code segment.
     *
     * @param szToken the token to find the type of
     * @return the matching {@code ERL.TokenType} constant
     */
    public TokenType getTokenType(String szToken)
    {
        printDebug(new String[]{szToken});

        if (szToken.matches("String|int|float|boolean|char")) return TokenType.TYPE;
        if (szToken.matches("array")) return TokenType.ARRAY;
        if (szToken.matches("^\"[^\"]*\"$")) return TokenType.STRING;
        if (szToken.matches("[0-9]+\\.[0-9]*")) return TokenType.FLOAT;
        if (szToken.matches("[0-9]+")) return TokenType.INT;
        if (szToken.matches("true|false")) return TokenType.BOOLEAN;
        if (szToken.matches("'.'")) return TokenType.CHAR;
        if (szToken.matches("const")) return TokenType.CONST;
        if (szToken.matches("global")) return TokenType.GLOBAL;
        if (szToken.matches("=")) return TokenType.ASSIGN;
        if (szToken.matches("\\+|-|\\*|/|\\^|MOD|DIV")) return TokenType.ARITHMETIC;
        if (szToken.matches("==|!=|<|<=|>|>=")) return TokenType.COMPARISON;
        if (szToken.matches("AND|OR|NOT")) return TokenType.LOGICAL;
        if (szToken.matches("[()\\[\\],]")) return TokenType.PARENTHESIS;
        if (szToken.matches("if")) return TokenType.IF;
        if (szToken.matches("then")) return TokenType.THEN;
        if (szToken.matches("else")) return TokenType.ELSE;
        if (szToken.matches("elseif")) return TokenType.ELSEIF;
        if (szToken.matches("endif")) return TokenType.ENDIF;
        if (szToken.matches("switch")) return TokenType.SWITCH;
        if (szToken.matches("case")) return TokenType.CASE;
        if (szToken.matches("default")) return TokenType.DEFAULT;
        if (szToken.matches("endswitch")) return TokenType.ENDSWITCH;
        if (szToken.matches(":")) return TokenType.COLON;
        if (szToken.matches("while")) return TokenType.WHILE;
        if (szToken.matches("endwhile")) return TokenType.ENDWHILE;
        if (szToken.matches("do")) return TokenType.DO;
        if (szToken.matches("until")) return TokenType.UNTIL;
        if (szToken.matches("for")) return TokenType.FOR;
        if (szToken.matches("to")) return TokenType.TO;
        if (szToken.matches("step")) return TokenType.STEP;
        if (szToken.matches("next")) return TokenType.NEXT;
        if (szToken.matches("procedure")) return TokenType.PROCEDURE;
        if (szToken.matches("endprocedure")) return TokenType.ENDPROCEDURE;
        if (szToken.matches("function")) return TokenType.FUNCTION;
        if (szToken.matches("return")) return TokenType.RETURN;
        if (szToken.matches("endfunction")) return TokenType.ENDFUNCTION;
        if (szToken.matches("\\.")) return TokenType.DOT;
        if (szToken.matches("main")) return TokenType.MAIN;
        if (szToken.matches("endmain")) return TokenType.ENDMAIN;
        if (szToken.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) return TokenType.IDENTIFIER;
        return TokenType.UNKNOWN;
    }

    /**
     * Generates a list of {@code ERL.Issues.Error} objects for each unknown token occurrence in the {@code tokens} list.
     * Each {@code ERL.Issues.Error} object is instantiated with the corresponding line number and an error description.
     *
     * @return a list of errors for each unknown token
     */
    public List<ERL.Issues.Error> generateErrors()
    {
        List<ERL.Issues.Error> errors = new ArrayList<>();
        int iLineCount = 1;

        for (Token token : getTokens())
        {
            if (token.getType() == TokenType.UNKNOWN)
            { // Generate errors for unknown tokens
                for (String szMessage : splitUnknownErrors(token.getValue()))
                {
                    errors.add(new ERL.Issues.Error(iLineCount, szMessage));
                }
            }
            else if (token.getType() == TokenType.NEWLINE)
            {
                iLineCount++;
            }
        }

        return errors;
    }

    private List<String> splitUnknownErrors(String szValue)
    {
        String[][] szErrorMappings = {
                {"?", "The ternary operator is not used in the ERL"},
                {"\"", "String literal not closed"},
                {"'", "Use \" for strings"},
                {"%", "Use MOD for modulo operations"},
                {"!", "Use NOT instead of '!' for logical operations"},
                {"||", "Use OR instead of '||' for logical operations"},
                {"&&", "Use AND instead of '&&' for logical operations"},
                {";", "Semicolons are not used in the ERL"},
                {"{", "Braces are not used in the ERL"},
                {"}", "Braces are not used in the ERL"},
        };

        String szRemainingValue = szValue;
        List<String> szMessages = new ArrayList<>();

        while (!szRemainingValue.isEmpty())
        {
            boolean bMapped = false;
            for (String[] szMapping : szErrorMappings)
            {
                int iStart = szRemainingValue.indexOf(szMapping[0]);
                if (iStart != -1)
                {
                    if (!szMessages.contains(szMapping[1]))
                    {
                        szMessages.add(szMapping[1]);
                    }
                    szRemainingValue = szRemainingValue.substring(0, iStart)
                            + szRemainingValue.substring(iStart + szMapping[0].length());
                    bMapped = true;
                }
            }

            if (!bMapped)
            {
                szMessages.add(String.format("Invalid: '%s'", szRemainingValue));
                break;
            }
        }

        return szMessages;
    }

    /**
     * Prints the index, token type and token value for each token in the {@code tokens} list.
     */
    public void printTokens()
    {
        for (int i = 0 ; i < getTokens().size() ; i++)
        {
            Token token = getTokens().get(i);
            //System.out.printf("%3d| %-16s %s%n", i, token.getType().toString(), token.getValue());
            System.out.printf("%-16s %s%n", token.getType().toString(), token.getValue());
        }
    }
}
