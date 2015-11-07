package crux;
import java.util.*;
import types.*;

public class SymbolTable {
    
    Map<String, Symbol> symbolMap;
    SymbolTable parent;
    boolean hasParent = false;
    int depth;
    
    public SymbolTable()
    {
        depth = 0;
        symbolMap = new LinkedHashMap<>();
    }
    
    public SymbolTable(SymbolTable parentTable) {
        depth = parentTable.depth + 1;
        symbolMap = new LinkedHashMap<>();
        hasParent = true;
        parent = parentTable;
    }
    
    public Symbol lookup(String name) throws SymbolNotFoundError
    {
        if ( symbolMap.containsKey(name))
            return symbolMap.get(name);
        else if ( hasParent )
            return parent.lookup(name);
        else
            throw new SymbolNotFoundError(name);   
    }
    
    public boolean isAlreadyDeclared(String name) {
        return symbolMap.containsKey( name );
    }
       
    public Symbol insert(String name) throws RedeclarationError
    {        
        Symbol newSymbol = new Symbol(name);
        
        if ( isAlreadyDeclared(name) )
            throw new RedeclarationError(newSymbol);
        else 
            symbolMap.put(name, newSymbol);
        
        return newSymbol;   //do i really need to return new symbol
    }
    
    @Override
    public String toString()
    {
        
        StringBuffer sb = new StringBuffer();
        if (hasParent) {
            sb.append(parent.toString());
        }
        
        String indent = new String();
        for (int i = 0; i < depth; i++) {
            indent += "  ";
        }
        
        for (Map.Entry<String, Symbol> s : symbolMap.entrySet()) {
            sb.append(indent + s.getValue().toString() + "\n");
        }
        return sb.toString();
    }
}

class SymbolNotFoundError extends Error
{
    private static final long serialVersionUID = 1L;
    private String name;
    
    SymbolNotFoundError(String name)
    {
        this.name = name;
    }
    
    public String name()
    {
        return name;
    }
}

class RedeclarationError extends Error
{
    private static final long serialVersionUID = 1L;

    public RedeclarationError(Symbol sym)
    {
        super("Symbol " + sym + " being redeclared.");
    }
}
