package ERL.Compiler.Tokenizer;

/**
 * Can represent any type of valid token, or an unknown one.
 *
 * @author Shivam Attari
 */
public enum TokenType
{
    TYPE,
    IDENTIFIER,
    ARRAY,
    STRING, INT, FLOAT, BOOLEAN, CHAR,
    CONST,
    GLOBAL,
    ASSIGN,
    ARITHMETIC,
    COMPARISON,
    LOGICAL,
    PARENTHESIS,
    IF, THEN, ELSE, ELSEIF, ENDIF,
    SWITCH, CASE, DEFAULT, ENDSWITCH, COLON,
    WHILE, ENDWHILE,
    DO, UNTIL,
    FOR, TO, STEP, NEXT,
    PROCEDURE, ENDPROCEDURE,
    FUNCTION, RETURN, ENDFUNCTION,
    DOT,
    MAIN, ENDMAIN,
    NEWLINE,
    UNKNOWN
}