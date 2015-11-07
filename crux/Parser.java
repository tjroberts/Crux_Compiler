package crux;

import ast.Command;
import ast.Expression;
import ast.ExpressionList;
import ast.IfElseBranch;
import ast.Return;
import ast.Statement;
import ast.StatementList;
import ast.WhileLoop;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import types.*;
import ast.*;

public class Parser {
    public static String studentName = "TODO: Your Name";
    public static String studentID = "TODO: Your 8-digit id";
    public static String uciNetID = "TODO: uci-net id";
  
// Typing System ===================================
    
    private Type tryResolveType(String typeStr)
    {
        return Type.getBaseType(typeStr);
    }
    
    
//OLD CODE BELOW
// SymbolTable Management ==========================
    private SymbolTable symbolTable;
    
    private void initSymbolTable()
    {
        symbolTable = new SymbolTable();
        
        Symbol s1 = symbolTable.insert("readInt");
        s1.setType(new FuncType(new TypeList() , new IntType()));
        
        Symbol s2 = symbolTable.insert("readFloat");
        s2.setType(new FuncType(new TypeList(), new FloatType()));
        
        Symbol s3 = symbolTable.insert("printBool");
        TypeList t3 = new TypeList();
        t3.append(new BoolType());
        s3.setType(new FuncType(t3 ,new VoidType()));
        
        Symbol s4 = symbolTable.insert("printInt");
        TypeList t4 = new TypeList();
        t4.append(new IntType());
        s4.setType(new FuncType(t4, new VoidType()));
        
        Symbol s5 = symbolTable.insert("printFloat");
        TypeList t5 = new TypeList();
        t5.append(new FloatType());
        s5.setType(new FuncType(t5, new VoidType()));
        
        Symbol s6 = symbolTable.insert("println");
        s6.setType(new FuncType(new TypeList(), new VoidType()));
    }
    
    private void enterScope()
    {
         symbolTable = new SymbolTable(symbolTable); //add new entry
    }
    
    private void exitScope()
    {
        symbolTable = symbolTable.parent;
    }

    private Symbol tryResolveSymbol(Token ident)
    {
        assert(ident.is(Token.Kind.IDENTIFIER));
        String name = ident.lexeme();
        try {
            return symbolTable.lookup(name);
        } catch (SymbolNotFoundError e) {
            String message = reportResolveSymbolError(name, ident.lineNumber(), ident.charPosition());
            return new ErrorSymbol(message);
        }
    }

    private String reportResolveSymbolError(String name, int lineNum, int charPos)
    {
        String message = "ResolveSymbolError(" + lineNum + "," + charPos + ")[Could not find " + name + ".]";
        errorBuffer.append(message + "\n");
        errorBuffer.append(symbolTable.toString() + "\n");
        return message;
    }

    private Symbol tryDeclareSymbol(Token ident)
    {        
        assert(ident.is(Token.Kind.IDENTIFIER));
        String name = ident.lexeme();
        try {
            return symbolTable.insert(name);
        } catch (RedeclarationError re) {
            String message = reportDeclareSymbolError(name, ident.lineNumber(), ident.charPosition());
            return new ErrorSymbol(message);
        }
    }

    private String reportDeclareSymbolError(String name, int lineNum, int charPos)
    {
        String message = "DeclareSymbolError(" + lineNum + "," + charPos + ")[" + name + " already exists.]";
        errorBuffer.append(message + "\n");
        errorBuffer.append(symbolTable.toString() + "\n");
        return message;
    }

// Helper Methods ==========================================

    private Token expectRetrieve(Token.Kind kind)
    {
        Token tok = currentToken;
        if (accept(kind))
            return tok;
        String errorMessage = reportSyntaxError(kind);
        throw new QuitParseException(errorMessage);
        //return ErrorToken(errorMessage);
    }
        
    private Token expectRetrieve(NonTerminal nt)
    {
        Token tok = currentToken;
        if (accept(nt))
            return tok;
        String errorMessage = reportSyntaxError(nt);
        throw new QuitParseException(errorMessage);
        //return ErrorToken(errorMessage);
    }
              
// Parser ==========================================
    
    public ast.Command parse()
    {
        ast.Command tree = null;
        try {
            tree = program();
        } catch (QuitParseException q) {
            errorBuffer.append("SyntaxError(" + lineNumber() + "," + charPosition() + ")");
            errorBuffer.append("[Could not complete parsing.]");
        }
        
        return tree;
    }

    public ast.DeclarationList program()
    {
        ast.DeclarationList expr = declaration_list();
        expect(Token.Kind.EOF);
        
        return expr;
    }
    
    
    
    
    //CODE FROM PROJECT 2 BELOW

// Error Reporting ==========================================
    private StringBuffer errorBuffer = new StringBuffer();
    
    private String reportSyntaxError(NonTerminal nt)
    {
        System.out.println("ERROR ON TOKEN " + currentToken);
        String message = "SyntaxError(" + lineNumber() + "," + charPosition() + ")[Expected a token from " + nt.name() + " but got " + currentToken.kind() + ".]";
        errorBuffer.append(message + "\n");
        return message;
    }
     
    private String reportSyntaxError(Token.Kind kind)
    {
        System.out.println("ERROR ON TOKEN " + currentToken);
        String message = "SyntaxError(" + lineNumber() + "," + charPosition() + ")[Expected " + kind + " but got " + currentToken.kind() + ".]";
        errorBuffer.append(message + "\n");
        return message;
    }
    
    public String errorReport()
    {
        return errorBuffer.toString();
    }
    
    public boolean hasError()
    {
        return errorBuffer.length() != 0;
    }
    
    private class QuitParseException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;
        public QuitParseException(String errorMessage) {
            super(errorMessage);
        }
    }
    
    private int lineNumber()
    {
        return currentToken.lineNumber();
    }
    
    private int charPosition()
    {
        return currentToken.charPosition();
    }
          
// Parser ==========================================
    private Scanner scanner;
    private Token currentToken;
    
    public Parser(Scanner scanner)
    {
        this.scanner = scanner;
        currentToken = scanner.next();
        initSymbolTable();
    }
    
// Helper Methods ==========================================
    private boolean have(Token.Kind kind)
    {
        return currentToken.is(kind);
    }
    
    private boolean have(NonTerminal nt)
    {
        return nt.firstSet().contains(currentToken.kind());
    }

    private boolean accept(Token.Kind kind)
    {
        if (have(kind)) {
            currentToken = scanner.next();
            return true;
        }
        return false;
    }    
    
    private boolean accept(NonTerminal nt)
    {
        if (have(nt)) {
            currentToken = scanner.next();
            return true;
        }
        return false;
    }
   
    private boolean expect(Token.Kind kind)
    {
        if (accept(kind))
            return true;
        String errorMessage = reportSyntaxError(kind);
        throw new QuitParseException(errorMessage);
        //return false;
    }
        
    private boolean expect(NonTerminal nt)
    {
        if (accept(nt))
            return true;
        String errorMessage = reportSyntaxError(nt);
        throw new QuitParseException(errorMessage);
        //return false;
    }
   
// Grammar Rules =====================================================
    
    // literal := INTEGER | FLOAT | TRUE | FALSE .
    public ast.Expression literal()
    {
        Token litToken;
        
        if ( have(Token.Kind.INTEGER))
            litToken = expectRetrieve(Token.Kind.INTEGER);
        else if ( have(Token.Kind.FLOAT))
            litToken = expectRetrieve(Token.Kind.FLOAT);
        else if ( have(Token.Kind.TRUE))
            litToken = expectRetrieve(Token.Kind.TRUE);
        else if ( have(Token.Kind.FALSE))
            litToken = expectRetrieve(Token.Kind.FALSE);
        else
            throw new QuitParseException(reportSyntaxError(NonTerminal.LITERAL));
        
        return Command.newLiteral(litToken);
    }
    
    // designator := IDENTIFIER { "[" expression0 "]" } .
    public Expression designator()
    {        
        int startLine = lineNumber();
        int startChar = charPosition();
        
        Symbol name = null;
        
        Token t = expectRetrieve(Token.Kind.IDENTIFIER);
        name = tryResolveSymbol(t);

        Expression location = new ast.AddressOf(startLine, startChar, name);    
        while (accept(Token.Kind.OPEN_BRACKET)) {
            location = new ast.Index(lineNumber(), charPosition(), location, expression0());
            expect(Token.Kind.CLOSE_BRACKET);
        }
        
        return location;
    }

    public ast.DeclarationList declaration_list() {
        
        ast.DeclarationList decList = new ast.DeclarationList(lineNumber(),charPosition());
        
        while ( have(NonTerminal.DECLARATION) ) 
            decList.add(declaration());
        
        return decList;
    }
    
    public ast.Declaration declaration() {
        
        ast.Declaration dec = null;
        
        if ( have(NonTerminal.VARIABLE_DECLARATION) ) 
            dec = variable_declaration();
        else if ( have(NonTerminal.ARRAY_DECLARATION) )
            dec = array_declaration();
        else if ( have(NonTerminal.FUNCTION_DEFINITION) )
            dec = function_definition();
        else {
            throw new QuitParseException(reportSyntaxError(NonTerminal.DECLARATION));
        }
            
        return dec;
    }
    
    public ast.VariableDeclaration variable_declaration() {
        int startLine = lineNumber();
        int startChar = charPosition();
        Symbol varName = null;
        
        expect(Token.Kind.VAR);
        if ( have(Token.Kind.IDENTIFIER))
            varName = tryDeclareSymbol(currentToken); 
        
        expect(Token.Kind.IDENTIFIER);
        
        expect(Token.Kind.COLON);
        
        varName.setType(type());
        
        expect(Token.Kind.SEMICOLON);
        
        return new ast.VariableDeclaration(startLine, startChar, varName);
    }
    
    public  Type type() {
        return Type.getBaseType(expectRetrieve(Token.Kind.IDENTIFIER).lexeme());
    }
    
    public ast.ArrayDeclaration array_declaration() {
        Symbol arrayName = null;
        int startLine = lineNumber();
        int startChar = charPosition();
        
        expect(Token.Kind.ARRAY);
        
        if ( have(Token.Kind.IDENTIFIER))
            arrayName = tryDeclareSymbol(currentToken);
        expect(Token.Kind.IDENTIFIER);
        
        expect(Token.Kind.COLON);
        
        Type baseType = type();
        
        expect(Token.Kind.OPEN_BRACKET);
        
        Token extentToken = expectRetrieve(Token.Kind.INTEGER);
        
        arrayName.setType(new ArrayType(Integer.parseInt(extentToken.lexeme()), baseType));
        
        //TODO: does not work for multi-dimensional arrays
        expect(Token.Kind.CLOSE_BRACKET);
        while ( accept(Token.Kind.OPEN_BRACKET) ) {
            expect(Token.Kind.INTEGER);
            expect(Token.Kind.CLOSE_BRACKET);
        }
        expect(Token.Kind.SEMICOLON);
        
        return new ast.ArrayDeclaration(startLine, startChar, arrayName);
    }
    
    public ast.FunctionDefinition function_definition() {
        
        int startLine = lineNumber();
        int startChar = charPosition();
        Symbol funcSymbol = null;
        List<Symbol> symbolList = null;
        StatementList body;
        
        expect(Token.Kind.FUNC);
        
        if ( have(Token.Kind.IDENTIFIER))
            funcSymbol = tryDeclareSymbol(currentToken);
        
        expect(Token.Kind.IDENTIFIER);
        
        expect(Token.Kind.OPEN_PAREN);
        
        symbolList = parameter_list();
        TypeList typeList = new TypeList(); //create type list from parameter_list
        for ( Symbol s : symbolList )
            typeList.append(s.type());
        
        expect(Token.Kind.CLOSE_PAREN);
        expect(Token.Kind.COLON);
        
        Type returnType = type();
        funcSymbol.setType(new FuncType(typeList, returnType));
        
        body = statement_block();
        
        return new ast.FunctionDefinition(startLine, startChar, funcSymbol, symbolList, body);
    }
    
    public List<Symbol> parameter_list() {   
        
        enterScope(); 
        
        List<Symbol> symbolList = new ArrayList<>();
        
        if ( have(NonTerminal.PARAMETER) ) {
            symbolList.add(parameter());
            
            while ( accept(Token.Kind.COMMA)) 
                symbolList.add(parameter());
        }
        
        return symbolList;
    }
    
    public Symbol parameter() {
        
        Symbol paramSymbol = null;
        
        if ( have(Token.Kind.IDENTIFIER) )
            paramSymbol = tryDeclareSymbol(currentToken);
        expect(Token.Kind.IDENTIFIER);
        
        expect(Token.Kind.COLON);
        
        paramSymbol.setType(type());
        
        return paramSymbol;
        
    }
    
    public StatementList statement_block() {
        
        StatementList body;
        
        expect(Token.Kind.OPEN_BRACE);
        body = statement_list();
        expect(Token.Kind.CLOSE_BRACE);
        
        exitScope(); //EXIT SCOPE
        
        return body;
    }
    
    public StatementList statement_list() {
        
        StatementList body = new StatementList(lineNumber(), charPosition());
        
        while ( have(NonTerminal.STATEMENT) ) {
            body.add(statement());
        }
        
        return body;
    }
    
    public Statement statement() {
        
        Statement statement = null;
        
        if ( have(NonTerminal.VARIABLE_DECLARATION))
            statement = variable_declaration();
        else if ( have(NonTerminal.CALL_STATEMENT))
            statement = call_statement();
        else if ( have(NonTerminal.ASSIGNMENT_STATEMENT))
            statement = assignment_statement();
        else if ( have(NonTerminal.IF_STATEMENT))
            statement = if_statement();
        else if ( have(NonTerminal.WHILE_STATEMENT))
            statement = while_statement();
        else if ( have(NonTerminal.RETURN_STATEMENT))
            statement = return_statement();
        else
            throw new QuitParseException(reportSyntaxError(NonTerminal.STATEMENT));
     
        return statement;
    }
    
    public ast.Assignment assignment_statement() {
        
        int startLine = lineNumber();
        int startChar = charPosition();
        ast.Dereference deRef;
        
        Expression source;
        Expression destination;
        
        expect(Token.Kind.LET);
        destination = designator();
        expect(Token.Kind.ASSIGN);
        
        source = expression0();
        
        expect(Token.Kind.SEMICOLON);
        
        return new ast.Assignment(startLine, startChar, destination, source);
    }
    
    public ast.Call call_statement() {
        
        ast.Call callStatement;
        
        callStatement = call_expression();
        expect(Token.Kind.SEMICOLON);
        
        return callStatement;
    }
    
    public ast.Call call_expression() {
                
        int startLine = lineNumber();
        int startChar = charPosition();
        
        Symbol callSymbol = null;
        ExpressionList expressionList;
        
        expect(Token.Kind.CALL);
        
        if ( have(Token.Kind.IDENTIFIER)) {
            callSymbol = tryResolveSymbol(currentToken);
        }
        
        expect(Token.Kind.IDENTIFIER);
        
        //************************    do i need to add the type for the symbol HERE??     ****************************
        
        expect(Token.Kind.OPEN_PAREN);
        expressionList = expression_list();
        expect(Token.Kind.CLOSE_PAREN);
        
        //callSymbol.setType(new FuncType(expressionList, ));
        
        return new ast.Call(startLine, startChar, callSymbol, expressionList);
    }
    
    public ExpressionList expression_list() {
        ExpressionList expressionList = new ExpressionList(lineNumber(), charPosition());
        
        if (have(NonTerminal.EXPRESSION0)) {
            expressionList.add(expression0());
            
            while ( accept(Token.Kind.COMMA))
                expressionList.add(expression0());
        }  
      
        return expressionList;
    }
    
    public Expression expression0() {
                
        Expression currentExpression;
        Expression rightSide = null;
        Token op = null;
        
        currentExpression = expression1();
        
        if ( have(NonTerminal.OP0) ) {
            op = op0();
            rightSide = expression1();
            currentExpression = Command.newExpression(currentExpression, op, rightSide);
        }

        return currentExpression;
    }
    
    public Expression expression1() {
        
        Expression currentExpression;
        Expression rightSide;
        Token op = null;
        
        currentExpression = expression2();
        while ( have(NonTerminal.OP1)) {
            op = op1();
            rightSide = expression2();
            currentExpression = Command.newExpression(currentExpression, op, rightSide);
        }
        
        return currentExpression;
    }
    
    public Expression expression2() {
        
        Expression currentExpression;
        Expression rightSide;
        Token op = null;
        
        currentExpression = expression3();
        
        while ( have(NonTerminal.OP2)) {
            op = op2();
            rightSide = expression3();
            currentExpression = Command.newExpression(currentExpression, op, rightSide);
        }
        
        return currentExpression;
    }
    
    public Expression expression3() {
        Expression currentExpression = null;
        Expression rightSide;
        
        if ( have(Token.Kind.NOT)) {
            Token op = expectRetrieve(Token.Kind.NOT);
            rightSide = expression3();
            currentExpression = Command.newExpression(rightSide, op, rightSide);
        }
        else if ( have(Token.Kind.OPEN_PAREN)) {
            expect(Token.Kind.OPEN_PAREN);
            currentExpression = expression0();
            expect(Token.Kind.CLOSE_PAREN);
        }
        else if (have(NonTerminal.DESIGNATOR)) {
            currentExpression = new ast.Dereference(lineNumber(), charPosition(), designator());
        }
        else if ( have(NonTerminal.CALL_EXPRESSION))
            currentExpression = call_expression();
        else if ( have(NonTerminal.LITERAL))
            currentExpression = literal();
        else
            throw new QuitParseException(reportSyntaxError(NonTerminal.EXPRESSION3));
        
        
        return currentExpression;
    }
    
    public Token op1() {
        Token op;
        
        if ( have(Token.Kind.ADD))
            op = expectRetrieve(Token.Kind.ADD);
        else if ( have(Token.Kind.SUB))
            op = expectRetrieve(Token.Kind.SUB);
        else if ( have(Token.Kind.OR))
            op = expectRetrieve(Token.Kind.OR);
        else
            throw new QuitParseException(reportSyntaxError(NonTerminal.OP1));
        
        return op;
        
    }
    
    public Token op0() {
        Token op;
        
        if ( have(Token.Kind.GREATER_EQUAL))
            op = expectRetrieve(Token.Kind.GREATER_EQUAL);
        else if ( have(Token.Kind.LESSER_EQUAL))
            op = expectRetrieve(Token.Kind.LESSER_EQUAL);
        else if ( have(Token.Kind.NOT_EQUAL))
            op = expectRetrieve(Token.Kind.NOT_EQUAL);
        else if ( have(Token.Kind.EQUAL))
            op = expectRetrieve(Token.Kind.EQUAL);
        else if ( have(Token.Kind.GREATER_THAN))
            op = expectRetrieve(Token.Kind.GREATER_THAN);
        else if ( have(Token.Kind.LESS_THAN))
            op = expectRetrieve(Token.Kind.LESS_THAN);
        else
            throw new QuitParseException(reportSyntaxError(NonTerminal.OP0));
        
        return op;
        
    }
    
    public Token op2() {
        Token op;
        
        if ( have(Token.Kind.MUL))
            op = expectRetrieve(Token.Kind.MUL);
        else if ( have(Token.Kind.DIV))
            op = expectRetrieve(Token.Kind.DIV);
        else if ( have(Token.Kind.AND))
            op = expectRetrieve(Token.Kind.AND);
        else
            throw new QuitParseException(reportSyntaxError(NonTerminal.OP2));
        
        return op;
    }
    
    public  IfElseBranch if_statement() {
        
        int startLine = lineNumber();
        int startChar = charPosition();
        
        Expression cond;
        StatementList thenBlock;
        StatementList elseBlock = null;
        
        expect(Token.Kind.IF);
        cond = expression0();
        
        enterScope();  //ENTER SCOPE FOR IF STATEMENT
        
        thenBlock = statement_block();
        
        if ( accept(Token.Kind.ELSE) ) {
            enterScope();  //ENTER SCOPE FOR IF STATEMENT
            elseBlock = statement_block();        
            return new IfElseBranch(startLine, startChar, cond, thenBlock, elseBlock);
        }
        else {
            return new IfElseBranch(startLine, startChar, cond, thenBlock, new StatementList(lineNumber(), charPosition()));
        }

    }
    
    public WhileLoop while_statement() {
        
        int startLine = lineNumber();
        int startChar = charPosition();
        
        Expression cond;
        StatementList body;
        
        expect(Token.Kind.WHILE);
        cond = expression0();
        
        enterScope();  //ENTER SCOPE FOR IF STATEMENT
        body = statement_block();
        
        return new WhileLoop(startLine, startChar, cond, body);
    }
    
    public Return return_statement() {
        
        int startLine = lineNumber();
        int startChar = charPosition();
        
        Expression arg;
        
        expect(Token.Kind.RETURN);
        arg = expression0();
        expect(Token.Kind.SEMICOLON);
        
        return new Return(startLine, startChar, arg);
    }
       
}
    
    
   
