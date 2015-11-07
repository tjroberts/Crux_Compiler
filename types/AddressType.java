package types;

public class AddressType extends Type {
    
    private Type base;
    
    public AddressType(Type base)
    {
        this.base = base;
    }
    
    public Type base()
    {
        return base;
    }

    @Override
    public String toString()
    {
        return "Address(" + base + ")";
    }

    @Override
    public boolean equivalent(Type that) {
        if (that == null)
            return false;
        if (!(that instanceof AddressType))
            return false;
        
        AddressType aType = (AddressType)that;
        return this.base.equivalent(aType.base);
    }
    
    @Override
    public Type assign(Type source)
    {
        if ( !this.base().equivalent(source) )
            return super.assign(source);
        
        return this.base();
    }
    
    @Override
    public Type add(Type that) {

        if (!(that instanceof AddressType))
            return super.add(that);
        
        AddressType aType = (AddressType)that;
        return this.base.add(aType.base);
    }

    @Override
    public Type sub(Type that) {
        if (!(that instanceof AddressType))
            return super.sub(that);
        
        AddressType aType = (AddressType)that;
        return this.base.sub(aType.base);
    }

    @Override
    public Type mul(Type that) {
        if (!(that instanceof AddressType))
            return super.mul(that);
        
        AddressType aType = (AddressType)that;
        return this.base.mul(aType.base);
    }

    @Override
    public Type div(Type that) {
        if (!(that instanceof AddressType))
            return super.div(that);
        
        AddressType aType = (AddressType)that;
        return this.base.div(aType.base);
    }
    
    @Override
    public Type compare(Type that) {
        if (!(that instanceof AddressType))
            return super.compare(that);
        
        AddressType aType = (AddressType)that;
        return this.base.compare(aType.base);
    }
    
    public Type and(Type that) {
        if ( !(that instanceof AddressType) )
            return super.and(that);
        
        AddressType aType = (AddressType)that;
        return this.base.and(aType.base);
    }
    
    public Type or(Type that) {
        if ( !(that instanceof AddressType) )
            return super.or(that);
        
        AddressType aType = (AddressType)that;
        return this.base.or(aType.base);
    }
    
    @Override
    public Type deref() {
        
        return ((AddressType)this).base;
    }

    
    
}
