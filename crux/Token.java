package crux;

public class Token {
	
	public static enum Kind {
		AND("and"),
		OR("or"),
		NOT("not"),
                LET("let"),
                VAR("var"),
                ARRAY("array"),
                FUNC("func"),
                IF("if"),
                ELSE("else"),
                WHILE("while"),
                TRUE("true"),
                FALSE("false"),
                RETURN("return"),
		
                OPEN_PAREN("("),
                CLOSE_PAREN(")"),
                OPEN_BRACE("{"),
                CLOSE_BRACE("}"),
                OPEN_BRACKET("["),
                CLOSE_BRACKET("]"),
		ADD("+"),
		SUB("-"),
		MUL("*"),
		DIV("/"),
                GREATER_EQUAL(">="),
                LESSER_EQUAL("<="),
                NOT_EQUAL("!="),
                CHECKER("!"),
                EQUAL("=="),
                GREATER_THAN(">"),
                LESS_THAN("<"),
                ASSIGN("="),
                COMMA(","),
                SEMICOLON(";"),
                COLON(":"),
                CALL("::"),
		
		IDENTIFIER(),
		INTEGER(),
		FLOAT(),
		ERROR(),
		EOF();
		
		// TODO: complete the list of possible tokens
		
		private String default_lexeme;
		
		Kind()
		{
			default_lexeme = "";
		}
		
		Kind(String lexeme)
		{
			default_lexeme = lexeme;
		}
		
		public boolean hasStaticLexeme()
		{
			return default_lexeme != null;
		}
		
		// OPTIONAL: if you wish to also make convenience functions, feel free
		//           for example, boolean matches(String lexeme)
		//           can report whether a Token.Kind has the given lexeme
	}
	
	private int lineNum;
	private int charPos;
	Kind kind;
	private String lexeme = "";
        
        public static boolean isValidLexeme(String s) {
            return (new Token(s)).kind != Kind.ERROR;
        }
        
        public static boolean isNumber(String s) {
            return ( ((new Token(s)).kind == Kind.INTEGER) || ((new Token(s)).kind == Kind.FLOAT) );
        }
	
        //factory function
	public static Token EOF(int linePos, int charPos)
	{
		Token tok = new Token(linePos, charPos);
		tok.kind = Kind.EOF;
		return tok;
	}
        
        public static Token ERROR(String lexeme, int linePos, int charPos)
        {
            Token tok = new Token(lexeme, linePos, charPos);
            tok.kind = Kind.ERROR;
            return tok;
        }

	private Token(int lineNum, int charPos)
	{
		this.lineNum = lineNum;
		this.charPos = charPos;
		
		// if we don't match anything, signal error
		this.kind = Kind.ERROR;
		this.lexeme = "No Lexeme Given";
	}
        
        public Token(String lexeme) {
            this(lexeme, 0, 0);
        }
	
	public Token(String lexeme, int lineNum, int charPos)
	{
                boolean foundPredefined = false;
                
		this.lineNum = lineNum;
		this.charPos = charPos;
                this.lexeme = lexeme;
                
                for ( Kind k : Kind.values() ) {
                    if ( lexeme.equals(k.default_lexeme) ) {
                        this.kind = k;
                        foundPredefined = true;
                    }
                }
                
                //not found in reserved words or character sequences
                if ( !foundPredefined ) {
                    
                    if (isInteger(lexeme) )
                        this.kind = Kind.INTEGER;
                    else if (isFloatingPoint(lexeme))
                        this.kind = Kind.FLOAT;
                    else if ( isIdentifier(lexeme))
                        this.kind = Kind.IDENTIFIER;
                    else
                        this.kind = Kind.ERROR;
                    
                    //TODO: IF ALL ELSE FAILS IT IS IDENTIFIER????
                }
                
                //maybe need to check if identifier or etc.
		
		// if we don't match anything, signal error
		//this.kind = Kind.ERROR; I COMMENTED THIS OUT NOT SURE WHAT TO DO WITH IT
                //this.lexeme = "Unrecognized lexeme: " + lexeme;
	}
        
        public Kind kind() {
            return this.kind;
        }
        
        public boolean is(Kind kind) {
            return this.kind == kind;
        }
	
	public int lineNumber() {
		return lineNum;
	}
	
	public int charPosition() {
		return charPos;
	}
	
	// Return the lexeme representing or held by this token
	public String lexeme() {
            return lexeme;
	}
	
        @Override
	public String toString() {
            if ( (this.kind == Kind.INTEGER || this.kind == Kind.FLOAT) || (this.kind == Kind.IDENTIFIER) ) {
               return this.kind + "(" + this.lexeme + ")(lineNum:" + this.lineNum + ", charPos:" + this.charPos + ")";
            }
            else if ( this.kind == Kind.ERROR ) {
                return this.kind + "(Unexcpected token: " + this.lexeme + 
                                   ")(lineNum:" + this.lineNum + ", charPos:" + this.charPos + ")";
            }
            else
                return this.kind + "(lineNum:" + this.lineNum + ", charPos:" + this.charPos + ")";
	}
        
        private boolean isInteger(String s) {
            //check if integer
            try {
                Integer.parseInt(String.valueOf(s.charAt(0))); //our grammar requires no negative numbers, this will fail for negative sign
                Integer.parseInt(s);
            }
            catch (NumberFormatException e) {
                return false;
            }
            return true;
        }
	
        private boolean isFloatingPoint(String s) {            
            try {
                Integer.parseInt(String.valueOf(s.charAt(0))); //our grammar requires a number in front of decimal point
                Float.parseFloat(s);
            }
            catch (NumberFormatException e) {
                return false;
            }
            return true;
        }
        
        private boolean isIdentifier(String s) {
            char iterChar;
            for ( int i = 0 ; i < s.length(); i++ ) {
                iterChar = s.charAt(i);
                
                if ( i == 0 ) {
                    if ( iterChar != '_' && !Character.isAlphabetic(iterChar) )
                        return false;
                }
                else {
                    
                    if ( !(Character.isAlphabetic(iterChar) || Character.isLetterOrDigit(iterChar) ) && iterChar != '_')
                        return false;
                    
                    /* CHANGED THIS, WOULDNT PARSE NUMBERS IN 
                    if ( !Character.isAlphabetic(iterChar) && iterChar != '_')
                        return false;
                    */
                }
            }
            
            return true;
        }
        
        private int findError(String lexeme) {
            char lexemePart;
            int lexemeError = lexeme.charAt(0);
            String rebuiltLexeme = "";
            boolean foundValid = false;
            
            for ( int i = 0 ; i < lexeme.length() ; i++) {
                lexemePart = lexeme.charAt(i);
                rebuiltLexeme += (char)lexemePart;
                
                if ( isValidLexeme(rebuiltLexeme) )
                    foundValid = true;
                else if ( foundValid && !isValidLexeme(rebuiltLexeme) )
                    return lexemePart;
                   
            }
            return lexemeError;
        }
        

}
