package mips;

import java.util.regex.Pattern;

import ast.*;
import types.*;
import java.io.*;

//TEMPORARY REGISTERS : $8 - $15
//      -Not preserved accross function calls

public class CodeGen implements ast.CommandVisitor {
    
    private StringBuffer errorBuffer = new StringBuffer();
    private TypeChecker tc;
    private Program program;
    private ActivationRecord currentFunction;

    public CodeGen(TypeChecker tc)
    {
        this.tc = tc;
        this.program = new Program();
    }
    
    public boolean hasError()
    {
        return errorBuffer.length() != 0;
    }
    
    public String errorReport()
    {
        return errorBuffer.toString();
    }

    private class CodeGenException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;
        public CodeGenException(String errorMessage) {
            super(errorMessage);
        }
    }
    
    public boolean generate(Command ast)
    {
        try {
            currentFunction = ActivationRecord.newGlobalFrame();
            ast.accept(this);
            
            return !hasError();
        } catch (CodeGenException e) {
            return false;
        }
    }
    
    public Program getProgram()
    {
        return program;
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
        
        currentFunction.getAddress(program, "$a0", node.symbol());
        
        //then push address to stack        
        program.pushInt("$a0");
    }

    @Override
    public void visit(LiteralBool node) {
          
        if ( node.value().name().equals("TRUE") )
            program.appendInstruction("li $a0, 1");
        else
            program.appendInstruction("li $a0, 0");
        
        program.pushInt("$a0");
    }

    @Override
    public void visit(LiteralFloat node) {
        program.appendInstruction("li.s $f2, " + node.value());
        program.pushFloat("$f2");
    }

    @Override
    public void visit(LiteralInt node) {
        
        program.appendInstruction("li $a0, " + node.value());
        program.pushInt("$a0");
    }

    @Override
    public void visit(VariableDeclaration node) {
        currentFunction.add(program, node);
    }

    @Override
    public void visit(ArrayDeclaration node) {
        //throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(FunctionDefinition node) {
        
        int funcLocation = 0;
        
        if ( node.function().name().equals("main") )
            funcLocation = program.appendInstruction("main:");
        else {
            funcLocation = program.appendInstruction("func." + node.function().name() + ":"); 
            currentFunction = new ActivationRecord(node, currentFunction);
        }
        
        node.body().accept(this);
        
        program.insertPrologue(funcLocation + 1, currentFunction.stackSize());
        
        if ( node.function().name().equals("main") )
            program.appendExitSequence();
        else
            program.appendEpilogue(currentFunction.stackSize());
        
        currentFunction = currentFunction.parent();
    }

    @Override
    public void visit(Addition node) {
        
        boolean isFloat = false;
        
        node.leftSide().accept(this);
        
        if ( tc.getType(node.leftSide()) instanceof FloatType ) {
            isFloat = true;
            program.popFloat("$f1");
        }
        else
            program.popInt("$a1");
        
        node.rightSide().accept(this);
        
        
        if ( tc.getType(node.rightSide()) instanceof FloatType ) {
            isFloat = true;
            program.popFloat("$f2");
        }
        else
            program.popInt("$a2");
        
        
        if ( isFloat ) {
            program.appendInstruction("add.s $f3, $f1, $f2");
            program.pushFloat("$f3");
        }
        else {
            program.appendInstruction("addu $a3, $a1, $a2");
            program.pushInt("$a3");
        }
        
    }

    @Override
    public void visit(Subtraction node) {
        
        boolean isFloat = false;
        
        node.leftSide().accept(this);
        
        if ( tc.getType(node.leftSide()) instanceof FloatType ) 
            isFloat = true;
        
        node.rightSide().accept(this);
        
        
        if ( tc.getType(node.rightSide()) instanceof FloatType ) 
            isFloat = true;
        
        
        if ( isFloat ) {
            
            program.popFloat("$f2");
            program.popFloat("$f1");
            
            program.appendInstruction("sub.s $f3, $f1, $f2");
            program.pushFloat("$f3");
        }
        else {
            
            program.popInt("$a2");
            program.popInt("$a1");
            
            program.appendInstruction("subu $a3, $a1, $a2");
            program.pushInt("$a3");
        }
    }

    @Override
    public void visit(Multiplication node) {
        
        boolean isFloat = false;
        
        node.leftSide().accept(this);
        
        if ( tc.getType(node.leftSide()) instanceof FloatType ) 
            isFloat = true;
        
        node.rightSide().accept(this);
        
        
        if ( tc.getType(node.rightSide()) instanceof FloatType ) 
            isFloat = true;
        
        if ( isFloat ) {
            
            program.popFloat("$f2");
            program.popFloat("$f1");
            
            program.appendInstruction("mul.s $f3, $f1, $f2");
            program.pushFloat("$f3");
        }
        else {
            
            program.popInt("$a2");
            program.popInt("$a1");
            
            program.appendInstruction("mul $a3, $a1, $a2");
            program.pushInt("$a3");
        }
    }

    @Override
    public void visit(Division node) {
        
        boolean isFloat = false;
        
        node.leftSide().accept(this);
        
        if ( tc.getType(node.leftSide()) instanceof FloatType ) 
            isFloat = true;
        
        node.rightSide().accept(this);
        
        if ( tc.getType(node.rightSide()) instanceof FloatType ) 
            isFloat = true;
        
        
        if ( isFloat ) {
            
            program.popFloat("$f2");
            program.popFloat("$f1");
            
            program.appendInstruction("div.s $f3, $f1, $f2");
            program.pushFloat("$f3");
        }
        else {
            program.popInt("$f2");
            program.popInt("$f1");
            
            program.appendInstruction("div $a3, $a1, $a2");
            program.pushInt("$a3");
        }
    }

    @Override
    public void visit(LogicalAnd node) {
       // throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(LogicalOr node) {
        //throw new RuntimeException("Implement this");
    }
    
    @Override
    public void visit(LogicalNot node) {
        //throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Comparison node) {
        
        boolean isFloat = false;
        
        //left side of expression
        node.leftSide().accept(this);
        if ( tc.getType(node.leftSide()) instanceof FloatType )
            isFloat = true;
        
        //right side of expression
        node.rightSide().accept(this);
        if ( tc.getType(node.rightSide()) instanceof FloatType )
            isFloat = true;

        
        //do operation
        if ( isFloat ) {
            
            program.popFloat("$f1");
            program.popFloat("$f2");
            
            String notEqualLabel = program.newLabel();
            String endLabel = program.newLabel();
            
            
            if ( node.operation() == Comparison.Operation.EQ )
                program.appendInstruction("c.eq.s $f1, $f2");
            else if ( node.operation() == Comparison.Operation.LT )
                program.appendInstruction("c.lt.s $f1, $f2");
            
            //check float flag to see result of floating point operation
            program.appendInstruction("bclt " + notEqualLabel);
            
            //push zero for true
            program.appendInstruction("li $a3, 0");
            program.appendInstruction("j " + endLabel);
            
            //push 1 for false
            program.appendInstruction(notEqualLabel + ":");
            program.appendInstruction("li $a3 1");
            
            
            program.appendInstruction(endLabel + ":");
        }
        else {
            program.popInt("$a1");
            program.popInt("$a2");
            
            if ( node.operation() == Comparison.Operation.EQ ) {
                program.appendInstruction("seq $a3, $a1, $a2");
            }
            else if ( node.operation() == Comparison.Operation.LT )  {
                program.appendInstruction("slt $a3, $a2, $a1");
            }
        }
        
        //push result
        program.pushInt("$a3");
    }

    @Override
    public void visit(Dereference node) {
        
        node.expression().accept(this);
        
        //need to store value that was dereferenced
        
        program.popInt("$a0");
        program.appendInstruction("lw $a1, 0($a0)");
        program.pushInt("$a1");
        
    }

    @Override
    public void visit(Index node) {
       // throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Assignment node) {
        
        node.destination().accept(this);
        
        node.source().accept(this);
        //retrieve value and address from stack
        
        program.popInt("$a0"); //this is the value to store
        program.popInt("$a1"); //this is where your going to store at
        
        program.appendInstruction("sw $a0, ($a1)");
                
    }
    
    @Override
    public void visit(Call node) {
        
        //get arguments for function
        node.arguments().accept(this);
                        
        program.appendInstruction("jal func." + node.function().name());
        
        
        //pop args off stack, 4 bytes per arg
        program.appendInstruction("addi $sp, $sp, " + (node.arguments().size() * 4));
        
        //push return value to stack if not a void function call
        if ( !(((FuncType)node.function().type()).returnType() instanceof VoidType) ) {
            program.appendInstruction("subu $sp, $sp, 4");
            program.appendInstruction("sw $v0, 0($sp)");
        }
        
    }

    @Override
    public void visit(IfElseBranch node) {
        
        String thenLabel = program.newLabel();
        String elseLabel = program.newLabel();
        String endLabel = program.newLabel();
        
        node.condition().accept(this);
        
        //get condition result, then jump if need be
        program.popInt("$a0");
        program.appendInstruction("beqz $a0, " + elseLabel);
        //program.appendInstruction("j " + thenLabel);
        
        //if block
        program.appendInstruction(thenLabel + ":");
        node.thenBlock().accept(this);
        program.appendInstruction("j " + endLabel);
        
        //else block
        program.appendInstruction(elseLabel + ":");
        node.elseBlock().accept(this);

        //right after if else block
        program.appendInstruction(endLabel + ":");
    }

    @Override
    public void visit(WhileLoop node) {
        String beginLoop = program.newLabel();
        String endLoop = program.newLabel();
        
        node.condition().accept(this);        

        program.appendInstruction(beginLoop + ":");
        
        node.body().accept(this);
        program.popInt("$a1");
        
        program.appendInstruction("bnez $a1, " + endLoop);
        program.appendInstruction("j " + beginLoop);
        program.appendInstruction(endLoop + ":");
    }

    @Override
    public void visit(Return node) {
        
        node.argument().accept(this);
        
        //pop the argument value off the stack and place in return register
        program.popInt("$v0"); 
    }

    @Override
    public void visit(ast.Error node) {
        String message = "CodeGen cannot compile a " + node;
        errorBuffer.append(message);
        throw new CodeGenException(message);
    }
}
