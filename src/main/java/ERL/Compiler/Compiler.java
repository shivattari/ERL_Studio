package ERL.Compiler;

import ERL.Compiler.Bytecode.BytecodeGenerator;
import ERL.Compiler.Parser.Parser;
import ERL.Compiler.Parser.PrintAST;
import ERL.Compiler.Semantics.SemanticAnalysis;
import ERL.Compiler.Tokenizer.Tokenizer;
import ERL.Issues.Error;
import ERL.Issues.Issue;

import java.util.ArrayList;
import java.util.List;

/**
 * Compiles code in the correct order, ensuring errors prevent further compilation.
 *
 * @author Shivam Attari
 */
public class Compiler
{
    private boolean bDebug = false;

    // Instance Variables
    private String szSourceCode;
    private String szBytecode;
    private List<Issue> issues;

    // Constructors
    public Compiler()
    {
        setSourceCode("");
        setBytecode("");
        setIssues(new ArrayList<>());
    }

    public Compiler(boolean bDebug)
    {
        this();
        this.bDebug = bDebug;
    }

    // Getters
    public String getSourceCode()
    {
        return szSourceCode;
    }

    public String getBytecode()
    {
        return szBytecode;
    }

    public List<Issue> getIssues()
    {
        return issues;
    }

    // Setters
    public void setSourceCode(String szSourceCode)
    {
        this.szSourceCode = szSourceCode;
    }

    public void setBytecode(String szBytecode)
    {
        this.szBytecode = szBytecode;
    }

    public void setIssues(List<Issue> issues)
    {
        this.issues = issues;
    }

    // Methods
    public void compile()
    {
        // Tokenize
        Tokenizer tokenizer = new Tokenizer(this.szSourceCode, bDebug);
        tokenizer.tokenize();
        getIssues().addAll(tokenizer.generateErrors());
        if (bDebug) tokenizer.printTokens();

        // Terminate compilation if there are any unknown tokens
        if (encounteredError()) return;

        Parser parser = new Parser(tokenizer.getTokens(), bDebug);
        parser.parse();

        if (parser.getIssue() != null)
        {
            getIssues().add(parser.getIssue());
        }

        if (bDebug)
        {
            System.out.println();
            PrintAST printAST = new PrintAST(parser.getProgramNode());
            printAST.printTree();
            System.out.println();
        }

        // Terminate compilation if parsing results in an error
        if (encounteredError()) return;

        SemanticAnalysis semanticAnalysis = new SemanticAnalysis(parser.getProgramNode(), bDebug);
        semanticAnalysis.analyseProgram();
        getIssues().addAll(semanticAnalysis.getIssues());

        // Terminate compilation if semantic analysis finds an error
        if (encounteredError()) return;

        // Generate Bytecode
        BytecodeGenerator tempGenerator = new BytecodeGenerator(parser.getProgramNode(), bDebug);
        tempGenerator.generate();
        setBytecode(tempGenerator.getBytecode());
        if (bDebug) System.out.println(getBytecode());
    }

    private boolean encounteredError()
    {
        for (Issue issue : getIssues())
        {
            if (issue instanceof Error) return true;
        }
        return false;
    }
}
