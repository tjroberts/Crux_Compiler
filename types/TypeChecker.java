package types;

import java.util.*;
import ast.*;

public class TypeChecker implements CommandVisitor {
    
    private HashMap<Command, Type> typeMap;
    private StringBuffer errorBuffer;
    
    //Variables I added
    private FunctionDefinition func;
    boolean currentFuncHasReturn = false;
    
    private void setCurrentFunction( FunctionDefinition func ) {
        this.func = func;
    }
    
    //Functions I added
    private boolean checkReturnType(Type checkType) {
        return ((FuncType)func.symbol().type()).returnType().equivalent(checkType);
    }
    
    private Type getCurrentFuncReturnType() {
        return ((FuncType)func.symbol().type()).returnType();
    }
    
    private List<crux.Symbol> getCurrentFuncArgs() {
        return func.arguments();
    }
    
    private String getCurrentFuncName() {
        return func.symbol().name();
    }
    
    

    /* Useful error strings:
     *
     * "Function " + func.name() + " has a void argument in position " + pos + "."
     * "Function " + func.name() + " has an error in argument in position " + pos + ": " + error.getMessage()
     *
     * "Function main has invalid signature."
     *
     * "Not all paths in function " + currentFunctionName + " have a return."
     *
     * "IfElseBranch requires bool condition not " + condType + "."
     * "WhileLoop requires bool condition not " + condType + "."
     *
     * "Function " + currentFunctionName + " returns " + currentReturnType + " not " + retType + "."
     *
     * "Variable " + varName + " has invalid type " + varType + "."
     * "Array " + arrayName + " has invalid base type " + baseType + "."
     */

    public TypeChecker()
    {
        typeMap = new HashMap<Command, Type>();
        errorBuffer = new StringBuffer();
    }

    private void reportError(int lineNum, int charPos, String message)
    {
        errorBuffer.append("TypeError(" + lineNum + "," + charPos + ")");
        errorBuffer.append("[" + message + "]" + "\n");
    }

    private void put(Command node, Type type)
    {
        if (type instanceof ErrorType) {
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType)type).getMessage());
        }
        typeMap.put(node, type);
    }
    
    public Type getType(Object node)
    {
        return typeMap.get(node);
    }
    
    public boolean check(Command ast)
    {
        ast.accept(this);
        return !hasError();
    }
    
    public boolean hasError()
    {
        return errorBuffer.length() != 0;
    }
    
    public String errorReport()
    {
        return errorBuffer.toString();
    }

    @Override
    public void visit(ExpressionList node) {
        for (Expression e : node) {
            e.accept(this);
        }
    }

    @Override
    public void visit(DeclarationList node) {
        for (Declaration d : node)
            d.accept(this);
        
    }

    @Override
    public void visit(StatementList node) {
        for (Statement s : node)
            s.accept(this);
    }

    @Override
    public void visit(AddressOf node) {
        
        put(node, new AddressType(node.symbol().type()));
    }

    @Override
    public void visit(LiteralBool node) {
        put(node, new BoolType());
    }

    @Override
    public void visit(LiteralFloat node) {
        put(node, new FloatType());
    }

    @Override
    public void visit(LiteralInt node) {
        put(node, new IntType());
    }

    @Override
    public void visit(VariableDeclaration node) {
        
        if ( node.symbol().type() instanceof VoidType || node.symbol().type() instanceof ErrorType )
            reportError(node.lineNumber(), node.charPosition(), "Variable " + node.symbol().name() + " has invalid type " + node.symbol().type() + ".");
        
        put(node, node.symbol().type());
    }

    @Override
    public void visit(ArrayDeclaration node) {
        
        ArrayType arrayDec = (ArrayType)node.symbol().type();
        
        if ( arrayDec.base() instanceof VoidType || arrayDec.base() instanceof ErrorType )
            reportError(node.lineNumber(), node.charPosition(), "Array " + node.symbol().name() + " has invalid base type " + arrayDec.base() + ".");
        
        put(node, ((ArrayType)node.symbol().type()));
    }

    @Override
    public void visit(FunctionDefinition node) {
                
        currentFuncHasReturn = false;
        setCurrentFunction(node);
        
        if ( getCurrentFuncName().equals("main") ) { 
            if ( !(getCurrentFuncReturnType() instanceof VoidType) )
                reportError(node.lineNumber(), node.charPosition(), "Function main has invalid signature.");
        }
        
        //check declared arguments for valid type and make sure not void
        int argLocation = 0;
        for ( crux.Symbol s : getCurrentFuncArgs() ) {
            if ( s.type() instanceof VoidType ) {
                reportError(node.lineNumber(), node.charPosition(), "Function " + getCurrentFuncName() 
                            + " has a void argument in position " + argLocation + ".");
            }
            else if ( s.type() instanceof ErrorType ) {
                reportError(node.lineNumber(), node.charPosition(), "Function " + getCurrentFuncName() 
                            + " has an error in argument in position " + argLocation + ": " + ((ErrorType)s.type()).getMessage());
            }
            argLocation++;
        }
        
        node.body().accept(this);
        
        //check if all paths have return, unless function is void type
        if ( !currentFuncHasReturn && !(getCurrentFuncReturnType() instanceof VoidType) ) {
            reportError(node.lineNumber(), node.charPosition(), "Not all paths in function " +  getCurrentFuncName() + " have a return.");
        }
        
        
    }

    @Override
    public void visit(Comparison node) {
        
        node.leftSide().accept(this);
        node.rightSide().accept(this);
        
        
        put(node, getType(node.leftSide()).compare(getType(node.rightSide())));
    }
    
    @Override
    public void visit(Addition node) {
        
        node.leftSide().accept(this);
        node.rightSide().accept(this);
        
        put(node, getType(node.leftSide()).add(getType(node.rightSide())));
    }
    
    @Override
    public void visit(Subtraction node) {
        
        node.leftSide().accept(this);
        node.rightSide().accept(this);
        
        put(node, getType(node.leftSide()).sub(getType(node.rightSide())));
    }
    
    @Override
    public void visit(Multiplication node) {
        
        node.leftSide().accept(this);
        node.rightSide().accept(this);
        
        put(node, getType(node.leftSide()).mul(getType(node.rightSide())));
    }
    
    @Override
    public void visit(Division node) {
        
        node.leftSide().accept(this);
        node.rightSide().accept(this);
        
        put(node, getType(node.leftSide()).div(getType(node.rightSide())));
    }
    
    @Override
    public void visit(LogicalAnd node) {
        
        node.leftSide().accept(this);
        node.rightSide().accept(this);
        
        put(node, getType(node.leftSide()).and(getType(node.rightSide())));
    }

    @Override
    public void visit(LogicalOr node) {
        
        node.leftSide().accept(this);
        node.rightSide().accept(this);
        
        put(node, getType(node.leftSide()).and(getType(node.rightSide())));
    }

    @Override
    public void visit(LogicalNot node) {
        
        node.expression().accept(this);
        
        put(node, getType(node.expression()).not());
    }
    
    @Override
    public void visit(Dereference node) {
                
        node.expression().accept(this);
        
        put(node, getType(node.expression()).deref());
        
    }

    @Override
    public void visit(Index node) {
        
        node.base().accept(this);
        node.amount().accept(this);
        
        put(node, getType(node.base()));
    }

    @Override
    public void visit(Assignment node) {
        
        node.destination().accept(this);
        node.source().accept(this);
        
        put(node,getType(node.destination()).assign(getType(node.source())));
    }

    @Override
    public void visit(Call node) {
        
        node.arguments().accept(this);       
        
        TypeList callList = new TypeList();
        for (Expression e : node.arguments())
            callList.append(getType(e));
            
        TypeList formalArgs = ((FuncType)node.function().type()).arguments();
        if ( !formalArgs.equivalent(callList) ) {
            reportError(node.lineNumber(), node.charPosition(), "Cannot call " + node.function().type() + " using " + callList + ".");
        }
        
        put(node, ((FuncType)node.function().type()).returnType() );
    }

    @Override
    public void visit(IfElseBranch node) {
        
        boolean originalFlag = currentFuncHasReturn;
        
        node.condition().accept(this);
        
        if ( !(getType(node.condition()) instanceof BoolType) )
            reportError(node.lineNumber(), node.charPosition(), "IfElseBranch requires bool condition not " + getType(node.condition()) + ".");
        

        currentFuncHasReturn = false;
        node.thenBlock().accept(this);
        boolean thenBlock = currentFuncHasReturn;
        
        currentFuncHasReturn = false;
        node.elseBlock().accept(this);
        boolean elseBlock = currentFuncHasReturn;
        
        currentFuncHasReturn = (thenBlock && elseBlock) || originalFlag;
    }
    
    //has to be address of array, but make sure the index returns another address of a base type....

    @Override
    public void visit(WhileLoop node) {
        
        node.condition().accept(this);
        
        if ( !(getType(node.condition()) instanceof BoolType) )
            reportError(node.lineNumber(), node.charPosition(), "WhileLoop requires bool condition not " + getType(node.condition()) + ".");
        
        
        node.body().accept(this);
        currentFuncHasReturn = false;
    }

    @Override
    public void visit(Return node) {
        
        currentFuncHasReturn = true;
        
        node.argument().accept(this);  
        
        if ( getType(node.argument()) != null ) {
            
            if ( !checkReturnType(getType(node.argument())) ) {
                reportError(node.lineNumber(), node.charPosition(), "Function " + getCurrentFuncName() + " returns " 
                            + getCurrentFuncReturnType() + " not " + getType(node.argument()) + ".");
            }
        }
        
    }

    @Override
    public void visit(ast.Error node) {
        put(node, new ErrorType(node.message()));
    }
}
