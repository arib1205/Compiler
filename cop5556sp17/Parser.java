package cop5556sp17;

import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;

import java.util.ArrayList;
import java.util.List;

import cop5556sp17.Scanner.IllegalNumberException;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.*;

public class Parser {

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}
	
	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when 
	 * the Parser is finished.
	 *
	 */
	@SuppressWarnings("serial")	
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 * @throws IllegalNumberException 
	 * @throws NumberFormatException 
	 */
	
	ASTNode parse() throws SyntaxException, NumberFormatException, IllegalNumberException {
		ASTNode a=program();
		matchEOF();
		return a;
	}

	Expression expression() throws SyntaxException, NumberFormatException, IllegalNumberException {
		//TODO
		Token temp=t;
		Expression e0=null;
		Expression e1=null;
		
		e0=term();
		//while(t.kind==LT||t.kind==LE||t.kind==GT||t.kind==GE||t.kind==EQUAL||t.kind==NOTEQUAL)
		while(match(LT,LE,GT,GE,EQUAL,NOTEQUAL))
			{
			Token op=t;
			consume();
			e1=term();
			e0=new BinaryExpression(temp,e0,op,e1);
			}
		return e0;
	}

	Expression term() throws SyntaxException, NumberFormatException, IllegalNumberException {
		//TODO
		Token temp=t;
		Expression e0=null;
		Expression e1=null;
		e0=elem();
		//while(t.kind==PLUS||t.kind==MINUS||t.kind==OR)
		while(match(PLUS,MINUS,OR))	
			{	
			Token op=t;
			consume();
			e1=elem();
			e0=new BinaryExpression(temp,e0,op,e1);
			}
		return e0;
		}
	

	Expression elem() throws SyntaxException, NumberFormatException, IllegalNumberException {
		//TODO
			Token temp=t;
			Expression e0=null;
			Expression e1=null;
			e0=factor();
			//while(t.kind==TIMES||t.kind==DIV||t.kind==AND||t.kind==MOD)
			while(match(TIMES,DIV,AND,MOD))	
				{
				Token op=t;
				consume();
				
				e1=factor();
				e0=new BinaryExpression(temp,e0,op,e1);
				}
			return e0;
	}

	Expression factor() throws SyntaxException, NumberFormatException, IllegalNumberException {
		Kind kind = t.kind;
		Expression e=null;
		
		switch (kind) {
		case IDENT: {
			e=new IdentExpression(t);
			consume();
		}
			break;
		case INT_LIT: {
			e=new IntLitExpression(t);
			consume();
		}
			break;
		case KW_TRUE:
		case KW_FALSE: {
			e=new BooleanLitExpression(t);
			consume();
		}
			break;
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
			e=new ConstantExpression(t);
			consume();
		}
			break;
		case LPAREN: {
			consume();
			e=expression();
			match(RPAREN);
		}
			break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal factor");
		}
		return e;
	}

	Block block() throws SyntaxException, NumberFormatException, IllegalNumberException {
		//TODO
		Block b=null;
		Token temp=t;
		ArrayList<Dec> d=new ArrayList<>();
		ArrayList<Statement> s=new ArrayList<>();
		
		if(t.kind==LBRACE)
			{
			consume();
			while(match(KW_INTEGER,KW_BOOLEAN,KW_IMAGE,KW_FRAME,OP_SLEEP,KW_WHILE,KW_IF,IDENT,OP_BLUR,OP_GRAY,OP_CONVOLVE,
					KW_SHOW,KW_HIDE,KW_MOVE,KW_XLOC,KW_YLOC,OP_WIDTH,OP_HEIGHT,KW_SCALE))
				{
				if(match(KW_INTEGER,KW_BOOLEAN,KW_IMAGE,KW_FRAME))
					d.add(dec());
				else
					s.add(statement());
				}
			match(RBRACE);
			b=new Block(temp,d,s);
			}
		else
			throw new SyntaxException("saw "+t.kind+" expected LBRACE");	
		
		return b;
	}

	Program program() throws SyntaxException, NumberFormatException, IllegalNumberException {
		//TODO
		Token temp=t;
		ArrayList<ParamDec> plist=new ArrayList<>();
		Block b=null;
		Program p=null;
		
		if(t.kind==IDENT)
			{
			consume();
			if(t.kind==LBRACE)
				{
				b=block();
				p=new Program(temp,plist,b);
				}
			else if(match(KW_URL,KW_FILE,KW_INTEGER,KW_BOOLEAN))
				{
				plist.add(paramDec());
				while(t.kind==COMMA)
					{
					consume();
					plist.add(paramDec());
					}
				b=block();
				p=new Program(temp,plist,b);
				}
			else
				throw new SyntaxException("saw "+t.kind+" expected LBRACE or KW_URL or KW_FILE or KW_INTEGER or KW_BOOLEAN");
				
			}
		else
			throw new SyntaxException("saw "+t.kind+"expected IDENT");
		
		return p;
	}

	ParamDec paramDec() throws SyntaxException {
		//TODO
		ParamDec pd=null;
		Token temp=t;
		
		if(match(KW_URL,KW_FILE,KW_INTEGER,KW_BOOLEAN))
		{
		consume();
		Token id=t;
		match(IDENT);
		pd=new ParamDec(temp,id);
		}
	else
		throw new SyntaxException("saw "+t.kind+" expected KW_URL or KW_FILE or KW_INTEGER or KW_BOOLEAN");
		
	return pd;
		
	}

	Dec dec() throws SyntaxException {
		//TODO
		Dec d=null;
		Token temp=t;
		
		if(match(KW_INTEGER,KW_BOOLEAN,KW_IMAGE,KW_FRAME))
			{
			consume();
			Token id=t;
			match(IDENT);
			d=new Dec(temp,id);
			}
		else
			throw new SyntaxException("saw "+t.kind+" expected KW_INTEGER or KW_BOOLEAN or KW_IMAGE or KW_FRAME");
		
		return d;
	}

	Statement statement() throws SyntaxException, NumberFormatException, IllegalNumberException {
		//TODO
		Statement s=null;
		Token temp=t;
		
		if(t.kind==OP_SLEEP)
			{
			consume();
			Expression e=expression();
			s=new SleepStatement(temp,e);
			match(SEMI);
			}
		else if(t.kind==KW_WHILE)
			{
			s=whileStatement();
			}
		else if(t.kind==KW_IF)
			{
			s=ifStatement();
			}
		else if(t.kind==IDENT)
			{
			if(scanner.peek().kind==ASSIGN)
				{
				s=assign();
				match(SEMI);
				}
			else
				{
				s=chain();
				match(SEMI);
				}
			}
		else if(match(OP_BLUR,OP_GRAY,OP_CONVOLVE)||match(KW_SHOW,KW_HIDE,KW_MOVE,KW_XLOC,KW_YLOC)||match(OP_WIDTH,OP_HEIGHT,KW_SCALE))
			{
			s=chain();
			match(SEMI);
			}
		else
			throw new SyntaxException("saw "+t.kind+" expected OP_SLEEP or KW_WHILE or KW_IF or IDENT");
		
		return s;
		
	}
	
	AssignmentStatement assign() throws SyntaxException, NumberFormatException, IllegalNumberException {
		//TODO
		IdentLValue id=null;
		Expression e=null;
		AssignmentStatement as=null;
		Token temp=t;
		
		if(t.kind==IDENT)
			{
			id=new IdentLValue(t);
			consume();
			if(t.kind==ASSIGN)
				{
				consume();
				e=expression();
				as=new AssignmentStatement(temp,id,e);
				}
			else 
				throw new SyntaxException("saw "+t.kind+" expected ASSIGN");
			}
		
		else
			throw new SyntaxException("saw "+t.kind+" expected IDENT");	
		
		return as;
		}
	
	
	WhileStatement whileStatement() throws SyntaxException, NumberFormatException, IllegalNumberException {
		//TODO
		Token temp=t;
		Expression e=null;
		Block b=null;
		WhileStatement wfs=null;
		
		if(t.kind==KW_WHILE)
		{
		consume();
		if(t.kind==LPAREN)
			{
			consume();
			e=expression();
			match(RPAREN);
			b=block();
			wfs=new WhileStatement(temp,e,b);
			}
		else
			throw new SyntaxException("saw "+t.kind+" expected LPAREN");
		}
	else
		throw new SyntaxException("saw "+t.kind+" expected KW_WHILE");
		
		return wfs;
	}
	
	IfStatement ifStatement() throws SyntaxException, NumberFormatException, IllegalNumberException {
		//TODO
		Token temp=t;
		Expression e=null;
		Block b=null;
		IfStatement ifs=null;
		
		if(t.kind==KW_IF)
			{
			consume();
			if(t.kind==LPAREN)
				{
				consume();
				e=expression();
				match(RPAREN);
				b=block();
				ifs=new IfStatement(temp,e,b);
				}
			else
				throw new SyntaxException("saw "+t.kind+" expected LPAREN");
			}
		else
			throw new SyntaxException("saw "+t.kind+" expected KW_IF");
		
		return ifs;
	}

	Chain chain() throws SyntaxException, NumberFormatException, IllegalNumberException {
		//TODO
		Chain c=null;
		ChainElem ce=null;
		Token temp=t;
		
		c=chainElem();
		if(match(ARROW,BARARROW))
			{
			Token op=t;
			consume();
			ce=chainElem();
			c=new BinaryChain(temp,c,op,ce);
			while(match(ARROW,BARARROW))
				{
				op=t;
				consume();
				ce=chainElem();
				c=new BinaryChain(temp,c,op,ce);
				}
			}
		else
			throw new SyntaxException("saw "+t.kind+" expected ARROW or BARARROW");
		
		return c;
	}

	ChainElem chainElem() throws SyntaxException, NumberFormatException, IllegalNumberException {
		//TODO
		ChainElem ce=null;
		Token temp=t;
		
		if(t.kind==IDENT)
			{
			ce=new IdentChain(t);
			consume();
			}
		else if(match(OP_BLUR,OP_GRAY,OP_CONVOLVE))
			{
			consume();
			ce=new FilterOpChain(temp,arg());
			}
		else if(match(KW_SHOW,KW_HIDE,KW_MOVE,KW_XLOC,KW_YLOC))
			{
			consume();
			ce=new FrameOpChain(temp,arg());
			}
		else if(match(OP_WIDTH,OP_HEIGHT,KW_SCALE))
			{
			consume();
			ce=new ImageOpChain(temp,arg());
			}
		else
			throw new SyntaxException("saw "+t.kind+" expected IDENT or OP_BLUR or OP_GRAY or OP_CONVOLVE or KW_SHOW "
													+ "or KW_HIDE or KW_MOVE or KW_XLOC or KW_YLOC or"
													+ "OP_WIDTH or OP_HEIGHT or KW_SCALE");
		return ce;
	}

	Tuple arg() throws SyntaxException, NumberFormatException, IllegalNumberException {
		//TODO
		Tuple tup=null;
		List<Expression> list=new ArrayList<>();
		Token temp=t;
		
		if(t.kind==LPAREN)
			{
			consume();
			list.add(expression());
			while(t.kind==COMMA)
				{
				consume();
				list.add(expression());
				}
			match(RPAREN);
			tup=new Tuple(temp,list);
			}
		else
			tup=new Tuple(temp,list);
		return tup;
		
	}
	
/*	void arrowOp() throws SyntaxException {
		//TODO  
		//if(t.kind==ARROW||t.kind==BARARROW)
		if(match(ARROW,BARARROW))
			consume();
	}
	
	void filterOp() throws SyntaxException {
		//TODO  
		//if(t.kind==OP_BLUR||t.kind==OP_GRAY||t.kind==OP_CONVOLVE)
		if(match(OP_BLUR,OP_GRAY,OP_CONVOLVE))	
			consume();
	}
	
	void frameOp() throws SyntaxException {
		//TODO 
		//if(t.kind==KW_SHOW||t.kind==KW_HIDE||t.kind==KW_MOVE||t.kind==KW_XLOC||t.kind==KW_YLOC)
		if(match(KW_SHOW,KW_HIDE,KW_MOVE,KW_XLOC,KW_YLOC))	
			consume();
	}
	
	void imageOp() throws SyntaxException {
		//TODO 
		//if(t.kind==OP_WIDTH||t.kind==OP_HEIGHT||t.kind==KW_SCALE)
		if(match(OP_WIDTH,OP_HEIGHT,KW_SCALE))
			consume();
	
	}
	
	void relOp() throws SyntaxException {
		//TODO
		//if(t.kind==LT||t.kind==LE||t.kind==GT||t.kind==GE||t.kind==EQUAL||t.kind==NOTEQUAL)
		if(match(LT,LE,GT,GE,EQUAL,NOTEQUAL))	
			consume();
	}
	
	
	void weakOp() throws SyntaxException {
		//TODO
		//if(t.kind==PLUS||t.kind==MINUS||t.kind==OR)
		if(match(PLUS,MINUS,OR))	
			consume();
	}
	
	void strongOp() throws SyntaxException{
		
		//if(t.kind==TIMES||t.kind==DIV||t.kind==AND||t.kind==MOD)
		if(match(TIMES,DIV,AND,MOD))
			consume();
	}*/

	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.isKind(EOF)) {
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.isKind(kind)) {
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + "expected " + kind);
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private boolean match(Kind... kinds) throws SyntaxException {
		
		for(int i=0;i<kinds.length;i++)
		{
			if(t.kind==kinds[i])
				return true;
		}
		return false;
		
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}
