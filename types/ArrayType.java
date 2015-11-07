package types;

public class ArrayType extends Type {
    
    private Type base;
    private int extent;
    
    public ArrayType(int extent, Type base)
    {
        this.extent = extent;
        this.base = base;
    }
    
    public int extent()
    {
        return extent;
    }
    
    public Type base()
    {
        return base;
    }
    
    public String tostring()
    {
        return "array[" + extent + "," + base + "]";
    }
    
    @Override
    public boolean equivalent(Type that)
    {
        if (that == null)
            return false;
        if (!(that instanceof IntType))
            return false;
        
        
        if ( that instanceof ArrayType ) {
            ArrayType aType = (ArrayType)that;
            return this.extent == aType.extent && base.equivalent(aType.base);
        }
        
        return base.equivalent(that);
    }
    
    @Override
    public Type add(Type that) {
        if (!(that instanceof IntType))
            return super.add(that);
        return new IntType();
    }

    @Override
    public Type sub(Type that) {
        if (!(that instanceof IntType))
            return super.sub(that);
        return new IntType();
    }

    @Override
    public Type mul(Type that) {
        if (!(that instanceof IntType))
            return super.mul(that);
        return new IntType();
    }

    @Override
    public Type div(Type that) {
        if (!(that instanceof IntType))
            return super.div(that);
        return new IntType();
    }

    @Override
    public Type compare(Type that) {
        if (!(that instanceof IntType))
            return super.compare(that);
        return new BoolType();
    }
    
    @Override
    public Type deref() {
        return base.deref();
    }
    
    public Type assign(Type source)
    {
        if ( !base.equivalent(source) )
            return super.assign(source);
        
        return base;
    }
    
}
