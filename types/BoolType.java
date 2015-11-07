package types;

public class BoolType extends Type {
    
    public BoolType()
    {
        //throw new RuntimeException("implement operators");
    }
    
    @Override
    public String toString()
    {
        return "bool";
    }

    @Override
    public boolean equivalent(Type that)
    {
        if (that == null)
            return false;
        if (!(that instanceof BoolType))
            return false;
        
        return true;
    }
    
    public Type and(Type that) {
        if ( !(that instanceof BoolType) )
            return super.and(that);
        return new BoolType();
    }
    
    public Type or(Type that) {
        if ( !(that instanceof BoolType) )
            return super.or(that);
        return new BoolType();  
    }
    
    public Type not() {
        if ( !(this instanceof BoolType) )
            return super.not();
        return new BoolType();
    }
    
}    
