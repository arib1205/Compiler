package cop5556sp17;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import java.util.ArrayList;
import java.util.List;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;
import cop5556sp17.Scanner.Token;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;
import static cop5556sp17.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		binaryChain.getE0().visit(this, null);
		binaryChain.getE1().visit(this, null);
		
		TypeName chain =binaryChain.getE0().getT();
		TypeName chainelem =binaryChain.getE1().getT();
		
		switch(binaryChain.getArrow().kind)
		{
		case ARROW:
			if(chain == TypeName.URL && chainelem == TypeName.IMAGE)
				binaryChain.setT(TypeName.IMAGE);
			
			else if(chain == TypeName.FILE && chainelem == TypeName.IMAGE)
				binaryChain.setT(TypeName.IMAGE);
			
			else if(chain == TypeName.FRAME && (binaryChain.getE1() instanceof FrameOpChain) && binaryChain.getE1().getFirstToken().isKind(KW_XLOC,KW_YLOC))
				binaryChain.setT(TypeName.INTEGER);
			
			else if(chain == TypeName.FRAME && (binaryChain.getE1() instanceof FrameOpChain) && binaryChain.getE1().getFirstToken().isKind(KW_SHOW,KW_HIDE,KW_MOVE))
				binaryChain.setT(TypeName.FRAME);
			
			else if(chain == TypeName.IMAGE && (binaryChain.getE1() instanceof ImageOpChain) && binaryChain.getE1().getFirstToken().isKind(OP_WIDTH,OP_HEIGHT))
				binaryChain.setT(TypeName.INTEGER);
			
			else if(chain == TypeName.IMAGE && chainelem == TypeName.FRAME)
				binaryChain.setT(TypeName.FRAME);
			
			else if(chain == TypeName.IMAGE && chainelem == TypeName.FILE)
				binaryChain.setT(TypeName.NONE);
			
			else if(chain == TypeName.IMAGE && (binaryChain.getE1() instanceof ImageOpChain) && binaryChain.getE1().getFirstToken().isKind(KW_SCALE))
				binaryChain.setT(TypeName.IMAGE);
			
			else if(chain == TypeName.IMAGE && (binaryChain.getE1() instanceof IdentChain) && chainelem == TypeName.IMAGE)
				binaryChain.setT(TypeName.IMAGE);
			
			else if(chain == TypeName.IMAGE && (binaryChain.getE1() instanceof FilterOpChain) && binaryChain.getE1().getFirstToken().isKind(OP_GRAY,OP_BLUR,OP_CONVOLVE))
				binaryChain.setT(TypeName.IMAGE);
			else if(chain == TypeName.INTEGER && (binaryChain.getE1() instanceof IdentChain) && chainelem == TypeName.INTEGER )
				binaryChain.setT(TypeName.INTEGER);
			else
				throw new TypeCheckException("Illegal Combination of types at BinaryChain");
			break;
			
		case BARARROW:
			if(chain == TypeName.IMAGE && (binaryChain.getE1() instanceof FilterOpChain) && binaryChain.getE1().getFirstToken().isKind(OP_GRAY,OP_BLUR,OP_CONVOLVE))
				binaryChain.setT(TypeName.IMAGE);
			else
				throw new TypeCheckException("Illegal Combination of types at BinaryChain");
			break;
		default:
			throw new TypeCheckException("Illegal Combination of types at BinaryChain");
		}
		
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		binaryExpression.getE0().visit(this, null);
		binaryExpression.getE1().visit(this, null);
		
		TypeName e0 = binaryExpression.getE0().getT();
		TypeName e1 = binaryExpression.getE1().getT();
		
		switch(binaryExpression.getOp().kind)
		{
			case PLUS:
			case MINUS:
				if(e0 == TypeName.INTEGER && e1 == TypeName.INTEGER)
					binaryExpression.setT(TypeName.INTEGER);
		
				else if(e0 == TypeName.IMAGE && e1 == TypeName.IMAGE)
					binaryExpression.setT(TypeName.IMAGE);
				else
					throw new TypeCheckException("Illegeal combination of types in Binary Expression");
				
				break;
				
			case TIMES:
				if(e0 == TypeName.INTEGER && e1 == TypeName.INTEGER)
					binaryExpression.setT(TypeName.INTEGER);
				
				else if(e0 == TypeName.INTEGER && e1 == TypeName.IMAGE)
					binaryExpression.setT(TypeName.IMAGE);
				
				else if(e0 == TypeName.IMAGE && e1 == TypeName.INTEGER)
					binaryExpression.setT(TypeName.IMAGE);
				else
					throw new TypeCheckException("Illegeal combination of types in Binary Expression");
				
				break;
			case MOD:
			case DIV:
				if(e0 == TypeName.INTEGER && e1 == TypeName.INTEGER)
					binaryExpression.setT(TypeName.INTEGER);
				else if(e0 == TypeName.IMAGE && e1 == TypeName.INTEGER)
					binaryExpression.setT(TypeName.IMAGE);
				else
					throw new TypeCheckException("Illegeal combination of types in Binary Expression");
				
				break;
				
			case AND:
			case OR:
				if(e0 == TypeName.BOOLEAN && e1 == TypeName.BOOLEAN)
					binaryExpression.setT(TypeName.BOOLEAN);
				
				else if(e0 == TypeName.INTEGER && e1 == TypeName.INTEGER)
					binaryExpression.setT(TypeName.INTEGER);
				else
					throw new TypeCheckException("Illegeal combination of types in Binary Expression");
				
				break;
				
			case LT:
			case GT:
			case LE:
			case GE:
				if(e0 == TypeName.INTEGER && e1 == TypeName.INTEGER)
					binaryExpression.setT(TypeName.BOOLEAN);
				
				else if(e0 == TypeName.BOOLEAN && e1 == TypeName.BOOLEAN)
					binaryExpression.setT(TypeName.BOOLEAN);
				else
					throw new TypeCheckException("Illegeal combination of types in Binary Expression");
			
				break;
			
			case EQUAL:
			case NOTEQUAL:
				if (e0 == e1)
					binaryExpression.setT(TypeName.BOOLEAN);
				else
					throw new TypeCheckException("Types don't match for equal or not equal");
			break;
			
		default:
			throw new TypeCheckException("Illegeal combination of types in Binary Expression");
		}
		
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		List<Dec> decs = block.getDecs();
		List<Statement> stat = block.getStatements();
		
		symtab.enterScope();
		
		for(int i=0; i<decs.size(); i++)
			{
			decs.get(i).visit(this, null);
			}
		for(int i=0; i<stat.size(); i++)
			{
			stat.get(i).visit(this, null);
			}
		
		symtab.leaveScope();
		
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		booleanLitExpression.setT(TypeName.BOOLEAN);
		
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		filterOpChain.getArg().visit(this, null);
		
		if (filterOpChain.getArg().getExprList().size()==0)
			filterOpChain.setT(TypeName.IMAGE);
		else
			throw new TypeCheckException("Type Mismatch at FilterOpChain");
		
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		frameOpChain.getArg().visit(this, null);
		
		if(frameOpChain.getFirstToken().isKind(KW_SHOW,KW_HIDE))
			{
			if(frameOpChain.getArg().getExprList().size() == 0)
				frameOpChain.setT(TypeName.NONE);
			else
				throw new TypeCheckException("Type Mismatch at FrameOpChain");
			}
		
		else if(frameOpChain.getFirstToken().isKind(KW_XLOC,KW_YLOC))
			{
			if(frameOpChain.getArg().getExprList().size() == 0)
				frameOpChain.setT(TypeName.INTEGER);
			else
				throw new TypeCheckException("Type Mismatch at FrameOpChain");
			}
		
		else if(frameOpChain.getFirstToken().isKind(KW_MOVE))
			{
			if(frameOpChain.getArg().getExprList().size() == 2)
				frameOpChain.setT(TypeName.NONE);
			else
				throw new TypeCheckException("Type Mismatch at FrameOpChain");
			}
		
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		Dec d = symtab.lookup(identChain.getText());
		if(d == null)
			{
			throw new TypeCheckException("Type Mismatch at IdentExpression");
			}
		else
			{
			identChain.setT(symtab.lookup(identChain.getFirstToken().getText()).getT());
			}
		
		identChain.setD(d);
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Dec d = symtab.lookup(identExpression.getText());
		
		if(d == null)
			{
			throw new TypeCheckException("Type Mismatch at IdentExpression");
			}
		else
			{
			identExpression.setT(symtab.lookup(identExpression.getFirstToken().getText()).getT());
			identExpression.setD(d);
			}
		
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		ifStatement.getE().visit(this, null);
		ifStatement.getB().visit(this, null);
		
		if(ifStatement.getE().getT() != TypeName.BOOLEAN)
			throw new TypeCheckException("Type Mismatch ifStatement");
		
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		intLitExpression.setT(TypeName.INTEGER);
		
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		sleepStatement.getE().visit(this, null);
		
		if(sleepStatement.getE().getT() != TypeName.INTEGER)
			throw new TypeCheckException("Type Mismatch at SleepStatement");
		
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		whileStatement.getE().visit(this, null);
		whileStatement.getB().visit(this, null);
		
		if(whileStatement.getE().getT()!=TypeName.BOOLEAN)
			throw new TypeCheckException("Type Mismatch at whileStatement");
		
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		if(symtab.presentInScope(declaration.getIdent().getText()))
			throw new TypeCheckException("Variables declared in same scope");
		else
			{
			declaration.setT(Type.getTypeName(declaration.getFirstToken()));
			symtab.insert(declaration.getIdent().getText(), declaration); 
			}
		
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO Auto-generated method stub
		List<ParamDec> pdec = program.getParams();
		
		for(int i=0; i<pdec.size(); i++)
			pdec.get(i).visit(this, null);
		
		program.getB().visit(this, null);
		
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		assignStatement.getVar().visit(this, null);
		assignStatement.getE().visit(this, null);
		
		if(assignStatement.getVar().getD().getT() != assignStatement.getE().getT())
			throw new TypeCheckException("Type Mismatch at Assign Statement");
		
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Dec d = symtab.lookup(identX.getText());
		if(d == null)
			{
			throw new TypeCheckException("Type mismatch for IdentLValue");
			}
		else
			{
			identX.setD(d);
			}
		
		return null;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		if(symtab.presentInScope(paramDec.getIdent().getText()))
			throw new TypeCheckException("Variables declared in same scope");
		
		else
			{
			paramDec.setT(Type.getTypeName(paramDec.getFirstToken()));
			symtab.insert(paramDec.getIdent().getText(), paramDec);
			}
		
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		// TODO Auto-generated method stub
		constantExpression.setT(TypeName.INTEGER);
		
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		imageOpChain.getArg().visit(this, null);
		
		if(imageOpChain.getFirstToken().isKind(OP_WIDTH,OP_HEIGHT))
			{
			Tuple t = imageOpChain.getArg();
			if(t.getExprList().size() == 0)
				imageOpChain.setT(TypeName.INTEGER);
			else
				throw new TypeCheckException("Type Mismatch at ImageOpChain");
			}
		else if(imageOpChain.getFirstToken().isKind(KW_SCALE))
			{
			Tuple t = imageOpChain.getArg();
			if(t.getExprList().size() == 1)
				imageOpChain.setT(TypeName.IMAGE);
			else
				throw new TypeCheckException("Type Mismatch at ImageOpChain");
		}
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		// TODO Auto-generated method stub
		List<Expression> list = tuple.getExprList();
		
		for(int i=0;i<list.size();i++)
		{
		list.get(i).visit(this, null);
		}
		
		for(int i=0;i<list.size();i++)
			{
			if(list.get(i).getT()!=TypeName.INTEGER)
				throw new TypeCheckException("Type mismatch at tuple");
			}
		return null;
	}


}
