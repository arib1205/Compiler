package cop5556sp17;

import java.util.ArrayList;
import java.util.Arrays;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.Kind;

public class Scanner {
	
	
	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), 
		KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"), 
		KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), 
		SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), 
		RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"), 
		EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="), 
		PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), 
		ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"), 
		KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"), 
		OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"), 
		KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"), 
		KW_SCALE("scale"), EOF("eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;

		String getText() {
			return text;
		}
	}
	
	public static enum State {START, ISDIGIT, IDENTPART, AFTER_EQUAL,  AFTER_LT, AFTER_DIV, AFTER_GT, AFTER_NOT,AFTER_OR, AFTER_HYPN }
/**
 * Thrown by Scanner when an illegal character is encountered
 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}
	
	/**
	 * Thrown by Scanner when an int literal is not a value that can be represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
	public IllegalNumberException(String message){
		super(message);
		}
	}
	

	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {
		public final int line;
		public final int posInLine;
		
		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}
		

	

	public class Token {
		public final Kind kind;
		public final int pos;  //position in input array
		public final int length;  

		//returns the text of this Token
		public String getText() {
			return chars.substring(this.pos, this.pos+this.length);
		}
		
		//returns a LinePos object representing the line and column of this Token
		LinePos getLinePos(){
			int lineNo;
			int column;
			lineNo = Arrays.binarySearch(linePos, pos);
			if(lineNo < 0) 
			{
				lineNo=-2-lineNo;
			}
			column = pos - linePos[lineNo]; 
			return new LinePos(lineNo, column);
		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		/** 
		 * Precondition:  kind = Kind.INT_LIT,  the text can be represented with a Java int.
		 * Note that the validity of the input should have been checked when the Token was created.
		 * So the exception should never be thrown.
		 * 
		 * @return  int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 */
		public int intVal() throws NumberFormatException, IllegalNumberException{
			int value;
			try{
			value = Integer.parseInt(this.getText());
			}
			catch(Exception e) {
				throw new IllegalNumberException("Illegal Number");
			}
			return value;
		}
		
		public boolean isKind(Kind kind){
			if(this.kind==kind)
				return true;
			else 
				return false;
		}
		
		public boolean isKind(Kind... kinds)  {
			
			for(int i=0;i<kinds.length;i++)
			{
				if(this.kind == kinds[i])
					return true;
			}
			return false;
			
		}
		
		
		@Override
		  public int hashCode() {
		   final int prime = 31;
		   int result = 1;
		   result = prime * result + getOuterType().hashCode();
		   result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		   result = prime * result + length;
		   result = prime * result + pos;
		   return result;
		  }

		  @Override
		  public boolean equals(Object obj) {
		   if (this == obj) {
		    return true;
		   }
		   if (obj == null) {
		    return false;
		   }
		   if (!(obj instanceof Token)) {
		    return false;
		   }
		   Token other = (Token) obj;
		   if (!getOuterType().equals(other.getOuterType())) {
		    return false;
		   }
		   if (kind != other.kind) {
		    return false;
		   }
		   if (length != other.length) {
		    return false;
		   }
		   if (pos != other.pos) {
		    return false;
		   }
		   return true;
		  }

		 

		  private Scanner getOuterType() {
		   return Scanner.this;
		  }
		
	}

	 


	Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();
	}


	
	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		
		int length = chars.length();
	    State state = State.START;
	    int startPos = 0;
	    int pos = 0;
	    ArrayList<Integer> lPList = new ArrayList<Integer>();
		lPList.add(0);
	    int ch;
	    while (pos <= length) {
	        ch = pos < length ? chars.charAt(pos) : -1;
	        switch (state) 
	        {
	            case START: 
	            { 
	            	if(Character.isWhitespace(ch)) 
	            		{
							pos++;
	            			if(ch == '\n')
	            				{
	            				lPList.add(Integer.valueOf(pos));
	            				}
	            			state = State.START;
	            			break;
	            		}
	            ch = pos < length ? chars.charAt(pos) : -1;
	            startPos = pos;
	            switch (ch) {
	            		case -1: {tokens.add(new Token(Kind.EOF, pos, 0)); pos++;}  break;
		        		case '+': {tokens.add(new Token(Kind.PLUS, startPos, 1));pos++;} break;
		            	case '*': {tokens.add(new Token(Kind.TIMES, startPos, 1));pos++;} break;
		            	case '/': {state= State.AFTER_DIV;pos++;}break;				//{tokens.add(new Token(Kind.DIV, startPos, 1));pos++;} break;
		            	case '%': {tokens.add(new Token(Kind.MOD, startPos, 1));pos++;} break;
		            	case '&': {tokens.add(new Token(Kind.AND, startPos, 1));pos++;} break;
		            	case '|': {state= State.AFTER_OR;pos++;}break;
		            	case '!': {state= State.AFTER_NOT;pos++;}break;
		            	case '-': {state= State.AFTER_HYPN;pos++;}break;
		            	case '=': {state= State.AFTER_EQUAL;pos++;}break;
		            	case '<': {state= State.AFTER_LT;pos++;}break;
		            	case '>': {state= State.AFTER_GT;pos++;}break;
		            	case ';': {tokens.add(new Token(Kind.SEMI, startPos, 1));pos++;} break; 
		            	case ',': {tokens.add(new Token(Kind.COMMA, startPos, 1));pos++;} break;
		            	case '(': {tokens.add(new Token(Kind.LPAREN, startPos, 1));pos++;} break;
		            	case ')': {tokens.add(new Token(Kind.RPAREN, startPos, 1));pos++;} break;
		            	case '{': {tokens.add(new Token(Kind.LBRACE, startPos, 1));pos++;} break;
		            	case '}': {tokens.add(new Token(Kind.RBRACE, startPos, 1));pos++;} break;
		            	case '0': {tokens.add(new Token(Kind.INT_LIT,startPos, 1));pos++;}break;
		            	default: {
		            				if (Character.isDigit(ch)) {state = State.ISDIGIT;pos++;} 
		            					else if (Character.isJavaIdentifierStart(ch)) 
		            					{
		            						state = State.IDENTPART;pos++;
		            					} 
		            					else {
		            						throw new IllegalCharException("illegal char " +(char)ch+" at pos "+pos);
		            					
		            					}
		            				}
		            			}
	            			}break;
	            			
	           case ISDIGIT: 
	           {
	           		if (Character.isDigit(ch)) 
	           		{
	           		pos++;
	            	}
	            	else {
	            		Token t=new Token(Kind.INT_LIT, startPos, pos - startPos);
	            		if(t.intVal()>=Integer.MAX_VALUE)
	            			throw new IllegalNumberException(
            						"illegal char " +ch+" at pos "+pos);
	            		else
	            			tokens.add(t); 
	            		state = State.START;	
	            		}
	            }break;
	            
	            case IDENTPART:  
	            {	
	            	if (Character.isJavaIdentifierPart(ch)) 
	            	{
	                 pos++;
	                } 
	            	else
	            	{
	                String tkn = chars.substring(startPos, pos);
	                	switch(tkn) 
	                	{
	                	case "integer": {tokens.add(new Token(Kind.KW_INTEGER, startPos, pos - startPos));}  break;
	                	case "boolean": {tokens.add(new Token(Kind.KW_BOOLEAN, startPos, pos - startPos));}  break;
	                	case "image": {tokens.add(new Token(Kind.KW_IMAGE, startPos, pos - startPos));}  break;
	                	case "url": {tokens.add(new Token(Kind.KW_URL, startPos, pos - startPos));}  break;
	                	case "file": {tokens.add(new Token(Kind.KW_FILE, startPos, pos - startPos));}  break;
	                	case "frame": {tokens.add(new Token(Kind.KW_FRAME, startPos, pos - startPos));}  break;
	                	case "while": {tokens.add(new Token(Kind.KW_WHILE, startPos, pos - startPos));}  break;
	                	case "if": {tokens.add(new Token(Kind.KW_IF, startPos, pos - startPos));}  break;
	                	case "sleep": {tokens.add(new Token(Kind.OP_SLEEP, startPos, pos - startPos));}  break;
	                	case "screenheight": {tokens.add(new Token(Kind.KW_SCREENHEIGHT, startPos, pos - startPos));}  break;
	                	case "screenwidth": {tokens.add(new Token(Kind.KW_SCREENWIDTH, startPos, pos - startPos));}  break;
	                	case "gray": {tokens.add(new Token(Kind.OP_GRAY, startPos, pos - startPos));}  break;
	                	case "convolve": {tokens.add(new Token(Kind.OP_CONVOLVE, startPos, pos - startPos));}  break;
	                	case "blur": {tokens.add(new Token(Kind.OP_BLUR, startPos, pos - startPos));}  break;
	                	case "scale": {tokens.add(new Token(Kind.KW_SCALE, startPos, pos - startPos));}  break;
	                	case "width": {tokens.add(new Token(Kind.OP_WIDTH, startPos, pos - startPos));}  break;
	                	case "height": {tokens.add(new Token(Kind.OP_HEIGHT, startPos, pos - startPos));}  break;
	                	case "xloc": {tokens.add(new Token(Kind.KW_XLOC, startPos, pos - startPos));}  break;
	                	case "yloc": {tokens.add(new Token(Kind.KW_YLOC, startPos, pos - startPos));}  break;
	                	case "hide": {tokens.add(new Token(Kind.KW_HIDE, startPos, pos - startPos));}  break;
	                	case "show": {tokens.add(new Token(Kind.KW_SHOW, startPos, pos - startPos));}  break;
	                	case "move": {tokens.add(new Token(Kind.KW_MOVE, startPos, pos - startPos));}  break;
	                	case "true": {tokens.add(new Token(Kind.KW_TRUE, startPos, pos - startPos));}  break;
	                	case "false": {tokens.add(new Token(Kind.KW_FALSE, startPos, pos - startPos));}  break;
	                	default: {tokens.add(new Token(Kind.IDENT, startPos, pos - startPos)); } break;	
	                	}
	                state = State.START;
	                				}
	         	}break;
	         	
	            case AFTER_LT: 
	            {
	            	if(pos==length)
        				{
	            		tokens.add(new Token(Kind.LT, startPos, 1));
        				}
	            	else if(ch=='-') 
						{	 
	            		pos++;
	            		tokens.add(new Token(Kind.ASSIGN, startPos, pos - startPos));
						}
	            	else if(ch=='=') 
    					{
	            		pos++;
    					tokens.add(new Token(Kind.LE, startPos, pos - startPos)); 
    					}
    				else 
    					{
    					tokens.add(new Token(Kind.LT, startPos, 1));
    					}
    				state = State.START;
	            } break;
	            
	            case AFTER_GT: 
	            {
	            	if(pos==length)
    					{
	            		tokens.add(new Token(Kind.GT, startPos, 1));
    					}
	            	else if(ch=='=') 
    					{ 
	            		pos++;
    					tokens.add(new Token(Kind.GE, startPos, pos - startPos));
    					}
    				else 
    					{
    					tokens.add(new Token(Kind.GT, startPos, 1));
    					}
    				state = State.START;
	            } break;
	         	
	            case AFTER_EQUAL:  
	            {
	            	if(pos==length)
    					{
	            		throw new IllegalCharException(
	    	            		"illegal char " +ch+" at pos "+pos);
    					}
	            	else if(ch=='=') 
	            		{ 
	            		pos++;
	            		tokens.add(new Token(Kind.EQUAL, startPos, pos - startPos));
	            		}
	            	else 
	            		{
	            		throw new IllegalCharException(
	            				"illegal char " +ch+" at pos "+pos);
	            		}
	            state = State.START;
	            }
	            break;
	            
	            case AFTER_NOT: 
	            {
	            	if(pos==length)
						{
	            		tokens.add(new Token(Kind.NOT, startPos, 1));
						}
	            	else if(ch=='=') 
	            		{
	            		pos++;
	            		tokens.add(new Token(Kind.NOTEQUAL, startPos, pos - startPos)); 
	            		}
	            	else 
	            		{
	            		tokens.add(new Token(Kind.NOT, startPos, 1));
	            		}
	            state = State.START;
	            } break;
	            
	                 
	            case AFTER_HYPN: 
	            {
	            	if(pos==length)
	            		{
    					tokens.add(new Token(Kind.MINUS, startPos, 1));
	            		}
	            	else if(ch=='>') 
    					{
	            		pos++;
    					tokens.add(new Token(Kind.ARROW, startPos, pos - startPos)); 
    					}
    				else 
    					{
    					tokens.add(new Token(Kind.MINUS, startPos, 1));
    					}
    				state = State.START;
	            } break;
	            
	            case AFTER_DIV:
	            {
	            if(pos==length)
        			{
        			tokens.add(new Token(Kind.DIV, startPos, 1));
        			}
        		else if(ch=='*')
	            	{
	            		
	            		while(true)
	            		{
	            			pos++;
	            		if(pos==length)
	            			{
	            			tokens.add(new Token(Kind.EOF, pos, 0));
	            			break;
	            			}
	            		if(chars.charAt(pos)=='*')
	            			{
	            				pos++;
	            				if(pos==length)
		            			{
		            			tokens.add(new Token(Kind.EOF, pos, 0));
		            			break;
		            			}
	            				if(chars.charAt(pos)=='/')
	            				{
	            					pos++;
	            				break;
	            				}
	            				pos--;
	            			}	
	            		}
	            	}	
	            else
	            	{
	            		tokens.add(new Token(Kind.DIV, startPos, 1));
	            	}
	            state=State.START;	
	            }break;	
	            
	            case AFTER_OR: 
	            {
	            	if(pos==length)
	            		{
	            		tokens.add(new Token(Kind.OR, startPos, 1));
	            		}
	            	else if(ch=='-') 
    					{
    					pos++;
    					if(pos==length)
	            			{
    						tokens.add(new Token(Kind.OR, startPos, 1));
    						tokens.add(new Token(Kind.MINUS, startPos+1, 1));
	            			}
    					else if(chars.charAt(pos)=='>') 
    						{pos++;
    						tokens.add(new Token(Kind.BARARROW, startPos, pos - startPos)); 
    						}
    					else 
    						{
    						tokens.add(new Token(Kind.OR, startPos, 1));
    						tokens.add(new Token(Kind.MINUS, startPos+1, 1));
    						}
    					}
    				else 
    					{
    					tokens.add(new Token(Kind.OR, startPos, 1));
    					}
    				state = State.START;
	            } break;
	            default:  assert false;
	        }
	    }
	    linePos = lPList.toArray(new Integer[lPList.size()]); 
		return this;  
	}


	
	Integer[] linePos;
	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum;
	
	
	
	/*
	 * Return the next token in the token list and update the state so that
	 * the next call will return the Token..  
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}
	
	/*
	 * Return the next token in the token list without updating the state.
	 * (So the following call to next will return the same token.)
	 */
	public Token peek(){
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum);		
	}

	

	/**
	 * Returns a LinePos object containing the line and position in line of the 
	 * given token.  
	 * 
	 * Line numbers start counting at 0
	 * 
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {
		return t.getLinePos();
	}


}


