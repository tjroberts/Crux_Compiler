package types;

public class FuncType extends Type {
   
   private TypeList args;
   private Type ret;
   
   public FuncType(TypeList args, Type returnType)
   {
      //throw new RuntimeException("implement operators");
      this.args = args;
      this.ret = returnType;
   }
   
   public Type returnType()
   {
      return ret;
   }
   
   public TypeList arguments()
   {
      return args;
   }
   
   @Override
   public String toString()
   {
      return "func(" + args + "):" + ret;
   }

   @Override
   public boolean equivalent(Type that)
   {
      if (that == null)
         return false;
      if (!(that instanceof FuncType))
         return false;
      
      FuncType aType = (FuncType)that;
      return this.ret.equivalent(aType.ret) && this.args.equivalent(aType.args);
   }
   
   public Type add(Type that) {
       
       if ( this.ret == that )
           return this.ret;
       
       return new ErrorType("Cannot add " + this + " with " + that + ".");
   }
	

    public Type sub(Type that) {
        if ( this.ret == that )
           return this.ret;
       
       return new ErrorType("Cannot subtract " + this + " with " + that + ".");
        
    }
	
    public Type mul(Type that) {
        if ( this.ret == that )
           return this.ret;
       
       return new ErrorType("Cannot multiply " + this + " with " + that + ".");
    }



    public Type div(Type that) {
        FuncType tempType = null;
        
        if ( that instanceof FuncType )
            tempType = (FuncType)that;
            
        
        if ( !((this.returnType() instanceof BoolType) && (tempType.returnType() instanceof BoolType)) )
            return super.or(that);
       
       return new BoolType();
    }



    public Type and(Type that) {
        FuncType tempType = null;
        
        if ( that instanceof FuncType ) {
            tempType = (FuncType)that;
            
            if ( !((this.returnType() instanceof BoolType) && (tempType.returnType() instanceof BoolType)) )
                return super.and(that);
        }
        else {
            if ( !(this.returnType() instanceof BoolType && that instanceof BoolType) )
                return super.and(that);
        }
            
       return new BoolType();
    }



    public Type or(Type that) {
        FuncType tempType = null;
        
        if ( that instanceof FuncType ) {
            tempType = (FuncType)that;
            
            if ( !((this.returnType() instanceof BoolType) && (tempType.returnType() instanceof BoolType)) )
                return super.or(that);
        }
        else {
            if ( !(this.returnType() instanceof BoolType && that instanceof BoolType) )
                return super.and(that);
        }
       
       return new BoolType();
    }


    public Type not() {
        if ( !(this.ret instanceof BoolType) )
           return super.not();
       
       return new BoolType();
    }



    public Type compare(Type that) {
        
        return null;
    }
    
    
}
