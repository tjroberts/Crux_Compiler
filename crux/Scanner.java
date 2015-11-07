package crux;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
//import java.util.NoSuchElementException;

public class Scanner implements Iterable<Token>, Iterator<Token> {
	public static String studentName = "Tyler Robertson";
	public static String studentID = "22991994";
	public static String uciNetID = "tjrobert";
	
	private int lineNum;  // current line count
	private int charPos;  // character offset for current line
	private int nextChar; // contains the next char (-1 == EOF)
        private Stack<Token> tokens;
	final private Reader input;
	
        private boolean stopIter; //for hasNext() method
        private boolean foundComm1;
        private boolean foundComm2;
        
	Scanner(Reader reader)
	{
            input = reader;
            tokens = new Stack<>();
            charPos = 0;
            nextChar = readChar();
            lineNum = 1;
            stopIter = false;
            foundComm1 = foundComm2 = false;
	}
        
        
        @Override
        public void remove() {
            //JUST TO MAKE IT HAPPY, NEED FOR LINUX
        }
	
	// OPTIONAL: helper function for reading a single char from input
	//           can be used to catch and handle any IOExceptions,
	//           advance the charPos or lineNum, etc.
	
        //returns '\n' when \r\n is found to notify of newline
	private int readChar() {
            int newChar = 0;
            
            try {
                newChar = input.read();
                if ( newChar == '\n' ) {
                    charPos = 0;
                    lineNum++;
                }
                else if ( newChar == '\r' ) { //my filesystem required this type of handling
                    newChar = input.read(); //throw away '\n' character
                    charPos = 0;
                    lineNum++;
                }
                else
                    charPos++;
            }
            catch (IOException e) {    
                System.out.println("Scanner.readChar() : An error occured while reading");
                newChar = -1; //feed eof so no more file reading happens (may lose last lexeme)
            }
            
            return newChar;
	}
        
        private void skipWhitespace() {
            while ( ((nextChar == '\n' || nextChar == '\r') || nextChar == ' ') && nextChar != -1 )
                nextChar = readChar();
        }
        
        private void gotoNextLine() {
            int nextLine = lineNum + 1;
            while ( (lineNum < nextLine) && (nextChar != -1) )
                nextChar = readChar();
        }
        
        private boolean isCommented() {
            return foundComm1 & foundComm2;
        }
        
        @Override
        public Iterator<Token> iterator() {
            return this;
        }
        
        @Override
        public boolean hasNext() {
            return !stopIter;
        }
        
        @Override
	public Token next () {
            foundComm1 = foundComm2 = false;
            String currLexeme = "";
            tokens.clear();
            int lastLine = 0;
            int lastCharPos = 0;
            
            skipWhitespace();
            if ( nextChar == -1) {
                stopIter = true;
                return Token.EOF(lineNum + 1, charPos);
            } 
            
            do {
                //check for comment operator
                if ( nextChar == '/') {
                    if ( foundComm1 ) {
                        tokens.pop(); //get rid of first comment bar
                        foundComm2 = true;
                    }
                    else
                        foundComm1 = true;
                }
                
                if ( isCommented() ) {
                    gotoNextLine();
                    skipWhitespace();
                    currLexeme = "";
                    foundComm1 = foundComm2 = false;
                    continue;
                }
                
                currLexeme += (char)nextChar;
                lastLine = lineNum;
                lastCharPos = charPos;
                
                //dont perform maximum munch on integers and floats
                if ( Token.isValidLexeme(currLexeme) )
                    tokens.push(new Token(currLexeme, lineNum, charPos - (currLexeme.length() - 1)));
                else if ( !tokens.isEmpty() ) {
                    return tokens.peek();    
                }
                else //isnt valid and has no fallback -> exit and return error token
                    break;
                
                nextChar = readChar();
                 
            } while ( (nextChar != ' ' && nextChar != -1) && (nextChar != '\n') );
            
            nextChar = readChar();
            
            if ( currLexeme.equals("") && nextChar == -1 ) { //no lexemes in file 
                stopIter = true;
                return Token.EOF(lineNum, charPos);
            }
            if ( tokens.isEmpty() ) {
                return Token.ERROR(currLexeme, lastLine, lastCharPos - (currLexeme.length() - 1));
            }
            else 
                return tokens.pop();
	}

}
